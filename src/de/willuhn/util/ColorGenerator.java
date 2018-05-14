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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Erzeugt Farbcodes aus einem vordefinierten Pool bzw. erzeugt automatisch neue Zufallsfarben.
 * Damit die Farben nicht bei jedem JVM-Neustart anders gewuerfelt werden, sind die
 * ersten 36 Farb-Werte statisch und unveraenderlich. Alle darauf folgenden werden
 * ausgewuerfelt und sind bei jedem JVM-Start anders.
 * Warum gerade 36 Farbwerte? Mehr hatte ich grad nicht zur Hand ;)
 * 
 * Die Palette der ersten 36 Farben ist thematisch unterteilt. Ab Position 0 kommen
 * 8 Farben aus Eclipse. Die naechsten 8 sind Pastel-Toene. Dann kommen
 * 8 satte Farben. Und ab Position 24 kommen 12 Office-Farben. Abhaengig davon,
 * mit welchen Farben das Rad beginnen soll, kann man eine der Konstanten PALETTE_*
 * hinzuaddieren. Wird als zum Beispiel "PALETTE_PASTEL" hinzuaddiert, beginnen die Farben mit
 * Pastel-Toenen. Danach kommen satte Farben, dann die Office-Farben und anschliessend
 * zufaellige.
 */
public class ColorGenerator
{
  private static List<int[]> colorCache = new ArrayList<int[]>();

  /**
   * Konstante, um mit den Eclipse-Farben zu beginnen.
   */
  public final static int PALETTE_ECLIPSE =  0;
  /**
   * Konstante, um mit den Pastel-Farben zu beginnen.
   */
  public final static int PALETTE_PASTEL  =  8;
  /**
   * Konstante, um mit den satten Farben zu beginnen.
   */
  public final static int PALETTE_RICH    = 16;
  /**
   * Konstante, um mit den Office-Farben zu beginnen.
   */
  public final static int PALETTE_OFFICE  = 24;
  
  // Basis-Palette
  static
  {
    // Eclipse-Farben
    colorCache.add(new int[]{225,225,255}); // hellblau
    colorCache.add(new int[]{223,197, 41}); // ocker
    colorCache.add(new int[]{249,225,191}); // blass orange
    colorCache.add(new int[]{255,205,225}); // rosa
    colorCache.add(new int[]{225,255,225}); // hellgruen
    colorCache.add(new int[]{255,191,255}); // rosa 2
    colorCache.add(new int[]{185,185,221}); // graublau
    colorCache.add(new int[]{ 40,255,148}); // gruen

    // Pastel-Farben
    colorCache.add(new int[]{255,161,161}); // hellrot
    colorCache.add(new int[]{255,215,161}); // orange
    colorCache.add(new int[]{250,255,161}); // gelb
    colorCache.add(new int[]{197,255,161}); // gruen
    colorCache.add(new int[]{161,255,253}); // tuerkis
    colorCache.add(new int[]{161,192,255}); // blau
    colorCache.add(new int[]{161,255,213}); // gruen 2
    colorCache.add(new int[]{243,161,255}); // rosa

    // Satte Farben
    colorCache.add(new int[]{238, 85, 27}); // rot
    colorCache.add(new int[]{ 81,180, 51}); // gruen
    colorCache.add(new int[]{  5,141,199}); // blau
    colorCache.add(new int[]{ 25, 76,126}); // marine
    colorCache.add(new int[]{255,127, 15}); // orange
    colorCache.add(new int[]{153,107, 19}); // braun
    colorCache.add(new int[]{153, 41, 95}); // violett
    colorCache.add(new int[]{226,226, 54}); // sandgelb

    // Office-Farben
    colorCache.add(new int[]{  0, 87,150});
    colorCache.add(new int[]{255, 83, 23});
    colorCache.add(new int[]{255,218, 46});
    colorCache.add(new int[]{105,171, 41});
    colorCache.add(new int[]{142,  0, 47});
    colorCache.add(new int[]{147,210,255});
    colorCache.add(new int[]{ 65, 81,  8});
    colorCache.add(new int[]{186,215,  0});
    colorCache.add(new int[]{ 93, 45,128});
    colorCache.add(new int[]{255,164, 23});
    colorCache.add(new int[]{206,  0, 19});
    colorCache.add(new int[]{  0,148,216});
  
  }

  /**
   * Erzeugt die RGB-Werte fuer eine Farbe.
   * @param pos Offset.
   * Bei einem Wert zwischen 0 und 35 wird immer die gleiche Farbe aus einer
   * vordefinierten Palette geliefert. Bei hoeheren Werten wird eine zufaellig
   * ausgewuerfelt, die bei jedem Aufruf einen anderen Wert hat.
   * Warum nicht zwei Funktionen - eine fuer die statische Palette
   * und eine fuer Zufalls-Farben? Weil man solche Farbwerte typischerweise
   * zum Zeichnen von Charts (z.Bsp. Kreis- oder Linien-Diagrammen) braucht.
   * Dort hat man eine Liste von Messreihen, die in einer Schleife dem
   * Chart zugeordnet werden. Man kann also einfach den Schleifencounter
   * (meist "int i") einfach uebergeben. Fuer die ersten 36 Zahlenreihen
   * kriegt man feste Farbcodes - fuer alles darueber Zufallsfarben.
   * Da man meist nicht mehr als 36 Zahlenreihen hat, reicht der statische
   * Pool fuer gewoehnlich aus.
   * @return RGB-Werte.
   */
  public static int[] create(int pos)
  {
    int[] color = null;

    // Haben wir hier schon eine Farbe im Cache?
    if (colorCache.size() > pos)
      return colorCache.get(pos);

    Random rand = new Random();

    int brightness = 40;

    color = new int[]{
      255 - brightness - rand.nextInt(30),
      255 - brightness - rand.nextInt(30),
      255 - brightness - rand.nextInt(30)
    };

    // Farbrichtung rotieren
    // Stellt sicher, dass kein Grau rauskommt, sondern eine
    // Farbe dominiert
    color[pos % 3] = 255;
    
    // Wir fuegen die Farbe zum Cache hinzu, damit die Farbe
    // wenigstens innerhalb der JVM-Session konstant bleibt
    try
    {
      colorCache.add(pos,color);
    }
    catch (IndexOutOfBoundsException e)
    {
      // pueh
    }
    return color;
  }
  
  /**
   * Main-Methode, falls man den mal von der Konsole aus nutzen will.
   * @param args
   * @throws Exception
   */
  public final static void main(String[] args) throws Exception
  {
    for (int i=100;i<1000;++i)
    {
      int[] values = ColorGenerator.create(i);
      System.out.println(i + ": " + Arrays.toString(values));
    }
  }

}


/**********************************************************************
 * $Log: ColorGenerator.java,v $
 * Revision 1.4  2010/05/19 14:47:38  willuhn
 * @N Ausfall von STDOUT tolerieren
 *
 * Revision 1.3  2009/11/02 17:43:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/08/24 23:47:41  willuhn
 * @N Farbkreis erweitert
 *
 * Revision 1.1  2009/08/21 22:56:04  willuhn
 * @N Farb-Generator fuer die Erzeugung von Farbwerten aus einem Pool oder notfalls Zufallsfarben
 *
 **********************************************************************/
