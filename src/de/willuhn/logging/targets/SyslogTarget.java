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
package de.willuhn.logging.targets;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;

/**
 * Target, welches an einen Syslog-Server loggen kann.
 */
public class SyslogTarget implements Target
{

	private int port 								= 514;
	private InetAddress targetHost 	= null;
	private DatagramSocket socket 	= null;

	/**
	 * ct.
   * @param hostname Hostname des Servers, auf dem der Syslog-Server laeuft.
   * Ist dieser nicht angegeben, wird an Localhost geloggt.
   * Als Port wird 514 verwendet.
   * @throws Exception
   */
  public SyslogTarget(String hostname) throws Exception
	{
		this(hostname,514);
	}

  /**
   * ct.
   * @param hostname Hostname des Servers, auf dem der Syslog-Server laeuft.
   * Ist dieser nicht angegeben, wird an Localhost geloggt.
   * @param port UDP-Port, an den gesendet werden soll.
   * @throws Exception
   */
  public SyslogTarget(String hostname, int port) throws Exception
  {
		try
		{
			targetHost = InetAddress.getByName(hostname);
		}
		catch (Exception e)
		{
			Logger.warn("hostname " + hostname + " invalid, trying localhost");
			targetHost = InetAddress.getByName("localhost");
		}
		this.port = port;
		this.socket = new DatagramSocket();
  }

  @Override
  public void write(Message message) throws Exception
  {
  	if (message == null)
  		return;
		String s = "[" + message.getLevel().getName() + "] " + message.getText();
  	byte[] data = s.getBytes();

		DatagramPacket packet = new DatagramPacket(data, data.length, targetHost, port);
		socket.send(packet);
	}

  @Override
  public void close() throws Exception
  {
  	socket.close();
  }

}


/**********************************************************************
 * $Log: SyslogTarget.java,v $
 * Revision 1.1  2004/12/31 19:34:22  willuhn
 * @C some logging refactoring
 * @N syslog support for logging
 *
 **********************************************************************/