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

import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormDE;
import com.webif.divex.form.FormElementDEBase;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
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
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class AuthorizationMatrixServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(AuthorizationMatrixServlet.class);  
   
	class AuthMatrix extends FormElementDEBase
	{		
		AuthorizationTypeModel tmodel;
		ActionModel amodel;
		AuthorizationTypeActionModel matrixmodel;
		
		HashMap<Integer/*action_id*/, HashMap<Integer/*type_id*/, CheckBoxFormElementDE>> matrix = new HashMap();
		public void render(PrintWriter out) {
			try {
				out.print("<table class=\"auth_matrix\">");
				
				//show list of auth types
				out.print("<tr><td></td>");
				for(AuthorizationTypeRecord type : tmodel.getAll()) {
					out.print("<th>"+type.name+"</th>");
				}
				out.print("</tr>");
				
				//now display all of our check boxes
				for(ActionRecord action : amodel.getAll()) {
					out.print("<tr class=\"checklist\"><th>"+action.name+"</th>");
					for(AuthorizationTypeRecord type : tmodel.getAll()) {
						HashMap<Integer/*type_id*/, CheckBoxFormElementDE> clist = matrix.get(action.id);
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
		
		public AuthMatrix(DivEx parent,
				AuthorizationTypeModel _tmodel, 
				ActionModel _amodel, 
				AuthorizationTypeActionModel _matrixmodel) throws SQLException {
			super(parent);
			tmodel = _tmodel;
			amodel = _amodel;
			matrixmodel = _matrixmodel;
			
			//create checkboxes for each action for each authtype
			for(ActionRecord action : amodel.getAll()) {
				HashMap<Integer/*type_id*/, CheckBoxFormElementDE> as = new HashMap();
				matrix.put(action.id, as);
				Collection<Integer/*type_id*/> authorized = matrixmodel.getTypeByActionID(action.id);
				for(AuthorizationTypeRecord type : tmodel.getAll()) {
					CheckBoxFormElementDE check = new CheckBoxFormElementDE(parent);
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
				HashMap<Integer/*type_id*/, CheckBoxFormElementDE> list = matrix.get(action_id);
				for(Integer type_id : list.keySet()) {
					CheckBoxFormElementDE check = list.get(type_id);
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
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}
	}
	
    class AuthMatrixFormDE extends FormDE
    {
        private AuthMatrix matrix;
		public AuthMatrixFormDE(DivEx parent, String _origin_url) throws SQLException {
			super(parent, _origin_url);
			
			//pull action, auth_type, and matrix and construct matrix
			AuthorizationTypeModel atmodel = new AuthorizationTypeModel(auth);
			ActionModel amodel = new ActionModel(auth);
			AuthorizationTypeActionModel atamodel = new AuthorizationTypeActionModel(auth);
			matrix = new AuthMatrix(parent, atmodel, amodel, atamodel);
			add(matrix);
		}

		protected Boolean doSubmit() {			
			AuthorizationTypeActionModel atamodel = new AuthorizationTypeActionModel(auth);
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
		setAuth(request);
	
		//construct view
		MenuView menuview = createMenuView("admin");
		DivExRoot root = DivExRoot.getInstance(request);
		ContentView contentview;
		
		try {
			contentview = createContentView(root);
			
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
	
	protected ContentView createContentView(DivEx root) throws SQLException
	{			
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>Authorization Matrix</h1>"));
		FormDE form = new AuthMatrixFormDE(root, "admin");
		contentview.add(new DivExWrapper(form));
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		return view;
	}

}
