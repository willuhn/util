/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/Logger.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/05 21:46:29 $
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

  // Eine Queue mit den letzten Log-Eintraegen. Kann ganz nuetzlich sein,
  // wenn man irgendwo in der Anwendung mal die letzten Zeilen des Logs ansehen will.
  private Queue lastLines = new Queue(BUFFER_SIZE);

  private final static String DEBUG  = "DEBUG";
  private final static String INFO   = "INFO";
  private final static String WARN   = "WARN";
  private final static String ERROR  = "ERROR";
  
	private LoggerThread lt = null;
  /**
   * ct.
   */
  public Logger()
  {
  	lt = new LoggerThread();
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
		this.targets.add(new BufferedOutputStream(target));
	}
  /**
   * Schreibt eine Message vom Typ "debug" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void debug(String message)
  {
    write(DEBUG,message);
  }

  /**
   * Schreibt eine Message vom Typ "info" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void info(String message)
  {
    write(INFO,message);
  }

  /**
   * Schreibt eine Message vom Typ "warn" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void warn(String message)
  {
    write(WARN,message);
  }

  /**
   * Schreibt eine Message vom Typ "error" ins Log.
   * @param message zu loggende Nachricht.
   */
  public void error(String message)
  {
    write(ERROR,message);
  }

	/**
	 * Schreibt den Fehler ins Log.
	 * @param message zu loggende Nachricht.
   * @param t Exception oder Error.
   */
  public void error(String message, Throwable t)
	{
		write(ERROR,message);
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bos));
			write(ERROR,bos.toString());
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
		lt.interrupt();
		BufferedOutputStream bos = null;
		for (int i=0;i<this.targets.size();++i)
		{
			bos = (BufferedOutputStream) this.targets.get(i);
			try {
				bos.flush();
				bos.close();
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
   * @param level Name des Log-Levels.
   * @param message zu loggende Nachricht.
   */
  private void write(String level, String message)
  {
		lt.write(level,message);
  }
  
  /**
   * Das eigentliche Schreiben erfolgt in einem extra Thread damit's hoffentlich schneller geht.
   */
  private class LoggerThread extends Thread
  {
  	
  	private final static int maxLines = 100;
  	private Queue messages = new Queue(maxLines);

  	/**
     * ct.
     */
    public LoggerThread()
  	{
  		super(LoggerThread.class.getName());
  	}

		/**
		 * Loggt eine Zeile in's Logfile.
     * @param level Log-Level.
     * @param message Die eigentliche Nachricht.
     */
    public void write(String level, String message)
		{
			String s = "["+new Date().toString()+"] ["+level+"] " + message + "\n";
			try
      {
        messages.add(s);
      }
      catch (QueueFullException e)
      {
        System.out.print(s);
      }
		}

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
    	byte[] message;
			while(true)
			{
				if (messages.size() > 0)
				{
					BufferedOutputStream bos = null;
					message = ((String)messages.get()).getBytes();
					for (int i=0;i<targets.size();++i)
					{
						bos = (BufferedOutputStream) targets.get(i);
						try
						{
							bos.write(message);
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
