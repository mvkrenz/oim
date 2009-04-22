package edu.iu.grid.oim.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.ModelBase;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.CDataView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.TableView.Row;

public class LogServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(LogServlet.class);  
 	
    public LogServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		//pull log type
		String model = "%";
		String dirty_type = request.getParameter("type");
		if(dirty_type == null || dirty_type.compareTo("all") == 0) {
			model = "%";
		} else if(dirty_type.compareTo("resource") == 0) {
			model = "%ResourceModel";
		} else if(dirty_type.compareTo("vo") == 0) {
			model = "%VOModel";
		} else if(dirty_type.compareTo("sc") == 0) {
			model = "%SCModel";
		} else if(dirty_type.compareTo("contact") == 0) {
			model = "%ContactModel";
		} else if(dirty_type.compareTo("site") == 0) {
			model = "%SiteModel";
		} else if(dirty_type.compareTo("facility") == 0) {
			model = "%FacilityModel";
		}
		
		try {
			//construct view
			MenuView menuview = createMenuView("admin");
			DivExRoot root = DivExRoot.getInstance(request);
			ContentView contentview = createContentView(root, model);
			Page page = new Page(menuview, contentview, createSideView(root));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, String model) throws ServletException, SQLException
	{
		ContentView view = new ContentView();	
		view.add(new HtmlView("<h1>Log</h1>"));    	
    	
		try {
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	DocumentBuilder builder;
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			DNModel dmodel = new DNModel(auth);
			
			//pull log entries that matches the log type
			LogModel lmodel = new LogModel(auth);
			Collection<LogRecord> recs = lmodel.getLatest(model);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(LogRecord rec : recs) {

				//instantiate the model specified on the log (with Authorization as parameter)
				Class modelClass = Class.forName(rec.model);
				Constructor cons = modelClass.getConstructor(new Class[]{Authorization.class});
				ModelBase somemodel = (ModelBase) cons.newInstance(auth);
				
				try {
					//Parse the log XML stored in the record
					Document log = builder.parse(new StringBufferInputStream(rec.xml));
				
					//check the access
					if(!somemodel.hasLogAccess(xpath, log)) {
						continue;
					}
									
					//display the log
					view.add(new HtmlView("<h2>" + somemodel.getName() + " ("+rec.type+")</h2>"));
					DNRecord dnrec = dmodel.get(rec.dn_id);
					view.add(new HtmlView("<span>"+dnrec.dn_string+"<br/>Updated "+rec.timestamp.toString()+"</span>"));
					view.add(createLogView(xpath, log));
				} catch (SAXException e) {
					view.add(new HtmlView("XML log Parse Error (" + somemodel.getName() + ") "+ e.toString()));
				} catch (XPathExpressionException e) {
					view.add(new HtmlView("XPath Expression Error (" + somemodel.getName() + ") "+ e.toString()));
				}
			}
			
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return view;
	}
	private IView createLogView(XPath xpath, Document dom) throws XPathExpressionException
	{
		RecordTableView table = new RecordTableView("log_table");
		String type = (String)xpath.evaluate("//Type", dom, XPathConstants.STRING);
		
		//create header row
		Row row = table.new Row();
		row.setClass("top");
		row.addHeaderCell(new HtmlView("Field"));
		if(type.compareTo("Update") == 0) {
			row.addHeaderCell(new HtmlView("Old Value"));
			row.addHeaderCell(new HtmlView("New Value"));
		} else if(type.compareTo("Insert") == 0) {
			row.addHeaderCell(new HtmlView("Value"));
		} else if(type.compareTo("Remove") == 0) {
			row.addHeaderCell(new HtmlView("Value"));
		}
		table.addRow(row);
		
		//process keys
		for(Node node : pullNonTextNode(dom.getElementsByTagName("Key"))) {
			row = table.new Row();
			ArrayList<Node> key = pullNonTextNode(node.getChildNodes());
			Node name = key.get(0);
			Node value = key.get(1);
			row.addHeaderCell(new HtmlView(name.getTextContent()));
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(value.getTextContent())));
			if(type.compareTo("Update") == 0) {
				row.addCell(new HtmlView(""));
			}
			table.addRow(row);
		}
		
		//process values
		for(Node node : pullNonTextNode(dom.getElementsByTagName("Field"))) {
			row = table.new Row();
			ArrayList<Node> field = pullNonTextNode(node.getChildNodes());
			Node name = field.get(0);
			row.addHeaderCell(new HtmlView(name.getTextContent()));
			Node value = field.get(1);
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(value.getTextContent())));
			if(type.compareTo("Update") == 0) {
				Node newvalue = field.get(2);
				row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(newvalue.getTextContent())));
			}
			table.addRow(row);
		}
		
		return table;
	}
	
	private ArrayList<Node> pullNonTextNode(NodeList nodes)
	{
		ArrayList<Node> list = new ArrayList();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE) continue;
			list.add(node);
		}
		return list;
	}
	
	private SideContentView createSideView(DivExRoot root)
	{
		SideContentView view = new SideContentView();
		GenericView types = new GenericView();
		
		types.add(new LinkView("log?type=all", "All"));
		types.add(new HtmlView("<br/>"));
		
		types.add(new LinkView("log?type=resource", "Resource"));
		types.add(new HtmlView("<br/>"));
		
		types.add(new LinkView("log?type=vo", "Virtual Organization"));
		types.add(new HtmlView("<br/>"));
		
		types.add(new LinkView("log?type=sc", "Support Center"));
		types.add(new HtmlView("<br/>"));
		
		types.add(new LinkView("log?type=contact", "Contact"));
		types.add(new HtmlView("<br/>"));
		
		if(auth.allows("admin")) {
			types.add(new LinkView("log?type=site", "Site"));
			types.add(new HtmlView("<br/>"));
			
			types.add(new LinkView("log?type=facility", "Facility"));	
			types.add(new HtmlView("<br/>"));
		}
		view.add("Log Type", types);		
		return view;
	}
}
