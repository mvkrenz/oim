package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import javax.servlet.http.HttpServlet;

public class OIMServlet extends HttpServlet implements Servlet {

    protected Connection con = null;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		
		//cache oim db connection
		try
		{
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource)envContext.lookup("jdbc/oim");
			con = ds.getConnection();
			initContext.close();
		}
		catch(Exception es)
		{
			es.printStackTrace();
		}

	}

}
