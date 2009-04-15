package edu.iu.grid.oim.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.LinkView;
import edu.iu.grid.oim.view.ListView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class RSSServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RSSServlet.class);  
    
    public RSSServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//no authentication - believing what user says who they are..
		Integer user_id = Integer.parseInt(request.getParameter("user_id"));
		Authorization auth = new Authorization(user_id);

		try {
			//find the blogspot RSS URL for requested type
			String type = request.getParameter("type");
			URL rss = getURL(auth, type);
			
			//Proxy RSS feed 
			response.setContentType("application/atom+xml");
			BufferedReader in = new BufferedReader(new InputStreamReader(rss.openStream()));
			String inputLine;
			PrintWriter out = response.getWriter();
			while ((inputLine = in.readLine()) != null) {
			    out.write(inputLine);
			}
			in.close();
		} catch (SQLException e) {
			log.error(e);
		}
	}
	
	//Blogger doesn't support OR query!!!!!!!!!!!!!!!
	//I get following error when I try "Disjunctions not supported yet"
	//Starred this issue on Google code (http://code.google.com/p/gdata-issues/issues/detail?id=582)
	private URL getURL(Authorization auth, String type) throws MalformedURLException, SQLException
	{
		//For how to construct the GData URL,
		//http://code.google.com/apis/gdata/docs/2.0/reference.html	
		String url = "http://oimupdate.blogspot.com/feeds/posts/default";
		if(type.compareTo("all") == 0) {
			//all has no category to filter
		} else if(type.compareTo("mine") == 0) {
			if(auth.allows("admin")) {
				url += "/-/resource%7Cvo%7Csc%7Ccontact";
			} else {
				url += "/-/";
				ResourceModel rmodel = new ResourceModel(auth);
				for(ResourceRecord rrec : rmodel.getAllEditable()) {
					url += "resource_" + rrec.id + "%7C";
				}
				
				VOModel vomodel = new VOModel(auth);
				for(VORecord vorec : vomodel.getAllEditable()) {
					url += "vo_" + vorec.id + "%7C";
				}
				
				SCModel scmodel = new SCModel(auth);
				for(SCRecord screc : scmodel.getAllEditable()) {
					url += "sc_" + screc.id + "%7C";
				}
				
				ContactModel cmodel = new ContactModel(auth);
				for(ContactRecord crec : cmodel.getAllEditable()) {
					url += "sc_" + crec.id + "%7C";
				}
			}
		}
		log.info("Accessing Blogspot : " + url);
		return new URL(url);
	}

}
