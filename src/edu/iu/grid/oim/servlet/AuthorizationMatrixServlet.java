package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeActionRecord;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class AuthorizationMatrixServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(AuthorizationMatrixServlet.class);  
   
	class AuthMatrix extends DivRepFormElement
	{		
		ActionModel actionmodel;
		AuthorizationTypeActionModel matrixmodel;
		ArrayList<AuthorizationTypeRecord> authtypes;
		HashMap<Integer/*action_id*/, HashMap<Integer/*type_id*/, DivRepCheckBox>> matrix = new HashMap();
		
		public AuthMatrix(DivRep parent, UserContext context) throws SQLException 
		{
			super(parent);
			
			actionmodel = new ActionModel(context); 
			matrixmodel = new AuthorizationTypeActionModel(context);
			AuthorizationTypeModel tmodel = new AuthorizationTypeModel(context); 
			authtypes = tmodel.getAll();
			
			//create checkboxes for each action for each authtype
			for(ActionRecord action : actionmodel.getAll()) {
				HashMap<Integer/*type_id*/, DivRepCheckBox> as = new HashMap();
				matrix.put(action.id, as);
				Collection<Integer/*type_id*/> authorized = matrixmodel.getTypeByActionID(action.id);
				for(AuthorizationTypeRecord type : authtypes) {
					DivRepCheckBox check = new DivRepCheckBox(this);
					as.put(type.id, check);
					if(authorized.contains(type.id)) {
						check.setValue(true);
					}
				}
			}
		}
		
		public void render(PrintWriter out) {
			try {
				out.write("<table class=\"auth_matrix\">");
				out.write("<tr><td></td><th colspan=\""+authtypes.size()+"\">Authorization Types</th></tr>");
				
				//show list of auth types
				out.write("<tr class=\"checklist\"><th valign=\"bottom\">Actions</th>");
				for(AuthorizationTypeRecord type : authtypes) {
					out.write("<td>"+type.name+"</td>");
				}
				out.write("</tr>");
				
				//now display all of our check boxes
				ArrayList<ActionRecord> actions = actionmodel.getAll();
				Collections.sort(actions, new Comparator<ActionRecord> () {
					public int compare(ActionRecord a, ActionRecord b) {
						return a.name.compareToIgnoreCase(b.name);
					}
				});
				for(ActionRecord action : actions) {
					
					/*
					String tooltip = StringEscapeUtils.escapeHtml(action.description);
					if(tooltip.length() == 0) {
						tooltip = "(No Description given for this action)";
					}
					*/
					
					//name & check boxes
					out.write("<tr class=\"checklist\"><td>"+StringEscapeUtils.escapeHtml(action.name)+"</td>");
					for(AuthorizationTypeRecord type : authtypes) {
						HashMap<Integer/*type_id*/, DivRepCheckBox> clist = matrix.get(action.id);
						out.write("<td>");
						clist.get(type.id).render(out);
						out.write("</td>");
					}
					out.write("</tr>");
				}
				out.write("</table>");
			} catch (SQLException e) {
				log.error("Failed to render AuthMetric", e);
			}
		}
		
		public 	ArrayList<AuthorizationTypeActionRecord> getMatrixRecords() 
		{
			ArrayList<AuthorizationTypeActionRecord> recs = new ArrayList();
			for(Integer action_id : matrix.keySet()) {
				HashMap<Integer/*type_id*/, DivRepCheckBox> list = matrix.get(action_id);
				for(Integer type_id : list.keySet()) {
					DivRepCheckBox check = list.get(type_id);
					if(check.getValue()) {
						AuthorizationTypeActionRecord rec = new AuthorizationTypeActionRecord();
						rec.action_id = action_id;
						rec.authorization_type_id = type_id;
						recs.add(rec);
					}
				}
			}
			return recs;
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
    class AuthMatrixFormDE extends DivRepForm {
        private AuthMatrix matrix;
        private UserContext context;
		public AuthMatrixFormDE(UserContext context, String _origin_url) throws SQLException {
			super(context.getPageRoot(), _origin_url);
			this.context = context;
			matrix = new AuthMatrix(this, context);
			//add(matrix);
		}

		protected Boolean doSubmit() {			
			AuthorizationTypeActionModel atamodel = new AuthorizationTypeActionModel(context);
			try {
				context.getAuthorization().check("admin_authorization");
				atamodel.update(atamodel.getAll(), matrix.getMatrixRecords());
			} catch (AuthorizationException e) {
				alert(e.getMessage());
				return false;
			} catch (SQLException e) {
				alert(e.getMessage());
				return false;
			}
			context.message(MessageType.SUCCESS, "Successfully registered new an authorization type.");
			return true;
		}
    }
	
    public AuthorizationMatrixServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin_authorization");

		//construct view
		BootMenuView menuview = new BootMenuView(context, "admin");
		BootPage page = new BootPage(context, menuview, new Content(context), null);
		page.render(response.getWriter());		
	}
	
	class Content implements IView {
		UserContext context;
		public Content(UserContext context) {
			this.context = context;
		}
		
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\"content\">");
			//setup crumbs
			BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Authorization Matrix",  null);
			bread_crumb.render(out);
			
			try {
				DivRepForm form = new AuthMatrixFormDE(context, "admin");
				form.render(out);
			} catch (SQLException e) {
				log.error("Failed to create auth matrix", e);
			}
			out.write("</div>");
		}
	}
}
