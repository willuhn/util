/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/version/Updater.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/12/11 15:19:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.sql.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.util.ApplicationException;


/**
 * Ein generisches Update-Utility.
 */
public class Updater
{
  private UpdateProvider provider = null;
  private MyClassloader loader    = null;

  /**
   * ct.
   * @param provider der zu verwendende Provider.
   */
  public Updater(UpdateProvider provider)
  {
    this.provider = provider;
    this.loader   = new MyClassloader(provider.getClass().getClassLoader());
  }
  
  /**
   * Fuehrt das Update durch.
   * @throws ApplicationException wenn ein Fehler beim Update auftrat.
   */
  public void execute() throws ApplicationException
  {
    int currentVersion = provider.getCurrentVersion();
    Logger.info("current version: " + currentVersion);

    Logger.info("searching for available updates");
    
    // Wir ermitteln eine Liste aller Dateien im Update-Verzeichnis.
    File baseDir = provider.getUpdatePath();
    if (baseDir == null || !baseDir.exists() || !baseDir.canRead() || !baseDir.isDirectory())
    {
      Logger.warn("no update dir given or not readable");
      return;
    }
    
    File[] files = baseDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name)
      {
        if (name == null)
          return false;
        return name.endsWith(".sql") || name.endsWith(".class");
      }
    });
    
    // Jetzt sortieren wir die Dateien und holen uns anschliessend
    // nur die, welche sich hinter der aktuellen Versionsnummer
    // befinden.
    List l = Arrays.asList(files);
    Collections.sort(l);
    
    files = (File[]) l.toArray(new File[l.size()]);
    
    
    // wir iterieren ueber die Liste, und ueberspringen alle
    // bis zur aktuellen Version.
    ArrayList updates = new ArrayList();
    for (int i=0;i<files.length;++i)
    {
      File current = files[i];
      
      // Unterverzeichnisse (z.Bsp. "CVS") ignorieren wir.
      if (current.isDirectory())
        continue;

      // Update-Datei nicht lesbar.
      if (!current.canRead() || !current.isFile())
      {
        Logger.warn("update file " + current + " not readable, skipping");
        continue;
      }
      
      int number = toNumber(current.getName());
      if (number < 0)
      {
        Logger.error("invalid update filename: " + current.getName() + ", skipping");
        continue;
      }

      // Wir uebernehmen das Update nur, wenn dessen
      // Versionsnummer hoeher als die aktuelle ist.
      if (number > currentVersion)
        updates.add(current);
    }

    // Keine Updates gefunden
    if (updates.size() == 0)
    {
      Logger.info("no new updates found");
      return;
    }

    // Wir fuehren die Updates aus.
    Logger.info("found " + updates.size() + " update files");
    for (int i=0;i<updates.size();++i)
    {
      
      File f = (File) updates.get(i);
      execute(f); // Update ausfuehren

      // Neue Versionsnummer an Provider mitteilen
      try
      {
        provider.setNewVersion(toNumber(f.getName()));
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        throw new ApplicationException(e);
      }
    }
    Logger.info("update completed");
  }
  
  /**
   * Macht eine Versionsnummer aus dem Dateinamen.
   * @param filename Dateiname.
   * @return Versionsnummer oder -1 wenn sie ungueltig ist.
   */
  private int toNumber(String filename)
  {
    try
    {
      String number = filename;
      // Dateiendung abschneiden
      int ext = number.lastIndexOf(".");
      if (ext != -1)
        number = number.substring(0,ext);

      // Alles bis auf Zahlen entfernen
      // Ist auch noetig, weil Klassennamen nicht nur aus Zahlen bestehen duerfen
      number = number.replaceAll("[^0-9]","");
      return Integer.parseInt(number);
    }
    catch (Exception e)
    {
      Logger.error("invalid update filename: " + filename);
    }
    return -1;
  }
  
  /**
   * Fuehrt ein einzelnes Update durch.
   * @param update das auszufuehrende Update.
   * @throws ApplicationException
   */
  private void execute(File update) throws ApplicationException
  {
    String filename = update.getName();
    
    // SQL-Script direkt ausfuehren.
    if (filename.endsWith(".sql"))
    {
      Reader reader = null;
      try
      {
        reader = new FileReader(update);
        Logger.info("  executing " + filename);
        ScriptExecutor.execute(reader,provider.getConnection(),provider.getProgressMonitor());
        return;
      }
      catch (Exception e)
      {
        throw new ApplicationException(e);
      }
      finally
      {
        if (reader != null)
        {
          try {
            reader.close();
          } catch (Exception e) {/*useless*/}
        }
      }
    }

    // .class-File. Also Klasse laden, auf "Update" casten
    // und ausfuehren.
    if (filename.endsWith(".class"))
    {
      try
      {
        Class clazz = this.loader.findClass(update.getAbsolutePath());
        Update u = (Update) clazz.newInstance();
        Logger.info("  executing " + u.getClass().getName() + ": " + u.getName());
        u.execute(this.provider);
        return;
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        throw new ApplicationException(e);
      }
    }

    Logger.warn("unknown update file format: " + filename + ", skipping");
  }
  
  /**
   * Ein eigener Classloader, um aus einer .class-Datei
   * die Klasse zu laden.
   */
  private class MyClassloader extends ClassLoader
  {
    /**
     * ct
     * @param parent
     */
    public MyClassloader(ClassLoader parent)
    {
      super(parent);
    }
    
    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    public Class findClass(String name)
    {
      InputStream is = null;
      try
      {
        // Datei in Byte-Array laden
        File f = new File(name);
        is = new FileInputStream(f);
        byte[] data = new byte[(int)f.length()];
        is.read(data);

        // Verzeichnis-Angaben ignorieren
        name = f.getName();

        // ".class" abschneiden
        name = name.substring(0,name.lastIndexOf("."));
        
        // Byte-Array von Parent-Classloader laden
        return defineClass(name, data, 0, data.length);
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
      finally
      {
        if (is != null)
        {
          try {
            is.close();
          } catch (Exception e) {/* useless */};
        }
      }
    }
  }

}


/**********************************************************************
 * $Log: Updater.java,v $
 * Revision 1.6  2007/12/11 15:19:20  willuhn
 * @N Alles ausser *.class und *.sql ignorieren
 *
 * Revision 1.5  2007/12/11 14:33:46  willuhn
 * @B Verzeichnis-Angaben in Klassennamen ignorieren
 *
 * Revision 1.4  2007/12/11 00:27:22  willuhn
 * @B Bug beim Uebernehmen der Versionsnummer
 *
 * Revision 1.3  2007/12/07 00:42:56  willuhn
 * @B Alles ausser Zahlen aus Dateinamen von Updates streichen
 *
 * Revision 1.2  2007/12/03 09:36:27  willuhn
 * @N Patch von Heiner
 *
 * Revision 1.1  2007/10/01 23:16:56  willuhn
 * @N Erste voellig ungetestete Version eines generischen Updaters.
 *
 **********************************************************************/
