/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/JarInfo.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/06/10 20:57:34 $
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

	private Manifest manifest = null;

	/**
	 * Attribut des Typs Version.
	 */
	public final static String ATTRIBUTE_VERSION = "Implementation-Version";

  /**
	 * Attribut des Typs Title.
	 */
  public final static String ATTRIBUTE_TITLE   = "Implementation-Title";

  /**
   * Attribut des Typs Build.
   */
  public final static String ATTRIBUTE_BUILD	 = "Implementation-Buildnumber";

  /**
   * ct.
   * @param jar das Jar-File, aus dem die Infos gelesen werden sollen.
   * @throws IOException
   */
  public JarInfo(JarFile jar) throws IOException {
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
	 * Liefert die Versionsnummer des JARs, die im Manifest als Attribut &quot;Implementation-Version&quot; hinterlegt ist.
	 * Wenn der String das Format &lt;Major-Number&gt;.&lt;Minor-Number&gt; hat, wird die Version als Double zurueckgeliefert.
	 * Existiert das Attribut nicht oder kann es nicht geparst werden, wird 1.0 zurueckgeliefert.
	 * @return Version des Plugins.
	 */
	public double getVersion()
	{
		try {
			return Double.parseDouble(getAttribute(ATTRIBUTE_VERSION));
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
 * Revision 1.4  2004/06/10 20:57:34  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/05/25 23:24:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/14 21:56:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/14 21:48:34  willuhn
 * *** empty log message ***
 *
 **********************************************************************/