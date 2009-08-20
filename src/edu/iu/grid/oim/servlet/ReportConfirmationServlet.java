package edu.iu.grid.oim.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepSelectBox;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class ReportConfirmationServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ReportConfirmationServlet.class);  
	
    public ReportConfirmationServlet() {
        // TODO Auto-generated constructor stub
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		if(!auth.isLocal()) {
			auth.check("admin");
		}
		
		try {
			//construct view
			MenuView menuview = new MenuView(context, "admin");
			ContentView contentview = createContentView();
		
			//set crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Confirmation Report",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(context, menuview, contentview, createSideView());
			PrintWriter out = response.getWriter();
			page.render(out);			

		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() throws ServletException, SQLException
	{	
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Confirmation Report</h1>"));
		contentview.add(new HtmlView("<p>This pages shows lists of contacts who have not confirmed the content of OIM for more than "+StaticConfig.getConfirmationExpiration()+" days</p>"));
		contentview.add(new HtmlView("<p>This list only contains personal contact, and contact that are not disabled.</p>"));
		
		ResourceModel rmodel = new ResourceModel(context);
		ResourceContactModel rcontactmodel = new ResourceContactModel(context);
		
		VOModel vomodel = new VOModel(context);
		VOContactModel vocontactmodel = new VOContactModel(context);
		
		SCModel scmodel = new SCModel(context);
		SCContactModel sccontactmodel = new SCContactModel(context);
		
		ArrayList<ContactRecord> normal_list = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> critical_list = new ArrayList<ContactRecord>();
		HashMap<Integer, String> critical_details = new HashMap<Integer, String>();
		
		try {		
			ContactModel cmodel = new ContactModel(context);
			ArrayList<ContactRecord> recs = cmodel.getConfirmationExpiredPersonalContacts();
			contentview.add(new HtmlView("<p>Following " + recs.size() + " contact has not confirmed their profile out of " + cmodel.getAllNonDisabled().size() + " non-disabled contacts.</p>"));
			for(ContactRecord rec : recs) {
	
				//determine if this person is a security contact in sc, resource, or vo
				String critical_detail = "";
				
				ArrayList<ResourceContactRecord> rcrecs = rcontactmodel.getByContactID(rec.id);
				for(ResourceContactRecord rcrec : rcrecs) {
					if(rcrec.contact_type_id == 2) { //2 == security contact
						ResourceRecord rrec = rmodel.get(rcrec.resource_id);
						if(rrec.active && !rrec.disable) {
							critical_detail += "<p class=\"warning\">Resource Security Contact for "+rrec.name+"</p>";
						}
					}
				}
				
				ArrayList<VOContactRecord> vocrecs = vocontactmodel.getByContactID(rec.id);
				for(VOContactRecord vocrec : vocrecs) {
					if(vocrec.contact_type_id == 2) { //2 == security contact
						VORecord vorec = vomodel.get(vocrec.vo_id);
						if(vorec.active && !vorec.disable) {
							critical_detail += "<p class=\"warning\">VO Security Contact for "+vorec.name+"</p>";
						}
					}
				}		
			
				ArrayList<SCContactRecord> sccrecs = sccontactmodel.getByContactID(rec.id);
				for(SCContactRecord sccrec : sccrecs) {
					if(sccrec.contact_type_id == 2) { //2 == security contact
						SCRecord screc = scmodel.get(sccrec.sc_id);
						if(screc.active && !screc.disable) {
							critical_detail += "<p class=\"warning\">SC Security Contact for "+screc.name+"</p>";
						}
					}
				}	
				
				if(critical_detail.length() == 0) {
					normal_list.add(rec);
				} else {
					critical_list.add(rec);
					critical_details.put(rec.id, critical_detail);
				}
				
			}
			
			contentview.add(new HtmlView("<h2>Contacts who are not security contact</h2>"));
			for(ContactRecord rec : normal_list) {
				contentview.add(new HtmlView("<p><b>"+rec.name + " &lt;" + rec.primary_email+"&gt;</b></p>"));
			}
			
			contentview.add(new HtmlView("<br/><h2>Contacts who are security contact</h2>"));
			for(ContactRecord rec : critical_list) {
				contentview.add(new HtmlView("<p><b>"+rec.name + " &lt;" + rec.primary_email+"&gt;</b></p>"));
				contentview.add(new HtmlView("<div class=\"divrep_indent\">"));
				contentview.add(new HtmlView(critical_details.get(rec.id)));
				contentview.add(new HtmlView("</div>"));
			}			
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return contentview;
	}

	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		return view;
	}
}
