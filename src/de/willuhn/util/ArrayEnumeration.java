/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/ArrayEnumeration.java,v $
 * $Revision: 1.2 $
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

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Enumeration, die intern ein Array von Objekten haelt.
 */
public class ArrayEnumeration implements Enumeration
{

	private int index = 0;
	private Object[] array = null;

  /**
   * Erzeugt eine neue Enumeration.
   * @param array
   */
  public ArrayEnumeration(Object[] array)
  {
  	this.array = array;
  }

	/**
   * Erzeugt eine neue Enumeration.
   * @param array
   */
  public ArrayEnumeration(ArrayList array)
	{
		this.array = array.toArray();
	}
  /**
   * @see java.util.Enumeration#hasMoreElements()
   */
  public boolean hasMoreElements()
  {
    return index < array.length;
  }

  /**
   * @see java.util.Enumeration#nextElement()
   */
  public Object nextElement()
  {
  	return array[index++];
  }

}


/**********************************************************************
 * $Log: ArrayEnumeration.java,v $
 * Revision 1.2  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/06 19:58:29  willuhn
 * @N ArrayEnumeration
 *
 **********************************************************************/