package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.servlet.ServletBase;

public class ModelBase {
    static Logger log = Logger.getLogger(ModelBase.class); 
	private static Connection connection;
	
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
}
