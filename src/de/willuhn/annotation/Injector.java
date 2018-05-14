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

package de.willuhn.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

/**
 * Interface fuer einen Injector, der die zu injizierenden Werte liefert.
 */
public interface Injector
{
  /**
   * Injiziert den den Wert in die Bean.
   * @param bean die Bean, in die die Werte injiziert werden sollen.
   * @param field das Attribut oder die Methode, fuer welches der Wert gesetzt werden soll.
   * @param a die aktuelle Annotation.
   * @throws Exception
   */
  public void inject(Object bean, AccessibleObject field, Annotation a) throws Exception;
}



/**********************************************************************
 * $Log: Injector.java,v $
 * Revision 1.3  2011/06/28 11:03:25  willuhn
 * @N Inject unterstuetzt jetzt auch Methoden - nicht nur Member-Variablen
 *
 * Revision 1.2  2011-03-30 12:23:21  willuhn
 * @N Das Injizieren kann der Injector jetzt selbst machen
 *
 * Revision 1.1  2011-03-30 11:51:22  willuhn
 * @N Injector
 *
 **********************************************************************/