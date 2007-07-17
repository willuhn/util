/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootStrap.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/17 08:52:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.boot;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import de.willuhn.io.FileFinder;
import de.willuhn.logging.Logger;

/**
 * Ein Bootstrapper zum Starten von Java-Anwendungen mit dynamischem
 * Laden des Class-Path.
 * 
 * Aufruf mit
 * 
 * java de.willuhn.util.boot.BootStrap [1] [2] [3]......
 * 
 * [1]    Vollstaendiger Name der zu ladenden Klasse.
 * [2]    Basis-Verzeichnis, in dem rekursiv nach Jars und Klassen gesucht werden soll.
 * [3]... Liste von Parametern, die an die eigentlich Anwendung durchgereicht werden sollen.
 * @author willuhn
 */
public class BootStrap
{
  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    if (args == null || args.length < 2)
      usage();

    Logger.info("Bootstrapping system");

    final ClassLoader cl = bootStrap(args[1]);
    
    Logger.info("loading system class");
    Class c = cl.loadClass(args[0]);
    Method m = c.getMethod("main",new Class[]{String[].class});

    String[] params = new String[0];
    if (args.length > 2)
    {
      params = new String[args.length - 2];
      System.arraycopy(args,2,params,0,args.length-2);
      System.out.println(m);
      for (int i=0;i<params.length;++i)
      {
        Logger.info("forwarding argument: " + params[i]);
      }
    }
    Logger.info("invoking main method");
    m.invoke(null,new Object[]{params});
  }
  
  
  /**
   * @param baseDir
   * @return Classloader
   */
  private static ClassLoader bootStrap(String baseDir)
  {
    Logger.info("autodetecting classpath...");
    File f = new File(baseDir);
    
    URL[] urls = new URL[0];
    if (!f.canRead() || !f.isDirectory())
    {
      Logger.warn("unable to rad directors " + f.getAbsolutePath());
    }
    else
    {
      Logger.info("searching in directory " + f.getAbsolutePath());
      FileFinder finder = new FileFinder(f);
      finder.extension(".jar");
      File[] found = finder.findRecursive();

      urls = new URL[found.length];
      for (int i=0;i<found.length;++i)
      {
        File f2 = (File) found[i];
        try
        {
          // Ja, der Schritt ueber toURI() ist notwendig, weil dabei
          // Leerzeichen in Pfadangaben korrekt mit "%20" escaped werden ;) 
          urls[i] = f2.toURI().toURL();
          Logger.info("  adding: " + urls[i].toString());
        }
        catch (MalformedURLException e)
        {
          Logger.warn("  error while converting into url, skipping file");
        }
      }
    }
    Logger.info("creating new classloader");
    return new URLClassLoader(urls);
  }
  
  /**
   * 
   */
  private static void usage()
  {
    System.out.println("Usage:");
    System.out.println("  java " + BootStrap.class.getName() + " [1] [2] [3]...");
    System.out.println("    [1]    Vollstaendiger Name der zu ladenden Klasse");
    System.out.println("    [2]    Basis-Verzeichnis, in dem rekursiv nach Jars und Klassen gesucht werden soll.");
    System.out.println("    [3]... Liste von Parametern, die an die eigentlich Anwendung durchgereicht werden sollen.");
    System.exit(1);
  }
}


/*********************************************************************
 * $Log: BootStrap.java,v $
 * Revision 1.1  2007/07/17 08:52:00  willuhn
 * @N Ein Bootstrapper zum dynamischen Laden des Classpath
 *
 * Revision 1.4  2005/08/26 14:20:59  willuhn
 * @N added dummy SecurityManager
 *
 * Revision 1.3  2005/07/19 11:00:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/07/19 09:52:28  willuhn
 * @N added build scripts
 *
 * Revision 1.1  2005/07/15 16:32:45  willuhn
 * @N initial checkin
 *
 *********************************************************************/