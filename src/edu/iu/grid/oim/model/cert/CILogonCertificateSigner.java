package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

public class CILogonCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(CILogonCertificateSigner.class);  
	class CILogonCertificateSignerException extends CertificateProviderException {
		public CILogonCertificateSignerException(String msg, Exception e) {
			super("From CILogon: " + msg, e);
		}
		public CILogonCertificateSignerException(String msg) {
			super("From CILogon: " + msg);
		}
	};
	
	public Certificate signUserCertificate(String csr, String dn, String email_address) throws CertificateProviderException {
		return requestUserCert(csr, dn, email_address);
	}
	
	//pass csrs, and 
	public void signHostCertificates(Certificate[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		
		//request & approve all
		for(Certificate cert : certs) {
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
			
			Certificate issued_cert = requestHostCert(csr, service_name, thecn);
			log.debug("Requested host certificate. Digicert Request ID:" + issued_cert.serial);

			cert.serial = issued_cert.serial;
			cert.certificate = issued_cert.certificate;
			cert.intermediate = issued_cert.intermediate;
			cert.pkcs7 = issued_cert.pkcs7;
		}
	}
	
	private HttpClient createHttpClient() {
		HttpClient cl = new HttpClient();
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");  
	    return cl;
	}
	
	public Certificate requestUserCert(String csr, String dn, String email_address) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://test.cilogon.org/getosgcert");
		
		//need to strip first and last line (-----BEGIN CERTIFICATE REQUEST-----, -----END CERTIFICATE REQUEST-----)
		String []lines = csr.split("\n");
		String payload = "";
		for(String line : lines) {
			if(line.startsWith("-----")) continue;
			payload += line;
		}

		//post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		//post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		//post.setParameter("response_type", "xml");
		//post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("email", email_address);
		post.setParameter("hostname", dn);
		post.setParameter("cert_request", payload);
		post.setParameter("cert_lifetime", "34128000000");
		//post.setParameter("hash", StaticConfig.conf.getProperty("digicert.hash"));
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		try {
			cl.executeMethod(post);
			
			switch(post.getStatusCode()) {
			case 200:
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate();
				StringWriter writer = new StringWriter();
				IOUtils.copy(post.getResponseBodyAsStream(), writer, "UTF-8"); //should use ascii?
				cert.pkcs7 = writer.toString();
				
				//pull some information from the cert for validation purpose
				java.security.cert.Certificate[] chain = CertificateManager.parsePKCS7(cert.pkcs7);
					
				X509Certificate c0 = (X509Certificate)chain[0];
				cert.notafter = c0.getNotAfter();
				cert.notbefore = c0.getNotBefore();
				cert.intermediate = "NO-INT";
				cert.serial = c0.getSerialNumber().toString();
				return cert;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}

			
			/*
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				//System.out.println("failed to execute grid_retrieve_host_cert request");
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append(" Error while accessing: grid_request_email_cert");
					errors.append(" Code:" + code.getTextContent());
					errors.append(" Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new CILogonCertificateSignerException("Request failed for grid_request_email_cert\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate();
				
				Element serial_e = (Element)ret.getElementsByTagName("serial").item(0);
				cert.serial = serial_e.getTextContent();
				
				Element certificate_e = (Element)ret.getElementsByTagName("certificate").item(0);
				cert.certificate = certificate_e.getTextContent();
				
				Element intermediate_e = (Element)ret.getElementsByTagName("intermediate").item(0);
				cert.intermediate = intermediate_e.getTextContent();
			
				Element pkcs7_e = (Element)ret.getElementsByTagName("pkcs7").item(0);
				cert.pkcs7 = pkcs7_e.getTextContent();

				return cert;
			}
			*/			
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
	
	private Certificate requestHostCert(String csr, String service_name, String cn) throws CILogonCertificateSignerException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://test.cilogon.org/getosgcert");

		//post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		//post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		//post.setParameter("response_type", "xml");
		//post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("email", "noemail@example.com");
		post.setParameter("hostname", cn);
		post.setParameter("cert_request", csr);
		post.setParameter("cert_lifetime", "34128000000");
		//post.setParameter("hash", StaticConfig.conf.getProperty("digicert.hash"));
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

		try {
			cl.executeMethod(post);
			
			switch(post.getStatusCode()) {
			case 200:
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate();
				StringWriter writer = new StringWriter();
				IOUtils.copy(post.getResponseBodyAsStream(), writer, "UTF-8"); //should use ascii?
				cert.certificate = writer.toString();
				
				//parse certificate and populate following fields
				cert.pkcs7 = "TODO..";
				cert.intermediate = "TODO.."; //TODO
				cert.serial = "0000"; //TODO
				return cert;
			default:
				throw new CILogonCertificateSignerException("Unknown status code from cilogon: " +post.getStatusCode());	
			}

			
			/*
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				//System.out.println("failed to execute grid_retrieve_host_cert request");
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append(" Error while accessing: grid_request_email_cert");
					errors.append(" Code:" + code.getTextContent());
					errors.append(" Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new CILogonCertificateSignerException("Request failed for grid_request_email_cert\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate();
				
				Element serial_e = (Element)ret.getElementsByTagName("serial").item(0);
				cert.serial = serial_e.getTextContent();
				
				Element certificate_e = (Element)ret.getElementsByTagName("certificate").item(0);
				cert.certificate = certificate_e.getTextContent();
				
				Element intermediate_e = (Element)ret.getElementsByTagName("intermediate").item(0);
				cert.intermediate = intermediate_e.getTextContent();
			
				Element pkcs7_e = (Element)ret.getElementsByTagName("pkcs7").item(0);
				cert.pkcs7 = pkcs7_e.getTextContent();

				return cert;
			}
			*/			
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make cilogon/rest request", e);
		}
	}
	
	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		HttpClient cl = createHttpClient();
		/*
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_host_revoke");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("serial", serial_id);
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append("Code:" + code.getTextContent());
					errors.append(" Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new CILogonCertificateSignerException("Request failed for grid_request_host_revoke\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				//nothing particular to do
			} else {
				throw new CILogonCertificateSignerException("Unknown return code from grid_request_host_revoke: " +result.getTextContent());
			}
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make grid_request_host_revoke request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make grid_request_host_revoke request", e);
		} catch (ParserConfigurationException e) {
			throw new CILogonCertificateSignerException("Failed to parse returned String from grid_request_host_revoke", e);
		} catch (SAXException e) {
			throw new CILogonCertificateSignerException("Failed to parse returned String from grid_request_host_revoke", e);
		}
		*/
	}

	@Override
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		/*
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_email_revoke");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("serial", serial_id);
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append("Code:" + code.getTextContent());
					errors.append(" Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new CILogonCertificateSignerException("Request failed for grid_email_revoke\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				//nothing particular to do
			} else {
				throw new CILogonCertificateSignerException("Unknown return code from grid_email_revoke: " +result.getTextContent());
			}
		} catch (HttpException e) {
			throw new CILogonCertificateSignerException("Failed to make grid_email_revoke request", e);
		} catch (IOException e) {
			throw new CILogonCertificateSignerException("Failed to make grid_email_revoke request", e);
		} catch (ParserConfigurationException e) {
			throw new CILogonCertificateSignerException("Failed to parse returned String from grid_email_revoke", e);
		} catch (SAXException e) {
			throw new CILogonCertificateSignerException("Failed to parse returned String from grid_email_revoke", e);
		}
		*/
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
