package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ButtonView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;

public class VOServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOServlet.class);  
	
    public VOServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//pull list of all vos
		ResultSet vos = null;
		Authorization auth = new Authorization(request, con);
		VOModel model = new VOModel(con, auth);
		vos = model.getAllVOs();
		Set<Integer> accessible_ids = model.getAccessibleIDs();
		log.debug(accessible_ids);
			
		//construct view
		MenuView menuview = createMenuView(baseURL(), "vo");
		ContentView contentview = createContentView(vos, accessible_ids);
		Page page = new Page(menuview, contentview);
		
		response.getWriter().print(page.toHTML());
	}
	
	protected ContentView createContentView(ResultSet vos, Set<Integer> accessible_ids) 
		throws ServletException
	{
		ContentView contentview = new ContentView();	
		contentview.add("<h1>Virtual Organization</h1>");
		
		try {
			while(vos.next()) {
				VORecord rec = new VORecord(vos);
				contentview.add("<table class='record_table'>");
				contentview.add("<tr><th width='180px'>Name</th><td>"+nullImageFilter(rec.name)+"</td></tr>");
				contentview.add("<tr><th>Long Name</th><td>"+nullImageFilter(rec.long_name)+"</td></tr>");
				contentview.add("<tr><th>Description</th><td>"+nullImageFilter(rec.description)+"</td></tr>");
				contentview.add("<tr><th>Primary URL</th><td>"+nullImageFilter(rec.primary_url)+"</td></tr>");
				contentview.add("<tr><th>AUP URL</th><td>"+nullImageFilter(rec.aup_url)+"</td></tr>");
				contentview.add("<tr><th>Membership Services URL</th><td>"+nullImageFilter(rec.membership_services_url)+"</td></tr>");
				contentview.add("<tr><th>Purpose URL</th><td>"+nullImageFilter(rec.purpose_url)+"</td></tr>");
				contentview.add("<tr><th>Support URL</th><td>"+nullImageFilter(rec.support_url)+"</td></tr>");
				contentview.add("<tr><th>App Description</th><td>"+nullImageFilter(rec.app_description)+"</td></tr>");
				contentview.add("<tr><th>Community</td><td>"+nullImageFilter(rec.community)+"</th></tr>");

				if(accessible_ids.contains(rec.id)) {
					contentview.add("<tr><th></th><td>");
					contentview.add(new ButtonView("Edit", ServletBase.baseURL()+"/voedit?vo_id=" + rec.id));
					contentview.add("</td></tr>");
				}
				
				contentview.add("</table>");
			}
		} catch (SQLException e) {
	        throw new ServletException(e);
		} 
	
		return contentview;
	}
	
	private String nullImageFilter(String str)
	{
		if(str == null) {
			return "<img src='"+baseURL()+"/images/null.png'/>";
		}
		return StringEscapeUtils.escapeHtml(str);
	}
}
