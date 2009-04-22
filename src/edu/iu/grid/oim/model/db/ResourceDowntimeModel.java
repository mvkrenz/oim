package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.ResourceDowntime;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.view.divex.form.ResourceDowntimeFormDE;

public class ResourceDowntimeModel extends SmallTableModelBase<ResourceDowntimeRecord> {
    static Logger log = Logger.getLogger(ResourceDowntimeModel.class); 

	public ResourceDowntimeModel(Authorization _auth) {
		super(_auth, "resource_downtime");
	}
	ResourceDowntimeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceDowntimeRecord(rs);
	}
	public ResourceDowntimeRecord get(int id) throws SQLException {
		ResourceDowntimeRecord keyrec = new ResourceDowntimeRecord();
		keyrec.id = id;
		return get(keyrec);
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
	/*
	public void insertDetail(Integer resource_id, 
			ArrayList<ResourceDowntimeRecord> downtimes,
			HashMap<Integer, ArrayList<ResourceDowntimeServiceRecord>> downtime_services) throws Exception
	{
		try {
			getConnection().setAutoCommit(false);
			
			//process downtime schedule
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);
			for(ResourceDowntimeFormDE.DowntimeEditor downtime_editor : downtimes) {
				ResourceDowntimeRecord downtime = downtime_editor.getDowntimeRecord();
				
				downtime.resource_id = resource_id;
				
				dmodel.insert(downtime); //key is now set
			
				//process downtime service
				ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(auth);
				ArrayList<ResourceDowntimeServiceRecord> services = downtime_services.get(downtime_editor);
				for(ResourceDowntimeServiceRecord service : services) {
					service.resource_downtime_id = downtime.id;
					rdsmodel.insert(service);
				}
			}	
	
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	*/
	
	public void updateDetail(int resource_id, ArrayList<ResourceDowntime> downtimes) throws Exception
	{
		try {		
			getConnection().setAutoCommit(false);
	
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);
			ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(auth);
			
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
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back resource downtime transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}
