/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/History.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/08 21:38:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

/**
 * Bildet eine History ab, die immer eine definierte Anzahl der letzten
 * Elemente enthaelt.
 */
public class History extends Queue
{

  /**
   * ct.
   * @param capacity
   */
  public History(int capacity)
  {
    super(capacity);
  }

  /**
   * @see de.willuhn.util.Queue#push(java.lang.Object)
   */
  public synchronized void push(Object o)
  {
  	if (full())
  		pop();
    try
    {
      super.push(o);
    }
    catch (QueueFullException e){}
  }

}


/**********************************************************************
 * $Log: History.java,v $
 * Revision 1.1  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 **********************************************************************/