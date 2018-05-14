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
package de.willuhn.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;

import de.willuhn.logging.Logger;

/**
 * Implementierung eines Session-Containers.
 * @author willuhn
 */
public class Session extends Observable
{

  private static Worker worker = null;

  
  /**
   * Liefert den Worker-Thread und erstellt ggf einen neuen.
   * @return der Worker-Thread.
   */
  private final static synchronized Worker getWorker()
  {
    if (worker != null)
      return worker;
    worker = new Worker();
    worker.start();
    return worker;
  }


  private long timeout;
  private Hashtable data = new Hashtable();
  
  /**
   * Erzeugt eine Session mit dem Default-Timeout von 30 Minuten.
   */
  public Session()
  {
    this(1000l * 60 * 30);
  }

  /**
   * ct.
   * @param timeout Anzahl der Millisekunden, nach deren Ablauf ein Element wieder entfernt werden soll.
   */
  public Session(long timeout)
  {
    Logger.debug("creating new session. default timeout: " + timeout + " millis");
    this.timeout = timeout;
    getWorker().register(this);
  }

  /**
   * Liefert eine Liste aller in der Session vorhandenen Schluessel.
   * @return Liste der Schluessel.
   */
  public Enumeration keys()
  {
    return data.keys();
  }


  /**
   * Speichert einen Wert unter dem angegebenen Schluessel in der Session mit dem Default-Timeout.
   * Das Objekt wird nur dann nach Ablauf des Timeouts entfernt, wenn es innerhalb dieses
   * Zeitraumes nicht benutzt wurde.
   * @param key Name des Schluessels.
   * @param value Wert des Schluessels.
   */
  public void put(Object key, Object value)
  {
    put(key,value,this.timeout);
  }

  /**
   * Speichert einen Wert unter dem angegebenen Schluessel in der Session mit einem Timeoout.
   * Das Objekt wird nur dann nach Ablauf des Timeouts entfernt, wenn es innerhalb dieses
   * Zeitraumes nicht benutzt wurde.
   * @param key Name des Schluessels.
   * @param value Wert des Schluessels.
   * @param t Timeout in Millisekunden.
   */
  public void put(Object key, Object value, long t)
  {
    data.put(key,new SessionObject(value,t,false));
  }

  /**
   * Speichert einen Wert unter dem angegebenen Schluessel in der Session mit einem
   * konkreten Ziel-Datum fuer das Timeout.
   * Unabhaengig davon, ob das Objekt benutzt wird oder nicht, wird es zum angegebenen
   * Timeout entfernt.
   * @param key Name des Schluessels.
   * @param value Wert des Schluessels.
   * @param t Timeout als Datum.
   */
  public void put(Object key, Object value, Date t)
  {
    data.put(key,new SessionObject(value, t.getTime(),true));
  }

  
  /**
   * Liefert Wert aus der Session, der unter dem angegebenen Namen gespeichert ist.
   * @param key Name des Schluessels in der Session.
   * @return Wert des Schluessels.
   */
  public Object get(Object key)
  {
    synchronized(data)
    {
      SessionObject o = (SessionObject) data.get(key);
      return o == null ? null : o.getValue();
    }
  }

  /**
   * Liefert Wert aus der Session, der unter dem angegebenen Namen gespeichert ist
   * und entfernt den Wert gleichzeitig.
   * @param key Name des Schluessels in der Session.
   * @return Wert des Schluessels.
   */
  public Object remove(Object key)
  {
    synchronized(data)
    {
      SessionObject o = (SessionObject) data.remove(key);
      return o == null ? null : o.value;
    }
  }
  
  /**
   * Leert die Session.
   */
  public void clear()
  {
    synchronized (data)
    {
      data.clear();
    }
  }

  /**
   * Liefert die Anzahl der Elemente in der Session.
   * @return Anzahl der Elemente.
   */
  public int size()
  {
    synchronized(data)
    {
      return data.size();
    }
  }

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable
  {
    getWorker().unregister(this);
    super.finalize();
  }

  private class SessionObject
  {
    private Object value;

    private long timestamp      = System.currentTimeMillis();
    private long myTimeout      = timeout;
    private boolean hardTimeout = false;

    private SessionObject(Object value, long t, boolean hardTimeout)
    {
      this.value = value;
      this.hardTimeout = hardTimeout;
      if (this.hardTimeout)
      {
        Logger.trace("added object \"" + value + "\" to session. hard timeout: " + new Date(t).toString());
        this.timestamp = t;
        this.myTimeout = 0;
      }
      else
      {
        this.myTimeout = t;
        Logger.trace("added object \"" + value + "\" to session. timeout: " + t + " millis");
      }
    }
    
    /**
     * Liefert den Wert des Keys.
     * @return der Wert.
     */
    private Object getValue()
    {
      if (!hardTimeout)
        this.timestamp = System.currentTimeMillis();
      return this.value;
    }
  }

  
  /**
   * Der Worker-Thread.
   * @author willuhn
   */
  private final static class Worker extends Thread
  {
    private ArrayList sessions = new ArrayList();
    
    /**
     * ct.
     */
    public Worker()
    {
      super("Session Worker Thread");
      Logger.debug("Starting Session Worker Thread");
    }
    
    /**
     * Registriert eine neue Session in dem Worker.
     * @param session
     */
    private void register(Session session)
    {
      synchronized(this.sessions)
      {
        if (!this.sessions.contains(session))
          this.sessions.add(session);
      }
    }
    
    /**
     * Entfernt eine Session aus dem Worker.
     * @param session die Session.
     */
    private void unregister(Session session)
    {
      synchronized(this.sessions)
      {
        this.sessions.remove(session);
        if (this.sessions.size() == 0)
        {
          // wir haben keine Sessions mehr. Dann koennen wir uns beenden.
          try
          {
            Logger.debug("session worker thread no longer needed, shutting down");
            interrupt();
          }
          catch (Exception e)
          {
            Logger.error("unable to shut down worker thread",e);
          }
          finally
          {
            Session.worker = null;
          }
        }
      }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      try
      {
        while (!this.isInterrupted())
        {
          long current = System.currentTimeMillis();

          synchronized(this.sessions)
          {
            for (int i=0;i<this.sessions.size();++i)
            {
              Session s = (Session) this.sessions.get(i);
              Hashtable data = s.data;
  
              synchronized(data)
              {
                Enumeration e = data.keys();
                while (e.hasMoreElements())
                {
                  Object key          = e.nextElement();
                  SessionObject value = (SessionObject) data.get(key);
                  if (current > (value.timestamp + value.myTimeout))
                  {
                    Logger.trace("removing object " + key + " from session");
                    data.remove(key);
                    s.setChanged();
                    s.notifyObservers(value.value);
                  }
                }
              }
            }
          }
          sleep(1000l);
        }
      }
      catch (InterruptedException e)
      {
        Logger.debug("session worker thread interrupted");
      }
    }

}
}

/*********************************************************************
 * $Log: Session.java,v $
 * Revision 1.13  2009/11/24 10:48:18  willuhn
 * @N Session#clear
 *
 * Revision 1.12  2007/11/26 15:11:36  willuhn
 * @B Objekte in  Session liefen nicht ab
 *
 * Revision 1.11  2006/09/05 22:02:01  willuhn
 * @C Worker-Redesign in Settings und Session
 *
 * Revision 1.10  2006/09/05 20:40:11  willuhn
 * @N Worker-Thread Redesign
 *
 * Revision 1.9  2006/04/26 15:04:13  web0
 * *** empty log message ***
 *
 * Revision 1.8  2006/04/05 09:00:41  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/09/04 21:50:27  web0
 * *** empty log message ***
 *
 * Revision 1.6  2005/07/25 22:12:45  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/07/24 16:59:17  web0
 * @B fix in settings watcher
 *
 * Revision 1.4  2005/07/08 16:42:31  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/04/28 15:43:33  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/04/04 17:51:09  web0
 * @N new Session
 *
 **********************************************************************/