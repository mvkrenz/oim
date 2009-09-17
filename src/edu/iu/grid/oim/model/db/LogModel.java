package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.LogRecord;

public class LogModel extends ModelBase {
    static Logger log = Logger.getLogger(LogModel.class); 
    public static String NULL_TOKEN = "##null##";
        
    public LogModel(Context context) 
    {
    	super(context, "log");
    }
    
    LogRecord createRecord() throws SQLException
	{
		return new LogRecord();
	}
    
    public LogRecord get(int id) throws SQLException
    {
    	//no auth check -- client needs to figure out if the log is accessible to the user or not
    	
    	ArrayList<LogRecord> recs = new ArrayList<LogRecord>();

    	String sql = "SELECT * FROM log WHERE id = ?";
    	Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement(sql); 
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			LogRecord rec = new LogRecord(rs);
			return rec;
		}
		return null;
    }
    
    public Collection<LogRecord> getLatest(String type_filter, String model_filter, Integer days, String xml_reg) throws SQLException
    {
    	//no auth check -- client needs to figure out if the log is accessible to the user or not
    	
    	ArrayList<LogRecord> recs = new ArrayList<LogRecord>();

    	String sql = "SELECT * FROM log WHERE timestampdiff(DAY, timestamp,localtimestamp) < "+days+" AND type LIKE ? AND model LIKE ? AND xml REGEXP ? ORDER BY timestamp DESC";
    	Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement(sql); 
		stmt.setString(1, type_filter);
		stmt.setString(2, model_filter);
		stmt.setString(3, xml_reg);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			LogRecord rec = new LogRecord(rs);
			recs.add(new LogRecord(rs));
		}
		return recs;
    }
    
    public Collection<LogRecord> getDateRange(Timestamp start, Timestamp end) throws SQLException
    {
    	ArrayList<LogRecord> recs = new ArrayList<LogRecord>();
       	String sql = "SELECT * FROM log WHERE timestamp >= ? and timestamp <= ? ORDER BY timestamp DESC";
    	Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement(sql); 
		stmt.setTimestamp(1, start);
		stmt.setTimestamp(2, end);
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
    	
		String sql = "INSERT INTO log (`type`, `model`, `xml`, `dn_id`, `comment`) VALUES (?, ?, ?, ?, ?)";
		Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		stmt.setString(1, type);
		stmt.setString(2, model);
		stmt.setString(3, xml);
		Integer dn_id = auth.getDNID();
		if(dn_id == null) {
			stmt.setNull(4, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(4, dn_id);
		}
		stmt.setString(5, context.getComment());
		stmt.executeUpdate(); 
		
		ResultSet ids = stmt.getGeneratedKeys();  
		if(!ids.next()) {
			throw new SQLException("didn't get a new log id");
		}
		int logid = ids.getInt(1);
		
		stmt.close();
		
		return logid;
    }
    public String getName()
    {
    	return "Log";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		return false;
	}
}
