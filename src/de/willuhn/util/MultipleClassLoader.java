/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/MultipleClassLoader.java,v $
 * $Revision: 1.3 $
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
   * Fuegt das uebergebene Jar-File zum Class-Loader hinzu.
   * @param file das Jar-File.
   * @throws MalformedURLException
   */
  public static void addJar(File file) throws MalformedURLException
  {
  	if (file == null)
  		return;
		addClassloader(new URLClassLoader(new URL[]{file.toURL()}));
  }

  /**
	 * Fuegt rekursiv alle Jar-Files zum Class-Loader hinzu, die sich im uebergebenen Verzeichnis befinden.
   * @param directory Verzeichnis mit Jar-Files.
   * @return eine Liste mit allen Jar-Files, die geladen wurden.
   * @throws MalformedURLException
   */
  public static File[] addJars(File directory) throws MalformedURLException
	{
		// Liste aller Jars aus dem plugin-Verzeichnis holen
		FileFinder finder = new FileFinder(directory);
		finder.extension("jar");
		File[] jars = finder.findRecursive();

		if (jars == null || jars.length < 1)
		{
			return null;
		}

		URL[] urls = new URL[jars.length];
		for(int i=0;i<jars.length;++i)
		{
			File jar = (File) jars[i];
			urls[i] = jar.toURL();
		}
		addClassloader(new URLClassLoader(urls));
		return jars;
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
 * Revision 1.3  2004/01/25 18:40:05  willuhn
 * *** empty log message ***
 *
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