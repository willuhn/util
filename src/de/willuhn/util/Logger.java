/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/Logger.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/06/10 20:57:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import de.willuhn.util.Queue.QueueFullException;

/**
 * Kleiner System-Logger.
 * @author willuhn
 */
public class Logger
{

  private ArrayList targets = new ArrayList();

  // maximale Groesse des Log-Puffers (Zeilen-Anzahl)
  private final static int BUFFER_SIZE = 40;

  // Eine History mit den letzten Log-Eintraegen. Kann ganz nuetzlich sein,
  // wenn man irgendwo in der Anwendung mal die letzten Zeilen des Logs ansehen will.
  private History lastLines = new History(BUFFER_SIZE);

	/**
	 * Die Bezeichnungen der verschiedenen Log-Level.
	 */
	public final static String[] LEVEL_TEXT = new String[] {"DEBUG","INFO","WARN","ERROR"};

	/**
	 * Log-Level DEBUG.
	 * Hoechster Log-Level.
	 */
	public final static int LEVEL_DEBUG = 0;

	/**
	 * Log-Level INFO.
	 * Es werden keine DEBUG-Meldungen angezeigt.
	 */
	public final static int LEVEL_INFO  = 1;

	/**
	 * Log-Level WARN.
	 * Es werden nur Warnungen und Fehler angezeigt.
	 */
	public final static int LEVEL_WARN  = 2;

	/**
	 * Log-Level ERROR.
	 * Es werden nur Fehler angezeigt.
	 */
	public final static int LEVEL_ERROR = 3;
	
  
	/**
	 * Standard-Loglevel.
	 */
	public final static int LEVEL_DEFAULT = LEVEL_INFO;

	private int level = LEVEL_DEFAULT;

	private LoggerThread lt = null;

  /**
   * ct.
   * @param name Aliasname zur Identifizierung des Logger-Threads.
   */
  public Logger(String name)
  {
  	lt = new LoggerThread(name);
  	lt.start();
  }
  
	/**
	 * Fuegt der Liste der Ausgabe-Streams einen weiteren hinzu.
   * @param target AusgabeStream.
   */
  public void addTarget(OutputStream target)
	{
		if (target == null)
			return;
		this.targets.add(target);
	}

	/**
	 * Setzt den Log-Level.
   * @param level Log-Level.
   */
  public void setLevel(int level)
	{
		if (level >= 0 && level < LEVEL_TEXT.length)
			this.level = level;
	}

	/**
	 * Setzt den Log-Level basierend auf dem uebergebenen String.
   * @param level Name des Log-Levels (DEBUG,INFO,WARN,ERROR).
   */
  public void setLevel(String level)
	{
		if (level == null || "".equals(level))
			return;
		setLevel(getLevelByName(level));
	}
	
	/**
	 * Liefert den Log-Level basierend auf dem Aliasnamen.
   * @param name Name des Log-Levels. Siehe auch Logger.LEVEL_TEXT[].
   * @return int wert des Log-Levels oder Default-Loglevel.
   *         Siehe auch Logger.LEVEL_*.
   */
  public int getLevelByName(String name)
	{
		for (int i=0;i<LEVEL_TEXT.length;++i)
		{
			if (LEVEL_TEXT[i].equalsIgnoreCase(name))
				return i;
		}
		return LEVEL_DEFAULT;
	}

  /**
   * Schreibt eine Message vom Typ "debug" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void debug(String message)
  {
    write(LEVEL_DEBUG,message);
  }

  /**
   * Schreibt eine Message vom Typ "info" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void info(String message)
  {
    write(LEVEL_INFO,message);
  }

  /**
   * Schreibt eine Message vom Typ "warn" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void warn(String message)
  {
    write(LEVEL_WARN,message);
  }

  /**
   * Schreibt eine Message vom Typ "error" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void error(String message)
  {
    write(LEVEL_ERROR,message);
  }

	/**
	 * Schreibt den Fehler ins Log.
	 * @param message zu loggende Nachricht.
   * @param t Exception oder Error.
   */
  public void error(String message, Throwable t)
	{
		write(LEVEL_ERROR,message);
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bos));
			write(LEVEL_ERROR,bos.toString());
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
  public void close()
	{
		lt.shutdown();

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

		OutputStream os = null;
		for (int i=0;i<this.targets.size();++i)
		{
			os = (OutputStream) this.targets.get(i);
			try {
				os.flush();
				os.close();
			}
			catch (IOException io)
			{
			}
		}
	}

  /**
   * Liefert die letzten Zeilen des Logs.
   * @return String-Array mit den letzten Log-Eintraegen (einer pro Index).
   */
  public String[] getLastLines()
  {
    return (String[]) lastLines.toArray(new String[lastLines.size()]);
  }

  /**
   * Interne Methode zum Formatieren und Schreiben der Meldungen.
   * @param level Log-Levels.
   * @param message zu loggende Nachricht.
   */
  private void write(int level, String message)
  {
  	if (level < this.level)
  		return;
		lt.write(level,message);
  }
  
  /**
   * Das eigentliche Schreiben erfolgt in einem extra Thread damit's hoffentlich schneller geht.
   */
  private class LoggerThread extends Thread
  {
  	
  	private final static int maxLines = 100;
  	private Queue messages = new Queue(maxLines);
  	
  	private boolean quit = false;
  	private boolean finished = false;

  	/**
     * ct.
     * @param name Name des Loggers.
     * Wird fuer die Bezeichnung des Logger-Threads verwendet.
     */
    public LoggerThread(String name)
  	{
  		super("logger: " + name);
  	}

		/**
		 * Loggt eine Zeile in's Logfile.
     * @param level Log-Level.
     * @param message Die eigentliche Nachricht.
     */
    public void write(int level, String message)
		{
			if (quit)
				return; // wir nehmen keine Log-Meldungen mehr entgegen.

			String s = "["+new Date().toString()+"] ["+LEVEL_TEXT[level]+"] " + message;
			try
      {
        messages.push(s);
      }
      catch (QueueFullException e)
      {
        System.out.println(s);
      }
		}

		/**
     * Beendet den Logger-Thread.
     */
    public void shutdown()
		{
			this.quit = true;
		}

		/**
		 * Liefert true, wenn der Thread die letzten Meldungen rausgeschrieben hat.
     * @return true, wenn alles rausgeschrieben ist.
     */
    public boolean finished()
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
				if (messages.size() > 0)
				{
					String s = (String) messages.pop();
					lastLines.push(s);

					OutputStream os = null;
					message = (s + "\n").getBytes();
					for (int i=0;i<targets.size();++i)
					{
						os = (OutputStream) targets.get(i);
						try
						{
							os.write(message);
						}
						catch (IOException e)
						{
						}
					}
				}
				try
        {
          sleep(100);
        }
        catch (InterruptedException e)
        {
        }
			}
    }

  }
}

/*********************************************************************
 * $Log: Logger.java,v $
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
