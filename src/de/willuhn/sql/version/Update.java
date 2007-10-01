/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/sql/version/Update.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/01 23:16:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.sql.version;

import de.willuhn.util.ApplicationException;


/**
 * Interface fuer ein einzelnes Datenbank-Update.
 */
public interface Update
{
  /**
   * Fuehrt das Update durch.
   * @param provider Provider, ueber den das Update alle noetigen Infos beziehen kann.
   * @throws ApplicationException wenn beim Update ein Fehler aufgetreten ist.
   */
  public void execute(UpdateProvider provider) throws ApplicationException;
  
  /**
   * Sprechender Name des Updates.
   * @return Name des Updates.
   */
  public String getName();

}


/**********************************************************************
 * $Log: Update.java,v $
 * Revision 1.1  2007/10/01 23:16:56  willuhn
 * @N Erste voellig ungetestete Version eines generischen Updaters.
 *
 **********************************************************************/
