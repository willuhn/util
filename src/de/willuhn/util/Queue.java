/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Queue.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/03/06 18:24:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Bildet eine Queue nach FIFO-Prinzip ab (First in, First out).
 * Die Queue enthaelt eine definierte Anzahl von Elementen. Diese werden
 * in der Reihenfolge abgearbeitet, wie sie hinzugefuegt wurden.
 * Sie ist synchronized.
 */
public class Queue
{

	private int capacity = 10;
	private Vector v = null;

	/**
	 * Die Minimal-Kapazitaet der Queue.
	 */
	public static int CAPACITY_MIN = 2;

	/**
	 * Die Maximal-Kapazitaet der Queue.
	 */
	public static int CAPACITY_MAX = 1000;

  /**
   * ct.
   * @param capacity maximale Kapazitaet, die die Queue haben soll.
   */
  public Queue(int capacity)
  {
  	if (capacity >= CAPACITY_MIN && capacity <= CAPACITY_MAX)
  		this.capacity = capacity;
  	v = new Vector(this.capacity);
  }

	/**
	 * Entfernt das naechste zu bearbeitende Objekt aus der Queue und liefert es zurueck. 
   * @return naechstes zu bearbeitendes Objekt.
   */
  public synchronized Object pop()
	{
		Object o = v.get(0);
		v.removeElementAt(0);
		return o;
	}
	
	/**
	 * Liefert die aktuelle Groesse der Queue.
   * @return aktuelle Groesse der Queue. 
   */
  public int size()
	{
		return v.size();
	}

	/**
	 * Liefert eine Enumeration ueber alle Elemente der Queue.
	 * Es wird nur eine Kopie ausgegeben.
   * @return Enumeration ueber alle Elemente.
   */
  public synchronized Enumeration elements()
	{
		return new ArrayEnumeration(v.toArray());
	}

  /**
	 * Liefert ein Object-Array mit allen momentan in der Queue befindlichen Objekten.
   * @param type Objekt-Typ, der fuer das Array verwendet werden soll.
	 * @return Array mit Objects.
	 */
	public Object[] toArray(Object[] type)
	{
		return v.toArray(type);
	}

	/**
	 * Prueft, ob die Queue voll ist.
   * @return true, wenn sie voll ist.
   */
  public boolean full()
	{
		return (v.size() >= capacity);
	}

  /**
	 * Fuegt der Queue ein weiteres Objekt hinzu. Ist die Queue voll, wird eine
	 * Exception geworfen
   * @param o das hinzuzufuegende Objekt.
   * @throws QueueFullException Wenn die Queue voll ist.
   */
  public synchronized void push(Object o) throws QueueFullException
	{
		if (full())
			throw new QueueFullException("maximum queue size reached");
		v.addElement(o);
	}

	/**
	 * Wird geworfen, wenn die Queue voll ist und trotzdem versucht wird, Daten
	 * hineinzuschreiben.
	 */
	public static class QueueFullException extends Exception
	{

		/**
		 * Erzeugt eine neue Exception dieses Typs mit der genannten Meldung.
		 * @param message anzuzeigende Meldung.
		 */
		public QueueFullException(String message)
		{
			super(message);
		}
	}

}


/**********************************************************************
 * $Log: Queue.java,v $
 * Revision 1.4  2004/03/06 18:24:47  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/01/06 19:58:29  willuhn
 * @N ArrayEnumeration
 *
 * Revision 1.2  2004/01/05 23:08:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/05 21:46:29  willuhn
 * @N added queue
 * @N logger writes now in separate thread
 *
 **********************************************************************/