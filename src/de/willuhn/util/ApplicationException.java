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
package de.willuhn.util;

/**
 * Diese Exception muss geworfen werden, wenn Fehler auftreten
 * die dem Anwender gezeigt werden sollen. Klassicher Fall:
 * Benutzer gibt ein Datum ein. In der Business-Logik wird es
 * auf syntaktische Richtigkeit geprueft. Ist es falsch, wirft
 * die Prueffunktion diese Exception. Die Anzeige-Schicht
 * faengt sie, entnimmt den Fehlertext via getMessage() und
 * zeigt ihn in der Oberflaeche an.
 * Konsequenz: Fehlertexte in dieser Exception muessen fuer den
 * End-Benutzer! verstaendlich formuliert sein.
 * @author willuhn
 */
public class ApplicationException extends Exception
{

  /**
   * Erzeugt eine neue Exception.
   */
  public ApplicationException()
  {
    super();
  }

  /**
   * Erzeugt eine neue Exception.
   * @param message Fehlertext.
   */
  public ApplicationException(String message)
  {
    super(message);
  }

  /**
   * Erzeugt eine neue Exception.
   * @param cause urspruenglicher Grund.
   */
  public ApplicationException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Erzeugt eine neue Exception.
   * @param message Fehlertext. 
   * @param cause urspruenglicher Grund.
   */
  public ApplicationException(String message, Throwable cause)
  {
    super(message, cause);
  }

}

/*********************************************************************
 * $Log: ApplicationException.java,v $
 * Revision 1.2  2004/03/03 22:27:33  willuhn
 * @N added Lock
 *
 * Revision 1.1  2004/01/08 21:38:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/27 00:22:17  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 **********************************************************************/