/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * LGPLv2
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
		return (T) resolve(target,null);
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

		final String name = target.getSimpleName();
		
		Logger.debug(indent() + "booting service " + name);

		indent++;

		// Instanziieren
    try
    {
      s = (T) target.newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("unable to create instance of " + name,e);
    }

		Logger.debug(indent() + "checking dependencies for " + name);
		Class<Bootable>[] deps = s.depends();
		if (deps != null && deps.length > 0)
		{
      // Alle Abhaengigkeiten booten
      Logger.debug(indent() + "booting dependencies for " + name);

      for (Class<Bootable> dep:deps)
      {
        if (dep.equals(target))
        {
          Logger.info(indent() + name + " cannot have itself as dependency, skipping");
          indent--;
          continue;
        }
        resolve(dep,s);
      }
		}
    else
    {
      Logger.debug(indent() + "no dependencies found for " + name);
    }


		// Abhaengigkeiten sind alle gebootet, jetzt koennen wir uns selbst initialisieren
    try
    {
      Logger.debug(indent() + "init service " + name);

      // Muss vor dem Initialisieren passieren,
      // damit der Service schon bekannt ist, wenn in Init jemand
      // den Service braucht -> wuerde sonst eine Rekursion ausloesen
      this.services.put(s.getClass(),s);
      
      long start = System.currentTimeMillis();
      s.init(this,caller);
      this.order.add(s);
      long used = System.currentTimeMillis() - start;
      Logger.debug("used time to init " + name + ": " + used + " millis");
    }
    catch (SkipServiceException e)
    {
      this.services.remove(s.getClass());
      Logger.warn(indent() + "skipping service " + name + ". message: " + e.getMessage());
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
        service = this.order.pop();
        
        final String name = service.getClass().getSimpleName();
        if (monitor != null)
        {
          monitor.setStatusText("shutting down service " + name);
          monitor.addPercentComplete(1);
        }
        
        Logger.debug("shutting down service " + name);
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
