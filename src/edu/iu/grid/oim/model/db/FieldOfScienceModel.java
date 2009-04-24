package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class FieldOfScienceModel extends SmallTableModelBase<FieldOfScienceRecord> {
    static Logger log = Logger.getLogger(FieldOfScienceModel.class);  
	
    public FieldOfScienceModel(Context context) 
    {
    	super(context, "field_of_science");
    }
    FieldOfScienceRecord createRecord() throws SQLException
	{
		return new FieldOfScienceRecord();
	}
	public ArrayList<FieldOfScienceRecord> getAll() throws SQLException
	{
		ArrayList<FieldOfScienceRecord> list = new ArrayList<FieldOfScienceRecord>();
		for(RecordBase it : getCache()) {
			list.add((FieldOfScienceRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "Field Of Science";
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
