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
import java.util.LinkedHashMap;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateBase;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class CertificateManager {
    static Logger log = Logger.getLogger(CertificateManager.class);  
    
	private ICertificateSigner cp;
	public CertificateManager(ICertificateSigner cp) {
		this.cp = cp;
	}
	
	public enum Signers {
		CILogon, Digicert
	}
	
	public static Signers getSignerFromID(int id) {
		Signers[] values = Signers.values();
		if(id >= values.length) new IllegalArgumentException();
		return values[id];
	}
	
	public static LinkedHashMap<Integer, String> getSigners() 
	{
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		Signers[] values = Signers.values();
		for(int i = 0;i < values.length; ++i) {
			keyvalues.put(i,  values[i].name());
		}
		return keyvalues;
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
		case "CILogon":
			return new CertificateManager(new CILogonCertificateSigner());
		case "Digicert":
		default://kiss
			return new CertificateManager(new DigicertCertificateSigner());
		}	
	}
	
	
	public static CertificateManager Factory(DNRecord dnrecord) {
		//determine the singer from vo_id (if provided)
		String dn = dnrecord.dn_string;
		String signer = null;
		if(dn != null) {
			try {
				if(dn.contains("DigiCert-Grid")) {
					signer = "Digicert";
				} else {
					signer = "CILogon";
				}
			} catch (Exception e) {
				log.error("Exception while looking for dc with dn:"+dn, e);
			}
		}	
		
		if(signer == null) {
			log.error("CertificateManager.Factory failed to determine signer from provided dn"+ dn +" using default signer");
			signer = StaticConfig.conf.getProperty("certificate.signer"); //set to default.
		}
	
		switch(signer) {
		case "CILogon":
			return new CertificateManager(new CILogonCertificateSigner());
		case "Digicert":
		default://kiss
			return new CertificateManager(new DigicertCertificateSigner());
		}	
	}
	
	public static CertificateManager Factory(String dn) {
		//determine the singer from vo_id (if provided)
	
		String signer = null;
		if(dn != null) {
			try {
				if(dn.contains("DigiCert-Grid")) {
					signer = "Digicert";
				} else {
					signer = "CILogon";
				}
			} catch (Exception e) {
				log.error("Exception while looking for dc with dn:"+dn, e);
			}
		}	
		
		if(signer == null) {
			log.error("CertificateManager.Factory failed to determine signer from provided dn"+ dn +" using default signer");
			signer = StaticConfig.conf.getProperty("certificate.signer"); //set to default.
		}
	
		switch(signer) {
		case "CILogon":
			return new CertificateManager(new CILogonCertificateSigner());
		case "Digicert":
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

	//use user provided CSR
	public void signHostCertificates(CertificateBase[] certs, IHostCertificatesCallBack callback, String email_address) throws CertificateProviderException {
		cp.signHostCertificates(certs, callback, email_address);
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

	private static ASN1Primitive toDERObject(byte[] data) throws IOException
	{
	    ByteArrayInputStream inStream = new ByteArrayInputStream(data);
	    ASN1InputStream asnInputStream = new ASN1InputStream(inStream);
	    return asnInputStream.readObject();
	}

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
	
	/*
	//just return the first non - issued cert
	public static X509Certificate getChain(ArrayList<Certificate> chain) {
		for(Certificate cert : chain) {
			X509Certificate x509cert = (X509Certificate)cert;
			if(!isIssuedX509Cert(x509cert)) {
				return x509cert;
			}
		}
		return null;
	}
	*/
	
	public static boolean isIssuedX509Cert(X509Certificate cert) {
		if(cert.getBasicConstraints() == -1) {
			return true;
		}
		return false; //It's CA
	}

	public static ArrayList<Certificate> parsePKCS7(String pkcs7) throws CMSException, CertificateException, IOException {
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
	public static PKCS10CertificationRequest parseCSR(String csr_string) throws IOException {
		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));
		return csr;
	}
	
    public static String pullCNFromCSR(PKCS10CertificationRequest csr) throws CertificateRequestException {
		//pull CN from pkcs10
		X500Name name;
		RDN[] cn_rdn;
		try {
			name = csr.getSubject();
			cn_rdn = name.getRDNs(BCStyle.CN);
		} catch(Exception e) {
			throw new CertificateRequestException("Failed to decode CSR", e);
		}
		
		if(cn_rdn.length != 1) {
			throw new CertificateRequestException("Please specify exactly one CN containing the hostname. You have provided DN: " + name.toString());
		}
		String cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
    	return cn;
    }
    
    
    public static ArrayList<String> pullSANFromCSR(PKCS10CertificationRequest pkcs10) throws CertificateRequestException {
    	ArrayList<String> sans = new ArrayList<String>();
    	
		Attribute[] attrs = pkcs10.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
		for(Attribute attr : attrs) {
			Extensions extensions = Extensions.getInstance(attr.getAttrValues().getObjectAt(0));
			GeneralNames gns = GeneralNames.fromExtensions(extensions,Extension.subjectAlternativeName);
	        GeneralName[] names = gns.getNames();
	        for(GeneralName name : names) {
	        	//name.getTagNo() -- check for GeneralName.dNSName?
	        	sans.add(name.getName().toString());
	        }
	        /*
	         for(int k=0; k < names.length; k++) {
	             String title = "";
	             if(names[k].getTagNo() == GeneralName.dNSName) {
	                 title = "dNSName";
	             }
	             else if(names[k].getTagNo() == GeneralName.iPAddress) {
	                 title = "iPAddress";
	                 names[k].toASN1Object();
	             }
	             else if(names[k].getTagNo() == GeneralName.otherName) {
	                 title = "otherName";
	             }
	             System.out.println(title + ": "+ names[k].getName());
	         }
	         */
		}
    	return sans;
    }  
}
