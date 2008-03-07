/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/ZipCreator.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/07 00:46:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.willuhn.util.ProgressMonitor;


/**
 * Hilfsklasse zum Erzeugen von ZIP-Dateien.
 */
public class ZipCreator extends AbstractZipSupport
{
  private ZipOutputStream target = null;
  
  /**
   * @param os der OutputStream, in den die ZIP-Daten geschrieben werden sollen.
   * Der OutputStream wird intern nicht gepuffert, es sollte also bereits
   * ein BufferedOutputStream uebergeben werden. Ausserdem muss der ZipCreator
   * explizit durch Aufruf von <code>close()</code> geschlossen werden, da
   * er ja nicht selbst erkennen kann, wann alle Dateien hinzugefuegt wurden.
   * Das ist WICHTIG, da die ZIP-Datei sonst nicht lesbar ist.
   */
  public ZipCreator(OutputStream os)
  {
    this.target = new ZipOutputStream(os);
  }
  
  /**
   * Schliesst den ZipCreator und den zugehoerigen OutputStream.
   * @throws IOException
   */
  public void close() throws IOException
  {
    if (this.target != null)
    {
      try
      {
        this.target.close();
        monitor.setPercentComplete(100);
        monitor.setStatusText("zip file created successfully");
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
      }
      catch (IOException e)
      {
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        throw e;
      }
    }
  }
  
  private boolean running = false;
  
  /**
   * Fuegt der ZIP-Datei ein Verzeichnis/eine Datei hinzu.
   * Wenn es sich um ein Verzeichnis handelt, wird es rekursiv samt allen enthaltenen Dateien hinzugefuegt.
   * @param entry das hinzuzufuegende Verzeichnis/die Datei.
   * @throws IOException
   */
  public void add(File entry) throws IOException
  {
    _add(entry.getName(),entry);
  }

  /**
   * Fuegt der ZIP-Datei ein Verzeichnis/eine Datei hinzu.
   * Wenn es sich um ein Verzeichnis handelt, wird es rekursiv samt allen enthaltenen Dateien hinzugefuegt.
   * @param zipPath der Pfad, in dem die Datei gespeichert werden soll.
   * @param handle das hinzuzufuegende Verzeichnis/die Datei.
   * @throws IOException
   */
  private void _add(String zipPath, File handle) throws IOException
  {
    if (!handle.exists())
      return; // Das koennen wir tolierieren
    
    if (!handle.canRead())
      throw new IOException("cannot read " + handle.getAbsolutePath());

    // Status beim ersten Aufruf setzen
    if (!running)
    {
      monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
      running = true;
    }
    
    // Verzeichnisse fuegen wir rekursiv hinzu.
    // Als Basis-Pfad gilt das oberste.
    if (handle.isDirectory())
    {
      File[] children = handle.listFiles();
      if (children.length == 0)
      {
        // Leeres Verzeichnis. Legen wir trotzdem an
        // Verzeichnis selbst anlegen. Mit Slash am Ende
        // damit es als Verzeichnis anerkannt wird
        this.target.putNextEntry(new ZipEntry(zipPath + "/"));
        this.target.closeEntry();
        return;
      }
      for (int i=0;i<children.length;++i)
      {
        _add(zipPath + "/" + children[i].getName(),children[i]);
      }
      return;
    }

    // Das eigentliche Zippen
    try
    {
      monitor.addPercentComplete(1); // natuerlich geraten ;)
      monitor.log("    adding file " + zipPath);
      this.target.putNextEntry(new ZipEntry(zipPath));
      InputStream is  = new BufferedInputStream(new FileInputStream(handle));
      copy(is,this.target);
      this.target.closeEntry();

      is.close();
    }
    catch (IOException e)
    {
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw e;
    }
  }

}


/**********************************************************************
 * $Log: ZipCreator.java,v $
 * Revision 1.1  2008/03/07 00:46:53  willuhn
 * @N ZipCreator
 *
 **********************************************************************/
