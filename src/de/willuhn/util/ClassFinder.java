/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/ClassFinder.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/25 18:40:05 $
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
import java.util.Hashtable;

/**
 * Klassen-Sucher.
 * Diese Teil hier kann man mit Klassen fuettern und danach
 * in verschiedener Hinsicht befragen. Man kann z.Bsp. ein
 * Interface uebergeben und sich eine ggf. vorhandene Implementierung
 * liefern zu lassen.
 */
public class ClassFinder
{

	private static Hashtable cache = new Hashtable();
	private static ArrayList classes = new ArrayList();

  /**
   * ct.
   */
  private ClassFinder()
  {
  }

	/**
	 * Fuegt die Klasse dem Finder hinzu.
   * @param clazz die Klasse.
   */
  public static void addClass(Class clazz)
	{
		classes.add(clazz);
	}

	/**
	 * Sucht nach einem ggf. vorhandenen Implementor des uebergebenen Interfaces.
   * @param interphase das Interface.
   * @return ggf. gefundene Klasse oder null.
   */
  public static Class findImplementor(Class interphase)
	{
		if (interphase == null)
			return null;

		// erstmal im Cache checken
		Class found = (Class) cache.get(interphase);
		if (found != null)
			return found;
			
		Class test = null;
		// ueber alle Klassen iterieren 
		for (int i=0;i<classes.size();++i)
		{
			test = (Class) classes.get(i);
			if (test.getName().endsWith("_Stub") || test.getName().endsWith("_Skel"))
				continue; // Stubs ignorieren wir
			Class[] interfaces = test.getInterfaces();
			// alle Interfaces der aktuellen Klasse checken
			for (int j=0;j<interfaces.length;++j)
			{
				if (interfaces[j].equals(interphase))
				{
					cache.put(interphase,test);
					return test;
				}
			}
		}
		// nicht gefunden
		return null;
	}

}


/**********************************************************************
 * $Log: ClassFinder.java,v $
 * Revision 1.1  2004/01/25 18:40:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/