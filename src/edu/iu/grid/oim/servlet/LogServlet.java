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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
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
				
					//if user is not admin, I need to check the accessibility for each model
					Boolean simulate_non_admin = true;
					if(!auth.allows("admin") || simulate_non_admin) {
						//let's check to see if user has access to this record
						if(!somemodel.hasLogAccess(xpath, log)) {
							continue;
						}
					}				
					view.add(new HtmlView("<h2>" + somemodel.getName() + "("+rec.type+")</h2>"));
					
					DNRecord dnrec = dmodel.get(rec.dn_id);
					view.add(new HtmlView("<span class=\"right\">"+dnrec.dn_string+"<br/>Updated "+rec.timestamp.toString()+"</span>"));
					view.add(new CDataView(rec.xml));
				} catch (SAXException e) {
					view.add(new HtmlView("XML log Parse Error (" + somemodel.getName() + ") "+ e.toString()));
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
		
		types.add(new LinkView("log?type=contact", "Contat"));
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
