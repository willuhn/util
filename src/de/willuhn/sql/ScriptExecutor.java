/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/ScriptExecutor.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/11/26 22:11:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import de.willuhn.logging.Logger;
import de.willuhn.util.ConsoleProgessMonitor;
import de.willuhn.util.ProgressMonitor;

/**
 * Util-Klasse, mit der ein SQL-Script auf einer Connection
 * ausgefuehrt werden kann.
 */
public class ScriptExecutor
{
  /**
   * Main-Methode zum Starten an der Kommandozeile.
   * @param args Kommandozeilen-Parameter.
   * @throws Exception
   */
  public static void main(String args[]) throws Exception
  {
    if (args == null || args.length != 2)
      die("usage: java -cp de_willuhn_util.jar [jdbc-driver] [jdbc-url] < [sql-script.sql]");
    
    Class.forName(args[0]);
    Connection conn = null;
    try
    {
      conn = DriverManager.getConnection(args[1]);
      execute(new InputStreamReader(System.in),conn,new ConsoleProgessMonitor());
    }
    finally
    {
      if (conn != null)
        conn.close();
    }
  }
  
  /**
   * Zeigt eine Fehlermeldung an und beendet das Programm.
   * @param msg die Meldung.
   */
  private static void die(String msg)
  {
    System.err.println(msg);
    System.exit(1);
  }
  
  /**
   * Fuehrt ein SQL-Script auf einer Datenbank-Verbindung aus.
   * Hinweis: Weder die Connection noch der Reader wird geschlossen.
   * @param reader das auszufuehrende SQL-Script.
   * @param conn die Connection.
   * @throws IOException
   * @throws SQLException
   */
  public static void execute(Reader reader, Connection conn) throws IOException, SQLException
  {
    execute(reader, conn, null);
  }

  /**
   * Fuehrt ein SQL-Script auf einer Datenbank-Verbindung aus.
   * Hinweis: Weder die Connection noch der Reader wird geschlossen.
   * @param reader das auszufuehrende SQL-Script.
   * @param conn die Connection.
   * @param monitor ein Monitor, ueber den der Fortschritt der Ausfuehrung ausgegeben werden kann.
   * @throws IOException
   * @throws SQLException
   */
  public static void execute(Reader reader, Connection conn, ProgressMonitor monitor) throws IOException, SQLException
  {
    Statement stmt = null;
    String currentStatement = null;

    boolean commitState = false;
    try {

      BufferedReader br = null;
      String thisLine = null;
      StringBuffer all = new StringBuffer();

      int lines = 0;
      try {

        if (monitor != null) monitor.setStatusText("reading sql script");
        Logger.debug("reading sql script");
        br =  new BufferedReader(reader);
        while ((thisLine =  br.readLine()) != null)
        {
          if (monitor != null && lines++ % 20 == 0) monitor.addPercentComplete(1);
          if (!(thisLine.length() > 0))  // Zeile enthaelt nichts
            continue;
          if (thisLine.matches(" *?"))   // Zeile enthaelt nur Leerzeichen
            continue;
          if (thisLine.startsWith("--")) // Kommentare
            continue;
          if (thisLine.startsWith("\n") || thisLine.startsWith("\r")) // Leerzeile
            continue;
          all.append(thisLine.trim());
        }
      }
      catch (IOException e)
      {
        throw e;
      }
      finally
      {
        try {
          if (br != null)
            br.close();
        }
        catch (Exception e) {
          Logger.error("error while closing file reader",e);
        }
      }

      String s = all.toString();
      if (s == null || s.length() == 0)
      {
        Logger.info("no sql statements found in sql script");
        return;
      }
      commitState = conn.getAutoCommit();
      if (monitor != null) monitor.setStatusText("starting transaction");
      Logger.info("starting transaction");
      conn.setAutoCommit(false);

      stmt = conn.createStatement();
      if (monitor != null) monitor.setStatusText("executing sql commands");
      String[] commands = s.split(";");
      
      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / commands.length;
        monitor.setStatusText("executing sql commands");
      }

      for (int i=0;i<commands.length;++i)
      {
        if (monitor != null)  monitor.setPercentComplete((int)(i * factor));

        currentStatement = commands[i];
        if (currentStatement == null || currentStatement.length() == 0)
          continue; //skip empty line
        Logger.debug("executing: " + currentStatement);
        stmt.executeUpdate(currentStatement);
      }
      if (monitor != null) monitor.setStatusText("commit transaction");
      Logger.info("commit transaction");
      conn.commit();
      if (monitor != null)
      {
        monitor.setPercentComplete(100);
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
      }
    }
    catch (SQLException e)
    {
      if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      try
      {
        if (conn != null)
        {
          if (monitor != null) monitor.setStatusText("rollback transaction");
          Logger.info("rollback transaction");
          conn.rollback();
        }
      }
      catch (Exception e2)
      {
        Logger.error("error while rollback connection",e2);
      }

      Logger.error("error while executing sql script. Current statement: " + currentStatement,e);
      throw new SQLException("exception while executing sql script: " + e.getMessage() + ". Current statement: " + currentStatement);
    }
    finally {
      try
      {
        if (stmt != null)
          stmt.close();
      }
      catch (Exception e2)
      {
        Logger.error("error while closing statement",e2);
      }
      try
      {
        if (conn != null)
          conn.setAutoCommit(commitState);
      }
      catch (Exception e3)
      {
        Logger.error("error while restoring commit state",e3);
      }
    }
  }
}


/*********************************************************************
 * $Log: ScriptExecutor.java,v $
 * Revision 1.6  2008/11/26 22:11:35  willuhn
 * @N Console-Progressmonitor
 * @N main()-Funktion fuer ScriptExecutor
 *
 * Revision 1.5  2006/06/12 22:10:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/06/08 22:41:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/05/11 20:31:32  web0
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/09 23:17:44  web0
 * *** empty log message ***
 *
 * Revision 1.1  2006/01/30 14:54:11  web0
 * @N de.willuhn.sql
 *
 **********************************************************************/