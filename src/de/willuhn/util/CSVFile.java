/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/CSVFile.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/09/17 14:36:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

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
   * @throws IOException
   */
  public CSVFile(InputStream file) throws IOException
  {
  	this(file,null);
  }

  /**
	 * ct.
   * @param file die CSV-Datei.
   * @param separator Trennzeichen.
   * @throws IOException
   */
  public CSVFile(InputStream file,String separator) throws IOException
	{
		this.reader = new BufferedReader(new InputStreamReader(file));
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

		StringTokenizer t = new StringTokenizer(currentLine,separator);
		String[] cols = new String[t.countTokens()];
		int i=0;
		while (t.hasMoreTokens())
		{
			cols[i++] = t.nextToken();
		}
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
 * Revision 1.1  2004/09/17 14:36:59  willuhn
 * @N CSVFile
 *
 **********************************************************************/