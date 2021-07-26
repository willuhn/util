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
package de.willuhn.logging;

/**
 * Log-Level.
 */
public enum Level
{
  TRACE,
  DEBUG,
  INFO,
  WARN,
  ERROR;

  @Override
  public String toString()
  {
    return "Name: " + this.name();
  }

  /**
   * Prüft, ob mit dem übergebenen Log-Level Meldungen geloggt werden sollen.
   *
   * @param l das zu testende Log-Level.
   * @return {@code true}, wenn Log-Level eingeschaltet, sonst {@code false}.
   */
  public boolean includes(Level l)
  {
    return l != null && 0 <= this.compareTo(l);
  }
}

/**********************************************************************
 * $Log: Level.java,v $
 * Revision 1.6  2021/07/26 21:37:29  ruderphilipp
 * Umwandlung von class in enum
 *
 * Revision 1.5  2013/09/21 23:18:43  willuhn
 * @N Neues Log-Level "TRACE" unterhalb von "DEBUG"
 *
 * Revision 1.4  2005/03/24 17:28:25  web0
 * @B bug in Level.findByName
 *
 * Revision 1.3  2005/03/09 01:06:20  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/01/14 00:49:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 **********************************************************************/
