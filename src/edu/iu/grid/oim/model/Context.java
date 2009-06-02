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
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

public class Context {
    static Logger log = Logger.getLogger(Context.class);  
    
	private DivExRoot divex_root;
	private Authorization auth = new Authorization();
	private HttpServletRequest request;
	private Connection oim_connection = null;
	
	public Context(HttpServletRequest _request) throws AuthorizationException
	{	
		request = _request;		
		auth = new Authorization(request);
		divex_root = DivExRoot.getInstance(request);
	}

	public static Context getGuestContext()
	{
		return new Context();
	}
	private Context()
	{
	}

	public void close()
	{
		try {
			//close oim
			if(oim_connection != null) {
				oim_connection.close();
			}
			
		} catch (SQLException e) {
			log.error(e);
		}
	}
	//make sure to close the connection as soon as you are done (inside the same function that you call connectOIM)
	public Connection connectOIM() throws SQLException
	{	
		if(oim_connection != null) {
			//if the same context already have open connection, reuse it
			if(!oim_connection.isClosed()) {
				//log.info("Reusing OIM db connection for " + auth.getUserDN());
				return oim_connection;
			}
		}
		
		//reconnect
		try {
			javax.naming.Context initContext = new InitialContext();
			javax.naming.Context envContext = (javax.naming.Context)initContext.lookup("java:/comp/env");
			DataSource ds = (DataSource)envContext.lookup("jdbc/oim");
			oim_connection = ds.getConnection();
			log.info("Requesting new OIM db connection: " + oim_connection.toString() + " for " + auth.getUserDN());
			initContext.close();
			
			return oim_connection;
		} catch (NamingException e) {
			log.error(e);
		}
		return null;
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
