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
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CampusGridModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.VOModel;

import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class ProjectServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ProjectServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		UserContext context = new UserContext(request);
		
		try {	
			//construct view
			BootMenuView menuview = new BootMenuView(context, "project");
			ContentView contentview = null;
			ProjectRecord rec = null;
			SideContentView sideview = null;
			String id_str = request.getParameter("id");
			if(id_str != null) {
				Integer id = Integer.parseInt(id_str);
				ProjectModel model = new ProjectModel(context);
				rec = model.get(id);
				contentview = new ContentView(context);
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Project", "project");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				contentview.add(createProjectContent(context, rec)); 
				sideview = createSideView(context, rec);
			} else {
				contentview = createListContentView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, sideview);
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createListContentView(UserContext context) throws ServletException, SQLException
	{
		ProjectModel model = new ProjectModel(context);
		ArrayList<ProjectRecord> projects = model.getAll();
		Collections.sort(projects, new Comparator<ProjectRecord> (){
			public int compare(ProjectRecord a, ProjectRecord b) {
				return a.name.compareToIgnoreCase(b.name); // We are comparing based on name
			}
		});
		
		ArrayList<ProjectRecord> editable_projects = new ArrayList<ProjectRecord>();
		ArrayList<ProjectRecord> readonly_projects = new ArrayList<ProjectRecord>();
		for(ProjectRecord rec : projects) {
			if(model.canEdit(rec.id)) {
				editable_projects.add(rec);
			} else {
				readonly_projects.add(rec);
			}
		}
		
		ContentView contentview = new ContentView(context);
		
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<a href=\"projectedit\" class=\"btn pull-right\"><i class=\"icon-plus-sign\"></i> Add New Project</a>"));
			contentview.add(new HtmlView("<h2>Your Projects</h2>"));
			if(editable_projects.size() == 0) {
				contentview.add(new HtmlView("<p>You currently have no project that youare authorized to edit</p>"));
			}
			ItemTableView table = new ItemTableView(6);
			for(final ProjectRecord rec : editable_projects) {
				String name = rec.name;
				/*
				String disable_css = "";
				String tag = "";
				if(rec.disable) {
					disable_css += " disabled";
					tag += " [Disabled]";
				}
				if(!rec.active) {
					disable_css += " inactive";
					tag += " [Inactive]";
				}
				table.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"voedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
				*/
				table.add(new HtmlView("<a title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"projectedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+"</a>"));
			}
			contentview.add(table);
		}
		
		if(readonly_projects.size() != 0) {
			contentview.add(new HtmlView("<h2>Other Projects</h2>"));
			//contentview.add(new HtmlView("<p>Following are the currently registered virtual organizations on OIM - you do not have edit access on these records.</p>"));
	
			ItemTableView table = new ItemTableView(5);
			for(final ProjectRecord rec : readonly_projects) {
				String name = rec.name;
				/*
				GenericView vo = new GenericView();		
				String disable_css = "";
				String tag = "";
				if(rec.disable) {
					disable_css += " disabled";
					tag += " (Disabled)";
				}
				if(!rec.active) {
					disable_css += " inactive";
					tag += " (Inactive)";
				}
				vo.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"vo?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
				//vo.add(new HtmlView("<p>"+StringEscapeUtils.escapeHtml(rec.long_name)+"</p>"));
				table.add(vo);
				*/
				table.add(new HtmlView("<a title=\""+StringEscapeUtils.escapeHtml(rec.name)+"\" href=\"project?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+"</a>"));
			}
			contentview.add(table);
		}
		
		return contentview;
	}

	public DivRep createProjectContent(UserContext context, final ProjectRecord rec) {
		RecordTableView table = new RecordTableView();
		try {	
			table.addRow("Name", rec.name);
			table.addRow("Description", rec.desc);
			table.addRow("Organization", rec.organization);
			table.addRow("Department", rec.department);
			
			//parent vo/cg
			if(rec.vo_id != null) {
				VOModel model = new VOModel(context);
				VORecord parent_vo_rec = model.get(rec.vo_id);
				table.addRow("Parent Virtual Organization", parent_vo_rec.name);
			}
			if(rec.cg_id != null) {
				CampusGridModel model = new CampusGridModel(context);
				CampusGridRecord parent_cg_rec = model.get(rec.cg_id);
				table.addRow("Parent Campus Grid", parent_cg_rec.name);
			}

			//PI
			ContactModel cmodel = new ContactModel(context);
			ContactRecord pi = cmodel.get(rec.pi_contact_id);
			table.addRow("Principal Investigator", pi.name);
			
			//fos
			FieldOfScienceModel model = new FieldOfScienceModel(context);
			FieldOfScienceRecord fos = model.get(rec.fos_id);
			table.addRow("Field Of Science", fos.name);
		
			
			//table.addRow("Active", rec.active);
			//table.addRow("Disable", rec.disable);
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
	}
	
	private SideContentView createSideView(UserContext context, ProjectRecord rec)
	{
		SideContentView view = new SideContentView();
		
		ProjectModel model = new ProjectModel(context);
		if(model.canEdit(rec.id)) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"projectedit?id=" + rec.id + "\">Edit</a></p>"));
		}
	
		return view;
	}
}
