package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.divrep.form.MeshConfigFormDE;

public class MeshConfigServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(MeshConfigServlet.class);  

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_meshconfig");
			
		MeshConfigFormDE form;
		try {
			form = new MeshConfigFormDE(context, "home");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//put the form in a view and display
		ContentView contentview = new ContentView(context);		
		contentview.add(new DivRepWrapper(form));
				
		BootPage page = new BootPage(context, new BootMenuView(context, "meshconfig"), contentview, null);
		page.render(response.getWriter());	
	}	
}
