package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.LogRecord;

public class LogModel extends ModelBase {
    static Logger log = Logger.getLogger(LogModel.class);  
    
	//public enum Type {ALL, RESOURCE, VO, SC, CONTACT, SITE, FACILITY};
    
    public LogModel(Authorization _auth) 
    {
    	super(_auth, "log");
    }
    
    LogRecord createRecord(ResultSet rs) throws SQLException
	{
		return new LogRecord(rs);
	}
    
    public Collection<LogRecord> getLatest(String model) throws SQLException
    {
    	//no auth check -- client needs to figure out if the log is accessible to the user or not
    	
    	ArrayList<LogRecord> recs = new ArrayList<LogRecord>();

    	String sql = "SELECT * FROM log WHERE timestamp > curtime() - 86400 * 7 AND model LIKE ?";
		PreparedStatement stmt = getConnection().prepareStatement(sql); 
		stmt.setString(1, model);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			LogRecord rec = new LogRecord(rs);
			recs.add(new LogRecord(rs));
		}
    	
		return recs;
    }
    
    public int insert(String type, String model, String xml) throws SQLException
    {
    	//no auth check... accessing log table is non-auth action
    	
		String sql = "INSERT INTO log (`type`, `model`, `xml`, `dn_id`) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		stmt.setString(1, type);
		stmt.setString(2, model);
		stmt.setString(3, xml);
		Integer dn_id = auth.getDNID();
		if(dn_id == null) {
			stmt.setNull(4, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(4, dn_id);
		}
		stmt.executeUpdate(); 
		
		ResultSet ids = stmt.getGeneratedKeys();  
		if(!ids.next()) {
			throw new SQLException("didn't get a new log id");
		}
		int logid = ids.getInt(1);
		stmt.close();
		return logid;
    }
}
