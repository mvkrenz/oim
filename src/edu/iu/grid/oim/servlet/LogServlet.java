package edu.iu.grid.oim.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

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

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSelectBox;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ModelBase;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView.Row;

public class LogServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
	
    static Logger log = Logger.getLogger(LogServlet.class);  
    private Parameters params;
    
	XPath xpath = XPathFactory.newInstance().newXPath();
    
	abstract class List extends DivRep
	{
		public String title;
	
		public List(DivRep _parent, String _title) {
			super(_parent);
			title = _title;
		}

		protected void onEvent(DivRepEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		abstract public Collection<LogRecord> getRecords(Parameters params) throws SQLException;
		abstract public String getParameters();
	}
	
	class XMLFilterList extends List
	{
		String xml_filter;
		
		public XMLFilterList(DivRep _parent, HttpServletRequest request, String _title, String _xml_filter) {
			super(_parent, _title);
			xml_filter = _xml_filter;
		}

		public String getParameters() {
			return "";
		}

		@Override
		public Collection<LogRecord> getRecords(Parameters params) throws SQLException {
			LogModel lmodel = new LogModel(context);
			return lmodel.getLatest(params.getTransactionFilter(), params.getModelFilter(), params.getDays(), xml_filter);
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("</div>");
		}
		
	}
	
	class AllList extends List
	{    	
		public AllList(DivRep _parent, HttpServletRequest request) {
			super(_parent, "All Logs");
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<p class=\"info\">Table specific logs.</p>");
			out.write("</div>");
		}

		public Collection<LogRecord> getRecords(Parameters params) throws SQLException {
			LogModel lmodel = new LogModel(context);
			return lmodel.getLatest(params.getTransactionFilter(), params.getModelFilter(), params.getDays(), "%");
		}

		@Override
		public String getParameters() {
			return "";
		}
	}
	
    class Parameters extends DivRep
    {	
    	LinkedHashMap<Integer, List> lists;
    	private DivRepSelectBox listtype;

    	private LinkedHashMap<Integer, String> model_kv;
    	private DivRepSelectBox model;
    	
    	private LinkedHashMap<Integer, String> transaction_kv;
    	private DivRepSelectBox transaction;
    	
    	private DivRepSelectBox days;
    	
    	private DivRepButton update;
    	
    	public String getTransactionFilter()
    	{
    		if(transaction.getValue() != null) {
	    		switch(transaction.getValue()) {
	    		case 1:	return "insert";
	    		case 2:	return "update";
	    		case 3:	return "remove";
	    		}
    		}
    		return "%";
    	}
    	public String getModelFilter()
    	{
    		if(model.getValue() != null) {
	    		switch(model.getValue()) {
	    		case 1:	return "%.Resource%";
	    		case 2:	return "%.VO%";
	    		case 3:	return "%.SC%";
	    		case 4: return "%.Contact%";
	    		case 5: return "%.Site%";
	    		case 6: return "%.Facility%";
	    		case 7: return "%.ResourceWLCG%";
	    		}
    		}
    		return "%";
    	}
    	public int getDays()
    	{
    		if(days.getValue() == null) return 9999999;
    		return days.getValue();
    	}
    	
    	Parameters(DivRep parent, HttpServletRequest request) {
    		super(parent);
    		
    		lists = new LinkedHashMap<Integer, List>();
    		lists.put(1, new AllList(this, request));
    		lists.put(2, new XMLFilterList(this, request, "Activation Log", "%<Name>active</Name>%"));   		
    		
        	LinkedHashMap<Integer, String> list_kv = new LinkedHashMap<Integer, String>();
        	for(Integer id : lists.keySet()) {
        		list_kv.put(id, lists.get(id).title);
        	}
        	listtype = new DivRepSelectBox(this, list_kv);
        	listtype.setLabel("Log Type");
        	listtype.setValue(1);
        	listtype.setHasNull(false);
    		String str = request.getParameter("type");
    		if(str != null) {
    			listtype.setValue(Integer.parseInt(str));
    		}
    		listtype.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent arg0) {
					Parameters.this.redraw();
				}});
    		
    		LinkedHashMap<Integer, String> kv = new LinkedHashMap<Integer, String>();
    		kv.put(7, "Last 7 Days");  
    		kv.put(14, "Last 14 Days");
    		kv.put(30, "Last 30 Days");
    		kv.put(90, "Last 90 Days");
    		kv.put(180, "Last 180 Days");   
    		days = new DivRepSelectBox(this, kv);
    		days.setLabel("Time Period");
    		days.setValue(7);
    		days.setHasNull(false);
    		str = request.getParameter("days");
    		if(str != null) {
    			days.setValue(Integer.parseInt(str));
    		}
    		
    		transaction_kv = new LinkedHashMap<Integer, String>();
    		transaction_kv.put(1, "Insert");  
    		transaction_kv.put(2, "Update");
    		transaction_kv.put(3, "Remove");
    		transaction = new DivRepSelectBox(this, transaction_kv);
    		transaction.setLabel("Transaction");
    		transaction.setValue(null);
    		str = request.getParameter("transaction");
    		if(str != null) {
    			transaction.setValue(Integer.parseInt(str));
    		}
    		
    		model_kv = new LinkedHashMap<Integer, String>();
    		model_kv.put(1, "Resource");  
    		model_kv.put(2, "Virtual Organization");
    		model_kv.put(3, "Support Center");
    		model_kv.put(4, "Contact");
    		model_kv.put(5, "Site");
    		model_kv.put(6, "Facility");
    		model_kv.put(7, "Resource WLCG");
    		model = new DivRepSelectBox(this, model_kv);
    		model.setLabel("Model");
    		model.setValue(null);
    		str = request.getParameter("model");
    		if(str != null) {
    			model.setValue(Integer.parseInt(str));
    		}
    		
    		update = new DivRepButton(this, "Update Page");
    		update.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					redirect("?" + getParameters());
				}});
    	}
    	
    	//convert current parameter to a query string
    	public String getParameters() {
    		StringBuffer params = new StringBuffer();
    		if(listtype.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("type=" + listtype.getValue());
    		}
    		if(days.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("days=" + days.getValue());
    		}
    		if(transaction.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("transaction=" + transaction.getValue());
    		}
    		if(model.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("model=" + model.getValue());
    		}
    		
    		//list specific parameters
			if(params.length() != 0) params.append("&");
    		params.append(getCurrentList().getParameters());
    		
    		return params.toString();
    	}

		@Override
		protected void onEvent(DivRepEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");

			out.write("<h3>Logs to display</h3>");
	    	out.write("<div class=\"indent\">");
				listtype.render(out);
				out.write("<div class=\"indent\">");
					getCurrentList().render(out);
				out.write("</div>");
			out.write("</div>");
			
			out.write("<h3>Filters</h3>");
	    	out.write("<div class=\"indent\">");
				transaction.render(out);
				model.render(out);
				days.render(out);
			out.write("</div>");
			
			out.write("<br/>");
			update.render(out);
			
			out.write("</div>");
		}
		
		private List getCurrentList()
		{
			return lists.get(listtype.getValue());
		}
		public String getTitle() {
			return getCurrentList().title;
		}
		public Collection<LogRecord> getRecords() throws SQLException {
			return getCurrentList().getRecords(this);
		}
    }
    
    public LogServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		params = new Parameters(context.getPageRoot(), request);
		try {
			//construct view
			MenuView menuview = new MenuView(context, "log");
			ContentView contentview = createContentView(params);
			Page page = new Page(context, menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(Parameters params) throws ServletException, SQLException
	{
		ContentView view = new ContentView();	
		view.add(new HtmlView("<h1>"+params.getTitle()+"</h1>"));    	
    	
		try {
	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	factory.setNamespaceAware(false);
	    	factory.setValidating(false);
	    	DocumentBuilder builder = factory.newDocumentBuilder();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			DNModel dmodel = new DNModel(context);
			DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
			dformat.setTimeZone(getTimeZone());
			
			//pull log entries that matches the log type
			for(LogRecord rec : params.getRecords()) {
				
				//instantiate the model specified on the log (with Authorization as parameter)
				Class modelClass = Class.forName(rec.model);
				Constructor cons = modelClass.getConstructor(new Class[]{Context.class});
				ModelBase somemodel = (ModelBase) cons.newInstance(context);	
			
				try {
					byte[] bArray = rec.xml.getBytes();
					ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
					Document log = builder.parse(bais);
				
					/*
					if(!somemodel.hasLogAccess(xpath, log)) {
						continue;
					}
					*/
									
					//display the log
					String dn_string_to_print = "(DN not available)";
					if (rec.dn_id != null) {
						DNRecord dnrec = dmodel.get(rec.dn_id);
						if(dnrec != null) {
							dn_string_to_print = dnrec.dn_string;
						}
					}
					view.add(new HtmlView("<h2>" + somemodel.getName() + " ("+rec.type+")</h2>"));
					
					view.add(new HtmlView("<span class=\"sidenote\">By "+dn_string_to_print+"<br/>"+dformat.format(rec.timestamp)+ " (" + getTimeZone().getID() + ")</span>"));
					if(rec.comment != null) {
						view.add(new HtmlView("<p>"+StringEscapeUtils.escapeHtml(rec.comment)+"</p>"));
					}
					view.add(createLogView(xpath, somemodel, log));
				} catch (SAXException e) {
					view.add(new HtmlView("XML log Parse Error (" + somemodel.getName() + ") "+ e.toString()));
				} catch (XPathExpressionException e) {
					view.add(new HtmlView("XPath Expression Error (" + somemodel.getName() + ") "+ e.toString()));
				} catch (NullPointerException e) {
					//this happens if log xml contains invalid keys
					view.add(new HtmlView(e.toString()));
				}
			}
			
		} catch (Exception e) {
			throw new ServletException(e);
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
		ArrayList<Node> list = new ArrayList<Node>();
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
    	view.add(new DivRepWrapper(params));	
		
		return view;
	}
}
