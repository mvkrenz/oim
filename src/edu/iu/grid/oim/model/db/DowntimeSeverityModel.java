package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.DowntimeSeverityRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DowntimeSeverityModel extends SmallTableModelBase<DowntimeSeverityRecord> {
    static Logger log = Logger.getLogger(DowntimeSeverityModel.class);  
    
    public DowntimeSeverityModel(UserContext context) 
    {
    	super(context, "downtime_severity");
    }
    DowntimeSeverityRecord createRecord() throws SQLException
	{
		return new DowntimeSeverityRecord();
	}
	public ArrayList<DowntimeSeverityRecord> getAll() throws SQLException
	{
		ArrayList<DowntimeSeverityRecord> list = new ArrayList<DowntimeSeverityRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimeSeverityRecord)it);
		}
		return list;
	}
	public DowntimeSeverityRecord get(int id) throws SQLException {
		DowntimeSeverityRecord keyrec = new DowntimeSeverityRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Downtime Severity";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
}