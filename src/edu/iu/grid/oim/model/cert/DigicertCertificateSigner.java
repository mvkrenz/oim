package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.StringArray;

public class DigicertCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(DigicertCertificateSigner.class);  
	class DigicertCPException extends CertificateProviderException {
		public DigicertCPException(String msg, Exception e) {
			super(msg, e);
		}
		public DigicertCPException(String msg) {
			super(msg);
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
			
			//do request & approve
			String request_id = requestHostCert(csr, service_name, thecn);
			log.debug("Requested host certificate. Digicert Request ID:" + request_id);
			String order_id = approve(request_id, "Approving for test purpose"); //like 00295828
			log.debug("Approved host certificate. Digicert Order ID:" + order_id);

			cert.serial = order_id;
		}
		callback.certificateRequested();
		
		///////////////////////////////////////////////////////////////////////////////////////////
		//
		// TODO -- DigiCert will provide API to do this in more efficient way
		//
		
		//wait for a while before start pinging (per Greg)
		try {
			log.debug("Sleeping for 60 seconds");
			Thread.sleep(1000*60);
		} catch (InterruptedException e) {
			log.error("Sleep interrupted", e);
		}
		
		//wait until all certificates are issued (or timeout)
		log.debug("start looking for certificate that's issued");
		for(int retry = 0; retry < 40; ++retry) {	
			//count number of certificates issued so far
			int issued = 0;
			for(int c = 0; c < certs.length; ++c) {
				Certificate cert = certs[c];
				if(cert.pkcs7 == null) {
					try {
						//WARNING - this resets csr stored in current cert
						log.debug("Checking to see if OrderID:" + cert.serial + " has been issued");
						cert = retrieveByOrderID(cert.serial);
						if(cert != null) {
							//found new certificate issued
							certs[c] = cert;
							issued++;
							callback.certificateSigned(cert, c);
						}
					} catch(DigicertCPException e) {
						//TODO - need to ask DigiCert to give me more specific error code so that I can distinguish between real error v.s. need_wait
						log.warn("Failed to retrieve cert for order ID:" + cert.serial + ". try counter:" +retry+" probably not yet issued.. ignoring");
					}
 				} else {
 					issued++;
 				}
			}
			
			if(certs.length == issued) {
				//all issued.
				return;
			}
			
			//if we have less than 5 cert, wait few seconds between each loop in order to avoid
			//hitting digicert too often on the same cert
			if(certs.length - issued < 5) {
				log.debug(issued + " issued out of " + certs.length + " requests... waiting for 5 second before re-trying");
				try {
					Thread.sleep(1000*5);
				} catch (InterruptedException e) {
					log.error("Sleep interrupted", e);
				}
			}
		}
		
		//timed out..
		throw new CertificateProviderException("DigiCert didn't return certificate after predefined re-tries");
	}
	
	//used to check if certificate has been issued
	private String getDetail_under_construction(String order_id) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_certificate_details");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("id", order_id);

		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				//System.out.println("failed to execute grid_certificate_details request");
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append(" Error while accessing: grid_certificate_details");
					errors.append(" Code:" + code.getTextContent());
					errors.append(" Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_certificate_details\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				log.debug("Successfully retrieved certificate detail for " + order_id);
				return "success";
			}
			
			throw new DigicertCPException("Unknown return code from grid_certificate_details: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_certificate_details request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_certificate_details request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_certificate_details", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_certificate_details", e);
		}
	}
	
	private Document parseXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(in);
	}
	
	public Certificate requestUserCert(String csr, String cn, String email_address) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_email_cert");

		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("email", email_address);
		post.setParameter("full_name", cn);
		post.setParameter("csr", csr);

		try {
			cl.executeMethod(post);
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
				throw new DigicertCPException("Request failed for grid_request_email_cert\n" + errors.toString());
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
			
			throw new DigicertCPException("Unknown return code from grid_request_email_cert: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_request_email_cert request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_email_certrequest", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_email_cert", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_email_cert", e);
		}
	}
	
	private String requestHostCert(String csr, String service_name, String cn) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_host_cert");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("common_name", cn);
		if(service_name != null) {
			post.setParameter("service_name", service_name);
		}
		post.setParameter("csr", csr);
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				//System.out.println("failed to execute grid_request_host_cert request");
				NodeList error_code_nl = ret.getElementsByTagName("error_code");
				StringBuffer errors  = new StringBuffer();
				for(int i = 0;i < error_code_nl.getLength(); ++i) {
					Element error_code = (Element)error_code_nl.item(i);
					Element code = (Element)error_code.getElementsByTagName("code").item(0);
					Element description = (Element)error_code.getElementsByTagName("description").item(0);
					errors.append("Code:" + code.getTextContent());
					errors.append("Description:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_request_host_cert..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element request_id_e = (Element)ret.getElementsByTagName("request_id").item(0);
				String request_id = request_id_e.getTextContent();
				System.out.println("Obtained Digicert Request ID:" + request_id); //like "15757"
				return request_id;
			}
			throw new DigicertCPException("Unknown return code from grid_request_host_cert: " +result.getTextContent());
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_request_host_cert request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_host_cert request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_cert", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_cert", e);
		}
	}
	
	private String approve(String request_id, String comment) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_approve_request");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("comment", comment);
		post.setParameter("request_id", request_id);
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				//System.out.println("failed to execute grid_approve_request request");
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
				throw new DigicertCPException("Request failed for grid_approve_request\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element order_id_e = (Element)ret.getElementsByTagName("order_id").item(0);
				String order_id = order_id_e.getTextContent();
				return order_id;
			}
			
			throw new DigicertCPException("Unknown return code from grid_approve_request: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_approve_request request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_approve_request request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_approve_request", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_approve_request", e);
		}
	}
	
	private Certificate retrieveByOrderID(String order_id) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_retrieve_host_cert");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("id", order_id);
		
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
				throw new DigicertCPException("Request failed for grid_retrieve_host_cert\n" + errors.toString());
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
			
			throw new DigicertCPException("Unknown return code for grid_retrieve_host_cert: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_retrieve_host_cert request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_retrieve_host_cert request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_retrieve_host_cert", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_retrieve_host_cert", e);
		}
	}

	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		HttpClient cl = new HttpClient();
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_host_revoke");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("serial", serial_id);
		
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
				throw new DigicertCPException("Request failed for grid_request_host_revoke\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				//nothing particular to do
			} else {
				throw new DigicertCPException("Unknown return code from grid_request_host_revoke: " +result.getTextContent());
			}
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_request_host_revoke request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_host_revoke request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_revoke", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_revoke", e);
		}
	}

	@Override
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		//just use the host revoke
		revokeHostCertificate(serial_id);
	}
}
