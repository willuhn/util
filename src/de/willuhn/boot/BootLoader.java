/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/03 22:11:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.boot;

import java.util.HashMap;

import de.willuhn.util.Logger;

/**
 * Der BootLoader.
 * Über diese Klasse kann ein kaskadierender Boot-Prozess gestartet werden.
 */
public class BootLoader {

	/**
	 * Der Logger.
	 */
	private Logger logger = new Logger("BootLoader");

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
		logger.setLevel(Logger.LEVEL_DEBUG);
		logger.addTarget(System.out);
	}

  /**
	 * Startet den Boot-Prozess.
	 * @param target das zu bootende Ziel.
	 * Bevor der Loader die Klasse <code>target</code> via <code>init()</code>
	 * initialisiert, wird er alle Abhaengigkeiten aufloesen und zuvor alle
	 * entsprechend <code>depends</code> angegebenen Services starten.
   * @param caller Der Aufrufer. Kann null sein. Es ist jedoch sinnvoll,
   * diesen mit anzugeben, wenn man via <code>failedDependency(Service dependency,SkipServiceException)</code>
   * ueber das Fehlschlagen einer Abhaengigkeit informiert werden moechte.
	 * @throws Exception
	 */
	public final Service boot(Class target,Service caller) throws Exception
	{

		// Kein Target definiert
		if (target == null)
		{
			logger.warn("not service given");
			return null;
		}
		
		// Target schon gebootet
		if (services.get(target) != null)
		{
			logger.info(indent() + "service " + target.getName() + " allready booted, skipping");
			return (Service) services.get(target);
		}

		logger.info(indent() + "booting service " + target.getName());

		indent++;

		// Instanziieren
		Service s = (Service) target.newInstance();

		logger.info(indent() + "checking dependencies for " + target.getName());
		Class[] deps = s.depends();
		if (deps == null || deps.length == 0)
		{
			logger.info(indent() + "no dependencies found for " + target.getName());
			init(caller,s);
			return s;
		}

		// Alle Abhaengigkeiten booten
		for (int j=0;j<deps.length;++j)
		{
			if (deps[j].equals(target))
			{
				logger.info(indent() + deps[j].getName() + " cannot have itself as dependency, skipping");
				indent--;
				continue;
			}
			boot(deps[j],s);
		}

		// Abhaengigkeiten sind alle gebootet, jetzt koennen wir uns selbst initialisieren
		return init(caller,s);
	}

	private Service init(Service caller,Service s) throws Exception
	{
		Class target = s.getClass();

		Class[] childs;
		try {
			childs = s.init(s);
		}
		catch (SkipServiceException e)
		{
			indent--;
			logger.info(indent() + "init of service " + target.getName() + " failed [message: " + e.getMessage() + "], skipping");
			if (caller != null)
				caller.failedDependency(s,e);
			return s;
		}

		if (childs != null && childs.length > 0)
		{
			// huh, der Service will noch mehr Zeug starten
			for (int k=0;k<childs.length;++k)
			{
				Service child = null;
				boot(childs[k],s);
			}
		}

		indent--;

		logger.info(indent() + "service " + target.getName() + " booted successfully");
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
	 * Definiert einen anderen Logger als den standardmaessig verwendeten.
   * @param l der zu verwendende Logger.
   */
  public final void setLogger(Logger l)
	{
		if (l == null)
			return;
		this.logger = l;
	}

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable {
  	if (logger != null)
  		logger.close();
    super.finalize();
  }

}


/**********************************************************************
 * $Log: BootLoader.java,v $
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