package edu.iu.grid.oim.model.cert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.ArrayList;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import edu.iu.grid.oim.lib.StaticConfig;

//Uses CILogin OSG CA APIs
//https://docs.google.com/document/d/1c5BaQSTyHEJtOIF66mqrKh52sfaSCBnxTbqPEniMtkE/edit#heading=h.m82hlr8uhzkm

public class CILogonCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(CILogonCertificateSigner.class);  

    private KeyStore ks = null;
    
    public static KeyStore loadTrustStore() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("jks");
        trustStore.load(new FileInputStream(new File("path-to-truststore")), "password".toCharArray());
        return trustStore;
    }
    
    protected static TrustManager[] getTrustManagers() throws Exception {
        KeyStore trustStore = loadTrustStore();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    
    public CILogonCertificateSigner() {
    	//won't this interfare with anything else?
        System.setProperty("javax.net.ssl.keyStore", StaticConfig.conf.getProperty("cilogon.api.user.pkcs12"));
        System.setProperty("javax.net.ssl.keyStorePassword", StaticConfig.conf.getProperty("cilogon.api.user.pkcs12_password"));
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");  
    }
    
	class CILogonCertificateSignerException extends CertificateProviderException {
		public CILogonCertificateSignerException(String msg, Exception e) {
			super("From CILogon: " + msg, e);
		}
		public CILogonCertificateSignerException(String msg) {
			super("From CILogon: " + msg);
		}
	};
	
	public CertificateBase signUserCertificate(String csr, String dn, String email_address) throws CertificateProviderException {
		return requestUserCert(csr, dn, email_address);
	}
	
	//pass csrs, and 
	public void signHostCertificates(CertificateBase[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		
		//cilogin request & sign immediately.. to simulate digicert behavior, let's pretent we've requested first.
		callback.certificateRequested();
		
		//request & approve all
		for(int c = 0; c < certs.length; ++c) {
			CertificateBase cert = certs[c];
			 //don't request if it's already requested
			if(cert.serial != null) continue;
			
			//pull CN
			String cn;
			String csr = cert.csr;
			try {
				PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(Base64.decode(csr));
				X500Name name = pkcs10.getSubject();
				RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
				cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
			} catch (IOException e2) {
				throw new CertificateProviderException("Failed to obtain cn from given csr:" + csr, e2);
			}
			
			//split optional service name (like.. rsv/ce.grid.iu.edu)
			String tokens[] = cn.split("/");
			String service_name = null;
			String thecn = null;
			if(tokens.length == 1) {
				thecn = tokens[0];
			} else if (tokens.length == 2) {
				service_name = tokens[0];
				thecn = tokens[1];
			} else {
				throw new CertificateProviderException("Failed to parse Service Name from CN");
			}
			
			CertificateBase issued_cert = requestHostCert(csr, service_name, thecn);
			log.debug("Requested host certificate. Digicert Request ID:" + issued_cert.serial);
		
			cert.serial = issued_cert.serial;
			cert.certificate = issued_cert.certificate;
			cert.intermediate = issued_cert.intermediate;
			cert.pkcs7 = issued_cert.pkcs7;
			
			callback.certificateSigned(cert, c);
		}
	}
	
	private HttpClient createHttpClient() {
		HttpClient cl = new HttpClient();
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");  
	    return cl;
	}
	
	public CertificateBase requestUserCert(String csr, String dn, String email_address) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://osg.cilogon.org/getusercert");
		
		//need to strip first and last line (-----BEGIN CERTIFICATE REQUEST-----, -----END CERTIFICATE REQUEST-----)
		String []lines = csr.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}

		post.setParameter("email", email_address);
		post.setParameter("username", dn); //TODO - should use just the CN part?
		post.setParameter("cert_request", payload);
		post.setParameter("cert_lifetime", "34128000000"); //TODO - how long is this?		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		try {
			cl.executeMethod(post);
			
			switch(post.getStatusCode()) {
			case 200:
				CertificateBase cert = new CertificateBase();
				StringWriter writer = new StringWriter();
				IOUtils.copy(post.getResponseBodyAsStream(), writer, "UTF-8"); //should use ascii?
				cert.pkcs7 = writer.toString();
				
				//pull some information from the cert for validation purpose
				ArrayList<Certificate> chain = CertificateManager.parsePKCS7(cert.pkcs7);
					
				X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
				cert.notafter = c0.getNotAfter();
				cert.notbefore = c0.getNotBefore();
				cert.intermediate = "NO-INT";
				cert.serial = c0.getSerialNumber().toString();
				return cert;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}		
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (CertificateException e) {
			throw new CILogonCertificateSignerException("Failed to parse certificate", e);
		} catch (CMSException e) {
			throw new CILogonCertificateSignerException("Failed to parse certificate", e);
		}
	}
	
	protected String convertToPem(X509Certificate cert) throws CertificateEncodingException {
		 org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
		 String cert_begin = "-----BEGIN CERTIFICATE-----\n";
		 String end_cert = "-----END CERTIFICATE-----";

		 byte[] derCert = cert.getEncoded();
		 String pemCertPre = new String(encoder.encode(derCert));
		 String pemCert = cert_begin + pemCertPre + end_cert;
		 return pemCert;
	}
	
	private CertificateBase requestHostCert(String csr, String service_name, String cn) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post;
		if(service_name == null) {
			post = new PostMethod("https://osg.cilogon.org/gethostcert");
		} else {
			post = new PostMethod("https://osg.cilogon.org/getservicecert");
			post.setParameter("srvname", service_name);
		}
		post.setParameter("email", "noemail@example.com");
		post.setParameter("hostname", cn);
		post.setParameter("cert_request", csr);
		post.setParameter("cert_lifetime", "34128000000");
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		try {
			cl.executeMethod(post);
			
			switch(post.getStatusCode()) {
			case 200:
				CertificateBase cert = new CertificateBase();
				StringWriter writer = new StringWriter();
				IOUtils.copy(post.getResponseBodyAsStream(), writer, "UTF-8"); //should use ascii?
				cert.pkcs7 = writer.toString();
								
				//parse certificate and populate following fields
				ArrayList<Certificate> chain = CertificateManager.parsePKCS7(cert.pkcs7);
				
				X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
				cert.notafter = c0.getNotAfter();
				cert.notbefore = c0.getNotBefore();
				cert.intermediate = "NO-INT"; //TODO - no cilogon doesn't have intermediate - maybe put root CA?
				cert.serial = c0.getSerialNumber().toString();	        
				cert.certificate = convertToPem(c0);
								
				return cert;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}	
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (CertificateException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (CMSException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		}
	}
	
	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://osg.cilogon.org/revoke");
		post.setParameter("serial",serial_id);
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		try {
			cl.executeMethod(post);
			switch(post.getStatusCode()) {
			case 200:
				log.debug("Successfully made request for revocation for cilogon with serial id:"+serial_id);
				return;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}	
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		}
	}

	@Override
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		//cilogon revoke handles both user/host cert revoke
		revokeHostCertificate(serial_id);
	}

	@Override
	public String getUserDNBase() {
		return StaticConfig.conf.getProperty("cilogon.user_dn_base");
	}

	@Override
	public String getHostDNBase() {
		return StaticConfig.conf.getProperty("cilogon.host_dn_base");
	}
}
