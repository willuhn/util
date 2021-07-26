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
package de.willuhn.io;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Diese Klasse kann Dateien ueberwachen und bei Aenderung ein Event ausloesen.
 */
public class FileWatch
{
  private final static long INTERVAL = 10 * 1000L;
  
  private static Timer timer  = new Timer(true);
  private static Vector files = new Vector();

  static
  {
    timer.schedule(new Worker(),INTERVAL,INTERVAL);
  }
  
  /**
   * Registriert eine neue zu ueberwachende Datei.
   * @param file die zu ueberwachende Datei.
   * @param observer der zu benachrichtigende Observer.
   */
  public static void addFile(File file, Observer observer)
  {
    for (int i=0;i<files.size();++i)
    {
      FileObject f = (FileObject) files.get(i);
      if (f.file.equals(file))
      {
        // Datei haben wir schon. Dann haengen wir nur
        // den Observer dran.
        f.addObserver(observer);
        return;
      }
    }
    
    // Neue Datei
    files.add(new FileObject(file,observer));
  }
  
  /**
   * Entfernt eine zu ueberwachende Datei.
   * @param file die zu entfernende Datei.
   */
  public static void removeFile(File file)
  {
    for (int i=0;i<files.size();++i)
    {
      FileObject f = (FileObject) files.get(i);
      if (f.file.equals(file))
      {
        f.deleteObservers(); // ist eigentlich nicht noetig, aber damit werden gleich die Referenzen aufgehoben
        files.remove(i);
      }
    }
  }

  /**
   * Hilfsobjekt zum Checken einer einzelnen Datei auf Aenderungen.
   */
  private static class FileObject extends Observable
  {
    private File file         = null;
    private long lastModified = 0L;
    
    /**
     * ct.
     * @param file zu ueberwachende Datei.
     * @param observer zu beanchrichtigender Server.
     */
    private FileObject(File file,Observer observer)
    {
      this.file = file;
      this.lastModified = file.lastModified();
      this.addObserver(observer);
    }
    
    /**
     * Prueft die Datei auf Aenderungen und informiert ggf. die Observer.
     */
    private void check()
    {
      if (!file.exists())
      {
        // Datei existiert nicht mehr. Dann feuern wir das Event
        // und entfernen uns aus der Liste
        removeFile(this.file);
        this.setChanged();
        this.notifyObservers();
        return;
      }
      
      long newLastModified = file.lastModified();
      try
      {
        if (newLastModified != this.lastModified)
        {
          this.setChanged();
          this.notifyObservers(this.file);
        }
      }
      finally
      {
        this.lastModified = newLastModified;
      }
    }
  }
  
  /**
   * Worker, der die Dateien auf Aenderungen ueberwacht.
   */
  private static class Worker extends TimerTask
  {
    /**
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
      for (int i=0;i<files.size();++i)
        ((FileObject) files.get(i)).check();
    }
  }

}

/*********************************************************************
 * $Log: FileWatch.java,v $
 * Revision 1.1  2007/03/09 18:03:32  willuhn
 * @N classloader updates
 * @N FileWatch
 *
 **********************************************************************/