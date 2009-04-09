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
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.view.divex.form.ContactFormDE;
import edu.iu.grid.oim.view.divex.form.SCFormDE;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class ProfileEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ProfileEditServlet.class);  
	private String current_page = "profileedit";	

    public ProfileEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		ContactRecord rec;
		try {
			rec = auth.getContact();
			if(rec == null) {
				//create new record
				ContactModel model = new ContactModel(auth);
				rec = new ContactRecord();
				rec.submitter_dn_id = auth.getDNID();
				model.insert(rec);//generated key are inserted back to the rec now.
				
				//associate with this dn
				DNModel dnmode = new DNModel(auth);
				//dnmode.update(dnmodel.get(olddn), newdn)
			}
				
			String origin_url = BaseURL()+"/"+current_page;
			ContactFormDE form = new ContactFormDE(DivExRoot.getInstance(request), rec, origin_url, auth);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>Edit Profile</h1>"));	
			contentview.add(new DivExWrapper(form));
			
			Page page = new Page(createMenuView(current_page), contentview, createSideView());
			page.render(response.getWriter());	
			
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("TODO", new HtmlView("Whatever"));
		return view;
	}
}