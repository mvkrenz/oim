package edu.iu.grid.oim.model.cert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.cert.ICertificateSigner.Certificate;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;

public class CertificateManager {
	private ICertificateSigner cp;
	public CertificateManager() {
		String signer = StaticConfig.conf.getProperty("certificate.signer");
		if(signer != null && signer.equals("CILogonCertificateSigner")) {
			cp  = new CILogonCertificateSigner();
		} else {
			cp  = new DigicertCertificateSigner();
		}
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
	public void signHostCertificates(Certificate[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		cp.signHostCertificates(certs, callback);
	}
	
	public ICertificateSigner.Certificate signUserCertificate(String csr, String cn, String email_address) throws CertificateProviderException {
		ICertificateSigner.Certificate cert = cp.signUserCertificate(csr, cn, email_address);
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
	
	//TODO - not yet fully tested
	//TODO - x509 cert only contains a single cert - not the chain that pkcs7 contains
	public static String x509_to_pkcs(String x509) throws CertificateException, CMSException, IOException {

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		
		//load cert
		byte[] cert_b = decodepem(x509);
		InputStream in = new ByteArrayInputStream(cert_b);
		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
		
		/*
		//load intermediate
		String inter_pem = readFileAsString("inter.pem");
		byte[] inter_b = decodepem(inter_pem);
		in = new ByteArrayInputStream(inter_b);
		X509Certificate inter = (X509Certificate)cf.generateCertificate(in);
		*/
		
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
	
	public static java.security.cert.Certificate[] parsePKCS7(String pkcs7) throws CMSException, CertificateException, IOException {
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
		java.security.cert.Certificate[] chain = new java.security.cert.Certificate[collection.size()];
		Iterator itr = collection.iterator(); 
		int i = 0;
	    CertificateFactory cf = CertificateFactory.getInstance("X.509"); 
		while(itr.hasNext()) {
			X509CertificateHolder it = (X509CertificateHolder)itr.next();
			org.bouncycastle.asn1.x509.Certificate c = it.toASN1Structure();
			
			//convert to java.security certificate
		    InputStream is1 = new ByteArrayInputStream(c.getEncoded()); 
			chain[i++] = cf.generateCertificate(is1);
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
	
}
