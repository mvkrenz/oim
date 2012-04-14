package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import edu.iu.grid.oim.lib.StaticConfig;

@Deprecated
public class GOCTicket {
    static Logger log = Logger.getLogger(GOCTicket.class); 
    
    private Connection connection;
    
	public GOCTicket() throws NamingException, SQLException
	{
		//connect to GOC data DB
		javax.naming.Context initContext = new InitialContext();
		javax.naming.Context envContext = (javax.naming.Context)initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource)envContext.lookup("jdbc/gocticket");
		connection = ds.getConnection();
		log.info("Requesting new db connection: connection=" + connection.toString());
		initContext.close();		
	}
	
	public void setMetadata(int ticket_id, String key, String value) throws SQLException {
		String sql = "INSERT INTO gocticket.metadata (ticket_id, `key`, value, project_id) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = connection.prepareStatement(sql);
		stmt.setInt(1, ticket_id);
		stmt.setString(2, key);
		stmt.setString(3, value);
		stmt.setInt(4, StaticConfig.getFootprintsProjectID());
		stmt.execute();
	}

}
