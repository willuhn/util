/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/AbstractZipSupport.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/07 00:46:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basisklasse fuer ZIP-Support.
 * @author willuhn
 */
public class AbstractZipSupport
{
  protected ProgressMonitor monitor = new DummyMonitor();

  /**
   * Legt den Progress-Monitor fest, ueber den Ausgaben waehrend des Packens/Entpackens ausgegeben werden sollen.
   * Wird dieser nicht definiert, werden keine Ausgaben vorgenommen.
   * @param monitor
   */
  public void setMonitor(ProgressMonitor monitor)
  {
    if (monitor != null)
      this.monitor = monitor;
  }
  
  /**
   * Hilfsfunktion zum Kopieren der Daten von einem Stream zum andern.
   * @param is Quell-Stream.
   * @param os Ziel-Stream.
   * @throws IOException
   */
  protected void copy(InputStream is, OutputStream os) throws IOException
  {
    byte b[] = new byte[4096];
    int read = 0;
    while ((read = is.read(b)) >= 0)
    {
      if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
        os.write(b,0,read);
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
 * $Log: AbstractZipSupport.java,v $
 * Revision 1.1  2008/03/07 00:46:53  willuhn
 * @N ZipCreator
 *
 **********************************************************************/