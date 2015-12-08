package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOContactModel extends SmallTableModelBase<VOContactRecord> {
    static Logger log = Logger.getLogger(VOContactModel.class); 
	
	public VOContactModel(UserContext context) {
		super(context, "vo_contact");
	}
	VOContactRecord createRecord() throws SQLException
	{
		return new VOContactRecord();
	}

	public ArrayList<VOContactRecord> getByVOID(int vo_id) throws SQLException
	{ 
		ArrayList<VOContactRecord> list = new ArrayList<VOContactRecord>();
		for(RecordBase rec : getCache()) {
			VOContactRecord vcrec = (VOContactRecord)rec;
			if(vcrec.vo_id == vo_id) list.add(vcrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<VOContactRecord>> groupByContactTypeID(ArrayList<VOContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<VOContactRecord>> list = new HashMap<Integer, ArrayList<VOContactRecord>>();
		for(VOContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<VOContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<VOContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<VOContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<VOContactRecord> list = new ArrayList<VOContactRecord>();
		for(RecordBase rec : getCache()) {
			VOContactRecord vcrec = (VOContactRecord)rec;
			if(vcrec.contact_id.compareTo(contact_id) == 0) list.add(vcrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Virtual Organization Contact";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
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
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='vo_id']/Value", doc, XPathConstants.STRING));
		VOModel model = new VOModel(context);
		return model.canEdit(id);
	}
}
