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
    @Override
    public void setPercentComplete(int percent) {}
    @Override
    public void addPercentComplete(int percent) {}
    @Override
    public int getPercentComplete() {return 0;}
    @Override
    public void setStatus(int status) {}
    @Override
    public void setStatusText(String text) {}
    @Override
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