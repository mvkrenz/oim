package edu.iu.grid.oim.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  
import java.net.MalformedURLException;  
import java.net.URL;  
import java.net.URLConnection;  
import java.text.MessageFormat;  
import java.util.Properties;  
  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;  
  
import org.apache.commons.codec.binary.Base64;  
import org.w3c.dom.Document;  
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;  
import org.xml.sax.SAXException; 

import org.apache.log4j.Logger;

//forward user to specified URL plus browser token
public class BrowserJumpServlet extends ServletBase {
	static Logger log = Logger.getLogger(BrowserJumpServlet.class);

	private static final String urlPattern = "http://{0}/rpc/rpcxml.php";
	private static final String hostName = "user-agent-string.info";
	private static final String key = "free";
	private static final String rawXml = "<?xml version=\"1.0\"?><methodCall><methodName>ua.search</methodName><params><param><value><string>{0}</string></value></param><param><value><string>{1}</string></value></param></params></methodCall>";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userAgent = request.getHeader("User-Agent");
		log.debug("BrowserJump: User-Agent:" + userAgent);
		String urlformat_in = request.getParameter("urlformat");
		String urlformat = new String(new Base64(Integer.MAX_VALUE).decode(urlformat_in));
		try {
			Properties props = BrowserJumpServlet.getProperties(userAgent);  
			String url = MessageFormat.format(urlformat, props.getProperty("ua_family"));
			log.debug("Redirecting to :"+ url);
			response.sendRedirect(url);
		} catch (Exception e) {
			log.warn("Exception while contacting user-agent-string.info webservice with useragent:"+userAgent, e);
			String url = MessageFormat.format(urlformat, "Other Browsers");		
			log.debug("Redirecting to :"+ url);
			response.sendRedirect(url);
		}
	}

	public static Properties getProperties(String userAgent)
			throws MalformedURLException, IOException, SAXException,
			ParserConfigurationException {
		Properties prop = new Properties();
		Document doc = getDocument(userAgent);
		NodeList list = doc.getElementsByTagName("struct").item(0).getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n1 = list.item(i);
			if ("member".equals(n1.getNodeName())) {
				String name = getValueFromChild(n1, "name");
				String val = getValueFromFirstChild(getChild(n1, "value"));
				prop.setProperty(name, val);
			}
		}
		return prop;
	}

	private static String getValueFromFirstChild(Node node) {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n1 = list.item(i);
			if (n1.getNodeName() != null && !n1.getNodeName().trim().isEmpty()
					&& n1.getChildNodes().getLength() > 0) {
				return n1.getChildNodes().item(0).getNodeValue();
			}
		}
		throw new RuntimeException("Node has no child");
	}

	private static Node getChild(Node node, String nodeName) {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n1 = list.item(i);
			if (n1.getNodeName().equals(nodeName)) {
				return n1;
			}
		}
		throw new RuntimeException("Node has no child with nodeName = "	+ nodeName);
	}

	private static String getValueFromChild(Node node, String nodeName) {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n1 = list.item(i);
			if (n1.getNodeName().equals(nodeName)
					&& n1.getChildNodes().getLength() > 0) {
				return n1.getChildNodes().item(0).getNodeValue();
			}
		}
		throw new RuntimeException("Node has no child with nodeName = "	+ nodeName);
	}

	private static Document getDocument(String userAgent)
			throws MalformedURLException, IOException, SAXException,
			ParserConfigurationException {
		InputStream is = null;
		OutputStream os = null;
		try {
			String urlString = MessageFormat.format(urlPattern, hostName);
			String uaBase64 = new Base64(Integer.MAX_VALUE).encodeToString(userAgent.getBytes());
			String requestBody = MessageFormat.format(rawXml, uaBase64, key);
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.addRequestProperty("content-type", "application/xml");
			con.addRequestProperty("content-length", "" + requestBody.length());
			os = con.getOutputStream();
			os.write(requestBody.getBytes());
			os.flush();
			is = con.getInputStream();
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}
}
