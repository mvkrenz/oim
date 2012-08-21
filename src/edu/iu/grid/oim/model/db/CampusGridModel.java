package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.CampusGridContactRecord;
import edu.iu.grid.oim.model.db.record.CampusGridFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.CampusGridSubmitNodeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;


public class CampusGridModel extends SmallTableModelBase<CampusGridRecord> {
    static Logger log = Logger.getLogger(CampusGridModel.class); 

	public CampusGridModel(UserContext context) {
		super(context, "campusgrid");
	}
    public String getName()
    {
    	return "CampusGrid";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		/*
		if(field_name.equals("sc_id")) {
			SCModel model = new SCModel(context);
			SCRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("facility_id")) {
			FacilityModel model = new FacilityModel(context);
			FacilityRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		*/
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
	
	CampusGridRecord createRecord() throws SQLException
	{
		return new CampusGridRecord();
	}
	public ArrayList<CampusGridRecord> getAll() throws SQLException
	{
		ArrayList<CampusGridRecord> list = new ArrayList<CampusGridRecord>();
		for(RecordBase it : getCache()) {
			list.add((CampusGridRecord)it);
		}
		return list;
	}
	public CampusGridRecord get(int id) throws SQLException {
		CampusGridRecord keyrec = new CampusGridRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		if(auth.getContact() != null) {	
			CampusGridContactModel model = new CampusGridContactModel(context);
			Collection<CampusGridContactRecord> vcrecs = model.getByContactID(auth.getContact().id);
			for(CampusGridContactRecord rec : vcrecs) {
				if(rec.contact_type_id == 1) continue; //submitter contact can't edit.
				list.add(rec.campusgrid_id);
			}
		}
		return list;
	}
	
	public Boolean canEdit(Integer id) {
		if(auth.allows("admin")) return true;
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
	
	public void insertDetail(CampusGridRecord rec,
			ArrayList<CampusGridContactRecord> contacts,
			ArrayList<Integer> field_of_science_ids,
			ArrayList<Integer> submit_hosts) throws Exception 
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {			
			//insert VO itself and get the new ID
			insert(rec);
			
			//process contact information
			CampusGridContactModel cmodel = new CampusGridContactModel(context);
			//reset vo_id on all contact records
			for(CampusGridContactRecord vcrec : contacts) {
				vcrec.campusgrid_id = rec.id;
			}
			cmodel.insert(contacts);
			
			//process field of science
			ArrayList<CampusGridFieldOfScienceRecord> list = new ArrayList<CampusGridFieldOfScienceRecord>();
			for(Integer fsid : field_of_science_ids) {
				CampusGridFieldOfScienceRecord vfosrec = new CampusGridFieldOfScienceRecord();
				vfosrec.campusgrid_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			}
			CampusGridFieldOfScienceModel vofsmodel = new CampusGridFieldOfScienceModel(context);
			vofsmodel.insert(list);	
			
			//process submit hosts
			ArrayList<CampusGridSubmitNodeRecord> slist = new ArrayList<CampusGridSubmitNodeRecord>();
			for(Integer host_id : submit_hosts) {
				CampusGridSubmitNodeRecord vfosrec = new CampusGridSubmitNodeRecord();
				vfosrec.campusgrid_id = rec.id;
				vfosrec.resource_id = host_id;
				slist.add(vfosrec);
			}
			CampusGridSubmitNodeModel cgsnmodel = new CampusGridSubmitNodeModel(context);
			cgsnmodel.insert(slist);	
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back CampusGrid insert transaction.");
			conn.rollback();
			//re-throw original exception
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	public void updateDetail(CampusGridRecord rec,
			ArrayList<CampusGridContactRecord> contacts,
			ArrayList<Integer> field_of_science_ids, 
			ArrayList<Integer> submit_hosts) throws Exception
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {
			update(get(rec), rec);
			
			//process contact information
			CampusGridContactModel cmodel = new CampusGridContactModel(context);
			//reset vo_id on all contact records
			for(CampusGridContactRecord vcrec : contacts) {
				vcrec.campusgrid_id = rec.id;
			}
			Collection<CampusGridContactRecord> old_vo_contacts = cmodel.getByVOID(rec.id);
			cmodel.update(old_vo_contacts, contacts);
			
			//process field of science
			ArrayList<CampusGridFieldOfScienceRecord> list = new ArrayList<CampusGridFieldOfScienceRecord>();
			for(Integer fsid : field_of_science_ids) {
				CampusGridFieldOfScienceRecord vfosrec = new CampusGridFieldOfScienceRecord();
				vfosrec.campusgrid_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			
			}
			CampusGridFieldOfScienceModel vofsmodel = new CampusGridFieldOfScienceModel(context);
			vofsmodel.update(vofsmodel.getByCampusGridID(rec.id), list);
			
			//process submit hosts
			ArrayList<CampusGridSubmitNodeRecord> slist = new ArrayList<CampusGridSubmitNodeRecord>();
			for(Integer host_id : submit_hosts) {
				CampusGridSubmitNodeRecord vfosrec = new CampusGridSubmitNodeRecord();
				vfosrec.campusgrid_id = rec.id;
				vfosrec.resource_id = host_id;
				slist.add(vfosrec);
			}
			CampusGridSubmitNodeModel cgsnmodel = new CampusGridSubmitNodeModel(context);
			cgsnmodel.update(cgsnmodel.getAllByCampusGridID(rec.id), slist);	
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back campus grid update transaction.");
			conn.rollback();
	
			//re-throw original exception
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
}
