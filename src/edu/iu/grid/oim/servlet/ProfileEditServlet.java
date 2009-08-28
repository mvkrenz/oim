package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.divrep.form.ContactFormDE;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class ProfileEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ProfileEditServlet.class);  
	private String parent_page = "home";	
	private ContactFormDE form;
	
    public ProfileEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

		ContactRecord rec;
		try {
			rec = auth.getContact();
				
			String origin_url = StaticConfig.getApplicationBase()+"/"+parent_page;
			form = new ContactFormDE(context, rec, origin_url);
			
			//put the form in a view and display
			ContentView contentview = new ContentView();
			contentview.add(new HtmlView("<h1>Edit Your User Profile</h1>"));	
			
			contentview.add(new DivRepWrapper(form));
			
			Page page = new Page(context, new MenuView(context, "profileedit"), contentview, createSideView());
			page.render(response.getWriter());	
			
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private SideContentView createSideView() throws SQLException
	{
		SideContentView view = new SideContentView();
		view.add(new DivRepWrapper(new Confirmation(auth.getContactID(), context)));
		return view;
	}
	
	class Confirmation extends DivRep
	{
		DivRepButton confirm;
		final ContactRecord crec;
		final ContactModel cmodel;
		final Context context;
		
		public Confirmation(Integer contact_id, Context _context) throws SQLException {
			super(_context.getPageRoot());
			
	    	cmodel = new ContactModel(_context);
	    	crec = (ContactRecord) cmodel.get(contact_id).clone();	    	
	    	context = _context;
				
			confirm = new DivRepButton(this, "Confirm");
			confirm.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					Date d = new Date();
					Timestamp t = new Timestamp(d.getTime());
					t.setNanos(0);
					crec.confirmed = t;
					try {
						cmodel.update(cmodel.get(crec.id), crec);
						Confirmation.this.context.close();
						Confirmation.this.redraw();
						
						alert("Updated the confirmation date. Thank you!");
						form.setConfirmed(t);
					} catch (SQLException e1) {
						log.error(e1);
					}
				}
			});
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<h3>Content Confirmation</h3>");
			out.write("<p>Last confirmation: "+crec.confirmed.toString()+"</p>");
			if(crec.isConfirmationExpired()) {
				out.write("<p class=\"divrep_round divrep_elementerror\">You have not recently confirmed that your profile information is accurate</p>");
			}
			out.write("<p>If the information you see is accurate, please click following button for confirmation.</p>");
			confirm.render(out);
			out.write("</div>");
		}	
	}
}