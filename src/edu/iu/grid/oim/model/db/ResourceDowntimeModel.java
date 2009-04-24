package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.ResourceDowntime;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;

public class ResourceDowntimeModel extends SmallTableModelBase<ResourceDowntimeRecord> {
    static Logger log = Logger.getLogger(ResourceDowntimeModel.class); 

	public ResourceDowntimeModel(Context context) {
		super(context, "resource_downtime");
	}
    public String getName()
    {
    	return "Resource Downtime";
    }
	ResourceDowntimeRecord createRecord() throws SQLException
	{
		return new ResourceDowntimeRecord();
	}
	public ResourceDowntimeRecord get(int id) throws SQLException {
		ResourceDowntimeRecord keyrec = new ResourceDowntimeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		ResourceModel model = new ResourceModel(context);
		return model.canEdit(id);
	}
	
	public Collection<ResourceDowntimeRecord> getAll() throws SQLException {
		ArrayList<ResourceDowntimeRecord> list = new ArrayList<ResourceDowntimeRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceDowntimeRecord)it);
		}
		return list;
	}
	
	public Collection<ResourceDowntimeRecord> getFutureDowntimesByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceDowntimeRecord> list = new ArrayList<ResourceDowntimeRecord>();
		for(RecordBase it : getCache()) {
			ResourceDowntimeRecord rec = (ResourceDowntimeRecord)it;
			//search for downtime that ends in future.
			if(rec.resource_id == resource_id && rec.end_time.compareTo(new Date()) > 0) {
				list.add(rec);
			}
		}
		return list;
	}

	
	public void updateDetail(int resource_id, ArrayList<ResourceDowntime> downtimes) throws Exception
	{
		try {		
			getConnection().setAutoCommit(false);
	
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
			ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(context);
			
			//process downtime record itself
			ArrayList<ResourceDowntimeRecord> downtime_recs = new ArrayList();
			for(ResourceDowntime downtime : downtimes) {	
				downtime_recs.add(downtime.downtime);
			}
			dmodel.update(dmodel.getFutureDowntimesByResourceID(resource_id), downtime_recs);
			
			//process service records
			for(ResourceDowntime downtime : downtimes) {
				ResourceDowntimeRecord downtime_rec = downtime.downtime;

				ArrayList<ResourceDowntimeServiceRecord> services = downtime.services;
				//update the downtime_id
				for(ResourceDowntimeServiceRecord service : services) {
					service.resource_downtime_id = downtime_rec.id;

				}
				rdsmodel.update(rdsmodel.getByDowntimeID(downtime_rec.id), services);			
			}
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back resource downtime transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}
