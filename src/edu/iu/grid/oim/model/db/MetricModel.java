package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class MetricModel extends SmallTableModelBase<MetricRecord> {
    static Logger log = Logger.getLogger(MetricModel.class);  
    
    public MetricModel(Authorization auth) 
    {
    	super(auth, "metric");
    }
    MetricRecord createRecord() throws SQLException
	{
		return new MetricRecord();
	}
	public MetricRecord get(int id) throws SQLException {
		MetricRecord keyrec = new MetricRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<MetricRecord> getAll() throws SQLException
	{
		ArrayList<MetricRecord> list = new ArrayList<MetricRecord>();
		for(RecordBase it : getCache()) {
			list.add((MetricRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "Metric";
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