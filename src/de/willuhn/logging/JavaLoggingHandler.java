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

package de.willuhn.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;


/**
 * Ein Handler, der "java.util.logging.Handler" implementiert, damit Log-Ausgaben
 * von Java in unser Logging umgeleitet werden koennen.
 * Die Klasse muss nicht manuell instanziiert werden. Es genuegt ein
 * <code>Class.forName("de.willuhn.logging.JavaLoggingHandler");</code>
 * an passender Stelle. Der Handler registriert sich dann automatisch
 * an allen gefundenen Loggern.
 */
public class JavaLoggingHandler extends Handler
{
  private final static Handler singleton = new JavaLoggingHandler();
  
  private static Map logMapping = new HashMap();
  static
  {
    try
    {
      logMapping.put(java.util.logging.Level.CONFIG,Level.INFO);
      logMapping.put(java.util.logging.Level.FINE,Level.DEBUG);
      logMapping.put(java.util.logging.Level.FINER,Level.DEBUG);
      logMapping.put(java.util.logging.Level.FINEST,Level.TRACE);
      logMapping.put(java.util.logging.Level.INFO,Level.INFO);
      logMapping.put(java.util.logging.Level.SEVERE,Level.ERROR);
      logMapping.put(java.util.logging.Level.WARNING,Level.WARN);

      // Wir deaktivieren alle Logger von Java
      java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
      Handler[] handlers = logger.getHandlers();
      if (handlers != null)
      {
        for (int i=0;i<handlers.length;++i)
        {
          handlers[i].setLevel(java.util.logging.Level.OFF);
        }
      }

      // Wir biegen das Logging zu uns um
      LogManager lm = LogManager.getLogManager();
      java.util.logging.Logger root = lm.getLogger("");
      root.addHandler(singleton);
    }
    catch (Exception e)
    {
      // Loggen geht ja vermutlich nicht ;)
      e.printStackTrace();
    }
  }

  /**
   * Ueberschrieben, um die Ausgaben in unseren Logger umzuleiten.
   * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
   */
  public synchronized void publish(LogRecord record)
  {
    if (record == null)
      return;
    
    Level level = (Level) logMapping.get(record.getLevel());
    if (level == null)
      level = Level.DEFAULT;
        
    String message = record.getMessage();
    Throwable t    = record.getThrown();
    
    if (t != null)
    {
      ByteArrayOutputStream bos = null;
      try
      {
        bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        message += "\n" + bos.toString();
      }
      finally
      {
        try
        {
          bos.close();
        }
        catch (Exception npe)
        {
        }
      }
    }
    
    Message msg = new Message(new Date(record.getMillis()),level,null,record.getSourceClassName(),record.getSourceMethodName(),message,Thread.currentThread().getName());
    Logger.write(msg);
  }

  /**
   * @see java.util.logging.Handler#close()
   */
  public void close() throws SecurityException
  {
  }

  /**
   * @see java.util.logging.Handler#flush()
   */
  public void flush()
  {
  }
}
