/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/CheckSum.java,v $
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

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;

/**
 * Hilfsklasse zum Berechnen von Datenbank-Checksummen.
 */
public class CheckSum
{

  /**
   * Berechnet die MD5-Checksumme einer Datenbank.
   * In die Berechnung fliessen alle im Schem anthaltenen Tabellen
   * inclusive ihrer Spaltennamen und -typen ein.
   * Hinweis: Die Connection wird nicht geschlossen.
   * @param conn Connection.
   * @param catalog Name des Catalogs. Kann null sein.
   * @param schema Name des Schemas. Kann null sein.
   * @return MD5-Checksumme der Datenbank.
   * @throws SQLException
   * @throws NoSuchAlgorithmException
   */
  public static String md5(Connection conn, String catalog, String schema) throws SQLException, NoSuchAlgorithmException
  {
    Logger.info("calculating md5 checksum for database " + schema);
    StringBuffer sum = new StringBuffer();
    ResultSet rs = null;
    try {
      DatabaseMetaData dmd = conn.getMetaData();
      rs = dmd.getColumns(catalog,schema,null,null);
      String s = null;
      while (rs.next())
      {
        s = rs.getString("TABLE_NAME") + ":" + rs.getString("COLUMN_NAME") + ":" + rs.getString("TYPE_NAME");
        Logger.debug(s);
        sum.append(s + "\n");
      }
      return Checksum.md5(sum.toString().getBytes());
    }
    finally
    {
      try {
        if (rs != null)
          rs.close();
      }
      catch (Exception e)
      {
        Logger.error("error while closing resultset",e);
      }
    }
    
  }
  
}


/*********************************************************************
 * $Log: CheckSum.java,v $
 * Revision 1.1  2006/01/30 14:54:11  web0
 * @N de.willuhn.sql
 *
 **********************************************************************/