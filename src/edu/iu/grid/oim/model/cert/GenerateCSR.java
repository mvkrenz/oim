package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.OutputStream;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;


//http://www.journaldev.com/223/generating-a-certificate-signing-request-using-java-api
public class GenerateCSR {
	
    static Logger log = Logger.getLogger(GenerateCSR.class); 
    
    private SecureRandom random = null;
    private KeyPair keypair = null;
    private String csr;
   
    
    //sse http://grid-dk.googlecode.com/svn/trunk/SingleSignOn/src/ConfusaSLCSApp.java
    
    public GenerateCSR(X500Name name) throws NoSuchAlgorithmException, OperatorCreationException, IOException {

    	random = new SecureRandom();
    	
    	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, random); //keep it small for test
        keypair = keyGen.genKeyPair();

        //It doesn't matter if I use sha1 or sha256.. DigiCert will return certificate
        //in sha1
        JcaContentSignerBuilder signer_builder = new JcaContentSignerBuilder("SHA1withRSA"); 
        //JcaContentSignerBuilder signer_builder = new JcaContentSignerBuilder("SHA256withRSA"); 
        signer_builder.setSecureRandom(random);
        ContentSigner signer = signer_builder.build(keypair.getPrivate());
        
        JcaPKCS10CertificationRequestBuilder request_builder = new JcaPKCS10CertificationRequestBuilder(name, keypair.getPublic());
        PKCS10CertificationRequest request = request_builder.build(signer);

        csr = "-----BEGIN CERTIFICATE REQUEST-----\n";
        csr += new String(Base64.encodeBase64Chunked(request.getEncoded()));
        csr += "-----END CERTIFICATE REQUEST-----";
        
     
        /*
    	// write the CSR to a string
		StringWriter strWrt = new StringWriter();
		PEMWriter pemWrt = new PEMWriter(strWrt);
		pemWrt.writeObject(request.getSignature());
		pemWrt.close();
		csr = strWrt.toString();
       */
        /*
        if (ps != null)
            ps.close();
        if (bs != null)
            bs.close();
            */
    }
    
    
    public String getCSR() { 
    	return csr;
    }
    /*
 
    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey)keypair.getPublic();
    }
 
    */
 
  
    public PrivateKey getPrivateKey() {
    	return keypair.getPrivate();
    }
   
    /*
    //http://stackoverflow.com/questions/5127379/how-to-generate-a-rsa-keypair-with-a-privatekey-encrypted-with-password
    public byte[] getEncryptedPrivateKey(String password) throws Exception {
    	byte[] encodedprivkey = keypair.getPrivate().getEncoded();

    	// We must use a PasswordBasedEncryption algorithm in order to encrypt the private key, 
    	// you may use any common algorithm supported by openssl, 
    	// you can check them in the openssl documentation http://www.openssl.org/docs/apps/pkcs8.html
    	//String MYPBEALG = "PBEWithSHA1AndDESede";
    	String MYPBEALG = "PBEWithSHA2AndDESede";
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
    	return encinfo.getEncoded();
    }
 	*/
    
    /*
	private static String GetHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	*/
    /*
	public void saveSSHPublicKey(FileWriter fout) throws IOException {
		RSAPublicKey publickey = (RSAPublicKey)keypair.getPublic();

		//part 1 -- header
        fout.write("ssh-rsa ");
		
        //part 1 -- internal header
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] sshrsa = new byte[] {0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a'};
		out.write(sshrsa);
		
		// part 2 -- Encode the public exponent 
		BigInteger e = publickey.getPublicExponent();
		byte[] data = e.toByteArray();
		encodeUInt32(data.length, out);
		out.write(data);
		
		// part 3 -- Encode the modulus
		BigInteger m = publickey.getModulus();
		data = m.toByteArray();
		encodeUInt32(data.length, out);
		out.write(data);
		
		fout.write(Base64.encode(out.toByteArray()));
	}
	*/
	

	public void encodeUInt32(int value, OutputStream out) throws IOException
	{
		byte[] tmp = new byte[4];
		tmp[0] = (byte)((value >>> 24) & 0xff);
		tmp[1] = (byte)((value >>> 16) & 0xff);
		tmp[2] = (byte)((value >>> 8) & 0xff);
		tmp[3] = (byte)(value & 0xff);
		out.write(tmp);
	}
	
   
	/*
	private void saveSSHPrivateKey(FileWriter fout) throws IOException {
        RSAPrivateKey privatekey = (RSAPrivateKey)keypair.getPrivate();
		fout.write(Base64.encode(privatekey.getEncoded()));
	}
    */
 
	/*
	public void saveDER(String path) throws IOException {
		FileOutputStream ospvt = new FileOutputStream(path + "/private.der");
		try {
		  ospvt.write(keypair.getPrivate().getEncoded());
		  ospvt.flush();
		} finally {
		  ospvt.close();
		}
		FileOutputStream ospub = new FileOutputStream(path + "/public.der");
		try {
		  ospub.write(keypair.getPublic().getEncoded());
		  ospub.flush();
		} finally {
		  ospub.close();
		}
	}
	*/
	public void saveCSR(String path) throws IOException {
		
		//RSAPublicKey publickey = (RSAPublicKey)keypair.getPublic();
		//RSAPrivateKey privatekey = (RSAPrivateKey)keypair.getPrivate();

		/*
		FileWriter fout = new FileWriter(path + "/id_rsa.pub");
		saveSSHPublicKey(fout);
		fout.close();
        */
        
		/*
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keypair.getPublic().getEncoded());
		fout = new FileWriter(path + "/public.key");
		BufferedWriter out = new BufferedWriter(fout);
		out.write(encoder.encode(x509EncodedKeySpec.getEncoded()));
		out.close();
		*/
 
		/*
		// Store (unencrypted) OpenSSH Private Key
		fout = new FileWriter(path + "/id_rsa");
		fout.write("-----BEGIN RSA PRIVATE KEY-----\n");
		fout.write(encoder64.encode(privatekey.getEncoded()));
		fout.write("\n-----END RSA PRIVATE KEY-----\n");
		fout.close();
		*/
		
		/*
		//save csr
		FileWriter fout = new FileWriter(path + "/request.csr");
		fout.write(new String(csr));
		fout.close();
		*/
	}
	
    public static void main(String[] args) throws Exception {
    	  
    	//generate public/private keys
    	GenerateCSR gcsr = 
    		new GenerateCSR(
    			new X500Name("CN=\"Soichi Hayashi/emailAddress=hayashis@indiana.edu\", OU=PKITesting, O=OSG, L=Bloomington, ST=IN, C=United States"));
    
    	//gcsr.saveDER("c:/trash");
    	//gcsr.saveCSR("c:/trash");
    	/*
        System.out.println(new String(gcsr.getCSR()));
        
        BASE64Encoder encoder = new BASE64Encoder();
        
		System.out.println("Public Key: " + encoder.encode(gcsr.getPublicKey().getEncoded()));
		System.out.println("Private Key: " + encoder.encode(gcsr.getPrivateKey().getEncoded()));
		gcsr.saveKeypair("c:\\trash");
		gcsr.saveCSR("c:\\trash");
		*/
    	
    }
}

