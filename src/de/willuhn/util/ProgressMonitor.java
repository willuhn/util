/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/ProgressMonitor.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/07 18:06:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

/**
 * Ein Interface, welches (implementiert) verwendet werden kann, wenn
 * eine Funktion Ausgaben ueber den aktuellen Bearbeitungsstand ausgeben
 * soll. Hierzu kann es beispielsweise eine Implementierung geben, welche
 * die Ausgaben auf die Console schreibt oder in einem grafischen Dialog
 * als Fortschrittsbalken zeigt.
 */
public interface ProgressMonitor
{
	/**
	 * Teilt dem Monitor mit, wieviel Prozent der Aufgabe bereits abgearbeitet sind.
   * @param percent prozentualer Fortschritt (muss zwischen 0 und 100 liegen).
   */
  public void percentComplete(int percent);

	/**
	 * Teilt dem Monitor den aktuellen Status des Tasks mit.
   * Das koennen selbst definierte Konstanten sein.
   * @param status der aktuelle Status.
   */
  public void setStatus(int status);

	/**
	 * Teilt dem Monitor einen sprechenden Status-Text mit.
   * @param text Status-Text.
   */
  public void setStatusText(String text);

	/**
	 * Teilt dem Monitor mit, dass der angegebene Text protokolliert werden soll.
   * @param msg die zur protokollierende Nachricht.
   */
  public void log(String msg);
}


/**********************************************************************
 * $Log: ProgressMonitor.java,v $
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 * Revision 1.1  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 **********************************************************************/