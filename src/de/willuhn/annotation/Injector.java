/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/annotation/Injector.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/03/30 11:51:22 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.annotation;

import java.lang.annotation.Annotation;

/**
 * Interface fuer einen Injector, der die zu injizierenden Werte liefert.
 */
public interface Injector
{
  /**
   * Liefert den zu injizierenden Wert fuer die Annotation.
   * @param bean die Bean, in die die Werte injiziert werden sollen.
   * @param a die aktuelle Annotation.
   * @return der zu setzende Wert.
   * @throws Exception
   */
  public Object get(Object bean, Annotation a) throws Exception;
}



/**********************************************************************
 * $Log: Injector.java,v $
 * Revision 1.1  2011/03/30 11:51:22  willuhn
 * @N Injector
 *
 **********************************************************************/