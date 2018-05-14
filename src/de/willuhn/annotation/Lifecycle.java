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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, mit der der Lifecycle einer Bean festgelegt werden kann.
 * Es ist der Anwendung, die diesen Lifecycle auswertet, selbst ueberlassen,
 * welchen Lifecycle-Typ sie verwendet, wenn an der konkreten Bean
 * keine entsprechende Annotation definiert ist.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Lifecycle {
  
  
  /**
   * Definiert die Licecycle-Typen.
   */
  public static enum Type
  {
    /**
     * Bean-Instanz lebt fuer die Dauer der Anwendung.
     */
    CONTEXT,
    
    /**
     * Bean-Instanz lebt fuer die Dauer der Session (des Users).
     */
    SESSION,
    
    /**
     * Bean-Instanz lebt lediglich fuer die Dauer eines einzelnen Requests.
     */
    REQUEST,
  }
  
  /**
   * Typ des Lifecycle.
   * @return Typ des Lifecycle 
   */
  Lifecycle.Type value();
}


/*********************************************************************
 * $Log: Lifecycle.java,v $
 * Revision 1.1  2011/06/28 09:54:16  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.2  2009/08/05 11:03:17  willuhn
 * @N Neue Annotation "Lifecycle"
 *
 * Revision 1.1  2009/08/05 09:03:40  willuhn
 * @C Annotations in eigenes Package verschoben (sind nicht mehr REST-spezifisch)
 *
 **********************************************************************/