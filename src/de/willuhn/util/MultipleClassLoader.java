/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/MultipleClassLoader.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/02/26 18:47:11 $
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
import java.util.Hashtable;

/**
 * ClassLoader der sich beliebiger anderer ClassLoader bedient.
 * @author willuhn
 * 05.01.2004
 */
public class MultipleClassLoader extends ClassLoader
{

  private static ArrayList loaders = new ArrayList();
  private static Hashtable cache = new Hashtable();
	private static Logger logger = new Logger();
  
  private static ClassFinder finder = new ClassFinder();
  
  static {
    // System-Classloader hinzufuegen
    addClassloader(getSystemClassLoader());
  }

	/**
	 * Optionale Angabe eines Loggers.
	 * Wird er angegeben, schreibt der ClassLoader seine Infos da rein.
   * @param l zu verwendender ClassLoader.
   */
  public static void setLogger(Logger l)
	{
		if (l == null)
			return;
		logger = l;
	}

  /**
   * Fuegt einen weiteren ClassLoader hinzu,
   * @param loader der hinzuzufuegende Classloader.
   */
  public static void addClassloader(ClassLoader loader)
  {
  	if (loader == null)
  		return;
  	logger.debug("multipleClassLoader: adding class loader " + loader.getClass().getName());
    loaders.add(loader);
  }
  
  /**
   * Fuegt das uebergebene Jar-File oder Verzeichnis zum Class-Loader hinzu.
   * @param file das Jar-File oder Verzeichnis.
   * @throws MalformedURLException
   */
  public static void add(File file) throws MalformedURLException
  {
  	if (file == null)
  		return;
		logger.debug("multipleClassLoader: adding file " + file.getAbsolutePath());
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
			logger.debug("multipleClassLoader: adding file " + jar.getAbsolutePath());
		}
		addClassloader(new URLClassLoader(urls));
		return jars;
	}

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String)
   */
  public static Class load(String className) throws ClassNotFoundException
  {

		// wir schauen erstmal im Cache nach.
		Class c = (Class) cache.get(className);
		if (c != null)
		{
			return c;
		}
		
    ClassLoader l = null;
    for (int i=0;i<loaders.size();++i)
    {
      try {
        l = (ClassLoader) loaders.get(i);
        c = Class.forName(className,true,l);
        if (c != null)
        {
        	// Klasse gefunden. Die tun wir gleich noch in den Cache.
        	cache.put(className,c);
        	// und registrieren sie im ClassFinder
        	finder.addClass(c);
					return c;
        }
      }
      catch (Exception e)
      {
      }
    }
    throw new ClassNotFoundException("class not found: " + className);
  }

	/**
	 * Sucht nach einem ggf. vorhandenen Implementor des uebergebenen Interfaces.
	 * @param interphase das Interface.
	 * @return ggf. gefundene Klasse oder null.
	 */
	public static Class findImplementor(Class interphase)
	{
		return finder.findImplementor(interphase);
	}

	/**
	 * Klassen-Sucher.
	 * Diese Teil hier kann man mit Klassen fuettern und danach
	 * in verschiedener Hinsicht befragen. Man kann z.Bsp. ein
	 * Interface uebergeben und sich eine ggf. vorhandene Implementierung
	 * liefern zu lassen.
	 */
	private static class ClassFinder
	{

		private Hashtable cache = new Hashtable();
		private ArrayList classes = new ArrayList();

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
		private void addClass(Class clazz)
		{
			if (isStub(clazz))
				return;

			if (clazz.isInterface() || clazz.isPrimitive())
				return;
			
			classes.add(clazz);
		}

		/**
		 * Sucht nach einem ggf. vorhandenen Implementor des uebergebenen Interfaces.
		 * @param interphase das Interface.
		 * @return ggf. gefundene Klasse oder null.
		 */
		private Class findImplementor(Class interphase)
		{
			if (interphase == null)
			{
				return null;
			}

			if (!interphase.isInterface()) // kein Interface
				return interphase;

			long start = System.currentTimeMillis();
		
			// erstmal im Cache checken
			Class found = (Class) cache.get(interphase);
			if (found != null)
			{
				return found;
			}
			
			Class test = null;
			// ueber alle Klassen iterieren 
			for (int i=0;i<classes.size();++i)
			{
				test = (Class) classes.get(i);
				if (isStub(test))
					continue; // Stubs ignorieren wir

				// checken, ob die Klasse das Interface direkt
				// implementiert
				if (check(test,interphase))
				{
					cache.put(interphase,test);
					logger.debug("multipleClassLoader.ClassFinder: found implementor " + test.getName() + " for interface " + interphase.getName());
					logger.debug("multipleClassLoader.ClassFinder:   [used time: " + (System.currentTimeMillis() - start) + " millis]");
					return test;
				}

				// checken, ob weiter oben in der Ableitunghierachie jemand das
				// Interface implementiert. Maximale Iterationstiefe 10
				Class parent = null;
				for (int k=0;k<10;++k)
				{
					parent = test.getSuperclass();
					if (parent == null)
						break; // nichts mehr oben drueber
					if (check(parent,interphase))
					{
						cache.put(interphase,parent);
						logger.debug("multipleClassLoader.ClassFinder: found implementor " + parent.getName() + " for interface " + interphase.getName());
						logger.debug("multipleClassLoader.ClassFinder:   [used time: " + (System.currentTimeMillis() - start) + " millis]");
						return parent; // wir geben "test" zurueck, da es durch die Ableitung von
												 // "parent" ebenfalls das Interface implementiert.
					}
					test = parent;
				}
			}
			// nicht gefunden
			logger.debug("multipleClassLoader.ClassFinder: ...not found");
			return null;
		}

		/**
		 * Prueft, ob die Klasse ein Stub oder Skel ist.
     * @param clazz zu pruefende Klasse.
     * @return true wenn es einer ist.
     */
    private boolean isStub(Class clazz)
		{
			return (clazz.getName().endsWith("_Stub") || clazz.getName().endsWith("_Skel"));
		}

		/**
		 * Checkt, ob die Klasse das Interface implementiert.
		 * @param test Test-Klasse.
		 * @param interphase zu pruefendes Interface.
		 * @return true, wenn sie es implementiert.
		 */
		private boolean check(Class test, Class interphase)
		{
			Class[] interfaces = test.getInterfaces();
			for (int j=0;j<interfaces.length;++j)
			{
				if (interfaces[j].equals(interphase))
				{
					return true;
				}
			}
			return false;
		}
	
	}
}


/*********************************************************************
 * $Log: MultipleClassLoader.java,v $
 * Revision 1.7  2004/02/26 18:47:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/25 23:12:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/09 13:06:51  willuhn
 * @C misc
 *
 * Revision 1.4  2004/01/29 00:45:50  willuhn
 * *** empty log message ***
 *
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