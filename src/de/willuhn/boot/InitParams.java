/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/Attic/InitParams.java,v $
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

import java.util.HashMap;

/**
 */
public class InitParams {

	private HashMap params = new HashMap();
	
	public void setParam(String name,Object value)
	{
		params.put(name,value);
	}
	public Object getParam(String name)
	{
		return params.get(name);
	}
}


/**********************************************************************
 * $Log: InitParams.java,v $
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/