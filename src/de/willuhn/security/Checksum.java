/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/security/Checksum.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/01 17:15:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.willuhn.util.Base64;

/**
 * Hilfsklasse mit statischen Methoden zur Erzeugung von Checksummen.
 */
public class Checksum
{

  private Checksum()
  {
  }

  /**
   * Liefert eine MD5-Checksumme des
   * @param text
   * @return
   */
  public final static String md5(byte[] text) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hashed = md.digest(text);
		return Base64.encode(hashed);
	}
}


/**********************************************************************
 * $Log: Checksum.java,v $
 * Revision 1.1  2005/02/01 17:15:07  willuhn
 * *** empty log message ***
 *
 **********************************************************************/