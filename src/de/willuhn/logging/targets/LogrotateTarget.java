/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/logging/targets/LogrotateTarget.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/08/16 21:42:02 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.logging.targets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;

/**
 * Implementierung eines Targets, welches nach einer definierten Dateigroesse
 * das Log-File rotiert und optional zippt.
 * @author willuhn
 */
public class LogrotateTarget implements Target
{

  private File file = null;
  private OutputStream os = null;
  private boolean append = true;
  
  private long maxLength = 1L * 1024L * 1024L;
  private boolean zip = true;
  
  private static DateFormat DF = new SimpleDateFormat("yyyyMMdd-HHmm_ss");

  /**
   * ct.
   * @param target Die Ziel-Datei.
   * @param append Legt fest, ob an das Log angehaengt oder ueberschrieben werden soll.
   * @throws IOException
   */
  public LogrotateTarget(File target, boolean append) throws IOException
  {
    this.file = target;
    this.append = append;
    this.os = new FileOutputStream(this.file,this.append);
  }

  /**
   * Legt die Maximal-Groesse des Log-Files fest, nach dessen
   * Erreichen es rotiert werden soll.
   * Default-Groesse: 1MB.
   * @param length Angabe der Maximalgroesse in Bytes.
   */
  public void setMaxLength(long length)
  {
    this.maxLength = length;
  }
  
  /**
   * Legt fest, ob die rotierten Logs gezippt werden sollen.
   * Default: Aktiv.
   * @param zip
   */
  public void setZip(boolean zip)
  {
    this.zip = zip;
  }
  
  /**
   * @see de.willuhn.logging.targets.Target#write(de.willuhn.logging.Message)
   */
  public void write(Message message) throws Exception
  {
    if (message == null)
      return;

    checkRotate();
    os.write((message.toString() + "\n").getBytes());
  }

  /**
   * @see de.willuhn.logging.targets.Target#close()
   */
  public void close() throws Exception
  {
    os.close();
  }
  
  /**
   * Prueft die Dateigroesse und rotiert ggf.
   * @throws IOException
   */
  private synchronized void checkRotate() throws IOException
  {
    synchronized(os)
    {
      if (this.file.length() < this.maxLength)
        return;

      Logger.info("rotating log file " + this.file.getAbsolutePath());
      os.close();

      String name = this.file.getName();

      final File rf = new File(this.file.getParent(),name + "-" + DF.format(new Date()));
      if (!this.file.renameTo(rf))
        throw new IOException("error while renaming log file to " + rf.getAbsolutePath());
      
      this.file = new File(this.file.getParent(),name);
      this.os = new FileOutputStream(this.file,this.append);

      if (zip)
      {
        Logger.info("compressing old log file to " + rf.getAbsolutePath() + ".gz [background thread]");
        Thread t = new Thread("logrotate")
        {
          public void run()
          {
            OutputStream os = null;
            InputStream is  = null;
            try
            {
              os = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(rf.getAbsolutePath() + ".gz")));
              is = new BufferedInputStream(new FileInputStream(rf));
              byte[] buf = new byte[4096];
              int read = 0;
              do
              {
                read = is.read(buf);
                if (read > 0)
                  os.write(buf,0,read);
              }
              while(read != -1);
            }
            catch (Throwable t)
            {
              Logger.error("error while rotating logfile",t);
            }
            finally
            {
              rf.delete();
              if (os != null)
              {
                try
                {
                  os.close();
                }
                catch (Exception e)
                {
                  Logger.error("error while closing outputstream");
                }
              }
              if (is != null)
              {
                try
                {
                  is.close();
                }
                catch (Exception e)
                {
                  Logger.error("error while closing inputstream");
                }
              }
            }
          }
        };
        
        t.start();
      }
    }
    Logger.info("logrotation done");
  }
}


/*********************************************************************
 * $Log: LogrotateTarget.java,v $
 * Revision 1.3  2005/08/16 21:42:02  web0
 * @N support for appending to log files
 *
 * Revision 1.2  2005/08/09 14:27:36  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/08/09 14:09:26  web0
 * @N added logrotate target
 *
 *********************************************************************/