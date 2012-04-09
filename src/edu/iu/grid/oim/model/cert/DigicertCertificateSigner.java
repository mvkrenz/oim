package edu.iu.grid.oim.model.cert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DigicertCertificateSigner implements ICertificateSigner {
	
	class DigicertCPException extends CertificateProviderException {
		String msg;
		public DigicertCPException(String msg, Exception e) {
			super(e);
			this.msg = msg;
		}
		public DigicertCPException(String msg) {
			this.msg = msg;
		}
	};
	
	public Certificate signHostCertificate(String csr, String domain) throws CertificateProviderException {
		String requet_id = request(csr, domain);
		String order_id = approve(requet_id, "Approving for test purpose"); //like 00295828
		return retrieveByOrderID(order_id);
	}
	public Certificate signUserCertificate(String csr) throws CertificateProviderException {
		//String requet_id = request(csr, domain);
		//String order_id = approve(requet_id, "Approving for test purpose"); //like 00295828
		//return retrieveByOrderID(order_id);

		//TODO
		return null;
	}
	
	private Document parseXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(in);
	}
	
	public String request(String csr, String domain) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com//enterprise/api/?action=grid_request_host_cert");

		post.addParameter("customer_name", "052062");
		post.setParameter("customer_api_key", "MG9ij2Of4rakV7tXARyE347QQu00097U");
		post.setParameter("response_type", "xml");
		post.setParameter("validity", "1"); //security by obscurity -- from the DigiCert dev team
		post.setParameter("common_name", domain);
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
	
	public String approve(String request_id, String comment) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com//enterprise/api/?action=grid_approve_request");

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
	
	public Certificate retrieveByOrderID(String order_id) throws DigicertCPException {
		HttpClient cl = new HttpClient();
		//cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
	    cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");
	    //cl.getParams().setParameter("http.contenttype", "application/x-www-form-urlencoded")
		
		PostMethod post = new PostMethod("https://www.digicert.com//enterprise/api/?action=grid_retrieve_host_cert");

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
			
				Element pkcs7_e = (Element)ret.getElementsByTagName("pkcs7").item(0);
				cert.certificate = pkcs7_e.getTextContent();

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
}
