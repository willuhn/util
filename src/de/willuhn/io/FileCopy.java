/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/io/FileCopy.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/11 10:20:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Zum Dateien kopieren.
 */
public class FileCopy
{

  /**
	 * Kopiert die Quell-Datei zur Ziel-Datei.
	 * Wenn es sich beim Ziel um ein Verzeichnis handelt, wird die Quell-
	 * Datei in dieses Verzeichnis kopiert. Handelt es sich bei dem Ziel
	 * um eine Datei, wird sie gegen die Quelle ersetzt bzw. neu erstellt.
   * @param from Quelle.
   * @param to Ziel.
   * @param force Ohne Warnung ueberschreiben, falls die Zieldatei bereits existiert.
   * @throws IOException Wenn beim Kopieren ein Fehler auftrat
   * @throws FileExistsException wenn die Zieldatei existiert und <code>force</code> false ist.
   */
  public static void copy(File from, File to, boolean force) throws IOException, FileExistsException
	{

		if (from == null || to == null)
			throw new IOException("source or target is null");

		if (!from.canRead())
			throw new IOException("read permission failed in " + from.getAbsolutePath());

		if (to.isDirectory() && !to.exists())
			throw new IOException("target directory " + to.getAbsolutePath() + " does not exist");

		if (to.isFile() && to.exists() && !to.canWrite())
			throw new IOException("write permission failed in " + to.getAbsolutePath());
		
		if (to.isFile() && to.exists() && !force)
			throw new FileExistsException("file " + to.getAbsolutePath() + " allready exists");

		if (to.isDirectory())
			to = new File(to,from.getName());

		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		try {
			srcChannel = new FileInputStream(from).getChannel();
			dstChannel = new FileOutputStream(to).getChannel();
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		}
		finally
		{
			try {
			  if (srcChannel != null)
  				srcChannel.close();
			}	catch (IOException e) { /* useless */ }
			try {
			  if (dstChannel != null)
  				dstChannel.close();
			}	catch (IOException e) { /* useless */ }
		}
	}
	
	/**
   * Wird geworfen, wenn eine Datei ueberschrieben werden soll, die bereits existiert.
   */
  public static class FileExistsException extends Exception
	{
		/**
		 * Erzeugt eine neue Exception dieses Typs mit der genannten Meldung.
     * @param message anzuzeigende Meldung.
     */
    public FileExistsException(String message)
		{
			super(message);
		}
	}
}


/**********************************************************************
 * $Log: FileCopy.java,v $
 * Revision 1.2  2010/03/11 10:20:22  willuhn
 * @B NPE
 *
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 * Revision 1.1  2004/01/04 18:45:45  willuhn
 * @N FileCopy
 *
 **********************************************************************/