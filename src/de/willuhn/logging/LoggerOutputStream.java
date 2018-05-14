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
package de.willuhn.logging;

import de.willuhn.io.LineOutputStream;

/**
 * Ein OutputStream, der alle Ausgaben in den Logger schreibt.
 */
public class LoggerOutputStream extends LineOutputStream {

	private Level level;

  /**
   * ct.
   * @param logLevel das Log-Level, mit dem der OutputStream schreiben soll.
   */
  public LoggerOutputStream(Level logLevel)
  {
    super();
    this.level = logLevel;
  }

  /**
   * @see de.willuhn.io.LineOutputStream#writeLine(java.lang.String)
   */
  public void writeLine(String s)
  {
    Logger.write(level,s);
  }

}


/**********************************************************************
 * $Log: LoggerOutputStream.java,v $
 * Revision 1.2  2005/03/09 01:06:21  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.3  2004/11/10 17:48:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/30 20:58:52  willuhn
 * @C some refactoring
 *
 * Revision 1.1  2004/06/15 21:11:30  willuhn
 * @N added LoggerOutputStream
 *
 **********************************************************************/