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
    catch (QueueFullException e)
    {
    }
  }

}


/**********************************************************************
 * $Log: History.java,v $
 * Revision 1.1  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 **********************************************************************/