package edu.iu.grid.oim.model.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.model.UserContext;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

abstract public class CertificateRequestModelBase<T extends RecordBase> extends ModelBase<T> {
    protected CertificateRequestModelBase(UserContext context, String _table_name) {
		super(context, _table_name);
	}
	static Logger log = Logger.getLogger(CertificateRequestModelBase.class);  
	
    //provide String[] with XML serialization capability
    class StringArray  {
    	private String[] strings;
    	public String get(int idx) { return strings[idx]; }
    	public void set(int idx, String str) { strings[idx] = str; }
    	public String[] getAll() { return strings; }
    	public StringArray(String xml) {
    		//deserialize from xml
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		try {
    			DocumentBuilder db = dbf.newDocumentBuilder();
    			InputStream is = new ByteArrayInputStream(xml.getBytes());
    			Document dom = db.parse(is);
    			
    			//create string array
    			Node size_nl = dom.getElementsByTagName("Size").item(0);
    			String size_str = size_nl.getTextContent();
    			int size = Integer.parseInt(size_str);
    			strings = new String[size];
    			
    			//populate data
    			NodeList string_nl = dom.getElementsByTagName("String");
    			for(int i = 0;i < string_nl.getLength(); ++i) {
    				Node n = string_nl.item(i);
    				Node null_mark = n.getAttributes().getNamedItem("null");
    				if(null_mark != null) {
    					strings[i] = null;
    				} else {
    					strings[i] = n.getTextContent();
    				}
    			}
    			
    		}catch(ParserConfigurationException pce) {
    			pce.printStackTrace();
    		}catch(SAXException se) {
    			se.printStackTrace();
    		}catch(IOException ioe) {
    			ioe.printStackTrace();
    		}
    	}
    	public StringArray(int size) {
    		strings = new String[size];
    	}
    	public String toXML() {
    		StringBuffer out = new StringBuffer();
    		out.append("<StringArray>");
    		out.append("<Size>"+strings.length+"</Size>");
    		for(String s : strings) {
    			if(s == null) {
        			out.append("<String null=\"true\"></String>");
    			} else {
    				out.append("<String>"+StringEscapeUtils.escapeXml(s)+"</String>");
    			}
    		}
    		out.append("</StringArray>");
    		return out.toString();
    	}
    	public int length() { return strings.length; }
    
    }
 
    /*
	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	} 
	*/ 

	public class LogDetail {
		public ContactRecord contact; //user who made this action
		public String ip; //ip address
		public String status; //new status
		public String comment; //from the log
		public Date time;
	}

	//NO-AC
	public ArrayList<LogDetail> getLogs(Class model_class, Integer id) throws SQLException {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(false);
    	factory.setValidating(false);
    	DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			XPath xpath = XPathFactory.newInstance().newXPath();
			ContactModel cmodel = new ContactModel(context);
			
			ArrayList<LogDetail> logs = new ArrayList<LogDetail>();
			LogModel model = new LogModel(context);
			Collection<LogRecord> raws = model.getByModel(model_class, id.toString());
			for(LogRecord raw : raws) {
				LogDetail log = new LogDetail();
				log.comment = raw.comment;
				log.time = raw.timestamp;
				log.ip = raw.ip;
				if(raw.contact_id != null) {
					log.contact = cmodel.get(raw.contact_id);
				}
				//parse the xml
				byte[] bArray = raw.xml.getBytes();
				ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
				Document doc = builder.parse(bais);
				
				String type = (String)xpath.evaluate("//Type", doc, XPathConstants.STRING);
				if(type.equals("Insert")) {
					log.status = (String)xpath.evaluate("//Field[Name='status']/Value", doc, XPathConstants.STRING);
				} else if(type.equals("Update")) {
					log.status = (String)xpath.evaluate("//Field[Name='status']/NewValue", doc, XPathConstants.STRING);
				}
				logs.add(log);
			}
			return logs;
		} catch (ParserConfigurationException e) {
			log.error("Failed to instantiate xml parser to parse log", e);
		} catch (SAXException e) {
			log.error("Failed to parse log", e);
		} catch (IOException e) {
			log.error("Failed to parse log", e);
		} catch (XPathExpressionException e) {
			log.error("Failed to apply xpath on log", e);
		}

		return null;
	}
   
}
