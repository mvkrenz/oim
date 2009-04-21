package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.xpath.XPath;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.servlet.ServletBase;

public class ModelBase {
    static Logger log = Logger.getLogger(ModelBase.class); 
	private static Connection connection;
    
	protected Authorization auth;
    
	protected ModelBase(Authorization _auth)
	{
    	auth = _auth;
	}
	public ModelBase()
	{
		auth = Authorization.Guest;
	}
	public void setAuthorization(Authorization _auth)
	{
		auth = _auth;
	}
	
	protected Connection getConnection() throws SQLException
	{
		if(connection == null) {
			//cache oim db connection
			try {
				Context initContext = new InitialContext();
				Context envContext = (Context)initContext.lookup("java:/comp/env");
				DataSource ds = (DataSource)envContext.lookup("jdbc/oim");
				connection = ds.getConnection();
				log.info("OIM DB connection:" + connection.toString());
				initContext.close();
			} catch (NamingException e) {
				log.error(e);
				return null;
			}
		}
		return connection;
	}
	
	public String getName()
	{
		return getClass().getName();
	}
	
	//override this to reveal the log to particular user
	public Boolean hasLogAccess(XPath xpath, Document doc)
	{
		return false;
	}
}
