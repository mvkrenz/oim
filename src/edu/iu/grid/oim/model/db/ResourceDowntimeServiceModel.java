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
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;

public class ResourceDowntimeServiceModel extends SmallTableModelBase<ResourceDowntimeServiceRecord> {
    static Logger log = Logger.getLogger(ResourceDowntimeServiceModel.class); 

	public ResourceDowntimeServiceModel(Context context) {
		super(context, "resource_downtime_service");
	}
	ResourceDowntimeServiceRecord createRecord() throws SQLException
	{
		return new ResourceDowntimeServiceRecord();
	}
	public Collection<ResourceDowntimeServiceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceDowntimeServiceRecord)it);
		}
		return list;
	}
	public Collection<ResourceDowntimeServiceRecord> getByDowntimeID(int downtime_id) throws SQLException
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		for(RecordBase it : getCache()) {
			ResourceDowntimeServiceRecord rec = (ResourceDowntimeServiceRecord)it;
			if(rec.resource_downtime_id.compareTo(downtime_id) == 0) {
				list.add(rec);
			}
		}
		return list;
	}
	public Collection<ResourceDowntimeServiceRecord> getAllByResourceID(int resource_id) throws SQLException 
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		
		//grap all downtime_id
		ResourceDowntimeModel rdmodel = new ResourceDowntimeModel(context);
		Collection<ResourceDowntimeRecord> downtimes = rdmodel.getFutureDowntimesByResourceID(resource_id);
		for(ResourceDowntimeRecord downtime : downtimes) {
			list.addAll(getByDowntimeID(downtime.id));
		}
		
		return list;
	}
    public String getName()
    {
    	return "Resource Downtime / Service";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer resource_downtime_id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='resource_downtime_id']/Value", doc, XPathConstants.STRING));
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
		ResourceDowntimeRecord drec;
		try {
			drec = dmodel.get(resource_downtime_id);
			if(drec == null) {
				return false;
			}
			ResourceModel model = new ResourceModel(context);
			return model.canEdit(drec.resource_id);
		} catch (SQLException e) {
			log.error(e);
		}
		return false;
	}
}
