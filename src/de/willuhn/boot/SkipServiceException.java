/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/SkipServiceException.java,v $
 * $Revision: 1.2 $
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
 * Eine Exception, die von einem Service bei der Initialisierung
 * geworfen werden kann, wenn diese zwar fehlschlug, sie jedoch
 * nicht dazu fuehren soll, dass der gesamte Boot-Prozess abgebrochen wird.
 */
public class SkipServiceException extends Exception {

	private Bootable bootable = null;

  /**
   * ct.
   * @param bootable Dienst, der die Exception ausgeloest hat.
   * @param message Text.
   */
  public SkipServiceException(Bootable bootable,String message) {
		super(message);
		this.bootable = bootable;
  }

	/**
	 * ct.
	 * @param bootable Dienst, der die Exception ausgeloest hat.
	 * @param message Text.
	 */
	public SkipServiceException(Bootable bootable,String message, Throwable cause) {
		super(message,cause);
		this.bootable = bootable;
	}

	/**
	 * Liefert den Dienst, der den Fehler augeloest hat.
   * @return Dienst.
   */
  public Bootable getBootable()
	{
		return bootable;
	}

}


/**********************************************************************
 * $Log: SkipServiceException.java,v $
 * Revision 1.2  2005/02/27 15:11:42  web0
 * @C some renaming
 *
 * Revision 1.1  2004/06/03 22:11:49  willuhn
 * *** empty log message ***
 *
 **********************************************************************/