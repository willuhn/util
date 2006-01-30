/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/ScriptExecutor.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/30 14:54:11 $
 * $Author: web0 $
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
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import de.willuhn.logging.Logger;

/**
 * Util-Klasse, mit der ein SQL-Script auf einer Connection
 * ausgefuehrt werden kann.
 */
public class ScriptExecutor
{
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
    Statement stmt = null;
    String currentStatement = null;

    boolean commitState = false;
    try {

      BufferedReader br = null;
      String thisLine = null;
      StringBuffer all = new StringBuffer();

      try {
        br =  new BufferedReader(reader);
        while ((thisLine =  br.readLine()) != null)
        {
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

      commitState = conn.getAutoCommit();
      conn.setAutoCommit(false);

      stmt = conn.createStatement();
      Logger.info("executing sql commands");
      String[] commands = all.toString().split(";");
      for (int i=0;i<commands.length;++i)
      {
        currentStatement = commands[i];
        Logger.debug("executing: " + currentStatement);
        stmt.executeUpdate(currentStatement);
      }
      conn.commit();
    }
    catch (SQLException e)
    {
      try
      {
        if (conn != null)
          conn.rollback();
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
 * Revision 1.1  2006/01/30 14:54:11  web0
 * @N de.willuhn.sql
 *
 **********************************************************************/