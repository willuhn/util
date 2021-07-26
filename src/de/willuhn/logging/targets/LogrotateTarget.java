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

import de.willuhn.io.FileCopy;
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
  private boolean skipRotate = false;
  
  private long maxLength = 1L * 1024L * 1024L;
  private boolean zip = true;
  
  private final static DateFormat DF = new SimpleDateFormat("yyyyMMdd-HHmm_ss");
  private final static String lineSep = System.getProperty("line.separator");

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
    os.write((message.toString() + lineSep).getBytes());
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
    if (skipRotate)
      return;
    
    synchronized(os)
    {
      if (this.file.length() < this.maxLength)
        return;

      Logger.info("rotating log file " + this.file.getAbsolutePath());

      Logger.debug("closing old log file");
      os.close();

      String name = this.file.getName();

      if (zip)
      {
        File archiveFile = new File(this.file.getParent(),name + "-" + DF.format(new Date()) + ".gz");

        Logger.info("compressing old log file to " + archiveFile.getAbsolutePath());

        OutputStream os = null;
        InputStream is  = null;
        try
        {
          os = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(archiveFile)));
          is = new BufferedInputStream(new FileInputStream(this.file));
          byte[] buf = new byte[4096];
          int read = 0;
          do
          {
            read = is.read(buf);
            if (read > 0)
              os.write(buf,0,read);
          } while (read != -1);
          Logger.info("old log file compressed");
        }
        catch (Throwable t)
        {
          Logger.error("error while rotating logfile, disable rotating",t);
          skipRotate = true;
        }
        finally
        {
          if (os != null)
          {
            try
            {
              os.close();
            }
            catch (Exception e)
            {
              Logger.error("error while closing outputstream, disable rotating");
              skipRotate = true;
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
              Logger.error("error while closing inputstream, disable rotating");
              skipRotate = true;
            }
          }
        }
      }
      else
      {
        File archiveFile = new File(this.file.getParent(),name + "-" + DF.format(new Date()));
        Logger.info("copying log file to " + archiveFile.getAbsolutePath());
        try
        {
          FileCopy.copy(this.file,archiveFile,true);
        }
        catch (FileCopy.FileExistsException e)
        {
          Logger.error("unable to copy log file, disable rotating",e);
          skipRotate = true;
        }
      }
 
      Logger.info("deleting old log file");
      if (this.file.delete())
      {
        Logger.info("creating new log file " + name);
        this.file = new File(this.file.getParent(),name);
      }
      else
      {
        Logger.error("unable to delete old log file " + name + ", disable rotating");
        skipRotate = true;
      }
      this.os = new FileOutputStream(this.file,this.append);
      Logger.info("logrotation done");
    }
  }
}


/*********************************************************************
 * $Log: LogrotateTarget.java,v $
 * Revision 1.6  2009/06/07 22:05:54  willuhn
 * @N Logfile-Rotate ausschalten, wenn es fehlschlug - koennte sonst eine Rekursion ausloesen
 *
 * Revision 1.5  2007/03/26 23:52:08  willuhn
 * @N plattform specific line separator in logfiles
 *
 * Revision 1.4  2006/03/23 14:02:47  web0
 * @N new logrotate mechanism (runs no longer in background)
 *
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