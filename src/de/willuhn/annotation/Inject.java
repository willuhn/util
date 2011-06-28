/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/annotation/Inject.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/06/28 11:36:33 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;

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
      public void inject(Object bean, AccessibleObject field, Annotation a) throws Exception
      {
        field.setAccessible(true);
        if (field instanceof Field)
          ((Field)field).set(bean,value);
        else if (field instanceof Method)
          ((Method)field).invoke(bean,value);
        else
          throw new Exception("unable to inject into " + field.getClass().getSimpleName());
      }
    },a);
  }
  
  /**
   * Injiziert ein oder mehrere Werte in der Bean "bean" ueber den angegebenen Injector.
   * @param bean die Bean, deren Attribute injiziert werden sollen.
   * @param injector der Injector, der das Injizieren uebernehmen soll.
   * @throws Exception
   */
  public static void inject(Object bean, Injector injector) throws Exception
  {
    inject(bean,injector,new Class[]{});
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
      //////////////////////////////////////////////////////////////////////////
      // Member-Variablen
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
            if (matchTarget(at,ElementType.FIELD) && applicable(annotations,at))
            {
              f.setAccessible(true);
              injector.inject(bean,f,at);
              break; // Eigentlich macht es keinen Sinn, einen Wert mehrfach zu setzen
            }
          }
        }
      }
      //
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // Methoden
      Method[] methods = current.getDeclaredMethods();
      if (methods != null && methods.length > 0)
      {
        for (Method m:methods)
        {
          Annotation[] al = m.getAnnotations();
          if (al == null || al.length == 0)
            continue;
          
          for (Annotation at:al)
          {
            if (matchTarget(at,ElementType.METHOD) && applicable(annotations,at))
            {
              m.setAccessible(true);
              injector.inject(bean,m,at);
              break;
            }
          }
        }
      }
      //
      //////////////////////////////////////////////////////////////////////////
      
      Class superClass = current.getSuperclass();
      if (superClass == null)
        break; // Oben angekommen
      
      // Ansonsten mit der Super-Klasse weitermachen
      current = superClass;
    }
  }
  
  /**
   * Prueft, ob die Annotation auf den angegebenen Typ passt.
   * @param a die zu pruefende Annotation.
   * @param type der zu pruefende Typ.
   * @return true, wenn entweder kein Target angegeben ist oder der Wert passt.
   */
  private static boolean matchTarget(Annotation a, ElementType type)
  {
    Target target = a.getClass().getAnnotation(Target.class);
    if (target == null)
      return true; // Kein Target angegeben
    
    ElementType[] types = target.value();
    if (types == null || types.length == 0)
      return true;
    
    for (ElementType t:types)
    {
      if (t.equals(type))
        return true;
    }
    return false;
  }
  
  /**
   * Prueft, ob die Annotation angewendet werden soll.
   * @param list Liste der gesuchten Annotations.
   * @param a zu pruefende Annotation.
   * @return true, wenn "a" in "list" enthalten ist oder wenn "list" leer oder NULL ist. 
   */
  private static boolean applicable(Class<? extends Annotation>[] list, Annotation a)
  {
    // die 3. Bedingung tritt bei "inject(bean,value,(Class)null)" ein
    if (list == null || list.length == 0 || (list.length == 1 && list[0] == null))
      return true;
    
    for (Class test:list)
    {
      if (test == null)
        continue;
      
      if (a.annotationType().isAssignableFrom(test))
        return true;
    }
    return false;
  }
}



/**********************************************************************
 * $Log: Inject.java,v $
 * Revision 1.6  2011/06/28 11:36:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2011-06-28 11:03:25  willuhn
 * @N Inject unterstuetzt jetzt auch Methoden - nicht nur Member-Variablen
 *
 * Revision 1.4  2011-04-15 17:28:41  willuhn
 * @N Neue Funktion ohne Annotation-Parameter
 * @N die Liste der Annotationen konnte ein Null-Element enthalten, wurde nicht beachtet
 *
 * Revision 1.3  2011-03-30 12:23:21  willuhn
 * @N Das Injizieren kann der Injector jetzt selbst machen
 *
 * Revision 1.2  2011-03-30 11:54:53  willuhn
 * @N setAccessible fuer private Member
 *
 * Revision 1.1  2011-03-30 11:51:22  willuhn
 * @N Injector
 *
 **********************************************************************/