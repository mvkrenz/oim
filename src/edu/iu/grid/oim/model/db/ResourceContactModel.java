package edu.iu.grid.oim.model.db;

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
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class ResourceContactModel extends SmallTableModelBase<ResourceContactRecord> {
    static Logger log = Logger.getLogger(ResourceContactModel.class); 
	
	public ResourceContactModel(Context context) {
		super(context, "resource_contact");
	}
	ResourceContactRecord createRecord() throws SQLException
	{
		return new ResourceContactRecord();
	}

	public ArrayList<ResourceContactRecord> getByResourceID(int resource_id) throws SQLException
	{ 
		ArrayList<ResourceContactRecord> list = new ArrayList<ResourceContactRecord>();
		for(RecordBase rec : getCache()) {
			ResourceContactRecord vcrec = (ResourceContactRecord)rec;
			if(vcrec.resource_id == resource_id) list.add(vcrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<ResourceContactRecord>> groupByContactTypeID(ArrayList<ResourceContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<ResourceContactRecord>> list = new HashMap<Integer, ArrayList<ResourceContactRecord>>();
		for(ResourceContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<ResourceContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<ResourceContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<ResourceContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<ResourceContactRecord> list = new ArrayList<ResourceContactRecord>();
		for(RecordBase rec : getCache()) {
			ResourceContactRecord vcrec = (ResourceContactRecord)rec;
			if(vcrec.contact_id == contact_id) list.add(vcrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Resource Contact";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='resource_id']/Value", doc, XPathConstants.STRING));
		ResourceModel model = new ResourceModel(context);
		return model.canEdit(id);
	}
	
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("resource_id")) {
			ResourceModel model = new ResourceModel(context);
			ResourceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";			
		} else if(field_name.equals("contact_type_id")) {
			ContactTypeModel model = new ContactTypeModel(context);
			ContactTypeRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";			
		} else if(field_name.equals("contact_rank_id")) {
			ContactRankModel model = new ContactRankModel(context);
			ContactRankRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";			
		}
		return value;
	}
}
