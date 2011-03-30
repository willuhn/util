/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/annotation/Inject.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/03/30 11:54:53 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Util-Klasse zum Setzen von Annotations.
 */
public class Inject
{
  /**
   * Injiziert den Wert "value" in der Bean "bean" fuer all jene Attribute,
   * die mit Annotation "a" markiert sind.
   * @param bean die Bean, deren Attribute injiziert werden sollen.
   * @param a die gesucht Annotation.
   * @param value der zu setzende Wert.
   * @throws Exception Wenn beim Injizieren Fehler auftraten.
   */
  public static void inject(Object bean, Class<? extends Annotation> a, final Object value) throws Exception
  {
    inject(bean,new Injector() {
      public Object get(Object bean, Annotation a) throws Exception
      {
        return value;
      }
    },a);
  }
  
  /**
   * Injiziert ein oder mehrere Werte in der Bean "bean" ueber den angegebenen Injector.
   * @param bean die Bean, deren Attribute injiziert werden sollen.
   * @param injector der Injector, der das Injizieren uebernehmen soll.
   * @param annotations optionale Liste von Annotations, nach denen gesucht werden soll.
   * Sind keine angegeben, werden alle Annotations gefunden.
   * @throws Exception
   */
  public static void inject(Object bean, Injector injector, Class<? extends Annotation>... annotations) throws Exception
  {
    // Ich mag keine while(true)-Schleifen. Wenn die Abbruchbedingung
    // nicht erfuellt wird, haben wir eine Endlos-Schleife. Und da davon
    // auszugehen ist, dass eine Klasse unmoeglich mehr als 100 Superklassen
    // haben kann, limitieren wir das da.
    // Hier eigentlich nur fuer den Fall, dass es irgend eine Java-Implementierung gibt,
    // die bei Class#getSuperclass() nicht NULL liefert, wenn Class bereits ein
    // "java.lang.Object" ist (wir also schon oben angekommen sind). Ich hab
    // nirgends einen Hinweis gefunden, ob dieser spezifiziert ist. 
    Class current = bean.getClass();
    for (int i=0;i<100;++i)
    {
      Field[] fields = current.getDeclaredFields(); // getFields() liefert nur die public-Member
      if (fields != null && fields.length > 0)
      {
        for (Field f:fields)
        {
          Annotation[] al = f.getAnnotations(); // getDeclaredAnnotations() liefert keine geerbten Annotations
          if (al == null || al.length == 0)
            continue; // hat keine Annotations
          
          for (Annotation at:al)
          {
            if (applicable(annotations,at))
            {
              f.setAccessible(true);
              f.set(bean,injector.get(bean,at));
              break; // Eigentlich macht es keinen Sinn, einen Wert mehrfach zu setzen
            }
          }
        }
      }
      
      Class superClass = current.getSuperclass();
      if (superClass == null)
        break; // Oben angekommen
      
      // Ansonsten mit der Super-Klasse weitermachen
      current = superClass;
    }
  }
  
  /**
   * Prueft, ob die Annotation angewendet werden soll.
   * @param list Liste der gesuchten Annotations.
   * @param a zu pruefende Annotation.
   * @return true, wenn "a" in "list" enthalten ist oder wenn "list" leer oder NULL ist. 
   */
  private static boolean applicable(Class<? extends Annotation>[] list, Annotation a)
  {
    if (list == null || list.length == 0)
      return true;
    
    for (Class test:list)
    {
      if (a.annotationType().isAssignableFrom(test))
        return true;
    }
    return false;
  }
}



/**********************************************************************
 * $Log: Inject.java,v $
 * Revision 1.2  2011/03/30 11:54:53  willuhn
 * @N setAccessible fuer private Member
 *
 * Revision 1.1  2011-03-30 11:51:22  willuhn
 * @N Injector
 *
 **********************************************************************/