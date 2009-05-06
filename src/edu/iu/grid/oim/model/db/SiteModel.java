package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class SiteModel extends SmallTableModelBase<SiteRecord> {
    static Logger log = Logger.getLogger(SiteModel.class); 

	public SiteModel(Context context) {
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
	public ArrayList<SiteRecord> getByFacilityID(int facility_id) throws SQLException
	{
		ArrayList<SiteRecord> list = new ArrayList<SiteRecord>();
		for(RecordBase rec : getCache()) {
			SiteRecord srec = (SiteRecord)rec;
			if(srec.facility_id.compareTo(facility_id) == 0) list.add(srec);
		}
		return list;
	}
}
