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

package de.willuhn.sql.version;

import java.io.File;
import java.sql.Connection;

import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;


/**
 * Liefert dem Update-Prozess die benoetigten Informationen.
 */
public interface UpdateProvider
{
  /**
   * Liefert die aktuelle Version
   * @return die aktuelle Versionsnummer.
   * @throws ApplicationException wenn beim Ermitteln ein Fehler auftrat.
   */
  public int getCurrentVersion() throws ApplicationException;
  
  /**
   * Wird mit der neuen Versionsnummer aufgerufen, wenn das Update durchlief.
   * @param newVersion die neue Versionsnummer.
   * @throws ApplicationException Wenn beim Uebernehmen der Versionsnummer ein Fehler auftrat.
   */
  public void setNewVersion(int newVersion) throws ApplicationException;
  
  /**
   * Liefert die zu verwendende Connection.
   * @return die Connection.
   * @throws ApplicationException wenn beim Erstellen der Connection ein Fehler auftrat.
   */
  public Connection getConnection() throws ApplicationException;
  
  /**
   * Liefert einen Monitor, an den Meldungen ueber den Update-Verlauf gesendet werden koennen.
   * @return der Update-Monitor.
   */
  public ProgressMonitor getProgressMonitor();
  
  /**
   * Liefert den Pfad, in dem der Update-Prozess nach Updates suchen soll.
   * @return Pfad, in dem nach den Update gesucht werden soll.
   * @throws ApplicationException
   */
  public File getUpdatePath() throws ApplicationException;
}


/**********************************************************************
 * $Log: UpdateProvider.java,v $
 * Revision 1.3  2007/12/03 09:36:27  willuhn
 * @N Patch von Heiner
 *
 * Revision 1.2  2007/10/01 23:29:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/10/01 23:16:56  willuhn
 * @N Erste voellig ungetestete Version eines generischen Updaters.
 *
 **********************************************************************/
