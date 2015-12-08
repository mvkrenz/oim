package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DowntimeClassModel extends SmallTableModelBase<DowntimeClassRecord> {
    static Logger log = Logger.getLogger(DowntimeClassModel.class);  
    
    public DowntimeClassModel(UserContext context) 
    {
    	super(context, "downtime_class");
    }
    DowntimeClassRecord createRecord() throws SQLException
	{
		return new DowntimeClassRecord();
	}
	public ArrayList<DowntimeClassRecord> getAll() throws SQLException
	{
		ArrayList<DowntimeClassRecord> list = new ArrayList<DowntimeClassRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimeClassRecord)it);
		}
		return list;
	}
	public DowntimeClassRecord get(int id) throws SQLException {
		DowntimeClassRecord keyrec = new DowntimeClassRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Downtime Class";
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