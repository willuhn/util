/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Attic/Test.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/07 18:06:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

import java.io.File;
import java.util.zip.ZipFile;

import de.willuhn.io.ZipExtractor;

/**
 * @author willuhn
 */
public class Test
{

  public static void main(String[] args) throws Throwable
  {
    ZipFile f = new ZipFile("/tmp/install/dynameica.zip");
    ZipExtractor e = new ZipExtractor(f);
    e.setMonitor(new Monitor());

    e.extract(new File("/tmp/install/dynastore/foo"));
    
  }
  
  private static class Monitor implements ProgressMonitor
  {

    /**
     * @see de.willuhn.util.ProgressMonitor#percentComplete(int)
     */
    public void percentComplete(int percent)
    {
    }

    /**
     * @see de.willuhn.util.ProgressMonitor#setStatus(int)
     */
    public void setStatus(int status)
    {
    }

    /**
     * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
     */
    public void setStatusText(String text)
    {
      System.out.println("Status: " + text);
    }

    /**
     * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
     */
    public void log(String msg)
    {
      System.out.println(msg);
    }
  }
}


/*********************************************************************
 * $Log: Test.java,v $
 * Revision 1.1  2004/10/07 18:06:10  willuhn
 * @N ZipExtractor
 *
 **********************************************************************/