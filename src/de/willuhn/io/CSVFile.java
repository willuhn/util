/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/CSVFile.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/12 11:28:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Parser fuer CSV-Dateien.
 */
public class CSVFile
{

	private String separator 		= ";";

	private BufferedReader reader 	= null;
	private String currentLine 			= null;
	private boolean recall					= false;
	private boolean prev						= false;

  /**
   * ct.
   * @param file die CSV-Datei.
   */
  public CSVFile(InputStream file)
  {
  	this(file,null);
  }

  /**
	 * ct.
   * @param file die CSV-Datei.
   * @param separator Trennzeichen.
   */
  public CSVFile(InputStream file,String separator)
	{
		this.reader = new BufferedReader(new InputStreamReader(file));
		if (separator != null)
			this.separator = separator;
	}

  /**
   * ct.
   * @param file die CSV-Datei.
   * @param separator Trennzeichen.
   * @param encoding das Encoding.
   * @throws UnsupportedEncodingException
   */
  public CSVFile(InputStream file, String separator, String encoding) throws UnsupportedEncodingException
  {
    this.reader = new BufferedReader(new InputStreamReader(file,encoding));
    if (separator != null)
      this.separator = separator;
  }

  /**
	 * Prueft, ob weitere Zeilen vorhanden sind.
	 * Der interne Pointer rueckt durch Aufruf dieser Funktion nicht
	 * weiter. Die Funktion kann also mehrmals hintereinander aufgerufen
	 * werden. Der Reader rueckt erst durch Aufruf der Funktion <code>next()</code>
	 * weiter.
   * @return true, wenn noch Zeilen vorhanden sind, sonst false.
   * @throws IOException
   */
  public synchronized boolean hasNext() throws IOException
	{
		// Wenn die Funktion mehrmals aufgerufen wird, ohne dass
		// zwischendurch mal ein next() gemacht wurde, wollen wir
		// nicht weitersteppen
		if (recall)
			return prev;

		prev = ((currentLine = reader.readLine()) != null);
		recall = true;
		return prev;
	}

	/**
	 * Liefert die naechste Zeile der Datei.
   * @return naechste Zeile.
   * @throws IOException
   */
  public synchronized String[] next() throws IOException
	{
		if (!hasNext())
			throw new IOException("no more lines");

    String[] cols = currentLine.split(separator+"(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
		recall = false; // recall flag zuruecksetzen
		return cols;
	}

	/**
	 * Schliesst die CSV-Datei.
   * @throws IOException
   */
  public void close() throws IOException
	{
		reader.close();
	}
}


/**********************************************************************
 * $Log: CSVFile.java,v $
 * Revision 1.4  2011/04/12 11:28:19  willuhn
 * @N Konstruktor zur expliziten Angabe des Encodings
 *
 * Revision 1.3  2005/10/25 22:34:39  web0
 * @C CSV-Reader auf Regex umgestellt
 *
 * Revision 1.2  2005/03/09 01:06:20  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 * Revision 1.1  2004/09/17 14:36:59  willuhn
 * @N CSVFile
 *
 **********************************************************************/