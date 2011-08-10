/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/TypedProperties.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/08/10 09:43:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.willuhn.logging.Logger;

/**
 * Diese Klasse erweitert Java-Properties um Typsicherheit fuer primitive Typen.
 */
public class TypedProperties extends Properties
{
	/**
	 * Liefert den Wert des genannten Attributs als Boolean.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   * @return true oder false.
   */
  public boolean getBoolean(String name, boolean defaultValue)
	{
		String s = this.getInternal(name,defaultValue ? "true" : "false");
		return "true".equalsIgnoreCase(s);
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
		String s = this.getInternal(name,Integer.toString(defaultValue));
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Logger.error("unable to parse " + name + "=" + s + " as integer",e);
		}
		return defaultValue;
	}

  /**
   * Liefert den Wert des genannten Attributs als long.
   * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * Hinweis: Die Funktion wirft keine NumberFormat-Exception, wenn der
   * Wert nicht in eine Zahl gewandelt werden kann. Stattdessen wird der
   * Default-Wert zurueckgegeben.
   * @param name Name des Attributs.
   * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   * @return der Wert des Attributs.
   */
  public long getLong(String name, long defaultValue)
  {
    String s = this.getInternal(name,Long.toString(defaultValue));
    try
    {
      return Long.parseLong(s);
    }
    catch (NumberFormatException e)
    {
      Logger.error("unable to parse " + name + "=" + s + " as long",e);
    }
    return defaultValue;
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
		String s = this.getInternal(name,Double.toString(defaultValue));
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
      Logger.error("unable to parse " + name + "=" + s + " as double",e);
		}
		return defaultValue;
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
		return this.getInternal(name,defaultValue);
	}

  /**
   * Liefert ein Array von Werten.
   * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * Es koennen maximal 256 Werte gelesen oder gespeichert werden.
   * @param name Name des Attributs.
   * @param defaultValues DefaultWert, wenn das Attribut nicht existiert.
   * @return Werte des Attributs in Form eines String-Arrays.
   */
  public String[] getList(String name, String[] defaultValues)
  {
    List<String> l = new ArrayList<String>();
    String s = null;
    for (int i=0;i<255;++i)
    {
      s = this.getInternal(name + "." + i,null);
      if (s == null) continue;
      l.add(s);
    }
    
    return l.size() > 0 ? l.toArray(new String[l.size()]) : defaultValues;
  }


	/**
	 * Speichert einen boolschen Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setBoolean(String name, boolean value)
	{
		this.setInternal(name, value ? "true" : "false");
	}
	
	/**
	 * Speichert einen Integer-Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setInt(String name, int value)
	{
		this.setInternal(name,Integer.toString(value));
	}

	/**
	 * Speichert einen Double-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setDouble(String name, double value)
	{
		this.setInternal(name,Double.toString(value));
	}

  /**
   * Speichert einen Long-Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setLong(String name, long value)
  {
    this.setInternal(name,Long.toString(value));
  }

  /**
   * Speichert das Attribut mit der zugehoerigen Liste von Werten.
   * Wenn ein gleichnamiges Attribut bereits existiert, werden dessen Werte ueberschrieben.
   * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
   * Von dem Array werden die ersten maximal 256 Elemente gespeichert.
   * Alle darueber hinausgehenden Werte, werden ignoriert.
   * @param name Name des Attributs.
   * @param values Werte des Attributs.
   */
  public void setList(String name, String[] values)
  {
    // Wir entfernen immer erst alle Werte. Denn wenn vorher
    // ein laengeres Array drin steht, als wir jetzt reinschreiben,
    // wuerden die alten Werte am Ende des grossen Arrays nicht mehr
    // entfernt.
    for (int i=0;i<255;++i)
    {
      super.remove(name + "." + i);
    }

    // Keine neuen zu speichern
    if (values == null || values.length == 0)
      return;
      
    for (int i=0;i<values.length;++i)
    {
      if (i >= 255)
        break; // Schluss jetzt. Das waren genug Werte ;)
      if (values[i] == null)
        continue; // NULL-Werte ueberspringen
      super.setProperty(name + "." + i,values[i]);
    }
  }

  /**
   * Liefert den Wert des Attributes.
   * @param name
   * @param defaultValue
   * @return der Wert des Attributes.
   */
  private String getInternal(String name, String defaultValue)
  {
    String s = super.getProperty(name, defaultValue);
    if (s != null) s = s.trim();
    return s;
  }

  /**
   * Speichert das Attribut "name" mit dem zugehoerigen Wert "value".
   * Wenn ein gleichnamiges Attribut bereits existiert, wird es ueberschrieben.
   * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  private void setInternal(String name, String value)
  {
    // Wir speichern nur, wenn etwas geaendert wurde
    if (value == null)
      super.remove(name);
    else
      super.setProperty(name,value);
  }
  
}

/*********************************************************************
 * $Log: TypedProperties.java,v $
 * Revision 1.1  2011/08/10 09:43:40  willuhn
 * @N TypedProperties
 *
 **********************************************************************/