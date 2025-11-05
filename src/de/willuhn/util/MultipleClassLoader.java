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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.io.FileFinder;
import de.willuhn.logging.Logger;

/**
 * ClassLoader der sich beliebiger anderer ClassLoader bedient.
 */
public class MultipleClassLoader extends ClassLoader
{
  private String name           = null;
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
	 * Vergibt einen Namen fuer den Classloader.
	 * @param name Name fuer den Classloader.
	 */
	public void setName(String name)
	{
	  this.name = name;
	}
	
	/**
	 * Liefert den Namen des Classloaders.
	 * @return der Name des Classloaders.
	 */
	public String getName()
	{
	  if (this.name == null)
	    return "multipleClassLoader";
	  return this.name;
	}

  /**
   * Fuegt einen weiteren ClassLoader hinzu,
   * @param loader der hinzuzufuegende Classloader.
   */
  public void addClassloader(ClassLoader loader)
  {
    if (loader == null)
      return;
    Logger.debug(this.getName() + ": adding class loader " + loader.getClass().getName());
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
			Logger.debug(this.getName() + ": adding file " + jars[i].getAbsolutePath());
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
  public InputStream getResourceAsStream(String name)
  {
    InputStream is = ucl.getResourceAsStream(name);
    if (is != null)
      return is;
    return super.getResourceAsStream(name);
  }
  
  /**
   * @see java.lang.ClassLoader#getResource(java.lang.String)
   */
  public URL getResource(String name)
  {
    URL url = ucl.getResource(name);
    if (url != null)
      return url;
    return super.getResource(name);
  }

  /**
   * @see java.lang.ClassLoader#getResources(java.lang.String)
   */
  public Enumeration<URL> getResources(String name) throws IOException
  {
    Enumeration<URL> urls = ucl.getResources(name);
    if (urls != null && urls.hasMoreElements())
      return urls;
    return super.getResources(name);
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
  public Class loadClass(String name) throws ClassNotFoundException
  {
    return load(name);
  }

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
   */
  protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
  {
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
   * @throws LinkageError Das sind NoClassDefFoundError und Co.
   */
  public Class load(String className) throws ClassNotFoundException, LinkageError
  {

    // zuerst im Cache schauen.
    Class c = (Class) cache.get(className);
    if (c != null)
      return c;

    LinkageError error = null;
    try
    {
      // Dann versuchen wir es mit 'nem URLClassLoader, der alle URLs kennt.
      // Wir nehmen deswegen nur einen URLClassloader, damit sichergestellt
      // ist, dass dieser eine alle Plugins und deren Jars kennt.
      // URLClassLoader checken
      // Da wir dem UCL als Parent-Classloader nicht uns selbst sondern
      // den System-Classloader uebergeben haben, brauchen wir nur
      // den UCL fragen und nicht extra nochmal den System-Classloader
      return findVia(ucl,className);
    }
    catch (LinkageError r)
    {
      error = r;
    }
    catch (ClassNotFoundException e)
    {
      // kann passieren - wir suchen im naechsten Classloader
    }

    // ok, wir fragen die anderen ClassLoader
    ClassLoader l = null;
    for (int i=0;i<loaders.size();++i)
    {
      try
      {
        l = (ClassLoader) loaders.get(i);
        return findVia(l,className);
      }
      catch (LinkageError r)
      {
        error = r;
      }
      catch (ClassNotFoundException e)
      {
        // kann passieren - wir suchen im naechsten Classloader
      }
    }
    if (error != null)
      throw error;
    
    // Die oben weggeworfenee ClassNotFoundException muessen
    // wir nicht aufheben, da in der ohnehin nicht mehr
    // Informationen stehen als in unserer.
    throw new ClassNotFoundException(this.getName() + ": class not found: " + className);
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

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.getName();
  }
}


/*********************************************************************
 * $Log: MultipleClassLoader.java,v $
 * Revision 1.35  2011/07/18 15:43:35  willuhn
 * @N Name fuer den Classloader vergebbar
 *
 *********************************************************************/