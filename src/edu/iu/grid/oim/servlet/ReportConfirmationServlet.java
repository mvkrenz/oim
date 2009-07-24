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

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.common.DivRepSelectBox;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepEvent;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;

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
			
			Page page = new Page(menuview, contentview, createSideView());
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
		
		contentview.add(new HtmlView("<div class=\"divrep_indent\">"));
		
		try {		
			ContactModel cmodel = new ContactModel(context);
			for(ContactRecord rec : cmodel.getConfirmationExpiredPersonalContacts()) {
				contentview.add(new HtmlView("<p>"+rec.name + " &lt;" + rec.primary_email+"&gt;</p>"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		contentview.add(new HtmlView("</div>"));
		
		return contentview;
	}

	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		return view;
	}
}
