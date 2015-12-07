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
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceAliasModel extends SmallTableModelBase<ResourceAliasRecord> {
    static Logger log = Logger.getLogger(ResourceAliasModel.class); 

	public ResourceAliasModel(UserContext context) {
		super(context, "resource_alias");
	}
	ResourceAliasRecord createRecord() throws SQLException
	{
		return new ResourceAliasRecord();
	}
	public ArrayList<ResourceAliasRecord> getAll() throws SQLException
	{
		ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceAliasRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceAliasRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
		for(ResourceAliasRecord it : getAll()) {
			if(it.resource_id.compareTo(resource_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "Resource Alias";
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
		}
		return value;
	}
	
}
