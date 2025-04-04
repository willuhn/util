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
package de.willuhn.logging;

import java.util.Hashtable;

/**
 * Log-Level.
 */
public class Level
{

	private String name;
	private int value;

	private static Hashtable registry = new Hashtable();

  /**
   * Vordefinierter Log-Level fuer Trace-Meldungen.
   * Sie liegen noch unterhalb von DEBUG.
   */
  public final static Level TRACE  = new Level("TRACE",0);

	/**
	 * Vordefinierter Log-Level fuer Debug-Meldungen.
	 */
	public final static Level DEBUG  = new Level("DEBUG",1);

	/**
	 * Vordefinierter Log-Level fuer regulaere Meldungen.
	 */
	public final static Level INFO   = new Level("INFO",100);

	/**
	 * Vordefinierter Log-Level fuer Warnungen.
	 */
	public final static Level WARN   = new Level("WARN",300);

	/**
	 * Vordefinierter Log-Level fuer Fehler.
	 */
	public final static Level ERROR  = new Level("ERROR",500);
	
	/**
	 * Default-Loglevel.
	 */
	public final static Level DEFAULT = INFO;

  /**
   * ct.
   * @param name Name des Levels (zb "DEBUG").
   * @param value Wertigkeit des Levels.
   * Level mit hoher Prioritaet (z.Bsp. "ERROR" oder "WARNING" haben
   * eine hohe Wertigkeit, informative und Debug-Levels eine niedrige. 
   */
  public Level(String name, int value)
  {
  	this.name  = (name == null ? "" : name);
  	this.value = value;

		registry.put(name,this);
  }

	/**
	 * Liefert die Wertigkeit des Levels.
   * @return Wertigkeit.
   */
  public int getValue()
	{
		return value;
	}
	
	/**
	 * Liefert den Namen des Levels.
   * @return Name des Levels.
   */
  public String getName()
	{
		return name;
	}

	/**
	 * Findet ein Log-Level anhand seines Namens.
   * @param name Name des Log-Levels, nach dem gesucht wird.
   * @return der gefundene Log-Level oder <code>null</code>.
   */
  public static Level findByName(String name)
	{
		return (Level) registry.get(name);
	}

  @Override
  public boolean equals(Object obj)
  {
  	return ((Level)obj).value == this.value;
  }

  @Override
  public String toString()
  {
    return "Name: " + name + ", Level: " + value;
  }

}


/**********************************************************************
 * $Log: Level.java,v $
 * Revision 1.4  2005/03/24 17:28:25  web0
 * @B bug in Level.findByName
 *
 * Revision 1.3  2005/03/09 01:06:20  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/01/14 00:49:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 **********************************************************************/