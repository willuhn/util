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
