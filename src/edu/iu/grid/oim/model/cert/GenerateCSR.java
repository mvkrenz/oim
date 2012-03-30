package edu.iu.grid.oim.model.cert;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.LogModel;
 
import sun.security.pkcs.PKCS10;
import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;
 
//http://www.journaldev.com/223/generating-a-certificate-signing-request-using-java-api
public class GenerateCSR {
	
    static Logger log = Logger.getLogger(GenerateCSR.class); 
    
    private KeyPair keypair = null;
    private KeyPairGenerator keyGen = null;
    private byte[] csr;
    
    private SecureRandom random = new SecureRandom();
    
    public GenerateCSR(X500Name x500Name) throws Exception {
    	//generate public/private key pair
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(2048, random);
        keypair = keyGen.generateKeyPair();

        // generate PKCS10 certificate request (CSR)
        PKCS10 pkcs10 = new PKCS10(keypair.getPublic());
        Signature signature = Signature.getInstance("MD5WithRSA");
        signature.initSign(keypair.getPrivate());
        
        pkcs10.encodeAndSign(new X500Signer(signature, x500Name));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);
        pkcs10.print(ps);
        csr = bs.toByteArray();
       
        /*
        if (ps != null)
            ps.close();
        if (bs != null)
            bs.close();
            */
    }
    
    public byte[] getCSR() { 
    	return csr;
    }
 
    public PublicKey getPublicKey() {
        return keypair.getPublic();
    }
 
    public PrivateKey getPrivateKey() {
        return keypair.getPrivate();
    }
    
    //http://stackoverflow.com/questions/5127379/how-to-generate-a-rsa-keypair-with-a-privatekey-encrypted-with-password
    public byte[] getEncryptedPrivateKey(String password) throws Exception {
    	byte[] encodedprivkey = keypair.getPrivate().getEncoded();

    	// We must use a PasswordBasedEncryption algorithm in order to encrypt the private key, 
    	// you may use any common algorithm supported by openssl, 
    	// you can check them in the openssl documentation http://www.openssl.org/docs/apps/pkcs8.html
    	String MYPBEALG = "PBEWithSHA1AndDESede";
    	int count = 20;// hash iteration count
    	
    	//generate salt
    	byte[] salt = new byte[8];
    	random.nextBytes(salt);
    	
    	// Create PBE parameter set
    	PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
    	PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
    	SecretKeyFactory keyFac = SecretKeyFactory.getInstance(MYPBEALG);
    	SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    	Cipher pbeCipher = Cipher.getInstance(MYPBEALG);
    	pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

    	// Encrypt the encoded Private Key with the PBE key
    	byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);
    	
    	// Now construct  PKCS #8 EncryptedPrivateKeyInfo object
    	AlgorithmParameters algparms = AlgorithmParameters.getInstance(MYPBEALG);
    	algparms.init(pbeParamSpec);
    	EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);
    	
    	// and here we have it! a DER encoded PKCS#8 encrypted key!
    	return encinfo.getEncoded();
  
    	
    }
 
	private static String GetHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
    
	public void saveKeypair(String path) throws IOException {
		
        BASE64Encoder encoder = new BASE64Encoder();

		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keypair.getPublic().getEncoded());
		FileWriter fout = new FileWriter(path + "/public.key");
		BufferedWriter out = new BufferedWriter(fout);
		out.write(encoder.encode(x509EncodedKeySpec.getEncoded()));
		out.close();
 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keypair.getPrivate().getEncoded());
		fout = new FileWriter(path + "/private.key");
		out = new BufferedWriter(fout);
		out.write(encoder.encode(pkcs8EncodedKeySpec.getEncoded()));
		out.close();
	}
	
	public void saveCSR(String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path + "/the.csr");
		fos.write(getCSR());
		fos.close();
	}
	
    public static void main(String[] args) throws Exception {
    	  
    	//generate public/private keys
    	GenerateCSR gcsr = new GenerateCSR(new X500Name(
        		"journaldev.com <http://www.journaldev.com>", //CN common name
        		"Java", //Organization unit
        		"JournalDev", //Organization name
        		"Cupertino", "California", "USA" //location, state, country
        ));
    
        System.out.println(new String(gcsr.getCSR()));
        
        BASE64Encoder encoder = new BASE64Encoder();
        
		System.out.println("Public Key: " + encoder.encode(gcsr.getPublicKey().getEncoded()));
		System.out.println("Private Key: " + encoder.encode(gcsr.getPrivateKey().getEncoded()));
		gcsr.saveKeypair("c:\\trash");
		gcsr.saveCSR("c:\\trash");
    }
}


/*

Create CSR 
> openssl req -out CSR.csr -new -newkey rsa:2048 -nodes -keyout privateKey.key

decode content of CSR.csr
> openssl req -in CSR.csr -noout -text

decode content of privateKey.key
> openssl rsa -in privateKey.key -noout -text



 * 
 */