package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
    protected String table_name;
    
	protected ModelBase(Authorization _auth, String _table_name)
	{
    	auth = _auth;
    	table_name = _table_name;
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
	/*
	//I get following warning when I try to run DISABLE KEYS command
	//"Table storage engine for 'resource_downtime_service' doesn't have this option"
	//disableKey and enableKey allows dropping records from both parent / foreign table
	public void disableKey() throws SQLException
	{
		String sql = "ALTER TABLE "+table_name+" DISABLE KEYS";
		PreparedStatement stmt = getConnection().prepareStatement(sql);
		stmt.execute();
	}
	public void enableKey() throws SQLException
	{
		String sql = "ALTER TABLE "+table_name+" ENABLE KEYS";
		PreparedStatement stmt = getConnection().prepareStatement(sql);
		stmt.execute();
	}
	*/
}
