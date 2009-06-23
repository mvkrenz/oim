package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ConfigRecord;

public class ConfigModel extends ModelBase<ConfigRecord>  {
    static Logger log = Logger.getLogger(ConfigModel.class);  
    
    public enum Config {
    	ANNUAL_REVIEW_OPEN
    }
    
    public ConfigModel(Context context) 
    {
    	super(context, "config");
    }
    ConfigRecord createRecord() throws SQLException
	{
		return new ConfigRecord();
	}
    
	public String get(Config key) throws SQLException 
	{
    	Connection conn = connectOIM();			
    	String sql = "SELECT `value` FROM "+table_name+" WHERE `key` = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, key.name());
		ResultSet rs = stmt.executeQuery();
    	if(rs.next()) {
    		return rs.getString(1);
    	}
    	return null;
	}
	
	public void set(Config key, String value) throws SQLException
	{
    	Connection conn = connectOIM();			
    	conn.setAutoCommit(false);
    	try {
    		//upsert the key/value
    		String sql = "DELETE FROM "+table_name+" WHERE `key` = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, key.name());
			stmt.execute();
			
			if(value != null) {
				sql = "INSERT INTO "+table_name+" (`key`, `value`) VALUES (?, ?)";
				stmt = conn.prepareStatement(sql); 
				stmt.setString(1, key.name());
				stmt.setString(2, value);
				stmt.execute();
			}
    		conn.commit();
    		conn.setAutoCommit(true);
    	} catch(SQLException e) {
			conn.rollback();
			conn.setAutoCommit(true);
			throw new SQLException(e);
    	}
	}

	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//StaticConfig currently doesn't support log (need to call logUpdate in set() )
		return false;
	}
}