package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class DNModel extends SmallTableModelBase<DNRecord> {
    static Logger log = Logger.getLogger(DNModel.class);  
    
    public DNModel(Authorization auth) 
    {
    	super(auth, "dn");
    }
    public String getName()
    {
    	return "DN";
    }
    DNRecord createRecord() throws SQLException
	{
		return new DNRecord();
	}
	
	public DNRecord getByDNString(String dn_string) throws SQLException
	{
		for(RecordBase it : getCache()) 
		{
			DNRecord rec = (DNRecord)it;
			if(rec.dn_string.compareTo(dn_string) == 0) {
				return rec;
			}
		}
		return null;
	}
	public DNRecord getByContactID(int contact_id) throws SQLException
	{
		for(RecordBase it : getCache()) 
		{
			DNRecord rec = (DNRecord)it;
			if(rec.contact_id.compareTo(contact_id) == 0) {
				return rec;
			}
		}
		return null;
	}
	
	public DNRecord get(int id) throws SQLException {
		DNRecord keyrec = new DNRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<DNRecord> getAll() throws SQLException
	{
		ArrayList<DNRecord> list = new ArrayList<DNRecord>();
		for(RecordBase it : getCache()) {
			list.add((DNRecord)it);
		}
		return list;
	}
	
	public void insertDetail(DNRecord rec, 
			ArrayList<Integer> auth_types) throws Exception
	{
		try {		
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert rec itself and get the new ID
			insert(rec);
			
			//insert auth_type
			DNAuthorizationTypeModel amodel = new DNAuthorizationTypeModel(auth);
			ArrayList<DNAuthorizationTypeRecord> arecs = new ArrayList<DNAuthorizationTypeRecord>();
			for(Integer auth_type : auth_types) {
				DNAuthorizationTypeRecord arec = new DNAuthorizationTypeRecord();
				arec.dn_id = rec.id;
				arec.authorization_type_id = auth_type;
				arecs.add(arec);
			}
			amodel.insert(arecs);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back DN detail insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(DNRecord rec, 
			ArrayList<Integer> auth_types) throws Exception
	{
		//Do insert / update to our DB
		try {
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
			//update auth_type
			DNAuthorizationTypeModel amodel = new DNAuthorizationTypeModel(auth);
			ArrayList<DNAuthorizationTypeRecord> arecs = new ArrayList<DNAuthorizationTypeRecord>();
			for(Integer auth_type : auth_types) {
				DNAuthorizationTypeRecord arec = new DNAuthorizationTypeRecord();
				arec.dn_id = rec.id;
				arec.authorization_type_id = auth_type;
				arecs.add(arec);
			}
			amodel.update(amodel.getAllByDNID(rec.id), arecs);
		
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back DN detail update transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}