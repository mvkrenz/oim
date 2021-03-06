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
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class SiteModel extends SmallTableModelBase<SiteRecord> {
    static Logger log = Logger.getLogger(SiteModel.class); 

	public SiteModel(UserContext context) {
		super(context, "site");
	}
    public String getName()
    {
    	return "Site";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("sc_id")) {
			SCModel model = new SCModel(context);
			SCRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("facility_id")) {
			FacilityModel model = new FacilityModel(context);
			FacilityRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
	
	SiteRecord createRecord() throws SQLException
	{
		return new SiteRecord();
	}
	public ArrayList<SiteRecord> getAll() throws SQLException
	{
		ArrayList<SiteRecord> list = new ArrayList<SiteRecord>();
		for(RecordBase it : getCache()) {
			list.add((SiteRecord)it);
		}
		return list;
	}
	public SiteRecord get(int id) throws SQLException {
		SiteRecord keyrec = new SiteRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<SiteRecord> getByFacilityID(Integer facility_id) throws SQLException
	{
		ArrayList<SiteRecord> list = new ArrayList<SiteRecord>();
		for(RecordBase rec : getCache()) {
			SiteRecord srec = (SiteRecord)rec;
			if(srec.facility_id.equals(facility_id)) list.add(srec);
		}
		return list;
	}
	public ArrayList<SiteRecord> getByDNID(Integer id) throws SQLException {
		ArrayList<SiteRecord> list = new ArrayList<SiteRecord>();
	    for(RecordBase rec : getCache()) {
	    	SiteRecord crec = (SiteRecord)rec;
	    	if(crec.submitter_dn_id != null && crec.submitter_dn_id.equals(id)) {
	    		list.add(crec);
	    	}
	    }	    	
	    return list;
	}
}
