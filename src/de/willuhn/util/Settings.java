/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Settings.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/13 22:58:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import de.willuhn.logging.Logger;

/**
 * Diese Klasse erweitert Java-Properties um Typsicherheit fuer primitive
 * Typen, Support zum Laden und Speichern von String-Arrays, automatisches
 * Abspeichern beim Aufruf einer Set-Methode und sogar Speichern schon
 * beim Lesen. Das ist nuetzlich, wenn man eine Software ohne properties-Dateien
 * ausliefern will aber dennoch nach dem ersten Start beim Benutzer die
 * Config-Dateien mit den Default-Werten angelegt werden damit dieser
 * nicht in der Dokumentation nach den Schluesselnamen suchen muss
 * sondern sie bereits mit Default-Werten in den Dateien vorfindet.
 * @author willuhn
 */
public class Settings
{

	private String path;
  private String className;
  private Properties properties;

	private boolean storeWhenRead = true;

  private Settings()
  {
    // disabled
  }

  /**
   * Erzeugt eine neue Instanz der Settings, die exclusiv
   * nur fuer diese Klasse gelten. Existieren bereits Settings
   * fuer die Klasse, werden sie gleich geladen.
   * Hierbei wird eine Properties-Datei
   * [classname].properties im angegebenen Verzeichnis angelegt.
   * @param path
   * @param clazz Klasse, fuer die diese Settings gelten.
   */
  public Settings(String path, Class clazz)
  {
    this.className = clazz.getName();
    this.path = path;
    properties = new Properties();
    
    // Filenamen ermitteln
    try {
      // wir testen mal, ob wir die Datei lesen koennen.
      FileInputStream fis = new FileInputStream(getFile());
      properties.load(fis);
    }
    catch (FileNotFoundException e)
    {
      // ne, koemmer nicht, also erstellen wir ein neues.
      store();
    }
    catch (IOException ioe)
    {
    	Logger.error("error while loading " + getFile().getAbsolutePath(),ioe);
    }
  }

	/**
	 * Legt fest, ob die Einstellungen schon beim Lesen gespeichert werden sollen.
	 * Hintergrund: Jede Get-Funktion (getString(), getBoolean(),..) besitzt einen
	 * Parameter mit dem Default-Wert falls der Parameter noch nicht existiert.
	 * Ist dies der Fall und die zugehoerige Set-Methode wird nie aufgerufen,
	 * dann erscheint der Parameter nie physisch in der properties-Datei.
	 * Diese muesste dann manuell mit den Parametern befuellt werden, um
	 * sie aendern zu koennen. Da die Parameter-Namen aber nur in der
	 * Java-Klasse bekannt sind, wird es einem Fremden schwer fallen, die
	 * Namen der Parameter zu ermitteln. Fuer genau diesen Fall kann der
	 * Parameter auf true gesetzt werden. Alle abgefragten Parameter, werden
	 * dann nach der Abfrage mit dem aktuellen Wert (ggf. dem Default-Wert)
	 * sofort gespeichert.
   * @param b true, wenn sofort geschrieben werden soll.
   */
  public void setStoreWhenRead(boolean b)
	{
		this.storeWhenRead = b;
	}

  /**
   * Liefert das File, in dem die Settings gespeichert werden.
   * @return File in dem die Settings der Klasse gespeichert werden.
   */
  private File getFile()
  {
    return new File(this.path+"/"+className+ ".properties");
  }
  
	/**
	 * Liefert eine Liste aller Attribut-Namen, die in dieser Settings-Instanz gespeichert wurden.
   * @return Liste der Attribut-Namen.
   */
  public String[] getAttributes()
	{
		synchronized (properties)
		{
			Iterator it = properties.keySet().iterator();
			String[] attributes = new String[properties.size()];
			int i = 0;
			while (it.hasNext())
			{
				attributes[i++] = (String) it.next();
			}
			return attributes;
		}
	}

	/**
	 * Liefert den Wert des genannten Attributs als Boolean.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   * @return true oder false.
   */
  public boolean getBoolean(String name, boolean defaultValue)
	{
		String s = getProperty(name,defaultValue ? "true" : "false");
		boolean b = "true".equalsIgnoreCase(s);
		if (storeWhenRead)
			setAttribute(name,b);
		return b;
	}

	/**
	 * Liefert den Wert des genannten Attributs als int.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * Hinweis: Die Funktion wirft keine NumberFormat-Exception, wenn der
	 * Wert nicht in eine Zahl gewandelt werden kann. Stattdessen wird der
	 * Default-Wert zurueckgegeben.
	 * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public int getInt(String name, int defaultValue)
	{
		String s = getProperty(name,""+defaultValue);
		int i = defaultValue;
		try {
			i = Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
		}
		if (storeWhenRead)
			setAttribute(name,i);
		return i;
	}

	/**
	 * Liefert den Wert des genannten Attributs als double.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * Hinweis: Die Funktion wirft keine NumberFormat-Exception, wenn der
	 * Wert nicht in eine Zahl gewandelt werden kann. Stattdessen wird der
	 * Default-Wert zurueckgegeben.
	 * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public double getDouble(String name, double defaultValue)
	{
		String s = getProperty(name,""+defaultValue);
		double d = defaultValue;
		try {
			d = Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
		}
		if (storeWhenRead)
			setAttribute(name,d);
		return d;
	}

  private String getProperty(String name, String defaultValue)
	{
		return properties.getProperty(name,defaultValue);
	}

	/**
	 * Liefert den Wert des Attribute.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public String getString(String name, String defaultValue)
	{
		String s = getProperty(name,defaultValue);
		if (storeWhenRead)
			setAttribute(name,s);
		return s;
	}

  /**
   * Liefert ein Array von Werten.
   * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * Es koennen maximal 256 Werte gelesen oder gespeichert werden.
   * @param name Name des Attributs.
   * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   * @return Werte des Attributs in Form eines String-Arrays.
   */
  public String[] getList(String name, String[] defaultValues)
  {
    ArrayList l = new ArrayList();
    String s = null;
    for (int i=0;i<255;++i)
    {
      s = getProperty(name + "." + i,null);
      if (s == null) continue;
      l.add(s);
    }
    if (l.size() == 0)
    {
      if (storeWhenRead)
        setAttribute(name,defaultValues);
      return defaultValues;
    }
    String[] result = (String[]) l.toArray(new String[l.size()]);
    if (storeWhenRead)
      setAttribute(name,result);
    return result;
  }

	/**
	 * Speichert einen boolschen Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setAttribute(String name, boolean value)
	{
		setAttribute(name, value ? "true" : "false");
	}
	
	/**
	 * Speichert einen Integer-Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setAttribute(String name, int value)
	{
		setAttribute(name,""+value);
	}

	/**
	 * Speichert einen Double-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, double value)
	{
		setAttribute(name,""+value);
	}

  /**
   * Speichert das Attribut <name> mit dem zugehoerigen Wert <value>.
   * Wenn ein gleichnamiges Attribut bereits existiert, wird es ueberschrieben.
   * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setAttribute(String name, String value)
  {
  	if (value == null)
  		properties.remove(name);
  	else
	    properties.setProperty(name,value);
    store();
  }

  /**
   * Speichert das Attribut <name> mit der zugehoerigen Liste von Werten <value>.
   * Wenn ein gleichnamiges Attribut bereits existiert, werden dessen Werte ueberschrieben.
   * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
   * Von dem Array werden die ersten maximal 256 Elemente gespeichert.
   * Alle darueber hinausgehenden Werte, werden ignoriert.
   * @param name Name des Attributs.
   * @param values Werte des Attributs.
   */
  public void setAttribute(String name, String[] values)
  {
    // Wir entfernen immer erst alle Werte. Denn wenn vorher
    // ein laengeres Array drin steht, als wir jetzt reinschreiben,
    // wuerden die alten Werte am Ende des grossen Arrays nicht mehr
    // entfernt.
    for (int i=0;i<255;++i)
    {
      properties.remove(name + "." + i);
    }
    
    if (values == null || values.length == 0)
    {
      store();
      return;
    }
      
    for (int i=0;i<values.length;++i)
    {
      if (i >= 255)
        break; // Schluss jetzt. Das waren genug Werte ;)
      properties.setProperty(name + "." + i,values[i]);
    }
    store();
  }

  /**
   * Schreibt die Properties in die Datei.
   * Hinweis: Die Funktion wirft keine IOException, wenn die Datei nicht
   * gespeichert werden kann. Stattdessen wird der Fehler lediglich geloggt.
   */
  private void store()
  {
    try
    {
      properties.store(new FileOutputStream(getFile()),"Settings for class " + className);
    }
    catch (Exception e1)
    {
      Logger.error("unable to create settings. Do you " +
        "have write permissions in " + getFile().getAbsolutePath() + " ?",e1);
    }

  }
}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.1  2005/01/13 22:58:05  willuhn
 * @N Settings nach willuhn.util verschoben
 *
 **********************************************************************/