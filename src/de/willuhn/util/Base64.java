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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import de.willuhn.logging.Logger;

/**
 * Kleine Hilfe-Klasse zum Encoden und Decoden von Base64.
 * Der Zugriff auf die Encoder/Decoder von Java geschieht per Reflection, damit die Klasse sowohl
 * unter Java &lt; 8 lauffaehig ist (dort existiert java.util.Base64 noch nicht) als auch unter Java &ge; 9
 * (dort existiert "sun.misc.BASE64*" nicht mehr).
 */
public class Base64
{
  private static Encoder encoder = null;
  
  static
  {
    for (Encoder e:Arrays.asList(new JavaEncoder(),new SunEncoder()))
    {
      if (e.exists())
      {
        encoder = e;
        Logger.info("using base64 encoder/decoder: " + encoder.getClass().getSimpleName());
        break;
      }
    }
    
    if (encoder == null)
      Logger.warn("no base64 encoder/decoder found");
  }
  
	/**
	 * Dekodiert Base64 in Text.
   * @param base64 Base64.
   * @return Text.
	 * @throws IOException
   */
  public final static byte[] decode(String base64) throws IOException
	{
    if (encoder == null)
      throw new IOException("base64 decoder not available");
    
    return encoder.decode(base64);
	}

  /**
   * Kodiert Text nach Base64.
   * @param text
   * @return Base64-Version.
   */
  public final static String encode(byte[] text)
	{
    if (encoder == null)
      return null;

    return encoder.encode(text);
	}
  
  /**
   * Interface, welches die getrennten Implementierungen kapselt.
   */
  private static interface Encoder
  {
    /**
     * Liefert true, wenn der Encoder existiert.
     * @return true, wenn der Encoder existiert.
     */
    boolean exists();
    
    /**
     * Dekodiert Base64 in Text.
     * @param base64 Base64.
     * @return Text.
     * @throws IOException
     */
    public byte[] decode(String base64) throws IOException;

    /**
     * Kodiert Text nach Base64.
     * @param text
     * @return Base64-Version.
     */
    public String encode(byte[] text);
  }
  
  /**
   * Implementierung eines Encoders mit der SUN-Implementierung.
   */
  private static class SunEncoder implements Encoder
  {
    private final static String DECODER = "sun.misc.BASE64Decoder";
    private final static String ENCODER = "sun.misc.BASE64Encoder";
    
    private Object decoder = null;
    private Object encoder = null;
    private Method decode  = null;
    private Method encode  = null;
    
    /**
     * ct.
     */
    private SunEncoder()
    {
      this.decoder = load(DECODER);
      this.encoder = load(ENCODER);
    }

    /**
     * Laedt die Klasse und erzeugt eine Instanz.
     * @param className der Name der Klasse.
     * @return die Instanz oder NULL, wenn die Klasse nicht geladen/instanziiert werden konnte.
     */
    private static Object load(final String className)
    {
      try
      {
        return Class.forName(className).newInstance();
      }
      catch (Throwable t)
      {
        Logger.debug("base64 encoder/decoder " + className + " not available on this java version");
        return null;
      }
    }

    /**
     * @see de.willuhn.util.Base64.Encoder#exists()
     */
    public boolean exists()
    {
      return decoder != null && encoder != null;
    }
    
    /**
     * @see de.willuhn.util.Base64.Encoder#decode(java.lang.String)
     */
    public byte[] decode(String base64) throws IOException
    {
      if (this.decoder == null)
        throw new IOException("base64 decoder not available");

      try
      {
        if (this.decode == null)
          this.decode = this.decoder.getClass().getMethod("decodeBuffer",new Class[]{String.class});
        
        return (byte[]) this.decode.invoke(this.decoder,base64);
      }
      catch (Throwable t)
      {
        if (t instanceof IOException)
          throw (IOException) t;
        
        throw new IOException(t);
      }
    }
    
    /**
     * @see de.willuhn.util.Base64.Encoder#encode(byte[])
     */
    public String encode(byte[] text)
    {
      if (this.encoder == null)
      {
        Logger.warn("base64 encoder not available");
        return null;
      }

      try
      {
        if (this.encode == null)
          this.encode = this.encoder.getClass().getMethod("encode",new Class[]{byte[].class});

        return (String) this.encode.invoke(this.encoder,text);
      }
      catch (Throwable t)
      {
        Logger.error("unable to encode base64",t);
        return null;
      }
    }
  }

  /**
   * Implementierung eines Encoders mit der Java-Implementierung (ab Java 1.8 verfuegbar).
   */
  private static class JavaEncoder implements Encoder
  {
    private final static String BASE64 = "java.util.Base64";
    
    private Object decoder = null;
    private Object encoder = null;
    private Method decode  = null;
    private Method encode  = null;
    
    /**
     * ct.
     */
    private JavaEncoder()
    {
      this.decoder = load(BASE64,"getMimeDecoder");
      this.encoder = load(BASE64,"getMimeEncoder");
    }
    
    /**
     * Laedt den Encoder.
     * @param className der Name der Klasse.
     * @return die Instanz oder NULL, wenn die Klasse nicht geladen/instanziiert werden konnte.
     */
    private static Object load(final String className, final String method)
    {
      try
      {
        Class base64 = Class.forName(className);
        Method m = base64.getMethod(method);
        return m.invoke(base64);
      }
      catch (Throwable t)
      {
        Logger.debug("base64 encoder/decoder " + className + " not available on this java version");
        return null;
      }
    }
    
    /**
     * @see de.willuhn.util.Base64.Encoder#exists()
     */
    public boolean exists()
    {
      return decoder != null && encoder != null;
    }
    
    /**
     * @see de.willuhn.util.Base64.Encoder#decode(java.lang.String)
     */
    public byte[] decode(String base64) throws IOException
    {
      if (this.decoder == null)
        throw new IOException("base64 decoder not available");

      try
      {
        if (this.decode == null)
          this.decode = this.decoder.getClass().getMethod("decode",new Class[]{String.class});
        
        return (byte[]) this.decode.invoke(this.decoder,base64);
      }
      catch (Throwable t)
      {
        if (t instanceof IOException)
          throw (IOException) t;
        
        throw new IOException(t);
      }
    }
    
    /**
     * @see de.willuhn.util.Base64.Encoder#encode(byte[])
     */
    public String encode(byte[] text)
    {
      if (this.encoder == null)
      {
        Logger.warn("base64 encoder not available");
        return null;
      }

      try
      {
        if (this.encode == null)
          this.encode = this.encoder.getClass().getMethod("encodeToString",new Class[]{byte[].class});

        return (String) this.encode.invoke(this.encoder,text);
      }
      catch (Throwable t)
      {
        Logger.error("unable to encode base64",t);
        return null;
      }
    }
  }
}
