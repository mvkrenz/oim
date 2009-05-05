package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;

import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;

public class VOResourceOwnershipModel extends SmallTableModelBase<VOResourceOwnershipRecord> {
    static Logger log = Logger.getLogger(VOResourceOwnershipModel.class); 

	public VOResourceOwnershipModel(Context context) {
		super(context, "vo_resource_ownership");
	}
	VOResourceOwnershipRecord createRecord() throws SQLException
	{
		return new VOResourceOwnershipRecord();
	}
	public Collection<VOResourceOwnershipRecord> getAll() throws SQLException
	{
		ArrayList<VOResourceOwnershipRecord> list = new ArrayList<VOResourceOwnershipRecord>();
		for(RecordBase it : getCache()) {
			list.add((VOResourceOwnershipRecord)it);
		}
		return list;
	}
	public Collection<VOResourceOwnershipRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<VOResourceOwnershipRecord> list = new ArrayList<VOResourceOwnershipRecord>();
		for(VOResourceOwnershipRecord it : getAll()) {
			if(it.resource_id.equals(resource_id)) {
				list.add(it);
			}
		}
		return list;		
	}
	
	public Collection<VOResourceOwnershipRecord> getAllByVOID(int vo_id) throws SQLException
	{
		ArrayList<VOResourceOwnershipRecord> list = new ArrayList<VOResourceOwnershipRecord>();
		for(VOResourceOwnershipRecord it : getAll()) {
			if(it.vo_id.equals (vo_id)) {
				list.add(it);
			}
		}
		return list;		
	}
	
	public String getName()
    {
    	return "Resource / VO Ownership";
    }

	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='resource_id']/Value", doc, XPathConstants.STRING));
		ResourceModel model = new ResourceModel(context);
		return model.canEdit(id);
	}
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("resource_id")) {
			ResourceModel model = new ResourceModel(context);
			ResourceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
}

    