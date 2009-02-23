package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ButtonView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;

import org.apache.commons.lang.StringEscapeUtils;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	
    public VOEditServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String vo_id_str = request.getParameter("vo_id");
		VORecord vo = null;
		if(vo_id_str != null) {
			//edit record
			int vo_id = Integer.parseInt(vo_id_str);
			Authorization auth = new Authorization(request, con);
			VOModel model = new VOModel(con, auth);
			vo = model.getVO(vo_id);
		} else {
			//new record
			vo = new VORecord();
		}
		
		//construct view
		MenuView menuview = createMenuView(baseURL(), "vo");
		ContentView contentview = createView(vo);

		Page page = new Page(menuview, contentview);
		
		response.getWriter().print(page.toHTML());
	}
	
	protected ContentView createView(VORecord vo) 
		throws ServletException
	{
		ContentView contentview = new ContentView();
		
		String action;
		if(vo.id != null) {
			//edit vo
			contentview.add("<h1>Edit Virtual Organization</h1>");
			action = ServletBase.baseURL()+"/voedit?action=update&vo_id=" + vo.id;
		} else {
			//new vo
			contentview.add("<h1>Add Virtual Organization</h1>");	
			action = ServletBase.baseURL()+"/voedit?action=new";
		}

		contentview.add("<form action=\""+action+"\" method=\"post\">\n");

		contentview.add("<span>Name:</span>");
		contentview.add("<div><input type=\"edit\" name=\"name\" value=\""+
				StringEscapeUtils.escapeHtml(vo.name)+"\"></input></div>");
		
		contentview.add("<span>Long Name:</span>");
		contentview.add("<div><input type=\"edit\" name=\"long_name\" value=\""+
				StringEscapeUtils.escapeHtml(vo.long_name)+"\"></input></div>");
		
		contentview.add("<span>Description:</span>");
		contentview.add("<div><input type=\"edit\" name=\"description\" value=\""+
				StringEscapeUtils.escapeHtml(vo.description)+"\"></input></div>");
		
		contentview.add("<span>Primary URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"primary_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.primary_url)+"\"></input></div>");	
		
		contentview.add("<span>AUP URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"aup_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.aup_url)+"\"></input></div>");	
		
		contentview.add("<span>Membership Services URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"membership_services_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.membership_services_url)+"\"></input></div>");

		contentview.add("<span>Purpose URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"purpose_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.purpose_url)+"\"></input></div>");

		contentview.add("<span>Support URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"support_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.support_url)+"\"></input></div>");

		contentview.add("<span>App Description:</span>");
		contentview.add("<div><input type=\"edit\" name=\"app_description\" value=\""+
				StringEscapeUtils.escapeHtml(vo.app_description)+"\"></input></div>");
		
		contentview.add("<span>Community:</span>");
		contentview.add("<div><input type=\"edit\" name=\"community\" value=\""+
				StringEscapeUtils.escapeHtml(vo.community)+"\"></input></div>");
				
		contentview.add("</form>\n");
		return contentview;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
