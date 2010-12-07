/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/IOUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/12/07 16:01:53 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.willuhn.logging.Logger;

/**
 * Util-Klasse mit Hilfsfunktionen fuer Streams.
 */
public class IOUtil
{
  /**
   * Kopiert die Daten vom InputStream in den OutputStream.
   * @param is Quell-Stream.
   * @param os Ziel-Stream.
   * @return Anzahl der kopierten Bytes.
   * @throws IOException
   */
  public static long copy(InputStream is, OutputStream os) throws IOException
  {
    byte b[] = new byte[4096];
    long r = 0;
    int read = 0;
    while ((read = is.read(b)) != -1)
    {
      if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
      {
        os.write(b,0,read);
        r += read;
      }
    }
    return r;
  }
  
  /**
   * Schliesst den Stream.
   * Eine ggf. auftretende Exception wird nicht weitergeworfen sondern nur geloggt.
   * @param c der zu schliessende Stream.
   * @return true, wenn das Schliessen erfolgreich war, sonst false.
   */
  public static boolean close(Closeable c)
  {
    try
    {
      c.close();
      return true;
    }
    catch (IOException e)
    {
      Logger.error("error while closing stream",e);
      return false;
    }
  }
}



/**********************************************************************
 * $Log: IOUtil.java,v $
 * Revision 1.1  2010/12/07 16:01:53  willuhn
 * @N IOUtil
 *
 **********************************************************************/