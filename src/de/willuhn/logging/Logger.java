/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/Logger.java,v $
 * $Revision: 1.16 $
 * $Date: 2010/05/19 14:47:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.logging;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import de.willuhn.logging.targets.Target;
import de.willuhn.util.History;
import de.willuhn.util.Queue;
import de.willuhn.util.Queue.QueueFullException;

/**
 * Kleiner System-Logger.
 * @author willuhn
 */
public class Logger
{

  // maximale Groesse des Log-Puffers (Zeilen-Anzahl)
  private final static int BUFFER_SIZE = 200;

	// Die Liste der Log-Targets
	private static ArrayList targets = new ArrayList();

  // Eine History mit den letzten Log-Eintraegen. Kann ganz nuetzlich sein,
  // wenn man irgendwo in der Anwendung mal die letzten Zeilen des Logs ansehen will.
  private static History lastLines = new History(BUFFER_SIZE);

	private static Level level = Level.DEFAULT;

	private static LoggerThread lt = null;
	
	static
	{
		lt = new LoggerThread("Logger-Thread");
		lt.start();
	}
  
	/**
	 * Fuegt der Liste der Ausgabe-Targets ein weiteres hinzu.
   * @param target Ausgabe-Target.
   */
  public static void addTarget(Target target)
	{
		if (target == null)
			return;
		synchronized (targets)
		{
			targets.add(target);
		}
	}

  /**
   * Entfernt ein Target aus der Liste.
   * @param target zu entfernendes Target.
   */
  public static void removeTarget(Target target)
  {
    if (target == null)
      return;
    synchronized(targets)
    {
      targets.remove(target);
    }
  }

	/**
	 * Setzt den Log-Level.
   * @param level Log-Level.
   */
  public static void setLevel(Level level)
	{
		if (level == null)
			return;
		Logger.level = level;
	}

	/**
	 * Liefert den aktuellen Log-Level.
   * @return Log-Level.
   */
  public static Level getLevel()
	{
		return level;
	}
  
  /**
   * Prueft, ob Meldungen mit dem angegeben Log-Level derzeit geloggt werden.
   * @param l das zu testende Log-Level.
   * @return true, wenn Meldungen mit dem angegeben Log-Level derzeit geloggt werden.
   */
  public static boolean isLogging(Level l)
  {
    return l != null && l.getValue() >= Logger.level.getValue();
  }

  /**
   * Schreibt eine Message vom Typ "debug" ins Log.
   * @param message zu loggende Nachricht.
   */
  public static void debug(String message)
  {
    write(Level.DEBUG,message);
  }

  /**
   * Schreibt eine Message vom Typ "info" ins Log.
   * @param message zu loggende Nachricht.
   */
  public static void info(String message)
  {
    write(Level.INFO,message);
  }

  /**
   * Schreibt eine Message vom Typ "warn" ins Log.
   * @param message zu loggende Nachricht.
   */
  public static void warn(String message)
  {
    write(Level.WARN,message);
  }

  /**
   * Schreibt eine Message vom Typ "error" ins Log.
   * @param message zu loggende Nachricht.
   */
  public static void error(String message)
  {
    write(Level.ERROR,message);
  }

	/**
	 * Schreibt den Fehler ins Log.
	 * @param message zu loggende Nachricht.
   * @param t Exception oder Error.
   */
  public static void error(String message, Throwable t)
	{
		write(Level.ERROR,message,t);
	}

  /**
   * Flusht die noch nicht geschriebenen Log-Meldungen.
   * Eigentlich macht die Funktion nichts anderes, als solange
   * zu warten, bis die Queue leer ist ;).
   * @throws InterruptedException
   */
  public static void flush() throws InterruptedException
  {
    while(lt.messages.size() > 0)
    {
      Thread.sleep(100l);
    }
  }

  /**
   * Schliesst den Logger und die damit verbundene Log-Datei.
   */
  public static void close()
	{
		lt.shutdown();

		// Wir muessen noch etwas warten, bis der Thread alle Eintraege
		// aus der Queue geschrieben hat.
		try {
			while (!lt.finished())
			{
				Thread.sleep(50);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			lt.interrupt();
		}

		synchronized (targets)
		{
			Target target = null;
			for (int i=0;i<targets.size();++i)
			{
				target = (Target) targets.get(i);
				try {
					target.close();
				}
				catch (Exception io)
				{
				}
			}
			targets = new ArrayList();
		}
	}

  /**
   * Liefert die letzten Zeilen des Logs.
   * @return Array mit den letzten Log-Eintraegen (einer pro Index).
   */
  public static Message[] getLastLines()
  {
    return (Message[]) lastLines.toArray(new Message[lastLines.size()]);
  }

  /**
   * Schreibt eine Log-Meldung mit direkter Angabe des Log-Levels.
   * @param level Log-Levels.
   * @param message zu loggende Nachricht.
   */
  public static void write(Level level, String message)
  {
    write(level,message,null);
  }

  /**
   * Schreibt eine Log-Meldung mit direkter Angabe des Log-Levels.
   * @param level Log-Levels.
   * @param message zu loggende Nachricht.
   * @param t optionale Angabe einer Exception.
   */
  public static void write(Level level, String message, Throwable t)
  {
		write(level,null,null,null,message,t);
  }
  
  /**
   * Schreibt eine Log-Meldung mit direkter Angabe des Log-Levels.
   * @param level Log-Levels.
   * @param host optionale Angabe des Hostnamens.
   * @param clazz Name der loggenden Klasse.
   * @param method Name der loggenden Funktion.
   * @param message zu loggende Nachricht.
   * @param t optionale Angabe einer Exception.
   */
  public static void write(Level level, String host, String clazz, String method, String message, Throwable t)
  {
    // Wir checken, ob der uebergebene Level mindestens genauso wertig ist,
    // wie unser aktueller
    if (level.getValue() < Logger.level.getValue())
      return;

    if (t != null)
    {
      ByteArrayOutputStream bos = null;
      try {
        bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        message += "\n" + bos.toString();
      }
      finally {
        try {
          bos.close();
        }
        catch (Exception npe) {}
      }
    }

    if (clazz == null || method == null)
    {
      // Wir suchen den Verursacher, in dem wir den Stacktrace hochwandern,
      // bis wir nicht mehr selbst drin stehen
      StackTraceElement[] stack = new Throwable().getStackTrace();
      if (stack != null)
      {
        for (int i=0;i<stack.length;++i)
        {
          clazz = stack[i].getClassName();
          method = stack[i].getMethodName();
          if (!Logger.class.getName().equals(clazz))
            break;
        }
      }
    }

    write(new Message(new Date(),level,host,clazz,method,message));
  }

  /**
   * Schreibt eine fertige Message ins Log.
   * @param message zu loggende Nachricht.
   */
  public static void write(Message message)
  {
    lastLines.push(message);
    lt.write(message);
  }

  /**
   * Das eigentliche Schreiben erfolgt in einem extra Thread damit's hoffentlich schneller geht.
   */
  private static class LoggerThread extends Thread
  {
  	
  	private Queue messages = new Queue(Queue.CAPACITY_MAX / 2);
  	
  	private boolean quit = false;
  	private boolean finished = false;

  	/**
     * ct.
     * @param name Name des Loggers.
     * Wird fuer die Bezeichnung des Logger-Threads verwendet.
     */
    public LoggerThread(String name)
  	{
  		super(name);
      setDaemon(true);
  	}

    /**
		 * Loggt eine Zeile in's Logfile.
     * @param msg die zu loggende Nachricht.
     */
    private void write(Message msg)
		{
			if (quit)
				return; // wir nehmen keine Log-Meldungen mehr entgegen.

			// Das machen wir in einer Schleife solange, bis Hinzufuegen
			// zur Queue erfolgreich war. OK, wir haben eine Maximal-Zahl von Versuchen ;)
			int retryCount = 1000;
			int count = 0;
			while (true)
			{
				if (count++ >= retryCount)
				{
					println("***** [WARN] Logger queue full, writing to STDOUT *****");
					println(msg.toString());
					return;
				}

				try
				{
					messages.push(msg);
					return;
				}
				catch (QueueFullException e)
				{
					// try again ;)
				}
			}
		}

		/**
     * Beendet den Logger-Thread.
     */
    private void shutdown()
		{
			this.quit = true;
		}

		/**
		 * Liefert true, wenn der Thread die letzten Meldungen rausgeschrieben hat.
     * @return true, wenn alles rausgeschrieben ist.
     */
    private boolean finished()
		{
			return finished;
		}
    
    /**
     * Gibt den Text auf STDOUT aus.
     * @param text auszugebender Text.
     */
    private void println(String text)
    {
      try
      {
        System.out.println(text);
      }
      catch (Exception e)
      {
        // ignore - wenn STDOUT kaputt ist, koennen wir uns eh nicht mehr artikulieren ;)
      }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
    	Message msg = null;
			while(true)
			{
				if (messages.size() == 0 && quit)
				{
					finished = true;
					return;
				}

				if (messages.size() == 0)
				{
					// nichts zum Schreiben da, dann warten wir etwas
					try
					{
						sleep(100);
					}
					catch (InterruptedException e)
					{
					}
					continue;
				}

				msg = (Message) messages.pop();

				Target target = null;
				synchronized (targets)
				{
					if (targets.size() == 0)
					{
						println(msg.toString());
						continue;
					}

					for (int i=0;i<targets.size();++i)
					{
						target = (Target) targets.get(i);
						try
						{
							target.write(msg);
						}
						catch (Exception e)
						{
							println("alert: error while logging the following message: " + msg.toString());
						}
					}
				}
			}
    }
  }

}

/*********************************************************************
 * $Log: Logger.java,v $
 * Revision 1.16  2010/05/19 14:47:38  willuhn
 * @N Ausfall von STDOUT tolerieren
 *
 * Revision 1.15  2010/02/12 00:57:34  willuhn
 * @N Test-Methode, um herauszufinden, ob ein angebenes Level geloggt wird
 *
 * Revision 1.14  2008/06/13 13:40:47  willuhn
 * @N Class und Method kann nun explizit angegeben werden
 * @N Hostname kann mitgeloggt werden
 **********************************************************************/
