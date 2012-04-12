package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;

import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;

public class ResourceGroupServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ResourceGroupServlet.class);  
	
    public ResourceGroupServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		//setContext(request);
		//auth.check("edit_all_resource_group");
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "resourcegroup");
			ContentView contentview = createContentView();
			/*
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Resource Group",  null);
			contentview.setBreadCrumb(bread_crumb);
			*/
			BootPage page = new BootPage(context, menuview, contentview, createSideView());
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
		contentview.add(new HtmlView("<h1>Resource Groups</h1>"));
	
		ResourceGroupModel model = new ResourceGroupModel(context);
		SiteModel site_model = new SiteModel (context);
		OsgGridTypeModel ogt_model = new OsgGridTypeModel (context);
		
		ArrayList<ResourceGroupRecord> rgs = model.getAll();
		Collections.sort(rgs, new Comparator<ResourceGroupRecord> () {
			public int compare(ResourceGroupRecord a, ResourceGroupRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		for(ResourceGroupRecord rec : rgs) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
			
			RecordTableView table = new RecordTableView();
			contentview.add(table);

			table.addRow("Description", rec.description);
			table.addRow("Site", site_model.get(rec.site_id).name);
			table.addRow("OSG Grid Type", ogt_model.get(rec.osg_grid_type_id).name);
			//table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			/*
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
			table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/resourcegroupedit?id=" + rec.id)));
			*/
			if(auth.allows("edit_all_resource_group")) {
				table.add(new HtmlView("<a class=\"btn\" href=\"resourcegroupedit?id="+rec.id+"\">Edit</a>"));
			}
		}
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		/*
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Resource Group");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		*/
		if(auth.isUser()) {
			view.add(new HtmlView("<a class=\"btn\" href=\"resourcegroupedit\">Add New Resource Group</a>"));
		}
		//view.add("About", new HtmlView("This page shows a list of Resource Groups that that all registered OIM users are able to edit. We ask that you please refrain from editing resource groups that are not directly related to you unless there is a specific reason to do so! All changes are audited by GOC staff."));		
		return view;
	}
}
