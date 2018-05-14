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