/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/SkipServiceException.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/03 22:11:49 $
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
 * Eine Exception, die von einem Service bei der Initialisierung
 * geworfen werden kann, wenn diese zwar fehlschlug, sie jedoch
 * nicht dazu fuehren soll, dass der gesamte Boot-Prozess abgebrochen wird.
 */
public class SkipServiceException extends Exception {


  /**
   * ct.
   */
  public SkipServiceException() {
    super();
  }

  /**
   * ct.
   * @param message
   */
  public SkipServiceException(String message) {
    super(message);
  }

  /**
   * ct.
   * @param cause
   */
  public SkipServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * ct.
   * @param message
   * @param cause
   */
  public SkipServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}


/**********************************************************************
 * $Log: SkipServiceException.java,v $
 * Revision 1.1  2004/06/03 22:11:49  willuhn
 * *** empty log message ***
 *
 **********************************************************************/