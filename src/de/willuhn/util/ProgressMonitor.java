/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/ProgressMonitor.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/11/04 22:41:46 $
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
	 * Bitte einen absoluten Wert angeben. Der Fortschritt wird dann unabhaengig
	 * vom vorherigen Wert auf den hier uebergebenen gesetzt. 
   * @param percent prozentualer Fortschritt (muss zwischen 0 und 100 liegen).
   */
  public void setPercentComplete(int percent);

	/**
	 * Teilt dem Monitor mit, wieviel Prozent der Aufgabe gerade erledigt wurde.
	 * Bitte hier einen relativen positiven Wert angeben, um den der aktuelle
	 * Wert erhoeht werden soll.
   * @param percent Anzahl der Prozent-Punkte, um die der Fortschritt erhoeht werden soll.
   */
  public void addPercentComplete(int percent);

  /**
   * Liefert den aktuell angezeigten Fortschritt in Prozent.
   * @return aktueller Fortschritt.
   */
  public int getPercentComplete();
  
	/**
	 * Teilt dem Monitor den aktuellen Status mit.
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
 * Revision 1.3  2004/11/04 22:41:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/04 17:48:31  willuhn
 * *** empty log message ***
 *
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