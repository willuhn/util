/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/Attic/Service.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/03 00:24:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.boot;

/**
 */
public interface Service {

	public void init(InitParams params);
	
	public Class[] depends();
	
}


/**********************************************************************
 * $Log: Service.java,v $
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/