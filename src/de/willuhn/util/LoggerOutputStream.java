/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/LoggerOutputStream.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/11/10 17:48:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

import java.io.IOException;

/**
 * Ein OutputStream, der alle Ausgaben in den Logger schreibt.
 */
public class LoggerOutputStream extends LineOutputStream {

	private int level;

  /**
   * ct.
   * @param logLevel das Log-Level, mit dem der OutputStream schreiben soll.
   */
  public LoggerOutputStream(int logLevel)
  {
    super();
    this.level = logLevel;
  }

  /**
   * @see de.willuhn.util.LineOutputStream#writeLine(java.lang.String)
   */
  public void writeLine(String s) throws IOException
  {
    Logger.write(level,s);
  }

}


/**********************************************************************
 * $Log: LoggerOutputStream.java,v $
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