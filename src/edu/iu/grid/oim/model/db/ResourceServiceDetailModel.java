package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ResourceServiceDetailModel extends SmallTableModelBase<ResourceServiceDetailRecord> {
    static Logger log = Logger.getLogger(ResourceServiceDetailModel.class); 

	public ResourceServiceDetailModel(UserContext context) {
		super(context, "resource_service_detail");
	}
	ResourceServiceDetailRecord createRecord() throws SQLException
	{
		return new ResourceServiceDetailRecord();
	}
	public ResourceServiceDetailRecord get(int service_id, int resource_id, String key) throws SQLException {
		ResourceServiceDetailRecord keyrec = new ResourceServiceDetailRecord();
		keyrec.service_id = service_id;
		keyrec.resource_id = resource_id;
		keyrec.key = key;
		return get(keyrec);
	}
	public ArrayList<ResourceServiceDetailRecord> getAll() throws SQLException
	{
		ArrayList<ResourceServiceDetailRecord> list = new ArrayList<ResourceServiceDetailRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceServiceDetailRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceServiceDetailRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceServiceDetailRecord> list = new ArrayList<ResourceServiceDetailRecord>();
		for(ResourceServiceDetailRecord it : getAll()) {
			if(it.resource_id.equals(resource_id)) {
				list.add(it);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "Resource Service Detail";
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
