/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/Message.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/12/31 19:34:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.logging;

import java.util.Date;

/**
 * Eine zu loggende Nachricht.
 */
public class Message
{
	private Date date 	  = null;
	private Level level   = null;
	private String text   = null;
	private String clazz  = null;
	private String method = null;

	/**
	 * ct.
   * @param d
   * @param l
   * @param clazz
   * @param method
   * @param text
   */
  Message(
		Date d,
		Level l,
		String clazz,
		String method,
		String text
	)
	{
		this.date = d;
		this.level = l;
		this.clazz = clazz;
		this.method = method;
		this.text = text;
	}
	
	/**
	 * Datum, an dem die Nachricht ausgeloest wurde.
   * @return Datum.
   */
  public Date getDate()
	{
		return date;
	}

	/**
	 * Liefert das LogLevel der Nachricht.
   * @return LogLevel.
   */
  public Level getLevel()
	{
		return level;
	}

	/**
	 * Liefert die eigentliche Nachricht.
   * @return Nachricht.
   */
  public String getText()
	{
		return text;
	}

	/**
	 * Liefert den Namen der loggenden Klasse.
   * @return Name der loggenden Klasse.
   */
  public String getLoggingClass()
	{
		return clazz;
	}

	/**
	 * Liefert den Namen der loggenden Methode.
   * @return Name der loggenden Methode.
   */
  public String getLoggingMethod()
	{
		return method;
	}
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
		StringBuffer sb = new StringBuffer("[" + getDate().toString() + "]" +
							 												 "[" + getLevel().getName() + "]");
		if (clazz != null && method != null)
			sb.append("[" + clazz + "." + method + "]");

		sb.append(" ");
		sb.append(text);
		return sb.toString();
  }

}


/**********************************************************************
 * $Log: Message.java,v $
 * Revision 1.1  2004/12/31 19:34:22  willuhn
 * @C some logging refactoring
 * @N syslog support for logging
 *
 **********************************************************************/