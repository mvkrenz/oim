package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.RemoveFOSDialog;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class FieldOfScienceServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(FieldOfScienceServlet.class);  
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_fos");
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "fieldofscience");
			ContentView contentview = createContentView(context);
			/*
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			//bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Field of Science",  null);
			contentview.setBreadCrumb(bread_crumb);
			*/

			BootPage page = new BootPage(context, menuview, contentview, createSideView());
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{
		FieldOfScienceModel model = new FieldOfScienceModel(context);
		Collection<FieldOfScienceRecord> recs = model.getAll();
		
		ContentView contentview = new ContentView(context);	
		contentview.add(new HtmlView("<a class=\"btn pull-right\" href=\"fieldofscienceedit\">Add New Field Of Science</a>"));
		contentview.add(new HtmlView("<h2>Fields Of Science</h2>"));
		
		contentview.add(new HtmlView("<table class=\"table nohover\">"));
		contentview.add(new HtmlView("<thead><tr><th>Field Of Science</th><th>Used By</th><th></th><th></th></tr></thead>"));	
		
		VOFieldOfScienceModel vofosmodel = new VOFieldOfScienceModel(context);
		VOModel vomodel = new VOModel(context);
		ProjectModel pmodel = new ProjectModel(context);
		
		//sort fos by name
		ArrayList<FieldOfScienceRecord> _recs = (ArrayList<FieldOfScienceRecord>) recs;
		Collections.sort(_recs, new Comparator<FieldOfScienceRecord> () {
			public int compare(FieldOfScienceRecord a, FieldOfScienceRecord b) {
				return a.name.compareToIgnoreCase(b.name); // We are comparing based on name
			}
		});

		contentview.add(new HtmlView("<tbody>"));
		for(final FieldOfScienceRecord rec : recs) {
			contentview.add(new HtmlView("<tr>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.name)+"</td>"));	
			
			boolean used = false;
			
			//list of vo / projects using this fos
			contentview.add(new HtmlView("<td>"));
			ArrayList<VOFieldOfScienceRecord> vofosrecs = vofosmodel.getByFOS(rec.id);
			contentview.add(new HtmlView("<p>"));
			for(VOFieldOfScienceRecord vofosrec : vofosrecs) {
				VORecord vorec = vomodel.get(vofosrec.vo_id);
				contentview.add(new HtmlView("<span class='label label-info'>VO: "+vorec.name+"</span> "));
				
				used = true;
			}
			contentview.add(new HtmlView("</p>"));
			
			contentview.add(new HtmlView("<p>"));
			ArrayList<ProjectRecord> precs = pmodel.getByFOS(rec.id);
			for(ProjectRecord prec : precs) {
				contentview.add(new HtmlView("<span class='label label-warning'>Project: "+prec.name+"</span> "));
				
				used = true;
			}
			contentview.add(new HtmlView("</p>"));
			contentview.add(new HtmlView("</td>"));
			
			contentview.add(new HtmlView("<td>"));
			if(!used) {
				//TODO - this is very inefficient.. having dialog for every single fos that has delete button.
				final RemoveFOSDialog remove_fos_dialog = new RemoveFOSDialog(context.getPageRoot(), context);
				class RemoveButtonDE extends DivRepButton
				{
					public RemoveButtonDE(DivRep parent)
					{
						super(parent, "images/delete.png");
						setStyle(DivRepButton.Style.IMAGE);
						addClass("right");
					}
					protected void onEvent(DivRepEvent e) {
						remove_fos_dialog.setRecord(rec);
						remove_fos_dialog.open();	
					}
				};
				contentview.add(new RemoveButtonDE(context.getPageRoot()));
				contentview.add(remove_fos_dialog);
			}
			contentview.add(new HtmlView("</td>"));
			
			contentview.add(new HtmlView("<td>"));
			contentview.add(new HtmlView("<a class=\"btn btn-mini\" href=\"fieldofscienceedit?id="+rec.id+"\">Edit</a>"));
			contentview.add(new HtmlView("</td>"));
			contentview.add(new HtmlView("</tr>"));	
		}
		contentview.add(new HtmlView("</tbody>"));
		contentview.add(new HtmlView("</table>"));	
		
		return contentview;
	}
	
	
	private SideContentView createSideView()
	{
		return null;
		/*
		SideContentView view = new SideContentView();
		view.add(new HtmlView("<a class=\"btn\" href=\"fieldofscienceedit\">Add New Field Of Science</a>"));
		return view;
		*/
	}
	
}
