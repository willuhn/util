/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/02/27 15:11:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.boot;

import java.util.HashMap;

import de.willuhn.logging.Logger;

/**
 * Der BootLoader.
 * Über diese Klasse kann ein kaskadierender Boot-Prozess gestartet werden.
 */
public class BootLoader {

	/**
	 * Liste der initialisierten Services.
	 */
	private HashMap services = new HashMap();

	private int indent = 0;

	/**
   * ct.
   * Erzeugt einen neuen BootLoader. 
   */
  public BootLoader()
	{
	}

	/**
	 * Startet den Boot-Prozess.
	 * @param target das zu bootende Ziel.
	 * Bevor der Loader die Klasse <code>target</code> via <code>init()</code>
	 * initialisiert, wird er alle Abhaengigkeiten aufloesen und zuvor alle
	 * entsprechend <code>depends</code> angegebenen Services starten.
	 * @throws Exception
	 * @return der instanziierte Dienst.
	 */
	public final Bootable boot(Class target) throws Exception
	{
		return boot(target,null);
	}

	private final Bootable boot(Class target,Bootable caller) throws Exception
	{

		// Kein Target definiert
		if (target == null)
		{
			Logger.warn("not service given");
			return null;
		}
		
		// Target schon gebootet
		if (services.get(target) != null)
		{
			Logger.info(indent() + "service " + target.getName() + " allready booted, skipping");
			return (Bootable) services.get(target);
		}

		Logger.info(indent() + "booting service " + target.getName());

		indent++;

		// Instanziieren
		Bootable s = (Bootable) target.newInstance();

		Logger.info(indent() + "checking dependencies for " + target.getName());
		Class[] deps = s.depends();
		if (deps == null || deps.length == 0)
		{
			Logger.info(indent() + "no dependencies found for " + target.getName());
			init(caller,s);
			return s;
		}

		// Alle Abhaengigkeiten booten
		for (int j=0;j<deps.length;++j)
		{
			if (deps[j].equals(target))
			{
				Logger.info(indent() + deps[j].getName() + " cannot have itself as dependency, skipping");
				indent--;
				continue;
			}
			boot(deps[j],s);
		}

		// Abhaengigkeiten sind alle gebootet, jetzt koennen wir uns selbst initialisieren
		return init(caller,s);
	}

	private Bootable init(Bootable caller,Bootable s) throws Exception
	{
		Class target = s.getClass();

		Class[] childs;
		try {
			childs = s.init(s);
		}
		catch (SkipServiceException e)
		{
			indent--;
			Logger.info(indent() + "init of service " + target.getName() + " failed [message: " + e.getMessage() + "], skipping");
			if (caller != null)
				caller.failedDependency(e);
			return s;
		}

		if (childs != null && childs.length > 0)
		{
			// huh, der Service will noch mehr Zeug starten
			for (int k=0;k<childs.length;++k)
			{
				boot(childs[k],s);
			}
		}

		indent--;

		Logger.info(indent() + "service " + target.getName() + " booted successfully");
		services.put(target,s);
		return s;
	}

	/**
	 * Liefert abhaengig von der Iterationstiefe eine definierte Anzahl von Leerzeichen.
   * @return Leerzeichen.
   */
  private String indent()
	{
		String s = "";
		for (int i=0;i<indent;++i)
		{
			s += "  ";
		}
		return s;
	}

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable {
 		Logger.close();
    super.finalize();
  }

}


/**********************************************************************
 * $Log: BootLoader.java,v $
 * Revision 1.7  2005/02/27 15:11:42  web0
 * @C some renaming
 *
 * Revision 1.6  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.5  2004/06/30 20:58:52  willuhn
 * @C some refactoring
 *
 * Revision 1.4  2004/06/10 20:57:34  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/06/03 22:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/03 00:45:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/