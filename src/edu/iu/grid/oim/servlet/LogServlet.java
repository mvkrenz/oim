package edu.iu.grid.oim.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ModelBase;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
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
	
	class ActivationList extends List
	{
		public ActivationList(DivRep _parent, HttpServletRequest request) {
			super(_parent, "Activation Log");
			//xml_reg = "(<Name>)(active|disable)(</Name>)";
		}

		public String getParameters() {
			return "";
		}

		public Collection<LogRecord> getRecords(Parameters params) throws SQLException {
			LogModel lmodel = new LogModel(context);
			Collection<LogRecord> recs = new ArrayList<LogRecord>();
			for(LogRecord rec : lmodel.getDateRange(params.getStartTime(), params.getEndTime())) {
				if(params.filter(rec)) {
					if(rec.xml.contains("<Name>active</Name>") || rec.xml.contains("<Name>disable</Name>")) {
						recs.add(rec);
					}
				}
			}
			return recs;
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<p class=\"info\">Show logs that are related to activation and enabling.</p>");
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
			//out.write("<p class=\"info\">Table specific logs.</p>");
			out.write("</div>");
		}

		public Collection<LogRecord> getRecords(Parameters params) throws SQLException {
			LogModel lmodel = new LogModel(context);
			Collection<LogRecord> recs = new ArrayList<LogRecord>();
			for(LogRecord rec : lmodel.getDateRange(params.getStartTime(), params.getEndTime())) {
				if(params.filter(rec)) {
					recs.add(rec);
				}
			}
			return recs;
		}

		@Override
		public String getParameters() {
			return "";
		}
	}
	
	class SpecificList extends List
	{    	
		DivRepTextBox id;
		public SpecificList(DivRep _parent, HttpServletRequest request) {
			super(_parent, "ID Specific Log");
			
			id = new DivRepTextBox(this);
			id.setLabel("Log ID");
			id.setValue(request.getParameter("id"));
			id.setWidth(100);
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<p class=\"info\">Show a specific log from the Log ID. All other criterias are ignored.</p>");
			id.render(out);
			out.write("</div>");
		}

		public Collection<LogRecord> getRecords(Parameters params) throws SQLException {
			LogModel lmodel = new LogModel(context);
			Collection<LogRecord> recs = new ArrayList<LogRecord>();
			String id_str = id.getValue();
			if(id_str != null) {
				LogRecord rec = lmodel.get(Integer.parseInt(id.getValue()));
				if(rec != null) {
					recs.add(rec);
				}
			}
			return recs;
		}

		public String getParameters() {
			if(id.getValue() != null) {
				return "id=" + id.getValue();
			} 
			return "";
		}
	}
	
    class Parameters extends DivRep
    {    	
    	LinkedHashMap<Integer, List> lists;
    	private DivRepSelectBox listtype;
    	
    	private LinkedHashMap<Integer, DivRepCheckBox> models;
    	private LinkedHashMap<Integer, DivRepCheckBox> transactions;
    	
    	private DivRepSelectBox start_type;
    	private DivRepDate start_date;
    	
    	private DivRepSelectBox end_type;
    	private DivRepDate end_date;
    	
    	private DivRepButton update;
    	
    	public Boolean filter(LogRecord rec)
    	{
    		if(!isTransactionIncluded(rec.type)) return false;
    		if(!isModelIncluded(rec.model)) return false;
    		return true;
    	}
    	
    	public Boolean isTransactionIncluded(String transaction)
    	{
    		for(Integer id : transactions.keySet()) {
    			DivRepCheckBox check = transactions.get(id);
    			if(check.getValue()) {
	    			String pattern = "";
	    			switch(id) {
	    			case 1: pattern = "insert"; break;
	    			case 2: pattern = "update"; break;
	    			case 3: pattern = "remove"; break;
	    			}
	    			if(transaction.equals(pattern)) {
	    				return true;
	    			}
    			}
    		}
    		return false;
    	}
    	public Boolean isModelIncluded(String model)
    	{
    		for(Integer id : models.keySet()) {
    			DivRepCheckBox check = models.get(id);
    			if(check.getValue()) {
	    			String pattern = "";
	    			switch(id) {
	    			case 1: pattern = ".Resource"; break;
	    			case 2: pattern = ".VO"; break;
	    			case 3: pattern = ".SC"; break;
	    			case 4: pattern = ".Contact"; break;
	    			case 5: pattern = ".Site"; break;
	    			case 6: pattern = ".Facility"; break;
	    			case 7: pattern = ".ResourceWLCG"; break;
	    			
	    			case 8: pattern = ".Action"; break;
	    			case 9: pattern = ".Authorization"; break;
	    			case 10: pattern = ".DN"; break;
	    			case 11: pattern = ".CpuInfo"; break;
	    			case 12: pattern = ".FieldOfScience"; break;
	    			case 13: pattern = ".Metric"; break;
	    			case 14: pattern = ".Service"; break;
	    			}
	    			if(model.contains(pattern)) {
		    			return true;
	    			}
    			}
    		}
    		return false;
    	}
    	public Timestamp getStartTime()
    	{
    		Calendar now = Calendar.getInstance();
    		switch(start_type.getValue()) {
    		//last 24 hours
    		case 1: return new Timestamp(now.getTimeInMillis() - 1000L*3600*24);
    		//last 7 days
    		case 2: return new Timestamp(now.getTimeInMillis() - 1000L*3600*24 * 7);
    		//last 14 days
    		case 3: return new Timestamp(now.getTimeInMillis() - 1000L*3600*24 * 14);
    		//last 30 days
    		case 4: return new Timestamp(now.getTimeInMillis() - 1000L*3600*24 * 30);
    		//last 90 days
    		case 5: return new Timestamp(now.getTimeInMillis() - 1000L*3600*24 * 90);
    		//custom start date
    		case 999: return new Timestamp(start_date.getValue().getTime());
    		}
    		return null;
    	}
    	public Timestamp getEndTime()
    	{
    		Calendar now = Calendar.getInstance();
    		switch(end_type.getValue()) {
    		//Now
    		case 1: return new Timestamp(now.getTimeInMillis());
    		//custom start date
    		case 999: return new Timestamp(end_date.getValue().getTime());
    		}
    		return null;
    	}
    	
    	Parameters(DivRep parent, HttpServletRequest request) {
    		super(parent);
    		
    		lists = new LinkedHashMap<Integer, List>();
    		lists.put(1, new AllList(this, request));
    		lists.put(2, new ActivationList(this, request));   	
    		lists.put(3, new SpecificList(this, request));  
    		/*
    		lists.put(4, new ResourceHistory(this, request));  
    		lists.put(5, new VOHistory(this, request));  
    		lists.put(6, new SCHistory(this, request));  
    		*/
    		
        	LinkedHashMap<Integer, String> list_kv = new LinkedHashMap<Integer, String>();
        	for(Integer id : lists.keySet()) {
        		list_kv.put(id, lists.get(id).title);
        	}
        	listtype = new DivRepSelectBox(this, list_kv);
        	listtype.setLabel("Display");
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
    		kv.put(1, "24 Hours Ago");  
    		kv.put(2, "7 Days Ago");
    		kv.put(3, "14 Days Ago");
    		kv.put(4, "30 Days Ago");
    		kv.put(5, "90 Days Ago");
    		kv.put(999, "(Specify Date)");
    		start_type = new DivRepSelectBox(this, kv);
    		start_type.setLabel("Start Date");
    		start_type.setValue(1);
    		start_type.setHasNull(false);
    		start_type.addEventListener(new DivRepEventListener(){
				public void handleEvent(DivRepEvent e) {
					Integer v = Integer.parseInt((String) e.value);
					start_date.setHidden(!v.equals(999));
					start_date.redraw();
				}});
    		str = request.getParameter("start_type");
    		if(str != null) { start_type.setValue(Integer.parseInt(str)); }
    		
    		start_date = new DivRepDate(this);
    		str = request.getParameter("start_date");
    		if(str != null) { 
    			Long date = Long.parseLong(str);
    			start_date.setValue(new Date(date)); 
    		}
    		if(!start_type.getValue().equals(999)) {
    			start_date.setHidden(true);
    		}
    		
    		kv = new LinkedHashMap<Integer, String>();
    		kv.put(1, "Now");  
    		kv.put(999, "(Specify Date)");
    		end_type = new DivRepSelectBox(this, kv);
    		end_type.setLabel("End Date");
    		end_type.setValue(1);
    		end_type.setHasNull(false);
    		end_type.addEventListener(new DivRepEventListener(){
				public void handleEvent(DivRepEvent e) {
					Integer v = Integer.parseInt((String) e.value);
					end_date.setHidden(!v.equals(999));
					end_date.redraw();
				}});
    		str = request.getParameter("end_type");
    		if(str != null) { end_type.setValue(Integer.parseInt(str)); }
    		
    		end_date = new DivRepDate(this);
    		str = request.getParameter("end_date");
    		if(str != null) { 
    			Long date = Long.parseLong(str);
    			end_date.setValue(new Date(date)); 
    		}
    		if(!end_type.getValue().equals(999)) {
    			end_date.setHidden(true);
    		}
    		
    		///////////////////////////////////////////////////////////////////////////////////////
    		//Transaction Types
        	transactions = new LinkedHashMap<Integer, DivRepCheckBox>();
        	DivRepCheckBox item;
        	item = new DivRepCheckBox(this);
        	item.setLabel("Insert");
    		if(request.getParameter("transaction_1") != null) {item.setValue(true);}
        	transactions.put(1, item);
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Update");
    		if(request.getParameter("transaction_2") != null) {item.setValue(true);}
        	transactions.put(2, item);
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Remove");
    		if(request.getParameter("transaction_3") != null) {item.setValue(true);}
        	transactions.put(3, item);

    		///////////////////////////////////////////////////////////////////////////////////////
    		//Model Types
        	models = new LinkedHashMap<Integer, DivRepCheckBox>();
        	item = new DivRepCheckBox(this);
        	item.setLabel("Resource");
    		if(request.getParameter("model_1") != null) {item.setValue(true);}
        	models.put(1, item);  
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Virtual Organization");
    		if(request.getParameter("model_2") != null) {item.setValue(true);}
        	models.put(2, item);
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Support Center");
    		if(request.getParameter("model_3") != null) {item.setValue(true);}
        	models.put(3, item);  
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Contact");
    		if(request.getParameter("model_4") != null) {item.setValue(true);}
        	models.put(4, item);  
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Site");
    		if(request.getParameter("model_5") != null) {item.setValue(true);}
        	models.put(5, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Facility");
    		if(request.getParameter("model_6") != null) {item.setValue(true);}
        	models.put(6, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("ResourceWLCG");
    		if(request.getParameter("model_7") != null) {item.setValue(true);}
        	models.put(7, item); 

        	item = new DivRepCheckBox(this);
        	item.setLabel("Action");
    		if(request.getParameter("model_8") != null) {item.setValue(true);}
        	models.put(8, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Authorization");
    		if(request.getParameter("model_9") != null) {item.setValue(true);}
        	models.put(9, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("DN");
    		if(request.getParameter("model_10") != null) {item.setValue(true);}
        	models.put(10, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("CPU Info");
    		if(request.getParameter("model_11") != null) {item.setValue(true);}
        	models.put(11, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Fields Of Science");
    		if(request.getParameter("model_12") != null) {item.setValue(true);}
        	models.put(12, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Metric");
    		if(request.getParameter("model_13") != null) {item.setValue(true);}
        	models.put(13, item); 
        	
        	item = new DivRepCheckBox(this);
        	item.setLabel("Service");
    		if(request.getParameter("model_14") != null) {item.setValue(true);}
        	models.put(14, item); 
        	
    		update = new DivRepButton(this, "Update Page");
    		update.addClass("btn");
    		update.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					redirect("log?" + getParameters());
				}});
    	}
    	
    	//convert current parameter to a query string
    	public String getParameters() {
    		StringBuffer params = new StringBuffer();
    		if(listtype.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("type=" + listtype.getValue());
    		}
    		if(start_type.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("start_type=" + start_type.getValue());
    		}
    		if(start_date.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("start_date=" + start_date.getValue().getTime());
    		}
    		if(end_type.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("end_type=" + end_type.getValue());
    		}
    		if(end_date.getValue() != null) {
    			if(params.length() != 0) params.append("&");
    			params.append("end_date=" + end_date.getValue().getTime());
    		}
    		for(Integer id : transactions.keySet()) {
    			DivRepCheckBox check = transactions.get(id);
	    		if(check.getValue() == true) {
	    			if(params.length() != 0) params.append("&");
	    			params.append("transaction_"+id+"=on");
	    		}
    		}
      		for(Integer id : models.keySet()) {
    			DivRepCheckBox check = models.get(id);
	    		if(check.getValue() == true) {
	    			if(params.length() != 0) params.append("&");
	    			params.append("model_"+id+"=on");
	    		}
    		}
    		
    		//list specific parameters
			if(params.length() != 0) params.append("&");
    		params.append(getCurrentList().getParameters());
    		
    		return params.toString();
    	}

		protected void onEvent(DivRepEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");

			listtype.render(out);
			getCurrentList().render(out);

			start_type.render(out);
			start_date.render(out);
			end_type.render(out);
			end_date.render(out);

	    	out.write("<p><b>Transaction Type</b>");
			for(DivRepCheckBox transaction : transactions.values()) {
				transaction.render(out);
			}
			out.write("</p>");
			
	    	out.write("<p><b>Model Type</b>");
			for(DivRepCheckBox model : models.values()) {
				model.render(out);
			}
			out.write("</p>");

			out.write("<p>");
			update.render(out);
			out.write("</p>");
			
			out.write("<h3>Subscribe</h3>");
		    out.write("<a target=\"_blank\" class=\"btn\" href=\"log?xml=true&"+getParameters()+"\">XML</a>");			
			
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
		auth.check("read_all_logs");

		params = new Parameters(context.getPageRoot(), request);
		try {
			if(request.getParameter("xml") == null) {
				//construct HTML
				BootMenuView menuview = new BootMenuView(context, "log");
				ContentView contentview = createContentView(params);
				BootPage page = new BootPage(context, menuview, contentview, createSideView());
				page.render(response.getWriter());		
			} else {
				//construct XML
				response.setHeader("Content-type", "text/xml");
				outputXML(response.getWriter());
			}
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected void outputXML(PrintWriter out) throws ServletException, SQLException
	{
		out.write("<Logs>");
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
			Collection<LogRecord> recs = params.getRecords();
			for(LogRecord rec : recs) {
				out.write("<Log>");
				
				out.write("<ID>"+rec.id+"</ID>");
				
				//instantiate the model specified on the log (with Authorization as parameter)
				Class modelClass = Class.forName(rec.model);
				Constructor cons = modelClass.getConstructor(new Class[]{Context.class});
				ModelBase somemodel = (ModelBase) cons.newInstance(context);	
	
				byte[] bArray = rec.xml.getBytes();
				ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
				Document log = builder.parse(bais);
								
				//display the log
				String dn_string_to_print = "(DN not available)";
				if (rec.dn_id != null) {
					DNRecord dnrec = dmodel.get(rec.dn_id);
					if(dnrec != null) {
						dn_string_to_print = dnrec.dn_string;
					}
				}
				out.write("<Model><Name>" + StringEscapeUtils.escapeHtml(somemodel.getName()) + "</Name>");
				out.write("<ID>"+rec.model+"</ID></Model>");
				out.write("<Transaction>"+rec.type+"</Transaction>");
				
				out.write("<DN>" + dn_string_to_print+"</DN>");
				out.write("<Timestamp>" + rec.timestamp + "</Timestamp>");
				
				out.write("<Comment>");
				if(rec.comment != null) {
					out.write(StringEscapeUtils.escapeHtml(rec.comment));
				}
				out.write("</Comment>");
				out.write(rec.xml);	
				
				out.write("</Log>");
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}		
		
		out.write("</Logs>");
	}
	
	protected ContentView createContentView(Parameters params) throws ServletException, SQLException
	{
		ContentView view = new ContentView();	
		view.add(new HtmlView("<h2>"+params.getTitle()+"</h2>"));    	
    	
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
			Collection<LogRecord> recs = params.getRecords();
			if(recs.size() == 0) {
				view.add(new HtmlView("<p>No Log entry matches your current criteria. Please adjust.</p>"));
			} else {
				for(LogRecord rec : recs) {
					
					//instantiate the model specified on the log (with Authorization as parameter)
					Class modelClass = Class.forName(rec.model);
					Constructor cons = modelClass.getConstructor(new Class[]{Context.class});
					ModelBase somemodel = (ModelBase) cons.newInstance(context);	
				
					try {
						byte[] bArray = rec.xml.getBytes();
						ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
						Document log = builder.parse(bais);
										
						//display the log
						String dn_string_to_print = "(DN not available)";
						if (rec.dn_id != null) {
							DNRecord dnrec = dmodel.get(rec.dn_id);
							if(dnrec != null) {
								dn_string_to_print = dnrec.dn_string;
							}
						}
						view.add(new HtmlView("<h3 class=\"logheader\">" + somemodel.getName() + " ("+rec.type+")<a href=\"log?type=3&id="+rec.id+"\" class=\"sidenote\">"+rec.id+"</a></h3>"));
						
						view.add(new HtmlView("<div class=\"sidenote\">By "+dn_string_to_print+"<br/>"+dformat.format(rec.timestamp)+ " (" + getTimeZone().getID() + ")</div>"));
						if(rec.comment != null) {
							view.add(new HtmlView("<p>"+StringEscapeUtils.escapeHtml(rec.comment)+"</p>"));
						}
						view.add(createLogView(xpath, somemodel, log));
					} catch (SAXException e) {
						view.add(new HtmlView("XML log Parse Error (" + somemodel.getName() + ") "+ e.toString()));
					} catch (XPathExpressionException e) {
						view.add(new HtmlView("XPath Expression Error (" + somemodel.getName() + ") "+ e.toString()));
					}
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
			String name = key.get(0).getTextContent();
			String value = key.get(1).getTextContent();
			String human_value;
			try {
				human_value = model.getHumanValue(name, value);
			} catch (NumberFormatException e) {
				human_value = value + " (Format Exception)";
			} catch (SQLException e) {
				human_value = value + " (SQL Exception)";
			} catch (NullPointerException e) {
				human_value = value + " (Record no longer exists)";
			}
			row.addHeaderCell(new HtmlView("<img align=\"top\" src=\"images/key.png\" /> "+name));
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
			
			String name = field.get(0).getTextContent();
			String value = field.get(1).getTextContent();
			String human_value;
			row.addHeaderCell(new HtmlView(name));
			try {
				human_value = model.getHumanValue(name, value);
			} catch (NumberFormatException e) {
				human_value = value + " (Format Exception)";
			} catch (SQLException e) {
				human_value = value + " (SQL Exception)";
			} catch (NullPointerException e) {
				human_value = value + " (Record no longer exists)";
			}
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(human_value)));
			
			if(type.compareTo("Update") == 0) {
				String newvalue = field.get(2).getTextContent();
				try {
					human_value = model.getHumanValue(name, newvalue);
				} catch (NumberFormatException e) {
					human_value = newvalue + " (Format Exception)";
				} catch (SQLException e) {
					human_value = newvalue + " (SQL Exception)";
				} catch (NullPointerException e) {
					human_value = newvalue + " (Record no longer exists)";
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
