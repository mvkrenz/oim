package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.ResourceModel;
import edu.iu.grid.oim.model.record.ResourceRecord;

/**
 * Servlet implementation class MainServlet
 */
public class MainServlet extends OIMServlet implements Servlet {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(MainServlet.class);   
       
    public MainServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException
    {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Authorization auth = new Authorization(request, con);
		
		ResourceModel model = new ResourceModel(con, auth);
		ResultSet resources = model.getAllResources();
		try {
			while(resources.next()) {
				response.getWriter().println(resources.getString("name"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResourceRecord rec = new ResourceRecord();
		rec.name = "From hello";
		rec.fqdn = "test.fqdn";
		rec.interop_bdii = false;
		rec.interop_accounting = true;
		rec.interop_monitoring = false;
		rec.wlcg_accounting_name = "yo";
		rec.active = true;
		rec.disable = false;
		rec.resource_group_id = 1;
		try {
			model.insertResource(rec);
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
