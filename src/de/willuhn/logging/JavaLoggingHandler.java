/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/JavaLoggingHandler.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/10 09:08:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
      logMapping.put(java.util.logging.Level.FINEST,Level.DEBUG);
      logMapping.put(java.util.logging.Level.INFO,Level.INFO);
      logMapping.put(java.util.logging.Level.SEVERE,Level.ERROR);
      logMapping.put(java.util.logging.Level.WARNING,Level.WARN);

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
    
    Message msg = new Message(new Date(record.getMillis()),level,record.getSourceClassName(),record.getSourceMethodName(),message);
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


/**********************************************************************
 * $Log: JavaLoggingHandler.java,v $
 * Revision 1.2  2008/07/10 09:08:08  willuhn
 * @JavaLoggingHandler direkt am Java Root-Logger registrieren
 *
 * Revision 1.1  2007/04/11 23:59:22  willuhn
 * @N Log-Adapter fuer Java-Logging
 *
 **********************************************************************/
