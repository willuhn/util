/**********************************************************************
 * $Source: /cvsroot/jameica/util/src/de/willuhn/security/Attic/Wallet.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/11 19:01:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.logging.Logger;

/**
 * Ein schweizer Taschenmesser wenn es darum geht, mit Schluesseln
 * und Zertifikaten zu hantieren. Es kann als eine Art Brieftasche
 * verwendet werden, in dem zu schuetzende Daten abgelegt und
 * abgerufen werden koennen. Das koennen Dokumente aber auch Passworte
 * sein.
 * @author willuhn
 */
public class Wallet
{
	static
	{
		java.security.Security.addProvider(new BouncyCastleProvider());
	}

  private final static String DEFAULT_ALIAS = "wallet";

  private String walletfilename   = "default.wallet";
  private String password         = null;

	private KeyStore keystore						= null;
	private X509Certificate certificate = null;
	private PrivateKey privateKey 			= null;
	private PublicKey publicKey					= null;

	private SSLContext sslContext				= null;

  /**
   * Laedt das Wallet mit dem anegegebenen Dateinamen.
   * Existiert das Wallet noch nicht, wird es automatisch erstellt.
   * @param walletfilename Pfad- und Dateiname des Wallet.
   * @param password Passwort, mit dem das Wallet verschluesselt ist / verschluesselt werden soll.
   * @throws Exception
   */
  public Wallet(String walletfilename, String password) throws Exception
  {
    if (walletfilename == null || walletfilename.length() == 0)
    {
      Logger.warn("no wallet file given, using file \"" + walletfilename + "\" in current directory");
    }
    else
      this.walletfilename = walletfilename;

    this.password = password;
    if (this.password == null || this.password.length() == 0)
      throw new Exception("no password given for wallet");
    
    init();
  }


	/**
	 * Prueft die Zertifikate und erstellt sie bei Bedarf.
   * @throws Exception
   */
  private synchronized void init() throws Exception
	{

		Logger.info("init wallet");

		File wallet = getWalletFile();
		if (wallet.exists() && wallet.canRead())
    {
      Logger.info("loading wallet");
      getCertificate();
      Logger.info("wallet loaded successfully");
      return;
    }

		Logger.info("no wallet found, creating " + wallet.getAbsolutePath());

		////////////////////////////////////////////////////////////////////////////
		// Keys erstellen
		Logger.info("generating rsa keypair");
		KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
		KeyPair keypair = kp.generateKeyPair();

		this.privateKey = keypair.getPrivate();
		this.publicKey 	= keypair.getPublic();
		//
		////////////////////////////////////////////////////////////////////////////


		////////////////////////////////////////////////////////////////////////////
		// Zertifikat erstellen
		Logger.info("generating selfsigned x.509 certificate");
		Hashtable attributes = new Hashtable();
		attributes.put(X509Name.CN,InetAddress.getLocalHost().getCanonicalHostName());
		X509Name user   = new X509Name(attributes);
		X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*4)));
		generator.setNotBefore(new Date());
		generator.setIssuerDN(user);
		generator.setPublicKey(this.publicKey);
		generator.setSerialNumber(new BigInteger("1"));
		generator.setSignatureAlgorithm("MD5WITHRSA");

		this.certificate = generator.generateX509Certificate(this.privateKey);
		//
		////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// Keystore erstellen
		Logger.info("creating keystore");
		this.keystore = KeyStore.getInstance("PKCS12",BouncyCastleProvider.PROVIDER_NAME);
		this.keystore.load(null,password.toCharArray());

		Logger.info("adding private key and x.509 certifcate");
		this.keystore.setKeyEntry(DEFAULT_ALIAS,this.privateKey,
															password.toCharArray(),
															new X509Certificate[]{this.certificate});

		Logger.info("storing keystore: " + wallet.getAbsolutePath());
		OutputStream storeOut = null;
    try
    {
      storeOut = new FileOutputStream(wallet);
      this.keystore.store(storeOut,password.toCharArray());
    }
    finally
    {
      storeOut.close();
    }
		//
		////////////////////////////////////////////////////////////////////////////

    Logger.info("wallet created successfully");
	}
	
	/**
	 * Liefert die Datei mit dem Wallet.
	 * @return Wallet.
	 */
	private File getWalletFile()
	{
		return new File(this.walletfilename);
	}

  /**
	 * Liefert den PublicKey des Wallet.
   * @return Public-Key.
   * @throws Exception
   */
  public synchronized PublicKey getPublicKey()
  	throws Exception
	{
		if (this.publicKey != null)
			return this.publicKey;

		return getCertificate().getPublicKey();
	}

  /**
	 * Liefert den PrivateKey des Wallet.
	 * @return Private-Key.
   * @throws Exception
	 */
	public synchronized PrivateKey getPrivateKey()
		throws Exception
	{
		if (this.privateKey != null)
			return this.privateKey;

		this.privateKey = (PrivateKey) getKeyStore().getKey(DEFAULT_ALIAS,
                                                        password.toCharArray());
		return this.privateKey;
	}

  /**
	 * Liefert das X.509-Zertifikat des Wallet.
   * @return X.509-Zertifikat.
   * @throws Exception
   */
  public synchronized X509Certificate getCertificate() throws Exception
	{
		if (this.certificate != null)
			return this.certificate;

		this.certificate = (X509Certificate) getKeyStore().getCertificate(DEFAULT_ALIAS);
		return this.certificate;
	}

	/**
	 * Liefert den Keystore mit dem Zertifikat.
   * @return Keystore
   * @throws Exception
   */
  private synchronized KeyStore getKeyStore() throws Exception
	{
		if (keystore != null)
			return keystore;

		InputStream is = null;
		try
		{
			File f = getWalletFile();
			Logger.info("reading wallet from file " + f.getAbsolutePath());
			is = new FileInputStream(f);

			Logger.info("init ssl provider");
			this.keystore = KeyStore.getInstance("PKCS12",BouncyCastleProvider.PROVIDER_NAME);

			Logger.info("reading keys");
			this.keystore.load(is,password.toCharArray());
			return this.keystore;
		}
		finally
		{
			is.close();
		}
	}
	
	/**
	 * Liefert einen fertig konfigurierten SSLContext.
   * @return SSLContect.
   * @throws Exception
   */
  public SSLContext getSSLContext() throws Exception
	{
		if (sslContext != null)
			return sslContext;

		Logger.info("init ssl context");
		this.sslContext = SSLContext.getInstance("SSL");

		Logger.info("init SunX509 key manager");
		KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(this.getKeyStore(),password.toCharArray());

		Logger.info("init SunX509 trust manager");
		TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(this.getKeyStore());
				
		this.sslContext.init(keyManagerFactory.getKeyManagers(),
												 trustManagerFactory.getTrustManagers(),null);
		return this.sslContext;
	}


  public static void main(String[] args) throws Exception
  {
    if (args == null || args.length != 2)
    {
      usage();
    }
    new Wallet(args[0],args[1]);
  }

  private static void usage()
  {
    System.err.println("\njava " + Wallet.class.getName() + " <wallet filename> <wallet password>\n");
    System.err.println("   <wallet filename>  path and filename of your wallet");
    System.err.println("   <wallet password>  password of your wallet");
    System.exit(1);
  }

}


/**********************************************************************
 * $Log: Wallet.java,v $
 * Revision 1.1  2005/01/11 19:01:26  willuhn
 * @N added security.Wallet
 *
 **********************************************************************/