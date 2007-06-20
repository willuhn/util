/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/net/discovery/Attic/MulticastDiscovery.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/20 00:17:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.net.discovery;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.MarshalledObject;
import java.rmi.Naming;

import de.willuhn.logging.Logger;


/**
 * Eine kleine Hilfsklasse fuer RMI Multicast-Discovery.
 */
public class MulticastDiscovery
{
  private final static String HEADER         = "RMID";
  public final static String DEFAULT_ADDRESS = "224.0.0.1";
  public final static int DEFAULT_PORT       = 6789;
  
  private int port               = DEFAULT_PORT;
  private InetAddress address    = null;
  private DatagramPacket packet  = null;
  private MulticastSocket socket = null;
  private Worker worker          = null;
  
  /**
   * ct.
   * @param address
   * @param port
   * @throws IOException
   */
  private MulticastDiscovery(String address, int port) throws IOException
  {
    this.port    = port;
    this.address = InetAddress.getByName(address);

    this.socket  = new MulticastSocket(this.port);
    this.socket.joinGroup(this.address);

    this.worker = new Worker();
    this.worker.start();
  }
  
  /**
   * Stoppt den Listener.
   * @throws IOException
   */
  public final synchronized void stop() throws IOException
  {
    this.worker.shutdown();
  }
  
  /**
   * Erzeugt einen neuen Listener auf der Standard-Adresse mit dem Standard-Port.
   * @return der Listener.
   * @throws IOException
   */
  public final static MulticastDiscovery start() throws IOException
  {
    return start(DEFAULT_ADDRESS,DEFAULT_PORT);
  }
  
  /**
   * Erzeugt einen neuen Listener mit expliziter Angabe von Adresse und Port.
   * @param address Adresse.
   * @param port Port.
   * @return der Listener.
   * @throws IOException
   */
  public final static MulticastDiscovery start(String address, int port) throws IOException
  {
    return new MulticastDiscovery(address,port);
  }
  
  /**
   * Sucht nach dem angegebenen Service auf der Standard-Multicast-Adresse.
   * @param service Name des Service.
   * @return der Service.
   * @throws IOException
   */
  public final static Object lookup(String service) throws IOException
  {
    return lookup(service,DEFAULT_ADDRESS,DEFAULT_PORT);
  }
  
  /**
   * Sucht nach dem angegebenen Service auf der angegebenen Multicast-Adresse.
   * @param service Name des Service.
   * @param address Multicast-Adresse.
   * @param port Port.
   * @return der Service.
   * @throws IOException
   */
  public final static Object lookup(String service, String address, int port) throws IOException
  {
    InetAddress ia = InetAddress.getByName(address);
    MulticastSocket multi = null;
    
    try
    {
      byte [] buf = (HEADER + service).getBytes();
      multi = new MulticastSocket(port);
      multi.joinGroup(ia);

      for (int i =0;i<10;++i)
      {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        multi.send(packet);
        // Thread.sleep(5000);

//        Socket sock = listener.accept();
//        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
//        MarshalledObject mo=(MarshalledObject) ois.readObject();
//        sock.close();
//        return mo.get();
      }        
      return null;
    }
    finally
    {
      multi.leaveGroup(ia);
      multi.close();
    }
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
        Logger.info("start: " + toString());
        while (!isInterrupted())
        {
          byte[] buf = new byte[512];
          packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);

          try
          {
            String msg = new String(packet.getData()).trim();
            if (!msg.startsWith(HEADER))
              continue;
            
            msg = msg.substring(HEADER.length()+1);
            Logger.info("looking for service: " + msg);
            
            // Antwort schicken
            Socket response = new Socket(packet.getAddress(),packet.getPort());
            ObjectOutputStream os = new ObjectOutputStream(response.getOutputStream());
            os.writeObject(new MarshalledObject(Naming.lookup(msg)));
            os.flush();
            os.close();
            Logger.info("service found");
          }
          catch (Exception e)
          {
            Logger.error("error while looking for service",e);
          }
        }
      }
      catch (SocketException se)
      {
        if (!isInterrupted())
          Logger.error("error while receiving multicast message",se);
      }
      catch (IOException ioe)
      {
        Logger.error("error while receiving multicast message",ioe);
      }
      finally
      {
        Logger.info("stopped: " + toString());
      }
    }
    
    /**
     * @see java.lang.Thread#toString()
     */
    public String toString()
    {
      return "multicast-discovery " + address.getHostAddress() + ":" + port;      
    }
  }
}

/*******************************************************************************
 * $Log: MulticastDiscovery.java,v $
 * Revision 1.1  2007/06/20 00:17:40  willuhn
 * @N Spiel-Code fuer ein RMI-Service-Discovery via TCP Multicast
 *
 ******************************************************************************/
