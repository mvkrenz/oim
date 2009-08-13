package edu.iu.grid.oim.model.db;

import java.sql.Connection;
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
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class SCContactModel extends SmallTableModelBase<SCContactRecord> {
    static Logger log = Logger.getLogger(SCContactModel.class); 

	public SCContactModel(Context context) {
		super(context, "sc_contact");
	}
	SCContactRecord createRecord() throws SQLException
	{
		return new SCContactRecord();
	}
	public ArrayList<SCContactRecord> getBySCID(int sc_id) throws SQLException
	{ 
		ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
		for(RecordBase rec : getCache()) {
			SCContactRecord sccrec = (SCContactRecord)rec;
			if(sccrec.sc_id == sc_id) list.add(sccrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<SCContactRecord>> groupByContactTypeID(ArrayList<SCContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<SCContactRecord>> list = new HashMap<Integer, ArrayList<SCContactRecord>>();
		for(SCContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<SCContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<SCContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<SCContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
		for(RecordBase rec : getCache()) {
			SCContactRecord sccrec = (SCContactRecord)rec;
			if(sccrec.contact_id == contact_id) list.add(sccrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Support Center Contact";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("sc_id")) {
			SCModel model = new SCModel(context);
			SCRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_type_id")) {
			ContactTypeModel model = new ContactTypeModel(context);
			ContactTypeRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_rank_id")) {
			ContactRankModel model = new ContactRankModel(context);
			ContactRankRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='sc_id']/Value", doc, XPathConstants.STRING));
		SCModel model = new SCModel(context);
		return model.canEdit(id);
	}
}
