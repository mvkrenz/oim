package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class UserServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(UserServlet.class);  
	
    public UserServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		auth.check("admin");
		
		try {	
			//construct view
			MenuView menuview = new MenuView(context, "admin");
			ContentView contentview = createContentView();
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Users",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Users</h1>"));

		//pull list of all sites
		DNModel model = new DNModel(context);
		DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
		AuthorizationTypeModel authmodel = new AuthorizationTypeModel(context);
		
		for(DNRecord rec : model.getAll()) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.dn_string)+"</h2>"));
	
			RecordTableView table = new RecordTableView();
			contentview.add(table);

			ContactModel cmodel = new ContactModel(context);
			ContactRecord crec = cmodel.get(rec.contact_id);
			String contact = crec.name + " <" + crec.primary_email + ">";
 		 	table.addRow("Contact", contact);
	
			Collection<Integer/*auth_type*/> types = dnauthmodel.getAuthorizationTypesByDNID(rec.id);
			String auth_html = "";
			for(Integer auth_type : types) {
				AuthorizationTypeRecord auth_rec = authmodel.get(auth_type);
			 	auth_html += StringEscapeUtils.escapeHtml(auth_rec.name) + "<br/>";
			}
			table.addRow("Authorization Types", new HtmlView(auth_html));
		 	
			class EditButtonDE extends DivRepButton
			{
				String url;
				public EditButtonDE(DivRep parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(DivRepEvent e) {
					redirect(url);
				}
			};
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/useredit?id=" + rec.id)));
		}
		
		return contentview;
	}

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		view.add("About", new HtmlView("This page shows a list of DN entries with all associated information."));		
		return view;
	}
}
