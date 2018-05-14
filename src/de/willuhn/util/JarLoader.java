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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import de.willuhn.io.FileFinder;
import de.willuhn.logging.Logger;

/**
 * Util-Klasse, mit einzelne Jar-Dateien oder ganze Verzeichnisse
 * von Jar-Dateien zur Laufzeit in den Classppath geladen werden koennen.
 */
public class JarLoader
{
  /**
   * Laedt die Jars rekursive im angegebenen Verzeichnis.
   * @param dir das Verzeichnis, in dem sich die Jars befinden.
   * @throws IOException wenn die Jars nicht geladen werden koennen.
   */
  public static void loadJars(File dir) throws IOException
  {
    if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
      throw new IOException("unable to read dir " + dir);
    
    FileFinder finder = new FileFinder(dir);
    finder.extension(".jar");
    File[] jars = finder.findRecursive();
    for (File jar:jars)
      loadJar(jar);
  }
  
  /**
   * Laedt ein einzelnes Jar.
   * @param jar die zu ladende Jar-Datei.
   * @throws IOException wenn das Jar nicht geladen werden kann.
   */
  public static void loadJar(File jar) throws IOException
  {
    if (!jar.isFile() || !jar.canRead())
      throw new IOException("unable to read file " + jar);

    try
    {
      URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      
      Logger.debug("loading " + jar);
      method.invoke(loader, new Object[]{jar.toURI().toURL()});
    }
    catch (IOException ioe)
    {
      throw ioe;
    }
    catch (Exception e)
    {
      throw new IOException("unable to load jar " + jar + ": " + e.getMessage());
    }
  }
}



/**********************************************************************
 * $Log: JarLoader.java,v $
 * Revision 1.2  2010/09/29 10:47:39  willuhn
 * @B den Konstruktor gibts erst in Java 1.6
 *
 * Revision 1.1  2010-09-29 10:44:35  willuhn
 * @N Ein Jar-Loader und ein Platform-Util
 *
 * Revision 1.1  2010/09/28 16:40:38  willuhn
 * @N initial checkin
 *
 **********************************************************************/