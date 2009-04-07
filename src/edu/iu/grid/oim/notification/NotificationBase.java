package edu.iu.grid.oim.notification;

import org.w3c.dom.Node;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.xpath.*;

import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.org.apache.xpath.internal.NodeSet;
import com.webif.divex.DivEx;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.RecordTableView;

abstract public class NotificationBase {
	static Logger log = Logger.getLogger(NotificationBase.class);  
	
	public static NotificationBase factory(NotificationRecord rec)
	{
		NotificationBase notification = null;
		try {
			//parse XML
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = parser.parse(new InputSource(new StringReader(rec.notification)));
			XPath xpath = XPathFactory.newInstance().newXPath();
			String classname = (String) xpath.evaluate("//Notification/Class", doc);

			//instantiate specified notification object
			Class notification_class = Class.forName(classname);
			notification = (NotificationBase) notification_class.newInstance();
			//let it init itself
			notification.init(xpath, doc);

		} catch (XPathExpressionException e) {
			log.error(e);
		} catch (ClassNotFoundException e) {
			log.error(e);
		} catch (InstantiationException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return notification;
	}
	
	//returns human readable title of the notification
	abstract public String getTitle();
	
	//initialize itself from the xml
	abstract void init(XPath xpath, Document doc) throws XPathExpressionException;
	abstract public RecordTableView createReadView(DivEx root, Authorization auth);
	abstract public IView createEditView(DivEx root, Authorization auth);
}
