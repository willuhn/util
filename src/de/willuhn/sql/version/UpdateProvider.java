/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/version/UpdateProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/01 23:16:56 $
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
   * @throws ApplicationException wenn beim Ermitteln ein Fehler auftrat.
   */
  public int getCurrentVersion() throws ApplicationException;
  
  /**
   * Wird mit der neuen Versionsnummer aufgerufen, wenn das Update durchlief.
   * @param newVersion die neue Versionsnummer.
   */
  public void setNewVersion(int newVersion);
  
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
   * Liefert den Pfad, in dem der Update-Prozess 
   * @return Pfad, in dem nach den Update gesucht werden soll.
   * @throws ApplicationException
   */
  public File getUpdatePath() throws ApplicationException;
}


/**********************************************************************
 * $Log: UpdateProvider.java,v $
 * Revision 1.1  2007/10/01 23:16:56  willuhn
 * @N Erste voellig ungetestete Version eines generischen Updaters.
 *
 **********************************************************************/
