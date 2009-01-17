/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/security/Signature.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/01/17 00:25:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.util.Queue;

/**
 * Hilfsklasse mit statischen Methoden zur Erzeugung und Verifizierung von Signaturen.
 * Die Signaturen basieren auf SHA1-Checksummen der Daten und werden mittels 
 * Public-/Privatekey geschuetzt.
 * 
 * Alternativ kann man die Signaturen auch mit den folgenden beiden OpenSSL-Befehlen
 * erstellen/verifizieren. Die von dieser Java-Klasse erzeugten Signaturen sind
 * kompatibel zu denen von OpenSSL.
 * 
 * <pre>
 * openssl dgst -sha1 -sign ${privatekey-file} -out ${signature-file} &lt; {file}
 * openssl dgst -sha1 -verify ${publickey-file} -signature ${signature-file} {file}
 * </pre>
 */
public class Signature
{
  /**
   * Der zu verwendende Algorithmus.
   */
  private final static String ALG = "SHA1withRSA";

  /**
   * private.
   */
  private Signature()
  {
  }

  /**
   * Erzeugt eine Signatur fuer die uebergebenen Daten.
   * Die Signatur wird mit dem Algorithmus "SHA1withRSA" erstellt.
   * @param data die zu signierenden Daten.
   * @param key der Private-Key zum Signieren.
   * @return die Signatur.
   * @throws IOException wenn ein Fehler beim Lesen der Daten auftrat.
   * @throws GeneralSecurityException wenn ein Fehler beim Signieren auftrat.
   */
  public static byte[] sign(InputStream data, PrivateKey key) throws GeneralSecurityException, IOException
	{
    java.security.Signature sig = java.security.Signature.getInstance(ALG);
    sig.initSign(key);

    byte[] buf = new byte[1024];
    int read = 0;
    while ((read = data.read(buf)) != -1)
    {
      sig.update(buf,0,read);
    }
    return sig.sign();
	}
  
  /**
   * Prueft die Signatur fuer die uebergebenen Daten.
   * Die Signatur wird mit dem Algorithmus "SHA1withRSA" geprueft.
   * @param data die zu signierenden Daten.
   * @param key der Public-Key zum Pruefen.
   * @param signature die Signatur.
   * @return true, wenn die Signatur ok ist.
   * @throws IOException wenn ein Fehler beim Lesen der Daten auftrat.
   * @throws GeneralSecurityException wenn ein Fehler beim Verifizieren der Signatur auftrat.
   */
  public static boolean verifiy(InputStream data, PublicKey key, byte[] signature) throws GeneralSecurityException, IOException
  {
    java.security.Signature sig = java.security.Signature.getInstance(ALG);
    sig.initVerify(key);

    byte[] buf = new byte[1024];
    int read = 0;
    while ((read = data.read(buf)) != -1)
    {
      sig.update(buf,0,read);
    }
    return sig.verify(signature);
  }
  
  /**
   * Main-Methode, um das Signieren und Verifzieren von der Kommandozeile aus durchfuehren zu koennen.
   * @param args
   * @throws Exception
   */
  public final static void main(String[] args) throws Exception
  {
    if (args == null || args.length < 2)
      usage();

    ////////////////////////////////////////////////////////////////////////////
    // Queue zum Abarbeiten der Parameter
    Queue queue = new Queue(100);
    for (String s:args)
      queue.push(s.trim());

    // Das Kommando ist der erste Parameter.
    String command = (String) queue.pop();
    if (!command.equals("sign") && !command.equals("verify"))
      usage();

    // die restlichen pappen wir in eine Map
    Map<String,String> options = new HashMap<String,String>();
    String s = null;
    while (queue.size() > 0)
    {
      String curr = (String) queue.pop();
      if (curr == null || curr.length() == 0)
        continue;
      if (curr.startsWith("-"))
      {
        s = curr.substring(1);
        continue;
      }
      
      options.put(s,curr);
    }
    //
    ////////////////////////////////////////////////////////////////////////////
   
    InputStream is1 = null;
    InputStream is2 = null;
    InputStream is3 = null;
    OutputStream os = null;

    try
    {
      ////////////////////////////////////////////////////////////////////////////
      // 1. Load keystore
      String keyfile = options.get("keystore");
      if (keyfile == null)
        error("no keystore file given");
      
      String alias = options.get("alias");
      if (alias == null)
        alias = "default";
      
      String storepass = options.get("storepass");
      if (storepass == null)
        storepass = "";

      is1 = new BufferedInputStream(new FileInputStream(keyfile));
      
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(is1,storepass.toCharArray());
      ////////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////////
      // 2. Load file
      String file = options.get("file");
      if (file == null)
        error("no file to sign/verify given");
      is2 = new BufferedInputStream(new FileInputStream(file));
      ////////////////////////////////////////////////////////////////////////////
      

      String sig = options.get("sig");
      if (sig == null)
        sig = file + ".sha1";
      
      if (command.equals("sign"))
      {
        ////////////////////////////////////////////////////////////////////////////
        // 3. a) sign
        String keypass = options.get("keypass");
        if (keypass == null)
          keypass = "";
        
        PrivateKey key = (PrivateKey) keyStore.getKey(alias,keypass.toCharArray());
        if (key == null)
          error("key for alias \"" + alias + "\" not found");
        os = new BufferedOutputStream(new FileOutputStream(sig));
        os.write(Signature.sign(is2,key));
        System.out.println("signature created");
        ////////////////////////////////////////////////////////////////////////////
      }
      else
      {
        ////////////////////////////////////////////////////////////////////////////
        // 3. b) verify
        Certificate cert = keyStore.getCertificate(alias);
        if (cert == null)
          error("certificate for alias \"" + alias + "\" not found");
        
        is3 = new BufferedInputStream(new FileInputStream(sig));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int read = 0;
        while ((read = is3.read(buf)) != -1)
          bos.write(buf,0,read);
        boolean b = Signature.verifiy(is2,cert.getPublicKey(),bos.toByteArray());
        System.out.println("verification " + (b ? "OK" : "FAILED"));
        System.exit(b ? 0 : 1);
        ////////////////////////////////////////////////////////////////////////////
      }
    }
    finally
    {
      if (is1 != null)
      {
        try { is1.close(); } catch (Exception e) {/*useless*/}
      }
      if (is2 != null)
      {
        try { is2.close(); } catch (Exception e) {/*useless*/}
      }
      if (is3 != null)
      {
        try { is3.close(); } catch (Exception e) {/*useless*/}
      }
      if (os != null)
      {
        try { os.close(); } catch (Exception e) {/*useless*/}
      }
    }
  }
  
  /**
   * Gibt eine Fehlermeldung auf STDERR aus und beendet das Programm mit dem
   * Return-Code 3.
   * @param message
   */
  private static void error(String message)
  {
    System.err.println(message);
    System.exit(3);
  }
  
  /**
   * Gibt die Kommandozeilen-Optionen auf STDERR aus und beendet das Programm mit dem
   * Return-Code 2.
   */
  private static void usage()
  {
    PrintStream s = System.err;
    
    s.println("usage: java -cp de_willuhn_util.jar " + Signature.class.getName() + " [sign/verify] [options]\n");
    s.println("  sign  : create a new signature for a file");
    s.println("  verify: check the signature of a file\n");
    s.println("  options:");
    s.println("    -keystore <keystore>     path to keystore file (JKS format)");
    s.println("    -storepass <password>    password of keystore file");
    s.println("    -alias <alias>           alias name of keystore entry (contains public and/or private key)");
    s.println("    -keypass <password>      password of keystore entry (only needed for signing)");
    s.println("    -file <file>             the file to sign/verify");
    s.println("    -sig <file>              the signature file to create/verify");
    s.println("\nexamples:\n");
    s.println("java -cp de_willuhn_util.jar " + Signature.class.getName() + " sign -keystore my.keystore -storepass changeit -alias default -keypass foobar -file de_willuhn_util.jar -sig de_willuhn_util.jar.sig");
    s.println("java -cp de_willuhn_util.jar " + Signature.class.getName() + " verify -keystore my.keystore -storepass changeit -alias default -keypass foobar -file de_willuhn_util.jar -sig de_willuhn_util.jar.sig");
    s.println("");
    s.println("hint: a JKS keystore can be created using the SUN keytool program");
    s.println("\n");
    System.exit(2);
  }
  
}


/**********************************************************************
 * $Log: Signature.java,v $
 * Revision 1.1  2009/01/17 00:25:38  willuhn
 * @N Programm zum Erstellen/Verifizieren von OpenSSL-kompatiblen Signaturen mit SHA1-Digest
 * @N Java 1.5 compatibility
 *
 **********************************************************************/