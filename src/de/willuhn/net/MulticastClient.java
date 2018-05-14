/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * GNU LESSER GENERAL PUBLIC LICENSE 2.1.
 * Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import de.willuhn.logging.Logger;


/**
 * Eine kleine Hilfsklasse fuer RMI Multicast-Discovery.
 */
public class MulticastClient
{
  /**
   * Default-Multicast-Adresse.
   */
  public final static String DEFAULT_ADDRESS = "224.0.0.1";

  /**
   * Default-Port.
   */
  public final static int DEFAULT_PORT       = 6789;
  
  private InetAddress self       = null;

  private int port               = DEFAULT_PORT;
  private InetAddress address    = null;
  private DatagramPacket packet  = null;
  private MulticastSocket socket = null;
  private Worker worker          = null;
  
  /**
   * Erzeugt einen neuen Listener auf der Standard-Adresse mit dem Standard-Port.
   * @throws IOException
   */
  public MulticastClient() throws IOException
  {
    this(DEFAULT_ADDRESS,DEFAULT_PORT);
  }
  
  /**
   * Erzeugt einen neuen Listener mit expliziter Angabe von Adresse und Port.
   * @param address Adresse.
   * @param port Port.
   * @throws IOException
   */
  public MulticastClient(String address, int port) throws IOException
  {
    this.port    = port;
    this.address = InetAddress.getByName(address);
    this.self    = InetAddress.getLocalHost();

    this.socket  = new MulticastSocket(this.port);
    this.socket.joinGroup(this.address);

    this.worker = new Worker();
    this.worker.start();
  }
  
  /**
   * Stoppt den Listener.
   * @throws IOException
   */
  public synchronized void stop() throws IOException
  {
    this.worker.shutdown();
  }
  

  /**
   * Sendet Daten via Multicast.
   * @param data
   * @throws IOException
   */
  public void send(byte[] data) throws IOException
  {
    Logger.debug("sending " + data.length + " bytes");

    DatagramPacket packet = new DatagramPacket(data, data.length,this.address,this.port);
    socket.send(packet);
  }
  
  /**
   * Nimmt die empfangenen Daten entgegen.
   * Sollte ueberschrieben werden, wenn man
   * die Daten nutzen will.
   * @param packet
   * @throws IOException
   */
  public void received(DatagramPacket packet) throws IOException
  {
    Logger.debug("response from " + packet.getAddress().getHostName() + ": " + new String(packet.getData()));
  }

  /**
   * Worker-Thread.
   */
  private class Worker extends Thread
  {
    private Worker()
    {
      super();
      setName(toString());
    }

    /**
     * Beendet den Worker.
     * @throws IOException
     */
    private synchronized void shutdown() throws IOException
    {
      try
      {
        this.interrupt();
        socket.leaveGroup(address);
      }
      finally
      {
        socket.close();
      }
    }
    
    /**
     * @see java.lang.Thread#run()
     */
    public void run()
    {
      try
      {
        Logger.debug("start: " + toString());
        while (!isInterrupted())
        {
          byte[] buf = new byte[1024];
          packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);

          InetAddress sender = packet.getAddress();
          if (isOwn(sender))
            continue; // sind wir selbst
          
          received(packet);
        }
      }
      catch (SocketException se)
      {
        if (!isInterrupted())
          Logger.error("error while receiving data",se);
      }
      catch (IOException ioe)
      {
        Logger.error("error while receiving data",ioe);
      }
      finally
      {
        Logger.debug("stopped: " + toString());
      }
    }
    
    /**
     * @see java.lang.Thread#toString()
     */
    public String toString()
    {
      return "multicast-client " + address.getHostAddress() + ":" + port;      
    }
  }
  
  /**
   * ZUm Testen. Daten koennen via Kommandozeile uebergeben werden.
   * @param args
   * @throws Exception
   */
  public final static void main(String[] args) throws Exception
  {
    final MulticastClient client = new MulticastClient() {
    
      public void received(DatagramPacket packet) throws IOException
      {
        InetAddress sender = packet.getAddress();
        InetAddress self   = InetAddress.getLocalHost();
        System.out.println("\n" + sender.getCanonicalHostName() + "> " + new String(packet.getData()));

        // Prompt wieder anzeigen
        System.out.print(self.getCanonicalHostName() + "> ");
      }
    
    };
    try
    {
      System.out.println("type message and press ENTER to send.");
      System.out.println("press CTRL+C to exit.");
      
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader keyboard = new BufferedReader(isr);
      while (true)
      {
        System.out.print(client.self.getHostName() + "> ");
        String input = keyboard.readLine();
        if (input == null || input.length() == 0)
          continue;
        client.send(input.getBytes());
      }
    }
    finally
    {
      System.out.println("");
      Logger.flush();
      Logger.close();
      client.stop();
    }
  }
  
  /**
   * Prueft, ob die uebergebene IP-Adresse eine eigen ist.
   * @param address die zu pruefende Adresse.
   * @return true, wenn es die eigene ist.
   * @throws SocketException
   */
  private static boolean isOwn(InetAddress address) throws SocketException
  {
    Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
    while (ifaces.hasMoreElements())
    {
      NetworkInterface i = (NetworkInterface) ifaces.nextElement();
      Enumeration ips = i.getInetAddresses();
      while (ips.hasMoreElements())
      {
        if (address.equals(ips.nextElement()))
          return true;
      }
    }
    return false;
  }
}

/*******************************************************************************
 * $Log: MulticastClient.java,v $
 * Revision 1.2  2007/11/27 18:52:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/08/01 17:21:40  willuhn
 * @N generischer Multicast-Client, mit dem man P2P Daten austauschen. In der Main-Methode befindet sich eine Beispiel-Anwendung (Chat)
 *
 * Revision 1.2  2007/06/21 09:01:49  willuhn
 * @N System-Presets
 *
 * Revision 1.1  2007/06/20 00:17:40  willuhn
 * @N Spiel-Code fuer ein RMI-Service-Discovery via TCP Multicast
 *
 ******************************************************************************/
