/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/Bootable.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/27 15:11:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.boot;

/**
 * Interface eines ueber den BootLoader startfaehigen Dienst.
 * Alle Dienste, die ueber den Bootloader gestartet werden sollen,
 * muessen dieses Interface implementieren.
 * <br>Sie muessen ausserdem einen parameterlosen Konstruktor
 * mit dem Modifier <code>public</code> besitzen (siehe JavaBean-Spec.). 
 */
public interface Bootable {

  /**
	 * Wird vom BootLoader aufgerufen, wenn der Dienst initialisiert werden soll.
   * @param caller der Dienst, welcher das init ausgeloest hat und somit
   * von diesem Dienst abhaengig ist.
   * @return Liste von weiteren Klassen, die <b>nach</b> der erfolgreichen
   * Initialisierung zusaetzlich gestartet werden sollen.
   * @throws SkipServiceException wenn der Service uebersprungen werden soll. 
   * Die Exception kann vom Service bei der Initialisierung
   * geworfen werden kann, wenn diese zwar fehlschlug, sie jedoch
   * nicht dazu fuehren soll, dass der gesamte Boot-Prozess abgebrochen wird.
   * Stattdessen wird lediglich dieser Service uebersprungen. Um den gesamten
   * Boot-Prozess abzubrechen, muss folglich eine RuntimeException geworfen werden.
   */
  public Class[] init(Bootable caller) throws SkipServiceException;
	
	/**
	 * Liste von Abhaengigkeiten in Form von Class-Objekten.
	 * Die hier genannten Klassen werden <b>vor</b> der Initialisierung
	 * dieses Services gestartet.
   * @return Abhaengigkeiten.
   * Die Class-Objekte muessen alle diese Interface <code>Bootable</code> implementieren.
   */
  public Class[] depends();
  
  /**
   * Diese Funktion wird aufgerufen, wenn eine Abhaengigkeiten nicht erfuellt
   * werden konnte.
   * @param e die vom Service geworfene Exception.
   */
  public void failedDependency(SkipServiceException e);
  
	/**
	 * Liefert den Wert des benannten Parameters.
	 * Diese Funktion dient der Kommunikation zwischen Diensten.
	 * Ein Dienst kann ueber die Funktion zB Startparameter von
	 * seinem Aufrufer abfragen.
   * @param key Schluessel des Parameters.
   * @return Wert des Parameters.
   */
  public Object getParam(Object key);
  
  /**
   * Wird aufgerufen, wenn die Anwendung beendet wird.
   * Hier kann der Dienst Aufraeum-Arbeiten vornehmen.
   */
  public void shutdown();
}


/**********************************************************************
 * $Log: Bootable.java,v $
 * Revision 1.1  2005/02/27 15:11:42  web0
 * @C some renaming
 *
 * Revision 1.3  2004/06/30 20:58:52  willuhn
 * @C some refactoring
 *
 * Revision 1.2  2004/06/03 22:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/