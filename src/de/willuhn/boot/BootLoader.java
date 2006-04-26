/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
 * $Revision: 1.10 $
 * $Date: 2006/04/26 15:04:13 $
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
import java.util.Stack;

import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Der BootLoader.
 * Über diese Klasse kann ein kaskadierender Boot-Prozess gestartet werden.
 */
public class BootLoader {

	/**
	 * Lookup der initialisierten Services.
	 */
	private HashMap services = new HashMap();
  
  /**
   * Reihenfolge, in der die Services gebootet wurden.
   */
  private Stack order = new Stack();

	private int indent = 0;

	private ProgressMonitor monitor = null;

  /**
   * ct.
   * Erzeugt einen neuen BootLoader. 
   * @param monitor Monitor, ueber den die Dienste ihre Informationen ueber den Boot-Vorgang ausgeben koennen.
   */
  public BootLoader(ProgressMonitor monitor)
	{
		this.monitor = monitor;
	}
  
  /**
   * Liefert den Progress-Monitor.
   * @return der Progress-Monitor.
   */
  public final ProgressMonitor getMonitor()
  {
    return this.monitor;
  }

	/**
   * Liefert den gewuenschten Dienst und bootet das System
   * bei Bedarf bis genau zu diesem.
	 * @param target das gweuenschte (ung ggf zu bootende) Ziel.
	 * Bevor der Loader die Klasse <code>target</code> via <code>init()</code>
	 * initialisiert, wird er alle Abhaengigkeiten aufloesen und zuvor alle
	 * entsprechend <code>depends</code> angegebenen Services starten.
	 * @return der instanziierte Dienst.
	 */
	public final Bootable getBootable(Class target)
	{
		return resolve(target,null);
	}

	/**
   * Loest die Abhnaegigkeiten fuer einen Dienst auf.
	 * @param target der gewuenschte Dienst.
	 * @param caller der Aufrufer. Kann <code>null</code> sein.
	 * @return der instanziierte Dienst.
	 * @throws Exception
	 */
	private final Bootable resolve(Class target,Bootable caller)
	{

		// Target schon gebootet
		if (services.get(target) != null)
		{
			Logger.debug(indent() + "service " + target.getName() + " allready booted, skipping");
			return (Bootable) services.get(target);
		}

		Logger.info(indent() + "booting service " + target.getName());

		indent++;

		// Instanziieren
		Bootable s = null;
    try
    {
      s = (Bootable) target.newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("unable to create instance of " + target.getName(),e);
    }

		Logger.info(indent() + "checking dependencies for " + target.getName());
		Class[] deps = s.depends();
		if (deps != null)
		{
      // Alle Abhaengigkeiten booten
      Logger.debug(indent() + "booting dependencies for " + target.getName());

      for (int j=0;j<deps.length;++j)
      {
        if (deps[j].equals(target))
        {
          Logger.info(indent() + deps[j].getName() + " cannot have itself as dependency, skipping");
          indent--;
          continue;
        }
        resolve(deps[j],s);
      }
		}
    else
    {
      Logger.debug(indent() + "no dependencies found for " + target.getName());
    }


		// Abhaengigkeiten sind alle gebootet, jetzt koennen wir uns selbst initialisieren
    try
    {
      Logger.info(indent() + "init service " + target.getName());
      s.init(this,caller);
      this.services.put(s.getClass(),s);
      this.order.add(s);
    }
    catch (SkipServiceException e)
    {
      Logger.warn(indent() + "skipping service " + s.getClass().getName() + ". message: " + e.getMessage());
    }
    indent--;
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
  protected void finalize() throws Throwable
  {
    try
    {
      shutdown();
    }
    finally
    {
      super.finalize();
    }
  }
  
  /**
   * Faehrt alle Services in genau umgekehrter Reihenfolge wieder herunter, in der sie gebootet wurden.
   */
  public void shutdown()
  {
    try
    {
      Bootable service = null;
      while (!this.order.empty())
      {
        service = (Bootable) this.order.pop();
        Logger.info("shutting down service " + service.getClass().getName());
        service.shutdown();
      }
    }
    finally
    {
      this.order.clear();
      this.services.clear();
    }
  }

}


/**********************************************************************
 * $Log: BootLoader.java,v $
 * Revision 1.10  2006/04/26 15:04:13  web0
 * *** empty log message ***
 *
 * Revision 1.9  2006/04/26 09:37:07  web0
 * @N bootloader redesign
 *
 * Revision 1.8  2005/02/27 15:25:32  web0
 * *** empty log message ***
 *
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