package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigContactRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class MeshConfigContactModel extends SmallTableModelBase<MeshConfigContactRecord> {
    static Logger log = Logger.getLogger(MeshConfigContactModel.class); 
	
	public MeshConfigContactModel(UserContext context) {
		super(context, "mesh_config_contact");
	}
	MeshConfigContactRecord createRecord() throws SQLException
	{
		return new MeshConfigContactRecord();
	}

	public ArrayList<MeshConfigContactRecord> getByMeshConfigID(int id) throws SQLException
	{ 
		ArrayList<MeshConfigContactRecord> list = new ArrayList<MeshConfigContactRecord>();
		for(RecordBase rec : getCache()) {
			MeshConfigContactRecord vcrec = (MeshConfigContactRecord)rec;
			if(vcrec.mesh_config_id.equals(id)) list.add(vcrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<MeshConfigContactRecord>> 
		groupByContactTypeID(ArrayList<MeshConfigContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<MeshConfigContactRecord>> list = new HashMap<Integer, ArrayList<MeshConfigContactRecord>>();
		for(MeshConfigContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<MeshConfigContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<MeshConfigContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<MeshConfigContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<MeshConfigContactRecord> list = new ArrayList<MeshConfigContactRecord>();
		for(RecordBase rec : getCache()) {
			MeshConfigContactRecord vcrec = (MeshConfigContactRecord)rec;
			if(vcrec.contact_id.compareTo(contact_id) == 0) list.add(vcrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Mesh Config Test Contact";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			MeshConfigTestModel model = new MeshConfigTestModel(context);
			MeshConfigTestRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_type_id")) {
			ContactTypeModel model = new ContactTypeModel(context);
			ContactTypeRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_rank_id")) {
			ContactRank rank = ContactRank.get(Integer.parseInt(value));
			return value + " (" + rank + ")";
		} else if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	/*
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='mesh_config_test_id']/Value", doc, XPathConstants.STRING));
		MeshConfigTestModel model = new MeshConfigTestModel(context);
		return model.canEdit(id);
	}
	*/
}
