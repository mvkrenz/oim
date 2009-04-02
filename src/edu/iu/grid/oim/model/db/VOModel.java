package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.webif.divex.form.CheckBoxFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class VOModel extends SmallTableModelBase<VORecord>
{	
    static Logger log = Logger.getLogger(VOModel.class);  

    public VOModel(Authorization auth) 
    {
    	super(auth, "vo");
    }
    VORecord createRecord(ResultSet rs) throws SQLException
	{
		return new VORecord(rs);
	}
	public VORecord get(int id) throws SQLException {
		VORecord keyrec = new VORecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public Collection<VORecord> getAllEditable() throws SQLException
	{	   
		ArrayList<VORecord> list = new ArrayList();
	    if(auth.allows("admin_vo")) {
	    	//admin can edit all scs
	    	for(RecordBase rec : getCache()) {
	    		list.add((VORecord)rec);
	    	}
	    } else {
	    	//only select record that is editable
	    	HashSet<Integer> accessible = getEditableIDs();
		    for(RecordBase rec : getCache()) {
		    	VORecord vorec = (VORecord)rec;
		    	if(accessible.contains(vorec.id)) {
		    		list.add(vorec);
		    	}
		    }	    	
	    }
	    return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(auth);
		Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContactID());
		for(VOContactRecord rec : vcrecs) {
			list.add(rec.vo_id);
		}
		return list;
	}
	
	public VORecord getParentVO(int child_vo_id) throws SQLException
	{
		VOVOModel model = new VOVOModel(auth);
		VOVORecord vovo = model.get(child_vo_id);
		if(vovo == null) return null;
		return get(vovo.parent_vo_id);	
	}
	
	public void insertDetail(VORecord rec, 
			ArrayList<VOContactRecord> contacts, 
			Integer parent_vo_id, 
			HashMap<Integer, CheckBoxFormElementDE> field_of_science) throws Exception
	{
		try {
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert VO itself and get the new ID
			ResultSet rs = insert(rec);
			rec.id = rs.getInt(1);
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(auth);
			//reset vo_id on all contact records
			for(VOContactRecord vcrec : contacts) {
				vcrec.vo_id = rec.id;
			}
			cmodel.update(cmodel.getByVOID(rec.id), contacts);
			
			//process parent_vo
			VOVOModel vvmodel = new VOVOModel(auth);
			VOVORecord vvrec = new VOVORecord();
			vvrec.child_vo_id = rec.id;
			vvrec.parent_vo_id = parent_vo_id;
			VOVORecord vvrec_old = vvmodel.get(vvrec);
			if(vvrec_old != null) {
				//we have old record - need to update
				if(vvrec.parent_vo_id != null) {
					vvmodel.update(vvrec_old, vvrec);
				} else {
					//parent is changed to null - remove it
					vvmodel.remove(vvrec_old);
				}
			} else {
				//we don't have old record - need to insert
				if(vvrec.parent_vo_id != null) {
					//only if parent_vo is non-null
					vvmodel.insert(vvrec);
				}
			}
			
			//process field of science
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(auth);
			ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
			for(Integer fsid : field_of_science.keySet()) {
				CheckBoxFormElementDE elem = field_of_science.get(fsid);
				if(elem.getValue()) {
					VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
					vfosrec.vo_id = rec.id;
					vfosrec.field_of_science_id = fsid;
					list.add(vfosrec);
				}
			}
			vofsmodel.update(vofsmodel.getByVOID(rec.id), list);
		
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (AuthorizationException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(VORecord rec,
			ArrayList<VOContactRecord> contacts, 
			Integer parent_vo_id, 
			HashMap<Integer, CheckBoxFormElementDE> field_of_science) throws Exception
	{
		//Do insert / update to our DB
		try {
			//process detail information
			getConnection().setAutoCommit(false);
			
			VOModel model = new VOModel(auth);
			model.update(model.get(rec), rec);
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(auth);
			//reset vo_id on all contact records
			for(VOContactRecord vcrec : contacts) {
				vcrec.vo_id = rec.id;
			}
			cmodel.update(cmodel.getByVOID(rec.id), contacts);
			
			//process parent_vo
			VOVOModel vvmodel = new VOVOModel(auth);
			VOVORecord vvrec = new VOVORecord();
			vvrec.child_vo_id = rec.id;
			vvrec.parent_vo_id = parent_vo_id;
			VOVORecord vvrec_old = vvmodel.get(vvrec);
			if(vvrec_old != null) {
				//we have old record - need to update
				if(vvrec.parent_vo_id != null) {
					vvmodel.update(vvrec_old, vvrec);
				} else {
					//parent is changed to null - remove it
					vvmodel.remove(vvrec_old);
				}
			} else {
				//we don't have old record - need to insert
				if(vvrec.parent_vo_id != null) {
					//only if parent_vo is non-null
					vvmodel.insert(vvrec);
				}
			}
			
			//process field of science
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(auth);
			ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
			for(Integer fsid : field_of_science.keySet()) {
				CheckBoxFormElementDE elem = field_of_science.get(fsid);
				if(elem.getValue()) {
					VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
					vfosrec.vo_id = rec.id;
					vfosrec.field_of_science_id = fsid;
					list.add(vfosrec);
				}
			}
			vofsmodel.update(vofsmodel.getByVOID(rec.id), list);
		
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (AuthorizationException e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
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

