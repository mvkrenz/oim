package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.DowntimeActionRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DowntimeActionModel extends SmallTableModelBase<DowntimeActionRecord> {
    static Logger log = Logger.getLogger(DowntimeActionModel.class);  
    
    public DowntimeActionModel(Context context) 
    {
    	super(context, "downtime_action");
    }
    DowntimeActionRecord createRecord() throws SQLException
	{
		return new DowntimeActionRecord();
	}
	public ArrayList<DowntimeActionRecord> getAll() throws SQLException
	{
		ArrayList<DowntimeActionRecord> list = new ArrayList<DowntimeActionRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimeActionRecord)it);
		}
		return list;
	}
	public DowntimeActionRecord get(int id) throws SQLException {
		DowntimeActionRecord keyrec = new DowntimeActionRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Downtime Action";
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