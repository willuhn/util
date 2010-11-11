/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
 * $Revision: 1.16 $
 * $Date: 2010/11/11 16:24:08 $
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
import java.util.Map;
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
	private Map<Class,Bootable> services = new HashMap<Class,Bootable>();
  
  /**
   * Reihenfolge, in der die Services gebootet wurden.
   */
  private Stack<Bootable> order = new Stack<Bootable>();

	private int indent = 0;

  private ProgressMonitor dummy   = new DummyMonitor();
	private ProgressMonitor monitor = null;

  /**
   * Liefert den Progress-Monitor.
   * @return der Progress-Monitor.
   */
  public final ProgressMonitor getMonitor()
  {
    return this.monitor == null ? this.dummy : this.monitor;
  }

  /**
   * Speichert den Progress-Monitor.
   * @param monitor Monitor, ueber den die Dienste ihre Informationen ueber den Boot-Vorgang ausgeben koennen.
   */
  public final void setMonitor(ProgressMonitor monitor)
  {
    this.monitor = monitor;
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
	public final <T extends Bootable> T getBootable(Class<? extends Bootable> target)
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
	private final <T extends Bootable> T resolve(Class<? extends Bootable> target,Bootable caller)
	{

		// Target schon gebootet
    T s = (T) services.get(target);
		if (s != null)
			return s;

		Logger.debug(indent() + "booting service " + target.getName());

		indent++;

		// Instanziieren
    try
    {
      s = (T) target.newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("unable to create instance of " + target.getName(),e);
    }

		Logger.debug(indent() + "checking dependencies for " + target.getName());
		Class<Bootable>[] deps = s.depends();
		if (deps != null && deps.length > 0)
		{
      // Alle Abhaengigkeiten booten
      Logger.debug(indent() + "booting dependencies for " + target.getName());

      for (Class<Bootable> dep:deps)
      {
        if (dep.equals(target))
        {
          Logger.info(indent() + dep.getName() + " cannot have itself as dependency, skipping");
          indent--;
          continue;
        }
        resolve(dep,s);
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

      // Muss vor dem Initialisieren passieren,
      // damit der Service schon bekannt ist, wenn in Init jemand
      // den Service braucht -> wuerde sonst eine Rekursion ausloesen
      this.services.put(s.getClass(),s);
      
      long start = System.currentTimeMillis();
      s.init(this,caller);
      this.order.add(s);
      long used = System.currentTimeMillis() - start;
      Logger.info("used time to init " + target.getName() + ": " + used + " millis");
    }
    catch (SkipServiceException e)
    {
      this.services.remove(s.getClass());
      Logger.warn(indent() + "skipping service " + target.getName() + ". message: " + e.getMessage());
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
        if (monitor != null)
        {
          monitor.setStatusText("shutting down service " + service.getClass().getName());
          monitor.addPercentComplete(1);
        }
        
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

  /**
   * Dummy-Implementierung.
   */
  private class DummyMonitor implements ProgressMonitor
  {
    /**
     * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
     */
    public void setPercentComplete(int percent) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
     */
    public void addPercentComplete(int percent) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
     */
    public int getPercentComplete() {return 0;}
    /**
     * @see de.willuhn.util.ProgressMonitor#setStatus(int)
     */
    public void setStatus(int status) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
     */
    public void setStatusText(String text) {}
    /**
     * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
     */
    public void log(String msg) {}
  }
}


/**********************************************************************
 * $Log: BootLoader.java,v $
 * Revision 1.16  2010/11/11 16:24:08  willuhn
 * @N Bootloader ist jetzt getypt
 *
 * Revision 1.15  2008/03/07 16:29:16  willuhn
 * @N ProgressMonitor auch beim Shutdown verwenden
 *
 * Revision 1.14  2008/03/07 11:32:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2008/02/13 13:33:46  willuhn
 * @N timing information
 *
 * Revision 1.12  2008/02/13 00:29:18  willuhn
 * @C Log-Ausgaben
 *
 * Revision 1.11  2008/02/13 00:27:17  willuhn
 * @N Service bereits nach Erstellung verfuegbar machen
 **********************************************************************/