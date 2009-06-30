package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.DivRepForm;
import com.webif.divrep.common.DivRepFormElement;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeActionRecord;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
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
		
		HashMap<Integer/*action_id*/, HashMap<Integer/*type_id*/, CheckBoxFormElement>> matrix = new HashMap();
		public void render(PrintWriter out) {
			try {
				out.print("<table class=\"auth_matrix\">");
				out.print("<tr><td></td><th colspan=\""+authtypes.size()+"\">Authorization Types</th></tr>");
				
				//show list of auth types
				out.print("<tr class=\"checklist\"><th valign=\"bottom\">Actions</th>");
				for(AuthorizationTypeRecord type : authtypes) {
					out.print("<td>"+type.name+"</td>");
				}
				out.print("</tr>");
				
				//now display all of our check boxes
				for(ActionRecord action : actionmodel.getAll()) {
					
					String tooltip = StringEscapeUtils.escapeHtml(action.description);
					if(tooltip.length() == 0) {
						tooltip = "(No Description given for this action)";
					}
					
					//name & check boxes
					out.print("<tr class=\"checklist\"><td class=\"tooltip\" tooltip=\""+tooltip+"\">"
							+StringEscapeUtils.escapeHtml(action.name)+"</td>");
					for(AuthorizationTypeRecord type : authtypes) {
						HashMap<Integer/*type_id*/, CheckBoxFormElement> clist = matrix.get(action.id);
						out.print("<td>");
						clist.get(type.id).render(out);
						out.print("</td>");
					}
					out.print("</tr>");
		
				}
				out.print("</table>");
			} catch (SQLException e) {
				out.println("SQL Error!");
			}
		}
		
		public AuthMatrix(DivRep parent, Context context) throws SQLException 
		{
			super(parent);
			
			actionmodel = new ActionModel(context); 
			matrixmodel = new AuthorizationTypeActionModel(context);
			AuthorizationTypeModel tmodel = new AuthorizationTypeModel(context); 
			authtypes = tmodel.getAll();
			
			//create checkboxes for each action for each authtype
			for(ActionRecord action : actionmodel.getAll()) {
				HashMap<Integer/*type_id*/, CheckBoxFormElement> as = new HashMap();
				matrix.put(action.id, as);
				Collection<Integer/*type_id*/> authorized = matrixmodel.getTypeByActionID(action.id);
				for(AuthorizationTypeRecord type : authtypes) {
					CheckBoxFormElement check = new CheckBoxFormElement(parent);
					as.put(type.id, check);
					if(authorized.contains(type.id)) {
						check.setValue(true);
					}
				}
			}
		}
		
		public 	ArrayList<AuthorizationTypeActionRecord> getMatrixRecords() 
		{
			ArrayList<AuthorizationTypeActionRecord> recs = new ArrayList();
			for(Integer action_id : matrix.keySet()) {
				HashMap<Integer/*type_id*/, CheckBoxFormElement> list = matrix.get(action_id);
				for(Integer type_id : list.keySet()) {
					CheckBoxFormElement check = list.get(type_id);
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
		public void validate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
    class AuthMatrixFormDE extends DivRepForm
    {
        private AuthMatrix matrix;
		public AuthMatrixFormDE(DivRep parent, String _origin_url) throws SQLException {
			super(parent, _origin_url);
			
			//pull action, auth_type, and matrix and construct matrix
			matrix = new AuthMatrix(parent, context);
			add(matrix);
		}

		protected Boolean doSubmit() {			
			AuthorizationTypeActionModel atamodel = new AuthorizationTypeActionModel(context);
			try {
				auth.check("admin_authorization");
				atamodel.update(atamodel.getAll(), matrix.getMatrixRecords());
			} catch (AuthorizationException e) {
				alert(e.getMessage());
				return false;
			} catch (SQLException e) {
				alert(e.getMessage());
				return false;
			}
			
			return true;
		}
    }
	
    public AuthorizationMatrixServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//setContext(request);
	
		//construct view
		MenuView menuview = new MenuView(context, "admin");
		ContentView contentview;
		
		try {
			contentview = createContentView();
			
			//setup crumbs
			BreadCrumbView bread_crumb = new BreadCrumbView();
			bread_crumb.addCrumb("Administration",  "admin");
			bread_crumb.addCrumb("Authorization Matrix",  null);
			contentview.setBreadCrumb(bread_crumb);
			
			Page page = new Page(menuview, contentview, createSideView());
			page.render(response.getWriter());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
			
	}
	
	protected ContentView createContentView() throws SQLException
	{			
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>Authorization Matrix</h1>"));
		DivRepForm form = new AuthMatrixFormDE(context.getPageRoot(), "admin");
		contentview.add(new DivRepWrapper(form));
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		return view;
	}

}
