/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/LoggerOutputStream.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/15 21:11:30 $
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
import java.io.OutputStream;

/**
 * Ein OutputStream, der alle Ausgaben in den Logger schreibt.
 */
public class LoggerOutputStream extends OutputStream {

	private final static int BUF_SIZE = 1024;

	private Logger logger = null;
	private int level;
	private StringBuffer line = new StringBuffer();
	private char[] buffer = new char[BUF_SIZE];
	private int bufferCount = 0;

  /**
   * ct.
   * @param logger der Logger, in den geschrieben werden soll.
   * @param logLevel das Log-Level, mit dem der OutputStream schreiben soll.
   */
  public LoggerOutputStream(Logger logger, int logLevel)
  {
    super();
    this.logger = logger;
    this.level = logLevel;
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException {

		// Wenn ein Linebreak kommt, schreiben wir raus
		if (b == '\n')
		{
			line.append(buffer);
			logger.write(level,line.toString().replaceAll("\\r|\\n",""));
			line = new StringBuffer();
			bufferCount = 0;
			buffer = new char[BUF_SIZE];
			return;
		}

		// Meistens schreiben wir in den Char-Buffer
  	if (bufferCount < BUF_SIZE)
  	{
  		buffer[bufferCount++] = (char) b;
  		return;
  	}

		// Charbuffer ist voll, wir haengens an die Zeile
		line.append(buffer);
		bufferCount = 0;
		buffer = new char[BUF_SIZE];
  }

}


/**********************************************************************
 * $Log: LoggerOutputStream.java,v $
 * Revision 1.1  2004/06/15 21:11:30  willuhn
 * @N added LoggerOutputStream
 *
 **********************************************************************/