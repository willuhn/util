/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/I18N.java,v $
 * $Revision: 1.12 $
 * $Date: 2008/05/19 22:25:55 $
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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import de.willuhn.logging.*;

/**
 * Diese Klasse behandelt die Internationalisierung.
 * Sie uebersetzt nicht nur alle Strings sondern speichert auch alle
 * nicht uebersetzbaren Strings waehrend der aktuellen Sitzung und
 * speichert diese beim Beenden der Anwendung im Temp-Verzeichnis ab.
 * @author willuhn
 */
public class I18N
{

  private ResourceBundle bundle;
  private Properties fallbackBundle;
  private Properties unresolved = new Properties();

	private Locale locale;

  private final static String DEFAULTPATH = "lang/messages";

	/**
	 * ct.
   * Verwendet das Default-Locale und "lang/messages" als Resource-Path.
	 */
	public I18N()
	{
		this(null,null,null);
	}

  /**
   * ct.
   * Verwendet das uebergebene Locale und "lang/messages" als Resource-Path.
   * @param l Locale.
   */
  public I18N(Locale l)
  {
    this(null,l,null);
  }
	/**
	 * ct.
   * Verwendet den uebergebenen Resource-Path und das Default-Locale.
	 * @param resourcePath
	 */
	public I18N(String resourcePath)
	{
		this(resourcePath,null,null);
	}

	/**
   * Initialisiert diese Klasse mit dem angegebenen Locale.
   * @param resourcePath
   * @param l das zu verwendende Locale.
   */
  public I18N(String resourcePath, Locale l)
	{
		this(resourcePath,l,null);
	}

  /**
   * Initialisiert diese Klasse mit dem angegebenen Locale.
   * @param resourcePath
   * @param l das zu verwendende Locale.
   * @param loader der Classloader.
   */
  public I18N(String resourcePath, Locale l, ClassLoader loader)
  {
		if (resourcePath == null)
      resourcePath = DEFAULTPATH;

    if (l == null)
      this.locale = Locale.getDefault();
    else
      this.locale = l;

    Logger.info("loading resource bundle " + resourcePath + " for locale " + (l == null ? "<default>" : l.toString()));

  	if (loader != null)
      bundle = ResourceBundle.getBundle(resourcePath,l,loader);
    else
			bundle = ResourceBundle.getBundle(resourcePath,l);
  }
  
  /**
   * ct.
   * Verwendet den Inputstream zu Lesen der Resourcen.
   * @param is
   */
  public I18N(InputStream is)
  {
    fallbackBundle = new Properties();
    if (is == null)
      Logger.error("no inputstream given for I18N");
    try
    {
      fallbackBundle.load(is);
    }
    catch (IOException e)
    {
      Logger.error("error while reading resource bundle from inputstream",e);
    }
  }

  /**
   * Uebersetzt den angegebenen String und liefert die uebersetzte
   * Version zurueck. Kann der String nicht uebersetzt werden, wird
   * der Original-String zurueckgegeben.
   * @param key zu uebersetzender String.
   * @return uebersetzter String.
   */
  public String tr(String key)
  {
    String translated = null;
    try {
      if (bundle != null)
        translated = bundle.getString(key);
      else if (fallbackBundle != null)
      	translated = fallbackBundle.getProperty(key);
    }
    catch(MissingResourceException e) {
    }

    if (translated != null)
      return translated;
    
    unresolved.put(key,key);
    return key;
  }

  /**
   * Uebersetzt den angegebenen String und liefert die uebersetzte
   * Version zurueck. Kann der String nicht uebersetzt werden, wird
   * der Original-String zurueckgegeben.
   * <br><b>Hinweis:</b>. Die Textmarken fuer die Ersetzungen sind mit <code>{n}</code> zu definieren
   * wobei n von 0 beginnend hochgezaehlt wird und genauso oft vorkommen darf wie das String-Array
   * Elemente besitzt.<br>
   * Bsp: i18n.tr("Das ist eine {0} nuetzliche {1}", new String[] {"besonders","Funktion"});
   * @param key zu uebersetzender String.
   * @param replacements String-Array mit den einzusetzenden Werten.
   * @return uebersetzter String.
   */
  public String tr(String key, String[] replacements)
  {
    return MessageFormat.format(tr(key),(Object[])replacements);
  }
  
  /**
   * Uebersetzt den angegeben String und liefert die uebersetzte Version zurueck.
   * Diese Funktion existiert der Einfachheit halber fuer Strings, welche lediglich
   * ein Replacement besitzen. Die sonst notwendige Erzeugung eines String-Arrays
   * mit nur einem Element entfaellt damit.<br>
   * Bsp: i18n.tr("Das ist eine nuetzliche {0}", "Funktion");
   * @param key zu uebersetzender String.
   * @param replacement String mit dem einzusetzenden Wert.
   * @return uebersetzter String.
   */
  public String tr(String key, String replacement)
  {
    return tr(key,new String[]{replacement});
  }

  /**
   * Schreibt alle bis dato nicht uebersetzbaren Strings in den angegebenen OutputStream.
   * @param os Stream, in den geschrieben werden soll.
   * @throws IOException
   */
  public void storeUntranslated(OutputStream os) throws IOException
  {
		Logger.info("saving unresolved locale strings");
    unresolved.store(os, "unresolved strings for locale " + locale.toString());
  }
}

/*********************************************************************
 * $Log: I18N.java,v $
 * Revision 1.12  2008/05/19 22:25:55  willuhn
 * @B NPE
 *
 * Revision 1.11  2008/05/19 22:19:54  willuhn
 * @R UNDO: Fuehrt dazu, dass nicht mehr erkannt werden kann, ob ein Resource-Bundle ueberhaupt existiert.
 *
 * Revision 1.9  2007/03/26 23:58:19  willuhn
 * @C compiler warnings
 *
 * Revision 1.8  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.7  2004/11/12 16:19:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/05 19:42:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/05 01:50:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/10 20:57:34  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/04/01 22:07:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/03 22:27:33  willuhn
 * @N added Lock
 *
 * Revision 1.1  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.4  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
