package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletException;
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
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ModelBase;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
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
		setContext(request);
		
		//pull log type
		String filter = null;
		String title = null;
		
		String dirty_type = request.getParameter("type");
		if(dirty_type == null || dirty_type.equals("all")) {
			filter = "%";
			title = "Logs";
		} else if(dirty_type.equals("resource")) {
			filter = "%Resource%";
			title = "Resource Logs";
		} else if(dirty_type.equals("vo")) {
			filter = "%VO%";
			title = "Virtual Organization Logs";
		} else if(dirty_type.equals("sc")) {
			filter = "%SC%";
			title = "Support Center Logs";
		} else if(dirty_type.equals("contact")) {
			filter = "%Contact%";
			title = "Contact Logs";
		} else if(dirty_type.equals("site")) {
			filter = "%Site%";
			title = "Site Logs";
		} else if(dirty_type.equals("facility")) {
			filter = "%Facility%";
			title = "Facility Logs";
		}
		
		try {
			//construct view
			MenuView menuview = new MenuView(context, "log");
			ContentView contentview = createContentView(filter, title);
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(String filter, String title) throws ServletException, SQLException
	{
		ContentView view = new ContentView();	
		view.add(new HtmlView("<h1>"+title+"</h1>"));    	
    	
		try {
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	DocumentBuilder builder;
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			DNModel dmodel = new DNModel(context);
			
			//pull log entries that matches the log type
			LogModel lmodel = new LogModel(context);
			Collection<LogRecord> recs = lmodel.getLatest(filter);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(LogRecord rec : recs) {

				//instantiate the model specified on the log (with Authorization as parameter)
				Class modelClass = Class.forName(rec.model);
				Constructor cons = modelClass.getConstructor(new Class[]{Context.class});
				ModelBase somemodel = (ModelBase) cons.newInstance(context);
				
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
					view.add(createLogView(xpath, somemodel, log));
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
	
	private IView createLogView(XPath xpath, ModelBase model, Document dom) throws XPathExpressionException
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
			String field_name = name.getTextContent();
			String human_value;
			try {
				human_value = model.getHumanValue(field_name, value.getTextContent());
			} catch (NumberFormatException e) {
				human_value = "(Format Exception)";
			} catch (SQLException e) {
				human_value = "(SQL Exception)";
			}
			row.addHeaderCell(new HtmlView(field_name));
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(human_value)));
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
			String field_name = name.getTextContent();
			row.addHeaderCell(new HtmlView(field_name));
			
			Node value = field.get(1);
			String human_value;
			try {
				human_value = model.getHumanValue(field_name, value.getTextContent());
			} catch (NumberFormatException e) {
				human_value = "(Format Exception)";
			} catch (SQLException e) {
				human_value = "(SQL Exception)";
			}
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(human_value)));
			
			if(type.compareTo("Update") == 0) {
				Node newvalue = field.get(2);
				try {
					human_value = model.getHumanValue(field_name, newvalue.getTextContent());
				} catch (NumberFormatException e) {
					human_value = "(Format Exception)";
				} catch (SQLException e) {
					human_value = "(SQL Exception)";
				}
				row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(human_value)));
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
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		ListView list = new ListView();
		list.add(new LinkView("log?type=all", "All"));
		list.add(new LinkView("log?type=resource", "Resource"));
		list.add(new LinkView("log?type=vo", "Virtual Organization" ));
		list.add(new LinkView("log?type=sc", "Support Center"));
		list.add(new LinkView("log?type=contact", "Contact"));		
		if(auth.allows("admin")) {
			list.add(new LinkView("log?type=site", "Site"));	
			list.add(new LinkView("log?type=facility", "Facility"));	
		}
		view.add("Log Type", list);		
		return view;
	}
}
