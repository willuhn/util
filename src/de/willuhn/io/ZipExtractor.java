/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/ZipExtractor.java,v $
 * $Revision: 1.10 $
 * $Date: 2009/06/12 11:12:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Hilfklasse zum Entpacken von ZIP-Archiven.
 * @author willuhn
 */
public class ZipExtractor extends AbstractZipSupport
{

  private ZipFile zip = null;

  /**
   * ct.
   * @param zip das zu entpackende ZIP-File.
   * Das Zip-File wird nach dem Entpacken automatisch geschlossen.
   * Hierbei wird dessen <code>close()</code>-Methode aufgerufen.
   */
  public ZipExtractor(ZipFile zip)
  {
    this.zip = zip;
  }

  /**
   * Entpackt das ZIP-File in das angegebene Verzeichnis.
   * Bereits existierende Dateien/Verzeichnisse werden nur dann ueberschrieben, wenn sie neuer sind.
   * @param targetDirectory Ziel-Verzeichnis.
   * Es wird automatisch angelegt, wenn es noch nicht existiert.
   * @throws IOException
   */
  public void extract(File targetDirectory) throws IOException
  {
    monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
    monitor.setStatusText("extracting zip file " + zip.getName() + " to " + targetDirectory.getAbsolutePath());
    monitor.log("extracting zip file " + zip.getName() + " to " + targetDirectory.getAbsolutePath());
    Logger.info("extracting zip file " + zip.getName() + " to " + targetDirectory.getAbsolutePath());

    if (!targetDirectory.exists())
    {
      monitor.log("creating directory " + targetDirectory.getAbsolutePath());
      if (!targetDirectory.mkdirs())
        throw new IOException("unable to create target directory " + targetDirectory.getAbsolutePath());
    }

    // Liste aller Elemente des ZIP holen.
    Enumeration entries = zip.entries();

    // Ueber die Anzahl der Elemente in der ZIP-Datei haben wir
    // einen Anhaltspunkt fuer die Fortschrittsanzeige.
    int size = zip.size();
    monitor.log("uncompressing " + size + " elements");

    String currentName = null;
    File currentFile   = null;
    File backup        = null; 
    int i = 0;

    try
    {
      // Iterieren und entpacken.
      while (entries.hasMoreElements())
      {

        ZipEntry entry = (ZipEntry) entries.nextElement();
        currentName = entry.getName();

        // Fortschritt neu berechnen
        monitor.setPercentComplete(i * 100 / size);
        i++;

        currentFile = new File(targetDirectory,currentName);
        Logger.info(currentName);

        // Issn Verzeichnis, legen wir ggf. an.
        if (entry.isDirectory())
        {
          if (!currentFile.exists())
          {
            Logger.debug("  creating directory");
            currentFile.mkdirs();
          }
          else
          {
            Logger.info("  directory allready exists, skipping");
          }
          continue;
        }         

        // Die Dateien kommen nicht in der idealen Reihenfolge.
        // Es kann also passieren, dass zuerst eine Datei kommt,
        // das Verzeichnis, in dem sie sich befindet, aber erst
        // danach. In diesem Fall wuerde es zu einem Fehler
        // beim Schreiben der Datei kommen. Also ermitteln wir
        // von jeder Datei erst das Verzeichnis und erstellen
        // es bei Bedarf vorher noch schnell.
        int idx = currentName.lastIndexOf('/');
        if (idx >= 0)
        {
          // jepp, wir haben ein Verzeichnis, mal sehn ob's
          // existiert.
          String path = currentName.substring(0, idx);
          File f = new File(targetDirectory,path);
          if (!f.exists())
          {
            Logger.debug("  creating directory");
            if (!f.mkdirs())
              throw new IOException("unable to create directory " + f.getAbsolutePath());
          }
        }


        // Wir ueberschreiben ggf. durch aktuellere Dateien. Um das "sicher"
        // zu machen, erstellen wir von jeder Datei vorm Ueberschreiben ein
        // Backup das wir nach erfolgreichem Entpacken loeschen.
        if (currentFile.exists())
        {
          long zipTime  = entry.getTime();
          long fileTime = currentFile.lastModified();
          if (zipTime > fileTime)
          {
            Logger.info("  zip entry is newer, replacing");
            backup = new File(currentFile.getAbsolutePath() + ".bak");
            currentFile.renameTo(backup); // umbenennen
            currentFile = new File(targetDirectory,currentName); // wir erzeugen eine neue Referenz
          }
          else
          {
            Logger.info("  skipping, allready exists");
            continue;
          }
        }

        // Issne Datei, neu erzeugen.
        monitor.log(currentFile.getAbsolutePath());
      
        if (!currentFile.createNewFile())
          throw new IOException("unable to create file " + currentFile.getAbsolutePath());
      
        InputStream is  = zip.getInputStream(entry);
        if (is == null)
        {
          Logger.warn("  entry " + entry.getName() + " not found in archive, skipping");
          continue;
        }
        OutputStream os = new BufferedOutputStream(new FileOutputStream(currentFile));
        copy(is,os);

        is.close();
        os.flush();
        os.close();

        if (backup != null && backup.exists())
          backup.delete();
      }
      monitor.setPercentComplete(100);
      monitor.setStatusText("zip file " + zip.getName() + " uncompressed successfully");
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (IOException e)
    {
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      // Wenn es irgends zu einem Fehler kommt, benennen wir die ggf. letzte
      // existierende Backup-Datei schnell noch wieder zurueck
      try
      {
        if (backup != null && currentFile != null && backup.exists() && backup.isFile())
        {
          String name = currentFile.getAbsolutePath();
          if (currentFile.exists() && currentFile.isFile())
            currentFile.delete();
          backup.renameTo(new File(name));
        }
      }
      catch (Throwable t2)
      {
        // useless
      }
      throw e;
    }
    finally
    {
      if (zip != null)
      {
        try
        {
          zip.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
    }
  }
}

/*********************************************************************
 * $Log: ZipExtractor.java,v $
 * Revision 1.10  2009/06/12 11:12:44  willuhn
 * @N Falls ZIP-Entries kaputte Umlaute haben, kann es vorkommen, dass der zugehoerige InputStream nicht gefunden wird. Das warf eine NPE - jetzt werden die Entries (mit Warnung im Log) uebersprungen
 *
 * Revision 1.9  2008/12/17 00:47:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2008/12/16 16:11:30  willuhn
 * @N Uebersichtlichere Log-Ausgaben
 *
 * Revision 1.7  2008/03/07 00:46:53  willuhn
 * @N ZipCreator
 *
 * Revision 1.6  2005/07/15 08:53:17  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/03/09 01:06:20  web0
 * @D javadoc fixes
 *
 * Revision 1.4  2004/11/04 22:41:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/04 17:48:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 00:19:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 **********************************************************************/