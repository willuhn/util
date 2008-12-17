/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/FileUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/12/17 01:01:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.io;

import java.io.File;
import java.io.IOException;

/**
 * Hilfsklasse mit diversen statischen Funktionen.
 */
public class FileUtil
{
  /**
   * Loescht ein Verzeichnis rekursiv.
   * @param dir das rekursiv zu loeschende Verzeichnis.
   * @return true, wenn das Verzeichnis korrekt geloescht werden konnte.
   * @throws IOException wenn beim Loeschen ein Fehler auftrat.
   */
  public static boolean deleteRecursive(File dir) throws IOException
  {
    File candir = dir.getCanonicalFile();

    // Wen sich der kanonische Pfad vom absoluten unterscheidet,
    // ist es ein Symlink. Es waere viel zu gefaehrlich, dem zu folgen
    if (!candir.equals(dir.getAbsoluteFile()))
      return false;

    // Rekursion
    File[] files = candir.listFiles();
    if (files != null)
    {
      for (int i=0;i<files.length;++i)
      {
        // Wenn es sich so loeschen laesst, war es eine Datei,
        // ein leeres Verzeichnis oder ein Symlink. Im letzteren
        // Fall haben wir nur den Symlink geloescht, sind ihm
        // jedoch nicht gefolgt
        boolean deleted = files[i].delete();
        if (!deleted)
        {
          if (files[i].isDirectory())
            deleteRecursive(files[i]); // Scheint ein Verzeichnis zu sein?
        }
      }
    }

    // So, fertig. Verzeichnis selbst kann geloescht werden
    return dir.delete();  
  }
}


/**********************************************************************
 * $Log: FileUtil.java,v $
 * Revision 1.1  2008/12/17 01:01:18  willuhn
 * @N Funktion zum rekursiven Loeschen von Dateien/Verzeichnissen
 *
 **********************************************************************/
