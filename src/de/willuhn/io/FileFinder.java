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

package de.willuhn.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Hilfsklasse zum (rekursiven) Suchen von Dateien.
 */
public class FileFinder
{

	private File baseDir = null;
	private ArrayList contains = new ArrayList();
	private ArrayList found = new ArrayList();

	/**
	 * ct. 
   * @param baseDir Verzeichnis, ab dem gesucht werden soll.
   */
  public FileFinder(File baseDir)
	{
		this.baseDir = baseDir;
	}
	
	/**
	 * Suchkriterium via OR hinzufuegen.
	 * Die Datei muss den genannten String im Detainamen enthalten.
	 * Wird diese Funktion mehrmals aufgerufen, werden alle
	 * Suchkriterien mit ODER verknuepft.
	 * @param regex Regulaerer Ausdruck.
	 */
	public void matches(String regex)
	{
		if (regex == null || "".equals(regex))
			return;
		contains.add(regex);
	}

  /**
   * Suchkriterium via OR hinzufuegen.
   * Die Datei muss die genannte Dateiendung haben.
   * Ob die Dateiendung hierbei mit fuehrendem Punkt oder ohne angegeben wird, spielt keine Rolle.
   * Wird diese Funktion mehrmals aufgerufen, werden alle
   * Suchkriterien mit ODER verknuepft.
   * @param extension Datei-Endung. zb "jar" oder ".jar".
   */
  public void extension(String extension)
  {
    if (extension == null || "".equals(extension))
      return;
    if (extension.startsWith("."))
    	extension = extension.substring(1);
    contains.add(".*?\\."+extension+"$");
  }

	/**
	 * Sucht im aktuellen Verzeichnis und liefert das Ergebnis zurueck.
   * Hinweis: Die Funktion liefert nur Dateien, keine Verzeichnisse.
   * @return Liste der gefundenen Dateien.
   */
  public File[] find()
	{
		find(this.baseDir,false,false);
		return (File[]) found.toArray(new File[found.size()]);
	}

	/**
	 * Sucht rekursiv ab dem aktuellen Verzeichnis und liefert das Ergebnis zurueck.
   * Hinweis: Die Funktion liefert nur Dateien, keine Verzeichnisse.
	 * @return Liste der gefundenen Dateien.
	 */
  public File[] findRecursive()
	{
		find(this.baseDir,true,false);
		return (File[]) found.toArray(new File[found.size()]);
	}

  /**
   * Sucht im aktuellen Verzeichnis und liefert das Ergebnis zurueck.
   * @return Liste der gefundenen Dateien und Verzeichnisse.
   */
  public File[] findAll()
  {
    find(this.baseDir,false,true);
    return (File[]) found.toArray(new File[found.size()]);
  }

  /**
   * Sucht rekursiv ab dem aktuellen Verzeichnis und liefert das Ergebnis zurueck.
   * @return Liste der gefundenen Dateien und Verzeichnisse.
   */
  public File[] findAllRecursive()
  {
    find(this.baseDir,true, true);
    return (File[]) found.toArray(new File[found.size()]);
  }

  /**
	 * interne Suchfunktion.
   * @param dir Verzeichnis, ab dem gesucht werden soll.
   * @param recursive true, wenn rekursiv gesucht werden soll.
   * @param all true, wenn auch Verzeichnisse gefunden werden sollen.
   */
  private void find(File dir, boolean recursive, final boolean all)
	{
    if (dir == null || !dir.canRead())
      return;

		// Alle Dateien des Verzeichnisses suchen
		File[] files = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				File f = new File(dir,name);
				if (!all && !f.isFile()) return false;
				if (contains.size() == 0)
				{
					// es wurden keine Filter definiert, also matcht alles
					return true;
				}
				String regex = null;
				for (int i=0;i<contains.size();++i)
				{
					regex = (String) contains.get(i);
					if (name.matches(regex))
						return true;
				}
				return false;
			}
		});
    
		if (files != null)
		{
			for (int i=0;i<files.length;++i)
			{
				found.add(files[i]);
			}
		}

		if (!recursive)
		{
			return;
		}

		// So, und jetzt alle Unterverzeichnisse
		final File[] dirs = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				File f = new File(dir,name);
				return (f.isDirectory());
			}
		});

		for (int i=0;i<dirs.length;++i)
		{
			// und jetzt kommt die Rekursion
			find(dirs[i],true,all);
		}

	}
}


/**********************************************************************
 * $Log: FileFinder.java,v $
 * Revision 1.3  2007/04/19 00:01:26  willuhn
 * @B check if dirs are not readable (causes NPE)
 *
 * Revision 1.2  2006/06/30 13:42:46  willuhn
 * @B FileFinder hatte nur Dateien gefunden, aber keine Verzeichnisse
 *
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 * Revision 1.4  2004/06/08 22:26:03  willuhn
 * @C renamed FileFinder#contains() into FileFinder#matches()
 *
 * Revision 1.3  2004/06/03 00:24:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/05 19:13:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/03 19:33:59  willuhn
 * *** empty log message ***
 *
 **********************************************************************/