package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.EventListener;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.divex.form.VOFormDivex;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	private String current_page = "vo";	

	
    public VOEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		VORecord rec;
		String title;
		
		//pull data needed for the form
		HashMap<Integer, String> scs;
		HashMap<Integer, String> othervos;
		try {
			scs = getSCs();
			othervos = getVOs();
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		
		//if vo_id is provided then we are doing update, otherwise do new.
		String vo_id_str = request.getParameter("vo_id");
		if(vo_id_str != null) {
			//pull record to update
			int vo_id = Integer.parseInt(vo_id_str);
			Authorization auth = new Authorization(request, con);
			VOModel model = new VOModel(con, auth);
			rec = model.getVO(vo_id);	
			title = "Update Virtual Organization";

			//don't allow selecting myself as a parent
			othervos.remove(vo_id);
		} else {
			rec = new VORecord();
			title = "New Virtual Organization";	
		}
	
		VOFormDivex form;
		String origin_url = baseURL()+"/"+current_page;
		form = new VOFormDivex(DivExRoot.getInstance(request), rec, origin_url, 
				scs, othervos);
		
		//put the form in a view and display
		ContentView contentview = new ContentView();
		contentview.add("<h1>"+title+"</h1>");	
		contentview.add("<h2>VO Details</h2>");
		contentview.add(form);
		
		MenuView menuview = createMenuView(baseURL(), current_page);
		Page page = new Page(menuview, contentview);
		response.getWriter().print(page.toHTML());
	}
	
	private HashMap<Integer, String> getSCs() throws AuthorizationException, SQLException
	{
		//pull all SCs
		ResultSet scs = null;
		SCModel model = new SCModel(con, auth);
		scs = model.getAllSCs();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		while(scs.next()) {
			SCRecord rec = new SCRecord(scs);
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	private HashMap<Integer, String> getVOs() throws AuthorizationException, SQLException
	{
		//pull all VOs
		ResultSet vos = null;
		VOModel model = new VOModel(con, auth);
		vos = model.getAllVOs();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		while(vos.next()) {
			VORecord rec = new VORecord(vos);
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
}