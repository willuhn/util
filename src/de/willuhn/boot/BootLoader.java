/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/boot/BootLoader.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/06/03 00:45:11 $
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
		logger.setLevel(Logger.LEVEL_DEBUG);
		logger.addTarget(System.out);
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

		Service s = (Service) target.newInstance();

		logger.info("checking dependencies for " + target.getName());
		Class[] deps = s.depends();
		if (deps != null)
		{
			for (int i=0;i<deps.length;++i)
			{
				logger.info("booting dependency " + deps[i].getName());
				if (deps[i].equals(target))
				{
					logger.info(deps[i].getName() + " cannot have itself as dependency, skipping");
					continue;
				}
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

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable {
  	if (logger != null)
  		logger.close();
    super.finalize();
  }

}


/**********************************************************************
 * $Log: BootLoader.java,v $
 * Revision 1.2  2004/06/03 00:45:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/06/03 00:24:33  willuhn
 * *** empty log message ***
 *
 **********************************************************************/