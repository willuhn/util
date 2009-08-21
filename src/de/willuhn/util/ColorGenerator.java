/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/util/ColorGenerator.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/21 22:56:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Erzeugt Farbcodes aus einem vordefinierten Pool bzw. erzeugt automatisch neue Zufallsfarben.
 * Damit die Farben nicht bei jedem JVM-Neustart anders gewuerfelt werden, sind die
 * ersten 23 Farb-Werte statisch und unveraenderlich. Alle darauf folgenden werden
 * ausgewuerfelt und sind bei jedem JVM-Start anders.
 * Warum gerade 23 Farbwerte? Mehr hatte ich grad nicht zur Hand ;)
 */
public class ColorGenerator
{
  private static List<int[]> colorCache = new ArrayList<int[]>();

  // Basis-Palette
  static
  {
    // Eclipse-Farben
    colorCache.add(new int[]{225,225,255});
    colorCache.add(new int[]{223,197, 41});
    colorCache.add(new int[]{249,225,191});
    colorCache.add(new int[]{255,205,225});
    colorCache.add(new int[]{225,255,225});
    colorCache.add(new int[]{255,191,255});
    colorCache.add(new int[]{185,185,221});
    colorCache.add(new int[]{ 40,255,148});
    colorCache.add(new int[]{225,225,255});

    // Pastel-Farben
    colorCache.add(new int[]{255,161,161});
    colorCache.add(new int[]{255,215,161});
    colorCache.add(new int[]{250,255,161});
    colorCache.add(new int[]{197,255,161});
    colorCache.add(new int[]{161,255,213});
    colorCache.add(new int[]{161,255,253});
    colorCache.add(new int[]{161,192,255});
    colorCache.add(new int[]{243,161,255});

    // Satte Farben
    colorCache.add(new int[]{255, 74, 74});
    colorCache.add(new int[]{255,255, 74});
    colorCache.add(new int[]{ 74,255, 74});
    colorCache.add(new int[]{ 74,255,255});
    colorCache.add(new int[]{ 74, 74,255});
    colorCache.add(new int[]{255, 74,255});
  }

  /**
   * Erzeugt die RGB-Werte fuer eine Farbe.
   * @param pos Offset.
   * Bei einem Wert zwischen 0 und 22 wird immer die gleiche Farbe aus einer
   * vordefinierten Palette geliefert. Bei hoeheren Werten wird eine zufaellig
   * ausgewuerfelt, die bei jedem Aufruf einen anderen Wert hat.
   * Warum nicht zwei Funktionen - eine fuer die statische Palette
   * und eine fuer Zufalls-Farben? Weil man solche Farbwerte typischerweise
   * zum Zeichnen von Charts (z.Bsp. Kreis- oder Linien-Diagrammen) braucht.
   * Dort hat man eine Liste von Messreihen, die in einer Schleife dem
   * Chart zugeordnet werden. Man kann also einfach den Schleifencounter
   * (meist "int i") einfach uebergeben. Fuer die ersten 23 Zahlenreihen
   * kriegt man feste Farbcodes - fuer alles darueber Zufallsfarben.
   * Da man meist nicht mehr als 23 Zahlenreihen hat, reicht der statische
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
    colorCache.add(pos,color);
    return color;
  }

}


/**********************************************************************
 * $Log: ColorGenerator.java,v $
 * Revision 1.1  2009/08/21 22:56:04  willuhn
 * @N Farb-Generator fuer die Erzeugung von Farbwerten aus einem Pool oder notfalls Zufallsfarben
 *
 **********************************************************************/
