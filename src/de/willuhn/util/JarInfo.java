/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/JarInfo.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/14 21:56:39 $
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
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Hilfs-Klasse, um Meta-Informationen aus JARs zu lesen.
 * Dabei werden die INFOs aus dem Manifest verwendet.
 */
public class JarInfo {

	private JarFile jar = null;
	private Manifest manifest = null;

	public final static String ATTRIBUTE_VERSION = "Implementation-Version";
	public final static String ATTRIBUTE_TITLE   = "Implementation-Title";
	public final static String ATTRIBUTE_BUILD	 = "Implementation-Buildnumber";

  /**
   * ct.
   * @param jar das Jar-File, aus dem die Infos gelesen werden sollen.
   * @throws IOException
   */
  public JarInfo(JarFile jar) throws IOException {
  	this.jar = jar;
  	this.manifest = jar.getManifest();
  }

	/**
	 * Liefert den Wert des genannten Attributes.
   * @param name Name des Attributes.
   * @return Wert des Attributes.
   */
  public String getAttribute(String name)
	{
		return manifest.getMainAttributes().getValue(name);
	}

	/**
	 * Liefert die Versionsnummer des JARs, die im Manifest als Attribut ""Implementation-Version" hinterlegt ist.
	 * Wenn der String das Format "V_&lt;Major-Number&gt;_&lt;Minor-Number&gt; hat, wird es funktionieren.
	 * Existiert das Attribut nicht oder kann es nicht geparst werden, wird 1.0 zurueckgeliefert.
	 * @return Version des Plugins.
	 */
	public double getVersion()
	{
		try {
			String version = getAttribute(ATTRIBUTE_VERSION);
			version = version.substring(2).replace('_','.');
			return Double.parseDouble(version);
		}
		catch (Exception e)
		{
			return 1.0;
		}
	}

	/**
	 * Liefert die Build-Nummer des JARs, die im Manifest als Attribut ""Implementation-Buildnumber" hinterlegt ist.
	 * Existiert das Attribut nicht oder kann es nicht geparst werden, wird 1 zurueckgeliefert.
	 * @return Buildnumber des Plugins.
	 */
	public int getBuildnumber()
	{
		try {
			return Integer.parseInt(getAttribute(ATTRIBUTE_BUILD));
		}
		catch (Exception e)
		{
			return 1;
		}
	}

}


/**********************************************************************
 * $Log: JarInfo.java,v $
 * Revision 1.2  2004/04/14 21:56:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/14 21:48:34  willuhn
 * *** empty log message ***
 *
 **********************************************************************/