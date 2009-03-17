package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.DivExRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divex.form.VOFormDivex;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	private String current_page = "vo";	

    public VOEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		VORecord rec;
		String title;

		//if vo_id is provided then we are doing update, otherwise do new.
		String vo_id_str = request.getParameter("vo_id");
		if(vo_id_str != null) {
			//pull record to update
			int vo_id = Integer.parseInt(vo_id_str);
			Authorization auth = new Authorization(request, con);
			VOModel model = new VOModel(con, auth);
			try {
				rec = model.get(vo_id);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Virtual Organization";
		} else {
			rec = new VORecord();
			title = "New Virtual Organization";	
		}
	
		VOFormDivex form;
		String origin_url = baseURL()+"/"+current_page;
		try {
			form = new VOFormDivex(DivExRoot.getInstance(request), rec, origin_url, con, auth);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add("<h1>"+title+"</h1>");	
		contentview.add("<h2>VO Details</h2>");
		contentview.add(form);
		
		Page page = new Page(createMenuView(baseURL(), current_page), contentview, createSideView());
		response.getWriter().print(page.toHTML());
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}