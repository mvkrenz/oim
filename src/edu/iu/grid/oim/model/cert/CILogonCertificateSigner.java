package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.security.cert.Certificate;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

import org.json.*;

//Uses CILogin OSG CA APIs
//https://docs.google.com/document/d/1c5BaQSTyHEJtOIF66mqrKh52sfaSCBnxTbqPEniMtkE/edit#heading=h.m82hlr8uhzkm

public class CILogonCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(CILogonCertificateSigner.class);  

    private KeyStore ks = null;
    public CILogonCertificateSigner() {
    	//won't this interfare with something else?
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
	/*
	@Override
    public X500NameBuilder generateX500NameBuilder() {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);

        //DigiCert overrides the DN, so none of these matters - except CN which is used to send common_name parameter
        //We are creating this so that we can create private key
        x500NameBld.addRDN(BCStyle.O, "org");
        x500NameBld.addRDN(BCStyle.DC, "Open Science Grid");
        if(StaticConfig.isDebug()) {
        	//let's assume debug means we are using digicert pilot
        	x500NameBld.addRDN(BCStyle.O, "Test");
        } else {
        	x500NameBld.addRDN(BCStyle.O, "Production");
        }
        x500NameBld.addRDN(BCStyle.OU, "People");   
        
        return x500NameBld;
        //return x500NameBld.build();
    }
    */
	
	public CertificateBase signUserCertificate(String csr, String cn, String email_address) throws CertificateProviderException {
		return requestUserCert(csr, cn, email_address);
	}
	
	//pass csrs, and 
	public void signHostCertificates(CertificateBase[] certs, IHostCertificatesCallBack callback, String email_address) throws CertificateProviderException {
		
		//cilogin request & sign immediately.. to simulate digicert behavior, let's pretent we've requested first.
		callback.certificateRequested();
		
		//request & approve all
		for(int c = 0; c < certs.length; ++c) {
			CertificateBase cert = certs[c];
			 //don't request if it's already requested
			if(cert.serial != null) continue;
			
			/*
			//pull CN
			String cn;
			String csr = cert.csr;
			try {
				PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(Base64.decodeBase64(csr));
				X500Name name = pkcs10.getSubject();
				RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
				cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
			} catch (IOException e2) {
				throw new CertificateProviderException("Failed to obtain cn from given csr:" + csr, e2);
			}
			*/
			
			String cn;
			ArrayList<String> sans;
			try {
				PKCS10CertificationRequest pkcs10 = CertificateManager.parseCSR(cert.csr);
				cn = CertificateManager.pullCNFromCSR(pkcs10);
				sans = CertificateManager.pullSANFromCSR(pkcs10);
			} catch (IOException e) {
				throw new CertificateProviderException("Failed to parse csr:" + cert.csr, e);
			} catch (CertificateRequestException e) {
				throw new CertificateProviderException("Failed to obtain cn/sans from given csr:" + cert.csr, e);
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
			
			CertificateBase issued_cert = requestHostCert(cert.csr, service_name, thecn, sans, email_address);
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
	
	public CertificateBase requestUserCert(String csr, String cn, String email_address) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod(StaticConfig.conf.getProperty("cilogon.api.host")+"/getusercert");
		
		//need to strip first and last line (-----BEGIN CERTIFICATE REQUEST-----, -----END CERTIFICATE REQUEST-----)
		String []lines = csr.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}

		post.setParameter("email", email_address);
		post.setParameter("username", cn); 
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
				
				//convert issued cert to pem (why don't we do this at CertificateDownload??)
		        String pem = "-----BEGIN CERTIFICATE-----\n";
		        pem += new String(Base64.encodeBase64Chunked(c0.getEncoded()));
		        pem += "-----END CERTIFICATE-----";
		        cert.certificate = pem;
		        
		        //do we need this stuff?
				cert.intermediate = "(cilogin cert doesn't have intermediate)";
				
				//convert to hex.. to be consistent with Digicert?
				cert.serial = c0.getSerialNumber().toString(16);
				
				return cert;
			default:
				String response = post.getResponseBodyAsString();
				JSONObject obj = null;
				try {
					 obj = new JSONObject(response);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String errorMessage = obj.getString("message");
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode() + response);	
			}		
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (CertificateException e) {
			throw new CILogonCertificateSignerException("Failed to parse certificate: "+e, e);
		} catch (CMSException e) {
			throw new CILogonCertificateSignerException("Failed to parse certificate: "+e, e);
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
	
	private CertificateBase requestHostCert(String csr, String service_name, String cn, ArrayList<String> sans, String email_address) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post;
		if(service_name == null) {
			post = new PostMethod(StaticConfig.conf.getProperty("cilogon.api.host")+"/gethostcert");
		} else {
			post = new PostMethod(StaticConfig.conf.getProperty("cilogon.api.host")+"/getservicecert");
			post.setParameter("srvname", service_name);
		}
		post.setParameter("email", email_address);
		post.setParameter("hostname", cn);
		post.setParameter("cert_request", csr);
		post.setParameter("cert_lifetime", "34128000000");
		post.setParameter("alt_hostnames", StringUtils.join(sans, ","));
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
				
				//convert to hex.. to be consistent with Digicert?
				cert.serial = c0.getSerialNumber().toString(16);	     
				
				cert.certificate = convertToPem(c0);
								
				return cert;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}	
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (CertificateException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (CMSException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request "+e, e);
		}
	}
	
	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod(StaticConfig.conf.getProperty("cilogon.api.host")+"/revoke");
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
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request: "+e, e);
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
