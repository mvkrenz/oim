package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class AdminAnnualReviewServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(AdminAnnualReviewServlet.class);  
	
    public AdminAnnualReviewServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("admin");
		
		try {
			//construct view
			MenuView menuview = new MenuView(context, "admin");
			DivRepPage divreppage = DivRepRoot.initPageRoot(request);
			ContentView contentview = createContentView(divreppage);
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Annual Review Controller",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(DivRepPage page) 
		throws ServletException, SQLException
	{
		ActionModel model = new ActionModel(context);
		Collection<ActionRecord> recs = model.getAll();
		
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Annual Review Controller</h1>"));

		final ConfigModel config = new ConfigModel(context);
		String review_open = config.get(Config.ANNUAL_REVIEW_OPEN);
		if(review_open == null) {
			contentview.add(new HtmlView("<h2>Open Annual Review Process</h2>"));
			contentview.add(new HtmlView("<p>Annual review process is currently closed.</p>"));
			contentview.add(new HtmlView("<div class=\"indent\">"));
			
			final DivRepTextBox name = new DivRepTextBox(page);
			name.setLabel("New Review Name");
			name.setSampleValue("Annual OIM review of 2009");
			name.setRequired(true);
			contentview.add(name);
			
			final DivRepButton openbutton = new DivRepButton(page, "Open Annual Review");
			openbutton.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					if(name.isValid()) {
						try {
							config.set(Config.ANNUAL_REVIEW_OPEN, name.getValue());
							//redraw the entire page
							openbutton.modified(false);
							openbutton.redirect("?");
						} catch (SQLException e1) {
							openbutton.alert(e1.toString());
						}
					} else {
						openbutton.alert("Please fix!");
					}
				}});
			contentview.add(openbutton);
			
			contentview.add(new HtmlView("</div>"));
		} else {
			contentview.add(new HtmlView("<h2>Annual Review Process</h2>"));
			contentview.add(new HtmlView("<p><b>" + review_open + "</b> is currently open</p>"));	
			
			final DivRepButton closebutton = new DivRepButton(page, "Close Annual Review");
			closebutton.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					try {
						config.set(Config.ANNUAL_REVIEW_OPEN, null);
						closebutton.modified(false);
						closebutton.redirect("?");
					} catch (SQLException e1) {
						closebutton.alert(e1.toString());
					}
				}});
			contentview.add(closebutton);
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
	
		//TODO
		
		return view;
	}
}
