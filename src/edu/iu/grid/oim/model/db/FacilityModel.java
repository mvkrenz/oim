package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class FacilityModel extends SmallTableModelBase<FacilityRecord> {
    static Logger log = Logger.getLogger(FacilityModel.class);  
	
    public FacilityModel(Context context) 
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
	public ArrayList<FacilityRecord> getAllAlphabetized() throws SQLException
	{
		ArrayList<FacilityRecord> list = getAll();
		Collections.sort(list, new AlphabeticalComparator ());
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
	class AlphabeticalComparator implements Comparator<FacilityRecord> {
	    // Comparator interface requires defining compare method. 
		public int compare(FacilityRecord a, FacilityRecord b) {
			// We are comparing based on name
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}
}

