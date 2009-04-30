package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ResourceServiceModel extends SmallTableModelBase<ResourceServiceRecord> {
    static Logger log = Logger.getLogger(ResourceServiceModel.class); 

	public ResourceServiceModel(Context context) {
		super(context, "resource_service");
	}
	ResourceServiceRecord createRecord() throws SQLException
	{
		return new ResourceServiceRecord();
	}
	public ResourceServiceRecord get(int service_id, int resource_id) throws SQLException {
		ResourceServiceRecord keyrec = new ResourceServiceRecord();
		keyrec.service_id = service_id;
		keyrec.resource_id = resource_id;
		return get(keyrec);
	}
	public ArrayList<ResourceServiceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceServiceRecord> list = new ArrayList<ResourceServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceServiceRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceServiceRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceServiceRecord> list = new ArrayList<ResourceServiceRecord>();
		for(ResourceServiceRecord it : getAll()) {
			if(it.resource_id.compareTo(resource_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "Resource Service";
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
		} else if(field_name.equals("service_id")) {
			ServiceModel model = new ServiceModel(context);
			ServiceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
}
