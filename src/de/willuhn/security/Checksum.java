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
package de.willuhn.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.willuhn.util.Base64;

/**
 * Hilfsklasse mit statischen Methoden zur Erzeugung von Checksummen.
 */
public class Checksum
{
  /**
   * Konstante fuer SHA1-Checksumme.
   */
  public final static String SHA1 = "SHA1";

  /**
   * Konstante fuer SHA-256-Checksumme.
   */
  public final static String SHA256 = "SHA-256";

  /**
   * Konstante fuer MD5-Checksumme.
   */
  public final static String MD5 = "MD5";

  private Checksum()
  {
  }

  /**
   * Liefert eine MD5-Checksumme der Daten im Base64-Format.
   * @param text
   * @return die Checksumme.
   * @throws NoSuchAlgorithmException
   */
  public static String md5(byte[] text) throws NoSuchAlgorithmException
	{
    return Base64.encode(checksum(text,Checksum.MD5));
	}
  
  /**
   * Liefert eine Checksumme der Daten mit dem angegebenen Algorithmus.
   * @param text
   * @param alg der Algorithmus.
   * @return die Checksumme.
   * @throws NoSuchAlgorithmException
   */
  public static byte[] checksum(byte[] text, String alg) throws NoSuchAlgorithmException
  {
    MessageDigest md = MessageDigest.getInstance(alg);
    return md.digest(text);
  }
  
  /**
   * Liefert eine Checksumme der Daten.
   * @param data InputStream mit den Daten.
   * Hinweis: Die Funktion kuemmert sich NICHT um das Schliessen des Streams.
   * @param alg Algorithmus.
   * @return die Checksumme.
   * @see Checksum#MD5
   * @see Checksum#SHA1
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  public static byte[] checksum(InputStream data, String alg) throws NoSuchAlgorithmException, IOException
  {
    MessageDigest md = MessageDigest.getInstance(alg);
    byte[] buf = new byte[4096];
    int read = 0;
    while ((read = data.read(buf)) != -1)
      md.update(buf,0,read);

    return md.digest();
  }
}


/**********************************************************************
 * $Log: Checksum.java,v $
 * Revision 1.6  2011/04/27 08:38:29  willuhn
 * @N SHA256 als Konstante hinzugefuegt
 *
 * Revision 1.5  2009/01/17 00:25:39  willuhn
 * @N Programm zum Erstellen/Verifizieren von OpenSSL-kompatiblen Signaturen mit SHA1-Digest
 * @N Java 1.5 compatibility
 *
 * Revision 1.4  2009/01/16 17:08:58  willuhn
 * @C Checksum#checksum fuehrt kein Base64-Encoding durch
 *
 * Revision 1.3  2009/01/16 16:39:56  willuhn
 * @N Funktion zum Erzeugen von SHA1-Checksummen
 * @N Funktion zum Erzeugen von Checksummen aus InputStreams
 *
 * Revision 1.2  2005/03/09 01:06:20  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2005/02/01 17:15:07  willuhn
 * *** empty log message ***
 *
 **********************************************************************/