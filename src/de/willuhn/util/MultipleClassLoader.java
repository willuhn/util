/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/MultipleClassLoader.java,v $
 * $Revision: 1.31 $
 * $Date: 2007/10/25 23:13:22 $
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;

import de.willuhn.io.FileFinder;
import de.willuhn.logging.Logger;

/**
 * ClassLoader der sich beliebiger anderer ClassLoader bedient.
 * @author willuhn
 * 05.01.2004
 */
public class MultipleClassLoader extends ClassLoader
{
  private ArrayList loaders   	= new ArrayList();
  private Hashtable cache     	= new Hashtable();
  private ClassFinder finder   	= new ClassFinder();
  private URLLoader ucl       	= new URLLoader();

	/**
	 * Erzeugt eine neue Instanz des Classloaders.
	 */
	public MultipleClassLoader()
	{
		super();
	}

  /**
   * Fuegt einen weiteren ClassLoader hinzu,
   * @param loader der hinzuzufuegende Classloader.
   */
  public void addClassloader(ClassLoader loader)
  {
    if (loader == null)
      return;
    Logger.debug("multipleClassLoader: adding class loader " + loader.getClass().getName());
    loaders.add(loader);
    if (loader instanceof MultipleClassLoader)
    {
      finder.addFinder(((MultipleClassLoader) loader).getClassFinder());
    }
  }
  
  /**
   * Fuegt die uebergebene URL dem Class-Loader hinzu.
   * @param url die URL.
   */
  public void add(URL url)
  {
    this.ucl.addURL(url);
  }
  
  /**
   * Liefert eine Liste aller URLs, die im Classloader registriert sind.
   * Diese Liste enthaelt sowohl lokale Ressourcen als auch remote Ressourcen.
   * @return Liste aller URLs.
   */
  public URL[] getURLs()
  {
    return this.ucl.getURLs();
  }
  
  /**
   * Liefert eine Liste aller lokalen Ressourcen. Also getURLs() abzueglich remote Ressourcen.  
   * @return Liste der lokalen Files/Jars.
   */
  public File[] getFiles()
  {
    ArrayList l = new ArrayList();
    URL[] urls = getURLs();
    for (int i=0;i<urls.length;++i)
    {
      try
      {
        File f = new File(urls[i].getFile());
        if (!f.exists())
          continue;
        l.add(f);
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    return (File[])l.toArray(new File[l.size()]);
  }
  
  

  /**
   * Fuegt das uebergebene Jar-File oder Verzeichnis zum Class-Loader hinzu.
   * @param file das Jar-File oder Verzeichnis.
   * @throws MalformedURLException
   */
  public void add(File file) throws MalformedURLException
  {
    add(file.toURI().toURL()); // ungueltige Zeichen werden escaped wenn wir vorher eine URI draus machen (zB. Spaces).
  }

  /**
   * Fuegt rekursiv alle Jar-Files zum Class-Loader hinzu, die sich im uebergebenen Verzeichnis befinden.
   * @param directory Verzeichnis mit Jar-Files.
   * @param extensions Liste von Datei-Endungen, die beruecksichtigt werden sollen.
   * Also z.Bsp. ".jar,.zip".
   * @return eine Liste mit allen Jar-Files, die geladen wurden.
   * @throws MalformedURLException
   */
  public File[] addJars(File directory, String[] extensions) throws MalformedURLException
  {
    // Liste aller Jars aus dem plugin-Verzeichnis holen
    FileFinder finder = new FileFinder(directory);
    for (int i=0;i<extensions.length;++i)
    {
      finder.extension(extensions[i]);
    }
    File[] jars = finder.findRecursive();

    if (jars == null || jars.length < 1)
    {
      return null;
    }

    for(int i=0;i<jars.length;++i)
    {
			Logger.debug("multipleClassLoader: adding file " + jars[i].getAbsolutePath());
      add(jars[i].toURI().toURL()); // ungueltige Zeichen werden escaped wenn wir vorher eine URI draus machen (zB. Spaces).
    }
    return jars;
  }

  /**
   * Fuegt rekursiv alle Jar-Files zum Class-Loader hinzu, die sich im uebergebenen Verzeichnis befinden.
   * Diese Funktion beschraenkt sich bei der Suche auf die Standard-Archivendungen ".jar" und ".zip".
   * @param directory Verzeichnis mit Jar-Files.
   * @return eine Liste mit allen Jar-Files, die geladen wurden.
   * @throws MalformedURLException
   */
  public File[] addJars(File directory) throws MalformedURLException
  {
    return addJars(directory, new String[] {"jar","zip"});
  }

  /**
   * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
   */
  public InputStream getResourceAsStream(String name) {
    InputStream is = ucl.getResourceAsStream(name);
    if (is != null)
      return is;
    return super.getResourceAsStream(name);
  }

	/**
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class findClass(String name) throws ClassNotFoundException
	{
		return load(name);
	}

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String)
   */
  public Class loadClass(String name) throws ClassNotFoundException {
    return load(name);
  }

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
   */
  protected synchronized Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException {
    Class c = load(name);
    if (resolve)
      resolveClass(c);
    return c;
  }

  /**
   * Laedt die angegebene Klasse und initialisiert sie.
   * @param className Name der Klasse.
   * @return Die Klasse.
   * @throws ClassNotFoundException
   */
  public Class load(String className) throws ClassNotFoundException
  {

    // zuerst im Cache schauen.
    Class c = (Class) cache.get(className);
    if (c != null)
      return c;

    try {
      // Dann versuchen wir es mit 'nem URLClassLoader, der alle URLs kennt.
      // Wir nehmen deswegen nur einen URLClassloader, damit sichergestellt
      // ist, dass dieser eine alle Plugins und deren Jars kennt.
      // URLClassLoader checken
      // Da wir dem UCL als Parent-Classloader nicht uns selbst sondern
      // den System-Classloader uebergeben haben, brauchen wir nur
      // den UCL fragen und nicht extra nochmal den System-Classloader
      return findVia(ucl,className);
    }
    catch (Throwable r) {}

    // ok, wir fragen die anderen ClassLoader
    ClassLoader l = null;
    for (int i=0;i<loaders.size();++i)
    {
      try {
        l = (ClassLoader) loaders.get(i);
        return findVia(l,className);
      }
      catch (Throwable t)
      {
      }
    }
    throw new ClassNotFoundException("class not found: " + className);
  }

  /**
   * Sucht die Klasse ueber den angegebenen ClassLoader.
   * @param loader ClassLoader.
   * @param className Klasse.
   * @return die geladene Klasse.
   * @throws ClassNotFoundException
   */
  private Class findVia(ClassLoader loader, String className) throws ClassNotFoundException
  {
    Class c = loader.loadClass(className);
    // Klasse gefunden. Die tun wir gleich noch in den Cache.
    if (cache.put(className,c) == null)
    {
      // und registrieren sie im ClassFinder. Aber nur, wenn
      // sie im Cache noch nicht existierte.
      if (loader instanceof MultipleClassLoader)
        ((MultipleClassLoader)loader).finder.addClass(c);
      else
        finder.addClass(c);
    }
    return c;
  }

  /**
   * Liefert einen ClassFinder, der alle Klassen dieses ClassLoaders kennt.
   * @return ClassFinder.
   */
  public ClassFinder getClassFinder()
  {
    return finder;
  }

  /**
   * Wir ueberschreiben den URLClassLoader um an die Funktion "addURL" ranzukommen.
   */
  private class URLLoader extends URLClassLoader
  {

    /**
     * 
     */
    public URLLoader()
    {
      // Niemals den MultipleClassLoader selbst uebergeben, das wuerde eine
      // Rekursion ausloesen
      super(new URL[]{},MultipleClassLoader.this.getParent());
    }

    /**
     * @see java.net.URLClassLoader#addURL(java.net.URL)
     */
    protected void addURL(URL url)
    {
      super.addURL(url);
    }

  }
}


/*********************************************************************
 * $Log: MultipleClassLoader.java,v $
 * Revision 1.31  2007/10/25 23:13:22  willuhn
 * @N Support fuer kaskadierende Classloader und -finder
 * @C Classfinder ignoriert jetzt Inner-Classes
 *
 * Revision 1.30  2007/03/09 18:03:32  willuhn
 * @N classloader updates
 * @N FileWatch
 *
 * Revision 1.29  2005/07/24 16:59:17  web0
 * @B fix in settings watcher
 *
 * Revision 1.28  2005/05/19 17:43:58  web0
 * @B Uralt-Fehler im URLClassloader gefixt
 *
 * Revision 1.27  2005/05/02 11:23:20  web0
 * *** empty log message ***
 *
 * Revision 1.26  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.25  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 * Revision 1.24  2004/08/18 23:14:28  willuhn
 * @D Javadoc
 *
 * Revision 1.23  2004/07/25 17:15:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/06/30 20:58:53  willuhn
 * @C some refactoring
 *
 * Revision 1.21  2004/06/15 21:11:30  willuhn
 * @N added LoggerOutputStream
 *
 * Revision 1.20  2004/05/25 23:24:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/05/11 21:07:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/05/04 23:10:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/04 23:05:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/05/02 17:04:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/01 19:02:02  willuhn
 * @N added getResourceAsStream
 *
 * Revision 1.14  2004/04/01 00:23:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/03/31 22:50:51  willuhn
 * @B bugfixes in CLassLoader
 * @N massive performance speedup! ;)
 *
 * Revision 1.12  2004/03/30 22:07:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/29 19:56:56  willuhn
 * @D javadoc
 *
 * Revision 1.10  2004/03/18 01:24:56  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/06 18:24:47  willuhn
 * @D javadoc
 *
 * Revision 1.8  2004/02/27 01:09:42  willuhn
 * *** empty log message ***
 *
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