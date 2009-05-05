package edu.iu.grid.oim.model;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.webif.divex.DivExRoot;
import edu.iu.grid.oim.lib.Authorization;

public class Context {
    static Logger log = Logger.getLogger(Context.class);  
    
	private DivExRoot divex_root;
	private Authorization auth = new Authorization();
	private Connection connection = null;
	private HttpServletRequest request;
	
	public Context(HttpServletRequest _request)
	{	
		request = _request;
		
		try {
			javax.naming.Context initContext = new InitialContext();
			javax.naming.Context envContext = (javax.naming.Context)initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource)envContext.lookup("jdbc/oim");
			connection = ds.getConnection();
			initContext.close();
			
			auth = new Authorization(request, connection);
		} catch (SQLException e) {
			log.error(e);
		} catch (NamingException e) {
			log.error(e);
		}
		
		divex_root = DivExRoot.getInstance(request);
		
		//log.info("Context initialized with " + connection.toString());
	}
	
	public void finalize() throws Throwable
	{
		if(connection != null) {
			connection.close();
		}
	}
	
	public static Context getGuestContext(Connection db)
	{
		return new Context(db);
	}
	private Context(Connection db)
	{
		//initialize with only the connection db
		connection = db;
	}
	
	//getters
	public Connection getConnection()
	{
		return connection;
	}
	public Authorization getAuthorization()
	{
		return auth;
	}
	public DivExRoot getDivExRoot()
	{
		return divex_root;
	}
	public HttpServletRequest getRequest()
	{
		return request;
	}
}
