/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/MultipleClassLoader.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/23 00:24:17 $
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

/**
 * ClassLoader der sich beliebiger anderer ClassLoader bedient.
 * @author willuhn
 * 05.01.2004
 */
public class MultipleClassLoader extends ClassLoader
{

  private static ArrayList loaders = new ArrayList();
  
  static {
    // System-Classloader hinzufuegen
    addClassloader(getSystemClassLoader());
  }
  /**
   * Fuegt einen weiteren ClassLoader hinzu,
   * @param loader der hinzuzufuegende Classloader.
   */
  public static void addClassloader(ClassLoader loader)
  {
    loaders.add(loader);
  }

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String)
   */
  public static Class load(String className) throws ClassNotFoundException
  {
    ClassLoader l = null;
    Class c = null;
    for (int i=0;i<loaders.size();++i)
    {
      try {
        l = (ClassLoader) loaders.get(i);
        c = Class.forName(className,true,l);
        if (c != null)
          return c;
      }
      catch (Exception e)
      {
      }
    }
    throw new ClassNotFoundException("class not found: " + className);
  }
  
}


/*********************************************************************
 * $Log: MultipleClassLoader.java,v $
 * Revision 1.2  2004/01/23 00:24:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/05 18:04:46  willuhn
 * @N added MultipleClassLoader
 *
 *********************************************************************/