package edu.iu.grid.oim.model.cert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateBase;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;

public class CertificateManager {
    static Logger log = Logger.getLogger(CertificateManager.class);  
    
	private ICertificateSigner cp;
	public CertificateManager(ICertificateSigner cp) {
		this.cp = cp;
	}
	
	public static CertificateManager Factory(UserContext context, Integer vo_id) {
		//determine the singer from vo_id (if provided)
		String signer = null;
		if(vo_id != null) {
			try {
				VOModel model = new VOModel(context);
				VORecord vo = model.get(vo_id);
				if(vo == null) {
					log.error("Failed to find vo with vo_id:"+vo_id);
				} else {
					signer = vo.certificate_signer;
				}
			} catch (SQLException e) {
				log.error("SQLException while looking for vo with vo_id:"+vo_id, e);
			}
		}	
		
		if(signer == null) {
			log.error("CertificateManager.Factory failed to determine signer from provided vo_id:"+vo_id+" using default signer");
			signer = StaticConfig.conf.getProperty("certificate.signer"); //set to default.
		}
	
		switch(signer) {
		case "CILogonCertificateSigner":
			return new CertificateManager(new CILogonCertificateSigner());
		case "DigicertCertificateSigner":
		default://kiss
			return new CertificateManager(new DigicertCertificateSigner());
		}	
	}
	
	public String getUserDNBase() {
		return cp.getUserDNBase();
	}
	public String getHostDNBase() {
		return cp.getHostDNBase();
	}

	/*
	public ICertificateSigner.Certificate signHostCertificates(X500Name x500) {
		//generate CSR for user
		GenerateCSR gcsr;
		try {
			
			gcsr = new GenerateCSR(x500);
			
			ICertificateSigner.Certificate cert = signHostCertificates(gcsr.getCSR());
			return cert;
			
		} catch (Exception e) {
			System.out.println("Failed to generate CSR");
			return null;
		}
	}
	*/
	
	//use user provided CSR
	public void signHostCertificates(CertificateBase[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		cp.signHostCertificates(certs, callback);
	}
	
	public CertificateBase signUserCertificate(String csr, String cn, String email_address) throws CertificateProviderException {
		CertificateBase cert = cp.signUserCertificate(csr, cn, email_address);
		return cert;
	}
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		cp.revokeUserCertificate(serial_id);
	}
	
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		cp.revokeHostCertificate(serial_id);
	}
	
	private static byte[] decodepem(String pem) {
		String []lines = pem.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}
		return DatatypeConverter.parseBase64Binary(payload);
	}
	/*
	//TODO - not yet fully tested
	//TODO - x509 cert only contains a single cert - not the chain that pkcs7 contains
	public static String x509_to_pkcs(String x509) throws CertificateException, CMSException, IOException {

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		
		//load cert
		byte[] cert_b = decodepem(x509);
		InputStream in = new ByteArrayInputStream(cert_b);
		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
		
		//construct pkcs7 object
		ArrayList<X509Certificate> certs = new ArrayList();
		certs.add(cert);
		//certs.add(inter);
		CertPath cp = cf.generateCertPath(certs);	
		
		//output pkcs7 in PEM
		StringBuffer buf = new StringBuffer();
		buf.append("-----BEGIN PKCS7-----");
		buf.append(new String(Base64.encode(cp.getEncoded("PKCS7"))).trim());
		buf.append("-----END PKCS7-----");
			
		return buf.toString();
	}
	*/
	private static ASN1Primitive toDERObject(byte[] data) throws IOException
	{
	    ByteArrayInputStream inStream = new ByteArrayInputStream(data);
	    ASN1InputStream asnInputStream = new ASN1InputStream(inStream);
	    return asnInputStream.readObject();
	}
	/*
	public static boolean isBasicConstraintsCA(X509Certificate X509Certificate) throws IOException
	{
	    byte[] bytes = X509Certificate.getExtensionValue("2.5.29.19");
        if (bytes != null)
             {
        	BasicConstraints            bc = BasicConstraints.getInstance(ASN1Primitive.fromByteArray(bytes));
        	return bc.isCA();
               }
	    //assume it to be CA
        X509Certificate.getBasicConstraints()
	    return true;
	}
	*/
	
	public static X509Certificate getIssuedX509Cert(ArrayList<Certificate> chain) {
		for(Certificate cert : chain) {
			//System.out.println(x509cert.getBasicConstraints());
			X509Certificate x509cert = (X509Certificate)cert;
			if(isIssuedX509Cert(x509cert)) {
				return x509cert;
			}
		}
		return null;
	}
	public static boolean isIssuedX509Cert(X509Certificate cert) {
		if(cert.getBasicConstraints() == -1) {
			return true;
		}
		return false; //It's CA
	}

	/* Trying to simplify the parsePKCS7, but this attempt didn't work
	public static java.security.cert.Certificate[] parsePKCS7_2(String pkcs7) throws CMSException, CertificateException, IOException {
		InputStream stream = new ByteArrayInputStream(pkcs7.getBytes(StandardCharsets.UTF_8));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ArrayList<java.security.cert.Certificate> list = new ArrayList<java.security.cert.Certificate>();
		while (stream.available() > 0) {
			java.security.cert.Certificate cert = cf.generateCertificate(stream);
			list.add(cert);
		}
		return (java.security.cert.Certificate[]) list.toArray();
	}
	*/
	
	/* -- I can use getIssuedX509Cert to test for basic constraints
	//from http://stackoverflow.com/questions/2409618/how-do-i-decode-a-der-encoded-string-in-java
	private static String getExtensionValue(X509Certificate X509Certificate, String oid) throws IOException
	{
	    String decoded = null;
	    byte[] extensionValue = X509Certificate.getExtensionValue(oid);

	    if (extensionValue != null)
	    {
	        ASN1Primitive derObject = toDERObject(extensionValue);
	        if (derObject instanceof DEROctetString)
	        {
	            DEROctetString derOctetString = (DEROctetString) derObject;

	            derObject = toDERObject(derOctetString.getOctets());
	            if (derObject instanceof DERUTF8String)
	            {
	                DERUTF8String s = DERUTF8String.getInstance(derObject);
	                decoded = s.getString();
	            }

	        }
	    }
	    return decoded;
	}
	*/
	
	public static ArrayList<Certificate> parsePKCS7(String pkcs7) throws CMSException, CertificateException, IOException {
		/*
		//need to strip first and last line (-----BEGIN PKCS7-----, -----END PKCS7-----)
		String []lines = pkcs7.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}
		*/
		byte[] cert_b = decodepem(pkcs7);
		
		//convert cms to certificate chain
		CMSSignedData cms = new CMSSignedData(cert_b);
		Store s = cms.getCertificates();
		Collection collection = s.getMatches(null);
		ArrayList<Certificate> chain = new ArrayList<Certificate>();
		Iterator itr = collection.iterator(); 
		int i = 0;
	    CertificateFactory cf = CertificateFactory.getInstance("X.509"); 
		while(itr.hasNext()) {
			//use bouncycastle lib (toASN1Structure) to convert it to java.security certificate
			X509CertificateHolder it = (X509CertificateHolder)itr.next();
			org.bouncycastle.asn1.x509.Certificate c = it.toASN1Structure();
			//convert to java.security certificate
		    InputStream is1 = new ByteArrayInputStream(c.getEncoded()); 
			Certificate cert = cf.generateCertificate(is1);
			X509Certificate x509cert = (X509Certificate) cert;
			
			/*
			String ca_ext = getExtensionValue((X509Certificate) cert, "2.5.29.19"); //BasicConstraints
			if(ca_ext != null) {
				System.out.println(ca_ext);
			}
			*/
			
			if(isIssuedX509Cert(x509cert)) {
				chain.add(0, cert);//add to the top
			} else {
				chain.add(cert);		
			}
		}
		return chain;
	}
	
	//convert comma delimited DN (RFC1779) to apache format (delimited by /)
	public static String RFC1779_to_ApacheDN(String dn) {
		String tokens[] = dn.split(",");
		String out = StringUtils.join(tokens, "/");
		return "/"+out;
	}
	public static String X500Principal_to_ApacheDN(X500Principal dn) {
		String dn_string = dn.toString();
		String tokens[] = dn_string.split(",");
		StringBuffer out = new StringBuffer();
		for(int i = tokens.length;i != 0;i--) {
			out.append("/"+tokens[i-1].trim());
		}
		return out.toString();
	}
	
	//split 1 big string into strings with specified size delimited by newline
	public static String chunkString(String text, int size) {
	    // Give the list the right capacity to start with. You could use an array
	    // instead if you wanted.
		StringBuffer buf = new StringBuffer();

	    for (int start = 0; start < text.length(); start += size) {
	        buf.append(text.substring(start, Math.min(text.length(), start + size)));
	        buf.append("\n");
	    }
	    return buf.toString();
	}
}
