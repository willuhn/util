/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/Logger.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/12 18:18:19 $
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
  private final static int BUFFER_SIZE = 40;

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
	 * Fuegt der Liste der Ausgabe-Streams einen weiteren hinzu.
   * @param target AusgabeStream.
   */
  public static void addTarget(OutputStream target)
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
  public static void removeTarget(OutputStream target)
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
		write(Level.ERROR,message);
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bos));
			write(Level.ERROR,bos.toString());
		}
		finally {
			try {
				bos.close();
			}
			catch (Exception npe) {}
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
			OutputStream os = null;
			for (int i=0;i<targets.size();++i)
			{
				os = (OutputStream) targets.get(i);
				try {
					os.flush();
					os.close();
				}
				catch (IOException io)
				{
				}
			}
			targets = new ArrayList();
		}
	}

  /**
   * Liefert die letzten Zeilen des Logs.
   * @return String-Array mit den letzten Log-Eintraegen (einer pro Index).
   */
  public static String[] getLastLines()
  {
    return (String[]) lastLines.toArray(new String[lastLines.size()]);
  }

  /**
   * Interne Methode zum Formatieren und Schreiben der Meldungen.
   * @param level Log-Levels.
   * @param message zu loggende Nachricht.
   */
  protected static void write(Level level, String message)
  {
		// Wir checken, ob der uebergebene Level mindestens genauso wertig ist,
		// wie unser aktueller
  	if (level.getValue() < Logger.level.getValue())
  		return;

		lt.write(level,message);
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
  	}

		/**
		 * Loggt eine Zeile in's Logfile.
     * @param level Log-Level.
     * @param message Die eigentliche Nachricht.
     */
    private void write(Level level, String message)
		{
			if (quit)
				return; // wir nehmen keine Log-Meldungen mehr entgegen.

			String s = "["+new Date().toString()+"] ["+level.getName()+"] " + message;
			
			// Das machen wir in einer Schleife solange, bis Hinzufuegen
			// zur Queue erfolgreich war. OK, wir haben eine Maximal-Zahl von Versuchen ;)
			int retryCount = 1000;
			int count = 0;
			while (true)
			{
				if (count++ >= retryCount)
				{
					System.out.println("***** [WARN] Logger queue full, writing to STDOUT *****");
					System.out.println(s);
					return;
				}

				try
				{
					messages.push(s);
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
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
    	byte[] message;
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

				String s = (String) messages.pop();
				lastLines.push(s);

				OutputStream os = null;
				message = (s + "\n").getBytes();
				synchronized (targets)
				{
					if (targets.size() == 0)
					{
						System.out.print("warn: no logging target defined, logging to console: " + new String(message));
						continue;
					}

					for (int i=0;i<targets.size();++i)
					{
						os = (OutputStream) targets.get(i);
						try
						{
							os.write(message);
						}
						catch (IOException e)
						{
							System.out.print("alert: error while logging the following message: " + new String(message));
						}
					}
				}
			}
    }

  }
}

/*********************************************************************
 * $Log: Logger.java,v $
 * Revision 1.1  2004/11/12 18:18:19  willuhn
 * @C Logging refactoring
 *
 * Revision 1.19  2004/11/10 17:48:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/04 17:07:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/06/30 20:58:52  willuhn
 * @C some refactoring
 *
 * Revision 1.16  2004/06/15 21:11:30  willuhn
 * @N added LoggerOutputStream
 *
 * Revision 1.15  2004/06/10 20:57:34  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.14  2004/06/03 22:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/05/25 23:24:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/11 21:19:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/18 01:24:56  willuhn
 * @C refactoring
 *
 * Revision 1.10  2004/03/06 18:24:47  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/02/12 00:49:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/01/25 18:40:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/01/24 17:40:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/06 19:58:29  willuhn
 * @N ArrayEnumeration
 *
 * Revision 1.4  2004/01/06 18:07:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/01/05 23:08:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/05 21:46:29  willuhn
 * @N added queue
 * @N logger writes now in separate thread
 *
 * Revision 1.1  2004/01/03 19:33:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
