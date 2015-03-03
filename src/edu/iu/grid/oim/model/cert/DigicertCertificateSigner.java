package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.lib.StaticConfig;

public class DigicertCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(DigicertCertificateSigner.class);  
	class DigicertCPException extends CertificateProviderException {
		public DigicertCPException(String msg, Exception e) {
			super("From DigiCert: " + msg, e);
		}
		public DigicertCPException(String msg) {
			super("From DigiCert: " + msg);
		}
	};
	
	public CertificateBase signUserCertificate(String csr, String dn, String email_address) throws CertificateProviderException {
		return requestUserCert(csr, dn, email_address);
	}
	
	/*
	@Override
    public X500NameBuilder generateX500NameBuilder() {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);

        //DigiCert overrides the DN, so none of these matters - except CN which is used to send common_name parameter
        //We are creating this so that we can create private key
        x500NameBld.addRDN(BCStyle.DC, "com");
        x500NameBld.addRDN(BCStyle.DC, "DigiCert-Grid");
        if(StaticConfig.isDebug()) {
        	//let's assume debug means we are using digicert pilot
        	x500NameBld.addRDN(BCStyle.O, "OSG Pilot");
        } else {
        	x500NameBld.addRDN(BCStyle.O, "Open Science Grid");
        }
        x500NameBld.addRDN(BCStyle.OU, "People");   
        
        return x500NameBld;
        //return x500NameBld.build();
    }
    */
	
	//pass csrs, and 
	public void signHostCertificates(CertificateBase[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		
		//request & approve all
		for(CertificateBase cert : certs) {
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
		
		//wait until all certificates are issued (or timeout)
		log.debug("start looking for certificate that's issued");
		
		int timeout_msec = 5*60*1000;
		
		Date start = new Date();
		while(true) {	
			
			//wait few seconds between each loops
			try {
				log.debug("Sleeping for 5 seconds");
				Thread.sleep(1000*5);
			} catch (InterruptedException e) {
				log.error("Sleep interrupted", e);
			}
			
			//loop all certs - count number of certificates issued so far
			int issued = 0;
			for(int c = 0; c < certs.length; ++c) {
				CertificateBase cert = certs[c];
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
						log.info("Failed to retrieve cert for order ID:" + cert.serial + ". probably not yet issued.. ignoring");
					}
 				} else {
 					issued++;
 				}
			}
			
			if(certs.length == issued) {
				//all issued.
				return;
			}
			
			//check timeout
			Date now = new Date();
			if(now.getTime() - start.getTime() > timeout_msec) {
				log.debug("certificate issuing timeout reached..");
				//timed out..
				throw new CertificateProviderException("DigiCert didn't return certificate after "+(timeout_msec/1000)+" seconds");
			}
			/*
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
			*/
		}
	}
	
	private HttpClient createHttpClient() {
		HttpClient cl = new HttpClient();
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    return cl;
	}
	
	private Document parseXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(in);
	}
	
	public CertificateBase requestUserCert(String csr, String cn, String email_address) throws DigicertCPException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_email_cert");

		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("email", email_address);
		post.setParameter("full_name", cn);
		post.setParameter("csr", csr);
		post.setParameter("hash", StaticConfig.conf.getProperty("digicert.hash"));
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_request_email_cert\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				CertificateBase cert = new CertificateBase();
				
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
			throw new DigicertCPException("Failed to make grid_request_email_cert request:"+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_email_cert request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_email_cert: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_email_cert: "+e, e);
		}
	}
	
	private String requestHostCert(String csr, String service_name, String cn) throws DigicertCPException {
		HttpClient cl = createHttpClient();
		
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
		post.setParameter("hash", StaticConfig.conf.getProperty("digicert.hash"));
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
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
			throw new DigicertCPException("Failed to make grid_request_host_cert request: "+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_host_cert request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_cert: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_cert: "+e, e);
		}
	}
	
	private String approve(String request_id, String comment) throws DigicertCPException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_approve_request");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("comment", comment);
		post.setParameter("request_id", request_id);
		
		post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_approve_request\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element success_message = (Element)ret.getElementsByTagName("success_message").item(0);
				String message = success_message.getTextContent();
				log.debug(message);
				
				Element order_id_e = (Element)ret.getElementsByTagName("order_id").item(0);
				if(order_id_e == null) return null;
				String order_id = order_id_e.getTextContent();
				return order_id;
			}
			
			throw new DigicertCPException("Unknown return code from grid_approve_request: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_approve_request request: "+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_approve_request request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_approve_request: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_approve_request: "+e, e);
		}
	}
	
	private CertificateBase retrieveByOrderID(String order_id) throws DigicertCPException {
		HttpClient cl = createHttpClient();
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_retrieve_host_cert");
		post.addParameter("customer_name", StaticConfig.conf.getProperty("digicert.customer_name"));
		post.setParameter("customer_api_key", StaticConfig.conf.getProperty("digicert.customer_api_key"));
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("id", order_id);
		
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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_retrieve_host_cert\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				CertificateBase cert = new CertificateBase();
				
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
			throw new DigicertCPException("Failed to make grid_retrieve_host_cert request: "+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_retrieve_host_cert request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_retrieve_host_cert: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_retrieve_host_cert: "+e, e);
		}
	}

	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		//do request & approve
		String request_id = requestHostCertRevoke(serial_id);
		log.debug("Requested host certificate revocation. Digicert Request ID:" + request_id);
		String order_id = approve(request_id, "Approving host certificate revocation"); 
		log.debug("Approved host certificate revocation. Digicert Order ID:" + order_id);
	}
	
	public String requestHostCertRevoke(String serial_id) throws CertificateProviderException {
		HttpClient cl = createHttpClient();
		
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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_request_host_revoke\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element request_id_e = (Element)ret.getElementsByTagName("request_id").item(0);
				String request_id = request_id_e.getTextContent();
				System.out.println("Obtained Digicert Request ID:" + request_id); //like "15757"
				return request_id;
			} else {
				throw new DigicertCPException("Unknown return code from grid_request_host_revoke: " +result.getTextContent());
			}
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_request_host_revoke request: "+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_request_host_revoke request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_revoke: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_request_host_revoke: "+e, e);
		}
	}

	@Override
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
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
					errors.append("\nCode:" + code.getTextContent());
					errors.append("\nDescription:" + description.getTextContent());
					errors.append("\n");
				}
				throw new DigicertCPException("Request failed for grid_email_revoke\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				//nothing particular to do
			} else {
				throw new DigicertCPException("Unknown return code from grid_email_revoke: " +result.getTextContent());
			}
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make grid_email_revoke request: "+e, e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make grid_email_revoke request: "+e, e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_email_revoke: "+e, e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String from grid_email_revoke: "+e, e);
		}
	}
	

	@Override
	public String getUserDNBase() {
		return StaticConfig.conf.getProperty("digicert.user_dn_base");
	}

	@Override
	public String getHostDNBase() {
		return StaticConfig.conf.getProperty("digicert.host_dn_base");
	}
}
