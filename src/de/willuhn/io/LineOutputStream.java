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
package de.willuhn.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Ein OutputStream, der alle Daten, die erhaelt buffert und zeilenweise
 * an <code>writeLine(java.lang.String)</code> uebergibt. Sprich: Will
 * man Daten zeilenweise verarbeiten, darf aber nur einen OutputStream
 * angeben, dann kann man von dieser Klasse ableiten, die Funktion <code>writeLine</code>
 * implementieren und kann bequem zeilenweise lesen.
 * Hinweis: Ist die Zeile laenger als der angegebene Buffer, wird bei Erreichen
 * der Buffer-Groesse auch schon vor dem Zeilenende geschrieben.  
 */
public abstract class LineOutputStream extends OutputStream
{

	private int bufferSize = 1024;

  private char[] buffer;

	private StringBuffer line = new StringBuffer();
	private int bufferCount = 0;

  /**
   * Erzeugt einen LineOutputStream mit 1024 Byte Buffer.
   */
  public LineOutputStream()
  {
    this(1024);
  }

  /**
   * Erzeugt einen LineOutputStream mit der angebenen Buffer-Groesse.
   * @param bufferSize Buffer-Groesse in Bytes.
   */
  public LineOutputStream(int bufferSize)
  {
    super();
    this.bufferSize = bufferSize;
    buffer = new char[bufferSize];
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  public final void write(int b) throws IOException {

		// Wenn ein Linebreak kommt, schreiben wir raus
		if (b == '\n')
		{
			line.append(buffer);
			writeLine(line.toString().replaceAll("\\r|\\n",""));
			line = new StringBuffer();
			bufferCount = 0;
			buffer = new char[bufferSize];
			return;
		}

		// Meistens schreiben wir in den Char-Buffer
  	if (bufferCount < bufferSize)
  	{
  		buffer[bufferCount++] = (char) b;
  		return;
  	}

		// Charbuffer ist voll, wir haengens an die Zeile
		line.append(buffer);
		bufferCount = 0;
		buffer = new char[bufferSize];
  }

  /**
   * Wird aufgerufen, wenn eine Zeile vollstaendig ist und
   * geschrieben werden kann oder aber der Buffer voll ist.
   * Wichtig: In der Zeile ggf. vorhandene Linewraps werden
   * entfernt. Sollen Die Ausgaben also z.Bsp. via System.out
   * geschrieben werden, dann bitte "println()" statt "print()"
   * verwenden, um den entfernten Zeilenumbruch wieder anzufuegen.
   * @param s der zu schreibende String bereinigt um seinen Zeilenumbruch.
   * @throws IOException
   */
  public abstract void writeLine(String s) throws IOException;
}


/**********************************************************************
 * $Log: LineOutputStream.java,v $
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.1  2004/11/10 17:48:49  willuhn
 * *** empty log message ***
 *
 **********************************************************************/