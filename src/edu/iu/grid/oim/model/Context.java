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
	private Authorization auth;
	private Connection connection;
	private HttpServletRequest request;
	
	public Context(HttpServletRequest _request) throws NamingException, SQLException
	{	
		request = _request;
		
		//create DB connection (per context now!)
		javax.naming.Context initContext = new InitialContext();
		javax.naming.Context envContext = (javax.naming.Context)initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource)envContext.lookup("jdbc/oim");
		connection = ds.getConnection();
		initContext.close();
		
		divex_root = DivExRoot.getInstance(request);
		auth = new Authorization(request, connection);
		
		log.info("Context initialized with " + connection.toString());
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
