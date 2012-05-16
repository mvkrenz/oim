package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	
	public Certificate signUserCertificate(String csr, String dn) throws CertificateProviderException {
		return requestUserCert(csr, dn);
	}
	
	public Certificate signHostCertificate(String csr, String cn) throws CertificateProviderException {
		String request_id = requestHostCert(csr, cn);
		log.debug("Requested host certificate. Digicert Request ID:" + request_id);
		String order_id = approve(request_id, "Approving for test purpose"); //like 00295828
		log.debug("Approved host certificate. Digicert Order ID:" + order_id);
		
		log.debug("Waiting for 60 second before retrieving --- I am not sure if this even works, but if it does, this is not acceptable.");
		try {
			Thread.sleep(1000*60);
		} catch (InterruptedException e) {
			log.error("Sleep interrupted", e);
		} //wait for 30 seconds
		return retrieveByOrderID(order_id);
	}
	
	private Document parseXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(in);
	}
	
	public Certificate requestUserCert(String csr, String cn) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com//enterprise/api/?action=grid_request_email_cert");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("email", "hayashis@iu.edu");
		post.setParameter("full_name", cn);
		post.setParameter("csr", csr);
		//post.setParameter("dn", dn); //request to override dn with ours
		
		//post.setParameter("comments", "This is just a test request."); //for approver to see
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				System.out.println("failed to execute grid_retrieve_host_cert request");
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
				throw new DigicertCPException("Request failed..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate("Digicert");
				
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
			
			throw new DigicertCPException("Unknown return code: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		}
	}
	
	
	private String requestHostCert(String csr, String cn) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_host_cert");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("common_name", cn);
		post.setParameter("csr", csr);
		//post.setParameter("comments", "This is just a test request."); //for approver to see
		
		try {
			cl.executeMethod(post);
			Document ret = parseXML(post.getResponseBodyAsStream());
			NodeList result_nl = ret.getElementsByTagName("result");
			Element result = (Element)result_nl.item(0);
			if(result.getTextContent().equals("failure")) {
				System.out.println("failed to execute grid_request_host_cert request");
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
				throw new DigicertCPException("Request failed..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element request_id_e = (Element)ret.getElementsByTagName("request_id").item(0);
				String request_id = request_id_e.getTextContent();
				System.out.println("Obtained Digicert Request ID:" + request_id); //like "15757"
				return request_id;
			}
			throw new DigicertCPException("Unknown return code: " +result.getTextContent());
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		}
	}
	
	private String approve(String request_id, String comment) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_approve_request");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
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
				System.out.println("failed to execute grid_approve_request request");
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
				throw new DigicertCPException("Request failed..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				Element order_id_e = (Element)ret.getElementsByTagName("order_id").item(0);
				String order_id = order_id_e.getTextContent();
				//System.out.println("Obtained Digicert Order ID:" + order_id); //like "00295815"
				return order_id;
			}
			
			throw new DigicertCPException("Unknown return code: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		}
	}
	
	private Certificate retrieveByOrderID(String order_id) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_retrieve_host_cert");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
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
				throw new DigicertCPException("Request failed..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				
				ICertificateSigner.Certificate cert = new ICertificateSigner.Certificate("Digicert");
				
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
			
			throw new DigicertCPException("Unknown return code: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		}
	}

	@Override
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		HttpClient cl = new HttpClient();
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
		
		PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_host_revoke");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
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
				throw new DigicertCPException("Request failed..\n" + errors.toString());
			} else if(result.getTextContent().equals("success")) {
				//nothing particular to do
			}
			
			throw new DigicertCPException("Unknown return code: " +result.getTextContent());	
		} catch (HttpException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (IOException e) {
			throw new DigicertCPException("Failed to make request", e);
		} catch (ParserConfigurationException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		} catch (SAXException e) {
			throw new DigicertCPException("Failed to parse returned String", e);
		}
	}

	@Override
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		//just use the host revoke
		revokeHostCertificate(serial_id);
	}
}
