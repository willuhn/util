/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
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

import de.willuhn.util.Logger;

/**
 */
public class BootLoader {

	private Logger logger = new Logger("BootLoader");
	private InitParams params;
	private HashMap services = new HashMap();

	public BootLoader(InitParams params)
	{
		this.params = params;
	}

	public final void boot(Class target) throws Exception
	{
		if (target == null)
		{
			logger.warn("not target class given");
			return;
		}
		
		if (services.get(target) != null)
		{
			logger.info("service " + target.getName() + " allready booted, skipping");
			return;
		}

		logger.info("creating new instance of " + target.getName());
		Service s = (Service) target.newInstance();

		Class[] deps = s.depends();
		if (deps != null)
		{
			for (int i=0;i<deps.length;++i)
			{
				logger.info("booting dependency " + deps[i].getName());
				boot(deps[i]);
			}
		}
		s.init(params);
		logger.info("service " + target.getName() + " booted successfully");
		services.put(target,s);
	}

	public final void setLogger(Logger l)
	{
		if (l == null)
			return;
		this.logger = l;
	}

}


/**********************************************************************
 * $Log: BootLoader.java,v $
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/