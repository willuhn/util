/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/targets/Target.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/12/31 19:34:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.logging.targets;

import de.willuhn.logging.Message;

/**
 * Basis-Interface aller Logging-Ziele.
 */
public interface Target
{
  /**
	 * Schreibt die uebergebene Nachricht in das Logging-Target.
   * @param message zu loggende Nachricht.
   * @throws Exception
   */
  public void write(Message message) throws Exception;
  
  /**
   * Schliesst das Target.
   * @throws Exception
   */
  public void close() throws Exception;
}


/**********************************************************************
 * $Log: Target.java,v $
 * Revision 1.1  2004/12/31 19:34:22  willuhn
 * @C some logging refactoring
 * @N syslog support for logging
 *
 **********************************************************************/