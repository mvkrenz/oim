package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class SiteModel extends SmallTableModelBase<SiteRecord> {
    static Logger log = Logger.getLogger(SiteModel.class); 

	public SiteModel(Authorization _auth) {
		super(_auth, "sc_contact");
	}
	SiteRecord createRecord(ResultSet rs) throws SQLException
	{
		return new SiteRecord(rs);
	}
	public ArrayList<SiteRecord> getAll() throws SQLException
	{
		ArrayList<SiteRecord> list = new ArrayList<SiteRecord>();
		for(RecordBase it : getCache()) {
			list.add((SiteRecord)it);
		}
		return list;
	}
	public SiteRecord get(int id) throws SQLException {
		SiteRecord keyrec = new SiteRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public void insertDetail(SiteRecord rec) throws Exception
	{
		try {
			auth.check("admin");

			//insert Site itself and get the new ID
			insert(rec);
		} catch (AuthorizationException e) {
			log.error(e);
			throw new Exception(e);
		} catch (SQLException e) {
			log.error(e);
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(SiteRecord rec) throws Exception
	{
		//Do insert / update to our DB
		try {
			auth.check("admin");
			update(get(rec), rec);
		} catch (AuthorizationException e) {
			log.error(e);
			//re-throw original exception
			throw new Exception(e);
		} catch (SQLException e) {
			log.error(e);
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}
