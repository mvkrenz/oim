package edu.iu.grid.oim.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.Servlet;
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
import org.xml.sax.SAXException;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class FPTemplatesServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FPTemplatesServlet.class);  
	
    public FPTemplatesServlet() {
        // TODO Auto-generated constructor stub
    }
    

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("admin");
	
		try {			
			//construct view
			MenuView menuview = new MenuView(context, "admin");
			ContentView contentview = createContentView();
		
			PrintWriter out = response.getWriter();
			if(request.getParameter("plain") != null) {
				contentview.render(out);
			} else {
				//set crumbs
				BreadCrumbView bread_crumb = new BreadCrumbView();
				bread_crumb.addCrumb("Administration",  "admin");
				bread_crumb.addCrumb("Footprints Ticket Templates",  null);
				contentview.setBreadCrumb(bread_crumb);
				
				Page page = new Page(context, menuview, contentview, createSideView());
				page.render(out);			
			}
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{	
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Footprints Ticket Templates</h1>"));
		contentview.add(new HtmlView("<p>Following templates will be used to generate Footprints tickets after each Resource, VO, and SC registrartion.</p>"));
		
		DivRepPage page = context.getPageRoot();
		FPTemplatesForm form = new FPTemplatesForm(page, context);
		contentview.add(form);
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();

		/*
    	view.add(new HtmlView("<h3>Time Period</h3>"));
    	view.add(new HtmlView("<div class=\"indent\">"));
    	LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		keyvalues.put(7, "Last 7 days");
		keyvalues.put(14, "Last 14 days");
		keyvalues.put(30, "Last 30 days");
		keyvalues.put(365, "Last 365 days");
		final DivRepSelectBox select = new DivRepSelectBox(context.getPageRoot(), keyvalues);
		select.setLabel("Show Registration For");
		select.setValue(days);
		select.addEventListener(new DivRepEventListener(){
			public void handleEvent(DivRepEvent e) {
				String days = (String)e.value;
				select.redirect(StaticConfig.getApplicationBase() + "/reportregistration?days=" + days);
			}});
		view.add(new DivRepWrapper(select));		
		view.add(new HtmlView("</div>"));
		*/
		
		return view;
	}
	
	class FPTemplatesForm extends DivRepForm {
	
		private DivRepTextArea resource_template;	
		private DivRepTextArea vo_template;
		private DivRepTextArea sc_template;

		public FPTemplatesForm(DivRepPage page, Context context) throws AuthorizationException, SQLException
		{	
			super(page, StaticConfig.getApplicationBase()+"/admin");

			ConfigModel config = new ConfigModel(context);
			ContactModel contactmodel = new ContactModel(context);
			
			new DivRepStaticContent(this, "<h2>Resource Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##RESOURCE_NAME##</b> will be replaced by the actual resource name registered.</p>");
			resource_template = new DivRepTextArea(this);
			resource_template.setValue(config.ResourceFPTemplate.get());
			resource_template.setWidth(1000);
			resource_template.setHeight(200);
			
			/*
			contentview.add(new HtmlView("<h3>CC</h3>"));
			resource_contacts = new ContactEditor(page, contactmodel, false, false);
			resource_contacts.setShowRank(false);
			resource_contacts.setLabel("CC");
			addContacts(contactmodel, resource_contacts, config.ResourceFPCC.get());
			contentview.add(resource_contacts);
			*/
			
			new DivRepStaticContent(this, "<h2>VO Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##VO_NAME##</b> will be replaced by the actual VO name registered.</p>");
			vo_template = new DivRepTextArea(this);
			vo_template.setValue(config.VOFPTemplate.get());
			vo_template.setWidth(1000);
			vo_template.setHeight(200);

			/*
			contentview.add(new HtmlView("<h3>CC</h3>"));
			vo_contacts = new ContactEditor(page, contactmodel, false, false);
			vo_contacts.setShowRank(false);
			vo_contacts.setLabel("CC");
			addContacts(contactmodel, vo_contacts, config.VOFPCC.get());
			contentview.add(vo_contacts);
			*/
			
			new DivRepStaticContent(this, "<h2>SC Registration</h2>");
			new DivRepStaticContent(this, "<p><b>##SC_NAME##</b> will be replaced by the actual SC name registered.</p>");

			sc_template = new DivRepTextArea(this);
			sc_template.setValue(config.SCFPTemplate.get());
			sc_template.setWidth(1000);
			sc_template.setHeight(200);
			
			/*
			contentview.add(new HtmlView("<h3>CC</h3>"));
			sc_contacts = new ContactEditor(page, contactmodel, false, false);
			sc_contacts.setShowRank(false);
			addContacts(contactmodel, sc_contacts, config.SCFPCC.get());
			contentview.add(sc_contacts);
			*/
	
		}
		protected Boolean doSubmit() {
			ConfigModel config = new ConfigModel(context);
			try {
				config.ResourceFPTemplate.set(resource_template.getValue());
				config.VOFPTemplate.set(vo_template.getValue());
				config.SCFPTemplate.set(sc_template.getValue());
			} catch (SQLException e) {
				log.error("Failed to update config", e);
				this.alert("Sorry, failed to update config");
				return false;
			}
			return true;
		}
		
	}
}
