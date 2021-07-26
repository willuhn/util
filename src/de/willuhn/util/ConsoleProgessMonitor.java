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

  @Override
  public void addPercentComplete(int percent)
  {
    this.percent += percent;
  }

  @Override
  public int getPercentComplete()
  {
    return this.percent;
  }

  @Override
  public void log(String msg)
  {
    System.out.println(msg);
  }

  @Override
  public void setPercentComplete(int percent)
  {
    this.percent = percent;
  }

  @Override
  public void setStatus(int status)
  {
  }

  @Override
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
