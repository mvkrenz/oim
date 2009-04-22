package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class ServiceModel extends SmallTableModelBase<ServiceRecord> {
    static Logger log = Logger.getLogger(ServiceModel.class);  
	
    public ServiceModel(Authorization _auth) 
    {
    	super(_auth, "service");
    }
    ServiceRecord createRecord() throws SQLException
	{
		return new ServiceRecord();
	}
	public ServiceRecord get(int id) throws SQLException {
		ServiceRecord keyrec = new ServiceRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ServiceRecord> getAll() throws SQLException
	{
		ArrayList<ServiceRecord> list = new ArrayList<ServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ServiceRecord)it);
		}
		return list;
	}
	
	public void insertDetail(ServiceRecord rec, 
			ArrayList<MetricServiceRecord> metrics) throws Exception
	{
		try {			
			getConnection().setAutoCommit(false);
			
			insert(rec);
			
			//process metric information
			MetricServiceModel cmodel = new MetricServiceModel(auth);
			for(MetricServiceRecord msrec : metrics) {
				msrec.service_id = rec.id;
			}
			cmodel.insert(metrics);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back Service Detail insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(ServiceRecord rec, 
			ArrayList<MetricServiceRecord> metrics) throws Exception
	{
		try {
		
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
			MetricServiceModel cmodel = new MetricServiceModel(auth);
			for(MetricServiceRecord msrec : metrics) {
				msrec.service_id = rec.id;
			}
			cmodel.update(cmodel.getAllByServiceID(rec.id), metrics);
			
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
}
