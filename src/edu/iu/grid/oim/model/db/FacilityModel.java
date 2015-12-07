package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class FacilityModel extends SmallTableModelBase<FacilityRecord> {
    static Logger log = Logger.getLogger(FacilityModel.class);  
	
    public FacilityModel(UserContext context) 
    {
    	super(context, "facility");
    }
    FacilityRecord createRecord() throws SQLException
	{
		return new FacilityRecord();
	}
	public FacilityRecord get(int id) throws SQLException {
		FacilityRecord keyrec = new FacilityRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<FacilityRecord> getAll() throws SQLException
	{
		ArrayList<FacilityRecord> list = new ArrayList<FacilityRecord>();
		for(RecordBase it : getCache()) {
			list.add((FacilityRecord)it);
		}
		return list;
	}
	
    public String getName()
    {
    	return "Facility";
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

