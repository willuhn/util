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
package de.willuhn.boot;


/**
 * Interface eines ueber den BootLoader startfaehigen Dienst.
 * Alle Dienste, die ueber den Bootloader gestartet werden sollen,
 * muessen dieses Interface implementieren.
 * <br>Sie muessen ausserdem einen parameterlosen Konstruktor
 * mit dem Modifier <code>public</code> besitzen (siehe JavaBean-Spec.). 
 */
public interface Bootable
{

  /**
	 * Wird vom BootLoader aufgerufen, wenn der Dienst initialisiert werden soll.
   * @param loader der Bootloader selbst.
   * @param caller der vorherige Dienst, welcher das init ausgeloest hat.
   * @throws SkipServiceException wenn der Service uebersprungen werden soll.
   * Die Exception kann vom Service bei der Initialisierung
   * geworfen werden, wenn diese zwar fehlschlug, sie jedoch
   * nicht dazu fuehren soll, dass der gesamte Boot-Prozess abgebrochen wird.
   * Stattdessen wird lediglich dieser Service uebersprungen. Um den gesamten
   * Boot-Prozess abzubrechen, muss folglich eine RuntimeException geworfen werden.
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException;
	
	/**
	 * Liste von Abhaengigkeiten in Form von Class-Objekten.
	 * Die hier genannten Klassen werden <b>vor</b> der Initialisierung
	 * dieses Services gestartet.
   * @return Abhaengigkeiten.
   * Die Class-Objekte muessen alle diese Interface <code>Bootable</code> implementieren.
   */
  public Class<Bootable>[] depends();
  
  /**
   * Wird aufgerufen, wenn die Anwendung beendet wird.
   * Hier kann der Dienst Aufraeum-Arbeiten vornehmen.
   */
  public void shutdown();
}


/**********************************************************************
 * $Log: Bootable.java,v $
 * Revision 1.4  2010/11/11 16:24:08  willuhn
 * @N Bootloader ist jetzt getypt
 *
 * Revision 1.3  2006/04/26 09:37:07  web0
 * @N bootloader redesign
 **********************************************************************/