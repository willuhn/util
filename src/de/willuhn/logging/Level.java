/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/Level.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/12 18:18:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
	 * Vordefinierter Log-Level fuer Debug-Meldungen.
	 */
	public final static Level DEBUG  = new Level("DEBUG",1);

	/**
	 * Vordefinierter Log-Level fuer regulaere Meldungen.
	 */
	public final static Level INFO   = new Level("INFO ",100);

	/**
	 * Vordefinierter Log-Level fuer Warnungen.
	 */
	public final static Level WARN   = new Level("WARN ",300);

	/**
	 * Vordefinierter Log-Level fuer Fehler.
	 */
	public final static Level ERROR  = new Level("ERROR",500);
	
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
}


/**********************************************************************
 * $Log: Level.java,v $
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 **********************************************************************/