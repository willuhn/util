/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Session.java,v $
 * $Revision: 1.8 $
 * $Date: 2006/04/05 09:00:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.util;

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

  private Worker worker = null;
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
    Logger.info("creating new session. default timeout: " + timeout + " millis");
    this.timeout = timeout;
    this.worker = new Worker();
    this.worker.start();
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
    worker.interrupt();
    super.finalize();
  }

  private class SessionObject
  {
    private Object value;

    private long timestamp      = System.currentTimeMillis();
    private long myTimeout      = timeout;
    private boolean hardTimeout = false;

    private SessionObject(Object value)
    {
      this(value,Session.this.timeout);
    }

    private SessionObject(Object value, long t)
    {
      this(value,t,false);
    }

    private SessionObject(Object value, long t, boolean hardTimeout)
    {
      this.value = value;
      this.hardTimeout = hardTimeout;
      if (this.hardTimeout)
      {
        Logger.debug("added object \"" + value + "\" to session. hard timeout: " + new Date(t).toString());
        this.timestamp = t;
        this.myTimeout = 0;
      }
      else
      {
        this.myTimeout = t;
        Logger.debug("added object \"" + value + "\" to session. timeout: " + t + " millis");
      }
    }
    
    /**
     * Liefert den Wert des Keys.
     * @return der Wert.
     */
    private Object getValue()
    {
      if (!hardTimeout)
      {
        this.timestamp = System.currentTimeMillis();
        Logger.debug("new timeout for object \"" + value + "\" " + new Date(this.timestamp + this.myTimeout).toString());
      }
      return this.value;
    }
  }

  private class Worker extends Thread
  {
    /**
     * ct.
     */
    public Worker()
    {
      super("Worker Thread for Session " + Session.this.hashCode());
      Logger.debug("Created Worker Thread for Session " + Session.this.hashCode());
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      Logger.debug("starting worker thread");
      while (true)
      {
        long current = System.currentTimeMillis();
        try
        {
          synchronized(data)
          {
            Enumeration e = data.keys();
            while (e.hasMoreElements())
            {
              Object key          = e.nextElement();
              SessionObject value = (SessionObject) data.get(key);
              if (current > (value.timestamp + value.myTimeout))
              {
                Logger.debug("removing object " + key + " from session");
                data.remove(key);
                Session.this.setChanged();
                Session.this.notifyObservers(value.value);
              }
            }
          }
          sleep(1000l);
        }
        catch (InterruptedException e)
        {
          Logger.info("worker thread for session " + Session.this.hashCode() + " interrupted");
        }
      }
    }

}
}

/*********************************************************************
 * $Log: Session.java,v $
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