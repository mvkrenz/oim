package edu.iu.grid.oim.model.cert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.cert.ICertificateSigner.Certificate;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;

public class CertificateManager {
	private ICertificateSigner cp;
	public CertificateManager() {
		cp  = new DigicertCertificateSigner();
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
	
	public static java.security.cert.Certificate[]  parsePKCS7(String pkcs7) throws CMSException, CertificateException, IOException {
		//need to strip first and last line (-----BEGIN PKCS7-----, -----END PKCS7-----)
		String []lines = pkcs7.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}
		
		//convert cms to certificate chain
		CMSSignedData cms = new CMSSignedData(Base64.decode(payload));
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
