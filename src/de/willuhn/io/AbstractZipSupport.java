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
    public int getPercentComplete()
    {
      return 0;
    }

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
 * Revision 1.2  2010/12/07 16:01:53  willuhn
 * @N IOUtil
 *
 * Revision 1.1  2008/03/07 00:46:53  willuhn
 * @N ZipCreator
 *
 **********************************************************************/