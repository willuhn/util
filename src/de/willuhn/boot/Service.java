/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/Attic/Service.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/30 20:58:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.boot;

/**
 * Interface eines ueber den BootLoader startfaehigen Services.
 * Alle Services, die ueber den Bootloader gestartet werden sollen,
 * muessen dieses Interface implementieren.
 * <br>Sie muessen ausserdem einen parameterlosen Konstruktor
 * mit dem Modifier <code>public</code> besitzen (siehe JavaBean-Spec.). 
 */
public interface Service {

  /**
	 * Wird vom BootLoader aufgerufen, wenn der Service initialisiert werden soll.
   * @param caller der Service, welcher das init ausgeloest hat und somit
   * von diesem Service abhaengig ist.
   * @return Liste von weiteren Klassen, die <b>nach</b> der erfolgreichen
   * Initialisierung zusaetzlich gestartet werden sollen.
   * @throws SkipServiceException wenn der Service uebersprungen werden soll. 
   * Die Exception kann vom Service bei der Initialisierung
   * geworfen werden kann, wenn diese zwar fehlschlug, sie jedoch
   * nicht dazu fuehren soll, dass der gesamte Boot-Prozess abgebrochen wird.
   * Stattdessen wird lediglich dieser Service uebersprungen. Um den gesamten
   * Boot-Prozess abzubrechen, muss folglich eine RuntimeException geworfen werden.
   */
  public Class[] init(Service caller) throws SkipServiceException;
	
	/**
	 * Liste von Abhaengigkeiten in Form von Class-Objekten.
	 * Die hier genannten Klassen werden <b>vor</b> der Initialisierung
	 * dieses Services gestartet.
   * @return Abhaengigkeiten.
   * Die Class-Objekte muessen alle diese Interface <code>Service</code> implementieren.
   */
  public Class[] depends();
  
  /**
   * Diese Funktion wird aufgerufen, wenn eine Abhaengigkeiten nicht erfuellt
   * werden konnte.
   * @param dependency Service, der mit einer ServletException abgebrochen ist.
   * @param e die vom Service geworfene Klasse.
   */
  public void failedDependency(Service dependency, SkipServiceException e);
  
	/**
	 * Liefert den Wert des benannten Parameters.
	 * Diese Funktion dient der Kommunikation zwischen Services.
	 * Ein Service kann ueber die Funktion zB Startparameter von
	 * seinem Aufrufer abfragen.
   * @param key Schluessel des Parameters.
   * @return Wert des Parameters.
   */
  public Object getParam(Object key);
  
  /**
   * Wird aufgerufen, wenn die Anwendung beendet wird.
   * Hier kann der Service Aufraeum-Arbeiten vornehmen.
   */
  public void shutdown();
}


/**********************************************************************
 * $Log: Service.java,v $
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