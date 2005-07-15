/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/ZipExtractor.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/07/15 08:53:17 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.willuhn.util.ProgressMonitor;

/**
 * Hilfklasse zum Entpacken von ZIP-Archiven.
 * @author willuhn
 */
public class ZipExtractor
{

  private ZipFile zip = null;
  private ProgressMonitor monitor = new DummyMonitor();

  /**
   * ct.
   * @param zip das zu entpackende ZIP-File.
   */
  public ZipExtractor(ZipFile zip)
  {
    this.zip = zip;
  }

  /**
   * Legt den Progress-Monitor fest, ueber den Ausgaben waehrend des Entpackens ausgegeben werden sollen.
   * Wird dieser nicht definiert, werden keine Ausgaben vorgenommen.
   * @param monitor
   */
  public void setMonitor(ProgressMonitor monitor)
  {
    if (monitor != null)
      this.monitor = monitor;
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
    if (targetDirectory == null)
      throw new IOException("no target directory defined");

    monitor.setStatusText("extracting zip file " + zip.getName() + " to " + targetDirectory.getAbsolutePath());
    monitor.log("extracting zip file " + zip.getName() + " to " + targetDirectory.getAbsolutePath());

    if (!targetDirectory.exists())
    {
      monitor.log("  creating directory " + targetDirectory.getAbsolutePath());
      if (!targetDirectory.mkdirs())
        throw new IOException("unable to create target directory " + targetDirectory.getAbsolutePath());
    }

    // Liste aller Elemente des ZIP holen.
    Enumeration entries = zip.entries();

    // Ueber die Anzahl der Elemente in der ZIP-Datei haben wir
    // einen Anhaltspunkt fuer die Fortschrittsanzeige.
    int size = zip.size();
    monitor.log("  uncompressing " + size + " elements");

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

        monitor.log("  processing " + currentName);

        // Fortschritt neu berechnen
        monitor.setPercentComplete(i * 100 / size);
        i++;

        currentFile = new File(targetDirectory,currentName);

        // Issn Verzeichnis, legen wir ggf. an.
        if (entry.isDirectory())
        {
          if (!currentFile.exists())
          {
            monitor.log("    creating directory " + currentFile.getAbsolutePath());
            currentFile.mkdirs();
          }
          else
          {
            monitor.log("    directory " + currentFile.getAbsolutePath() + " allready exists, skipping");
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
            monitor.log("    creating directory " + f.getAbsolutePath());
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
            monitor.log("    zip entry is newer, replacing");
            backup = new File(currentFile.getAbsolutePath() + ".bak");
            currentFile.renameTo(backup); // umbenennen
            currentFile = new File(targetDirectory,currentName); // wir erzeugen eine neue Referenz
          }
          else
          {
            monitor.log("    skipping, allready exists");
            continue;
          }
        }

        // Issne Datei, neu erzeugen.
        monitor.log("    creating file " + currentFile.getAbsolutePath());
      
        if (!currentFile.createNewFile())
          throw new IOException("unable to create file " + currentFile.getAbsolutePath());
      
        monitor.log("    uncompressing");
        InputStream is = zip.getInputStream(entry);
        OutputStream os = new FileOutputStream(currentFile);
        byte b[] = new byte[1028];
        int read = 0;
        do
        {
          read = is.read(b);
          if (read > 0)
            os.write(b,0,read);
        }
        while(read != -1);

        is.close();
        os.flush();
        os.close();

        if (backup != null && backup.exists())
          backup.delete();
      }
      monitor.setPercentComplete(100);
      monitor.setStatusText("zip file " + zip.getName() + " uncompressed successfully");
    }
    catch (IOException e)
    {
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
  }

  /**
   * Dummy-Implementierung.
   */
  private class DummyMonitor implements ProgressMonitor
  {
    /**
     * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
     */
    public void setPercentComplete(int percent) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
     */
    public void addPercentComplete(int percent) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
     */
    public int getPercentComplete() {return 0;}
    /**
     * @see de.willuhn.util.ProgressMonitor#setStatus(int)
     */
    public void setStatus(int status) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
     */
    public void setStatusText(String text) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
     */
    public void log(String msg) {}
  }
}

/*********************************************************************
 * $Log: ZipExtractor.java,v $
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