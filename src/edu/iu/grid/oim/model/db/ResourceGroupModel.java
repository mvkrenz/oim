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
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class ResourceGroupModel extends SmallTableModelBase<ResourceGroupRecord> {
    static Logger log = Logger.getLogger(ResourceGroupModel.class); 

	public ResourceGroupModel(Authorization _auth) {
		super(_auth, "resource_group");
	}
	ResourceGroupRecord createRecord() throws SQLException
	{
		return new ResourceGroupRecord();
	}
	public ResourceGroupRecord get(int id) throws SQLException {
		ResourceGroupRecord keyrec = new ResourceGroupRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceGroupRecord> getAll() throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = new ArrayList<ResourceGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceGroupRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceGroupRecord> getBySiteID(int site_id) throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = new ArrayList<ResourceGroupRecord>();
		for(RecordBase rec : getCache()) {
			ResourceGroupRecord srec = (ResourceGroupRecord)rec;
			if(srec.site_id.compareTo(site_id) == 0) list.add(srec);
		}
		return list;
	}
    public String getName()
    {
    	return "Resource Group";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
}
