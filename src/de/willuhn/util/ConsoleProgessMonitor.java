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

package de.willuhn.util;

/**
 * Dummy-Implementierung eines Progress-Monitors, der nach STDOUT schreibt.
 */
public class ConsoleProgessMonitor implements ProgressMonitor
{
  private int percent = 0;

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int percent)
  {
    this.percent += percent;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    return this.percent;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String msg)
  {
    System.out.println(msg);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int percent)
  {
    this.percent = percent;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String text)
  {
    System.out.println(text);
  }

}


/**********************************************************************
 * $Log: ConsoleProgessMonitor.java,v $
 * Revision 1.1  2008/11/26 22:11:35  willuhn
 * @N Console-Progressmonitor
 * @N main()-Funktion fuer ScriptExecutor
 *
 **********************************************************************/
