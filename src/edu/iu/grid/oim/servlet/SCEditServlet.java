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
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.view.divex.form.SCFormDE;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class SCEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(SCEditServlet.class);  
	private String current_page = "sc";	

    public SCEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		SCRecord rec;
		String title;

		//if sc_id is provided then we are doing update, otherwise do new.
		String sc_id_str = request.getParameter("sc_id");
		if(sc_id_str != null) {
			//pull record to update
			int sc_id = Integer.parseInt(sc_id_str);
			SCModel model = new SCModel(auth);
			if(!model.canEdit(sc_id)) {
				throw new ServletException("Sorry, you don't have permission to edit this SC " + sc_id);
			}
			
			try {
				SCRecord keyrec = new SCRecord();
				keyrec.id = sc_id;
				rec = model.get(keyrec);
			} catch (SQLException e) {
				throw new ServletException(e);
			}	
			title = "Update Support Center";
		} else {
			rec = new SCRecord();
			title = "New Support Center";	
		}
	
		SCFormDE form;
		String origin_url = BaseURL()+"/"+current_page;
		try {
			form = new SCFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>"+title+"</h1>"));	
		contentview.add(new DivExWrapper(form));
		
		Page page = new Page(createMenuView(current_page), contentview, createSideView());
		
		page.addExternalCSS(BaseURL()+"/jquery/plugin/jquery.autocomplete.css");
		page.addExternalJS(BaseURL()+"/jquery/plugin/jquery.autocomplete.js");
		page.addExternalJS(BaseURL()+"/autocomplete.js");
		
		page.render(response.getWriter());	
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}