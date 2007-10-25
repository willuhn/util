/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/ClassFinder.java,v $
 * $Revision: 1.7 $
 * $Date: 2007/10/25 23:13:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import de.willuhn.logging.Logger;

/**
 */
/**
 * Klassen-Sucher.
 * Diese Teil hier kann man mit Klassen fuettern und danach in verschiedener Hinsicht befragen.
 */
public class ClassFinder
{

	private Hashtable cache    = new Hashtable();
	private ArrayList classes  = new ArrayList();
  private ArrayList children = new ArrayList();

	/**
	 * ct.
	 */
	ClassFinder()
	{
	}
  
  /**
   * Fuegt einen Child-Finder hinzu.
   * @param finder
   */
  void addFinder(ClassFinder finder)
  {
    this.children.add(finder);
  }

	/**
	 * Fuegt die Klasse dem Finder hinzu.
	 * @param clazz die Klasse.
	 */
	void addClass(Class clazz)
	{
    if (isImpl(clazz))
      classes.add(clazz);
	}

	/**
	 * Sucht nach ggf vorhandenen Klassen, die das uebergebene Interface implementieren.
	 * Hinweis: Die Funktion liefert generell nur instanziierbare Klassen.
	 * Es werden also weder abstrakte Klassen, noch Interfaces oder RMI-Stubs geliefert.
	 * @param interphase das Interface.
	 * Handelt es sich hierbei nicht um ein Interface sondern eine instanziierbare
	 * nicht abstrakte Klasse, wir diese direkt und ohne Suche wieder zurueckgegeben.
	 * @return die gefundenen Klassen.
	 * @throws ClassNotFoundException wenn der Implementor nichts gefunden hat.
	 */
	public Class[] findImplementors(Class interphase) throws ClassNotFoundException
	{
    // erstmal im Cache checken
    Class[] found = (Class[]) cache.get(interphase);
    if (found != null && found.length > 0)
      return found;

    // Wenn eines eine Implementierung ist, liefern
    // wir sie direkt zurueck
    if (isImpl(interphase))
      return new Class[] {interphase};

    long start = System.currentTimeMillis();

    // So, jetzt geht die Suche los
    // Ggf. muessen wir die Ableitungshierachie hochwandern.
    // Wenn mehrere Klassen das Interface implementieren, sammeln
    // wir diese in einer Ranking-Liste.
    ArrayList ranking = new ArrayList();

    // Wir suchen in den Child-Findern
    for (int i=0;i<children.size();++i)
    {
      ClassFinder child = (ClassFinder) children.get(i);
      try
      {
        found = child.findImplementors(interphase);
        if (found != null && found.length > 0)
          ranking.addAll(Arrays.asList(found));
      }
      catch (ClassNotFoundException e)
      {
        // Wenn die Kinder nichts gefunden haben, machen wir weiter
      }
    }

    // Jetzt suchen wir lokal
    Class test = null;


		// Hier speichern wir alle direkten Treffer um bei der
		// Suche in der Ableitungs-Hierachie keine Duplikate
		// zu finden.
		Hashtable duplicates = new Hashtable();

		// ueber alle Klassen iterieren
		for (int i=0;i<classes.size();++i)
		{
			test = (Class) classes.get(i);

			// hey, die haben wir doch schon.
			if (duplicates.get(test) != null)
				continue;

			// checken, ob die Klasse das Interface irgendwie implementiert
			if (implementor(test,interphase))
			{
				// hier, wir haben einen direkten Implementor.
				// Wenn das der Fall ist, schenken wir uns
				// die Suche in der Ableitungshierachie dieser
				// Klasse (macht ja auch keinen Sinn, weil die
				// Parent-Klassen dann meist abstrakt sind)
				// und springen gleich zur naechsten.
				ranking.add(test);
				duplicates.put(test,test);
				continue;
			}
		}


		// Jetzt checken wir noch das Ranking.
		if (ranking.size() == 0)
		{
			// Mift, ueberhaupt nix gefunden
			Logger.debug("multipleClassLoader.ClassFinder: ...no implementor found for " + interphase.getName());
			throw new ClassNotFoundException("no implementor found for " + interphase.getName());
		}

		// ok, wir haben was. Das tun wir in den Cache als Array von Class-Objekten
		Class[] classes = (Class[]) ranking.toArray(new Class[ranking.size()]);
		cache.put(interphase,classes);
		Logger.debug("multipleClassLoader.ClassFinder:   [used time: " + (System.currentTimeMillis() - start) + " millis]");
		return classes;

	}

  /**
   * Prueft, ob die Klasse eine Implementierung ist.
   * @param clazz zu testende Klasse.
   * @return true, wenn sie akzeptiert wird.
   */
  private boolean isImpl(Class clazz)
  {
    if (clazz.isInterface() || clazz.isPrimitive())
      return false;

    if (Modifier.isAbstract(clazz.getModifiers()))
      return false;

    String s = clazz.getName();
    // Inner Classes ignorieren
    if (s.indexOf("$") != -1 || s.endsWith("_Stub") || s.endsWith("_Skel"))
      return false;
    
    return true;
  }
  
	/**
	 * Checkt, ob die Klasse das Interface implementiert.
	 * @param test Test-Klasse.
	 * @param interphase zu pruefendes Interface.
	 * @return true, wenn sie es <b>direkt</b> implementiert.
	 */
	private boolean directImplementor(Class test, Class interphase)
	{
		// Im ersten Schritt pruefen wir, ob die Klasse das Interface
		// direkt implementiert
		Class[] interfaces = test.getInterfaces();
		for (int j=0;j<interfaces.length;++j)
		{
			if (interfaces[j].equals(interphase))
				return true;

      // Bevor wir die naechste Iteration durchlaufen muessen wir nun
			// aber noch pruefen, ob das aktuell getestete Interface vielleicht
			// vom gewuenschten Interface abgeleitet ist. Also z.Bsp:
			// interface a;
			// interface b extends a;
			// class c implements b;
			if (implementor(interfaces[j],interphase))
				return true;
		}
		return false;
	}

	/**
	 * Checkt rekursiv in der Ableitungshierachie, ob die Klasse das
	 * Interface irgendwie implementiert.
	 * @param test Test-Klasse.
	 * @param interphase zu pruefendes Interface.
	 * @return true, wenn sie es implementiert.
	 */
	private boolean implementor(Class test, Class interphase)
	{
		if (directImplementor(test,interphase))
			return true;

		// jetzt die Iteration
		Class current = test;
		Class parent = null;
		while (true)
		{
			parent = current.getSuperclass();
			if (parent == null)
				return false;

			if (!parent.isInterface() &&
					directImplementor(parent,interphase))
			{
				return true;
			}
			current = parent; // naechste Iteration
		}
	}
}


/**********************************************************************
 * $Log: ClassFinder.java,v $
 * Revision 1.7  2007/10/25 23:13:22  willuhn
 * @N Support fuer kaskadierende Classloader und -finder
 * @C Classfinder ignoriert jetzt Inner-Classes
 *
 * Revision 1.6  2005/02/21 23:38:47  web0
 * undo
 *
 * Revision 1.5  2005/02/21 23:16:43  web0
 * @B equals() is not implemented in Class
 *
 * Revision 1.4  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.3  2004/06/30 20:58:53  willuhn
 * @C some refactoring
 *
 **********************************************************************/