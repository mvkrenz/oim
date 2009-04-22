package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webif.divex.form.CheckBoxFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

import edu.iu.grid.oim.model.VOReportConsolidator;

import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOModel extends SmallTableModelBase<VORecord>
{	
    static Logger log = Logger.getLogger(VOModel.class);  

    public VOModel(Authorization auth) 
    {
    	super(auth, "vo");
    }
    VORecord createRecord() throws SQLException
	{
		return new VORecord();
	}
    public String getName()
    {
    	return "Virtual Organization";
    }
	public VORecord get(int id) throws SQLException {
		VORecord keyrec = new VORecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public Collection<VORecord> getAllEditable() throws SQLException
	{	   
		ArrayList<VORecord> list = new ArrayList();
		//only select record that is editable
	    for(RecordBase rec : getCache()) {
	    	VORecord vorec = (VORecord)rec;
	    	if(canEdit(vorec.id)) {
	    		list.add(vorec);
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
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		return canEdit(id);
	}
	public boolean canEdit(int vo_id)
	{
		if(auth.allows("admin")) return true;
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(vo_id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
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
			ArrayList<Integer> field_of_science,
			ArrayList<VOReportConsolidator> report_consolidated_records) throws Exception
//			ArrayList<VOReportNameRecord> report_name_records,
//			ArrayList<VOReportNameFqanRecord> fqan_records,
//			ArrayList<VOReportContactRecord> report_contact_records) throws Exception

	{
		try {			
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert VO itself and get the new ID
			insert(rec);
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(auth);
			//reset vo_id on all contact records
			for(VOContactRecord vcrec : contacts) {
				vcrec.vo_id = rec.id;
			}
			cmodel.insert(contacts);
			
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
			for(Integer fsid : field_of_science) {
				VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
				vfosrec.vo_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			}
			vofsmodel.insert(list);
		
			//process VO report names
			VOReportNameModel vorepname_model = new VOReportNameModel(auth);
			VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(auth);
			VOReportContactModel vorepcontact_model = new VOReportContactModel(auth);

			for (VOReportConsolidator consolidated_record : report_consolidated_records) {
				VOReportNameRecord vorepname_record = consolidated_record.name; 
				vorepname_record.vo_id = rec.id; 

				// report names themselves
				vorepname_model.insert(vorepname_record);		

				ArrayList <VOReportNameFqanRecord> fqan_records = consolidated_record.fqans; 
				for (VOReportNameFqanRecord fqan_record : fqan_records ) {
					fqan_record.vo_report_name_id = vorepname_record.id;
				}
				vorepnamefqan_model.insert(fqan_records);

				ArrayList <VOReportContactRecord> contact_records = consolidated_record.vorep_contacts; 
				for (VOReportContactRecord contact_record : contact_records ) {
					contact_record.vo_report_name_id = vorepname_record.id;
				}
				vorepcontact_model.insert(contact_records);

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
	
	public void updateDetail(VORecord rec,
			ArrayList<VOContactRecord> contacts, 
			Integer parent_vo_id, 
			ArrayList<Integer> field_of_science, 
			ArrayList<VOReportConsolidator> report_consolidated_records) throws Exception
//			ArrayList<VOReportNameRecord> report_name_records,
//			ArrayList<VOReportNameFqanRecord> fqan_records,
//			ArrayList<VOReportContactRecord> report_contact_records) throws Exception
	{
		//Do insert / update to our DB
		try {
		
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
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
			for(Integer fsid : field_of_science) {
				VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
				vfosrec.vo_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			
			}
			vofsmodel.update(vofsmodel.getByVOID(rec.id), list);

			//process VO report names
			VOReportNameModel vorepname_model = new VOReportNameModel(auth);
			VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(auth);
			VOReportContactModel vorepcontact_model = new VOReportContactModel(auth);
			ArrayList<VOReportNameRecord> vorepname_records = new ArrayList<VOReportNameRecord> ();

			for (VOReportConsolidator consolidated_record : report_consolidated_records) {
				ArrayList<VOReportNameFqanRecord> old_fqan_records = new ArrayList<VOReportNameFqanRecord> ();
				ArrayList<VOReportContactRecord> old_contact_records = new ArrayList<VOReportContactRecord> ();

				VOReportNameRecord vorepname_record = consolidated_record.name; 
				vorepname_record.vo_id = rec.id;
				vorepname_records.add(vorepname_record);
				old_fqan_records.addAll(vorepnamefqan_model.getAllByVOReportNameID(vorepname_record.id));
				old_contact_records.addAll(vorepcontact_model.getByVOReportNameID(vorepname_record.id));

				// fqans
				ArrayList <VOReportNameFqanRecord> fqan_records = consolidated_record.fqans; 
				for (VOReportNameFqanRecord fqan_record : fqan_records ) {
					fqan_record.vo_report_name_id = vorepname_record.id;
				}
				vorepnamefqan_model.update (old_fqan_records, fqan_records);
		
				// VO reporting contacts
				ArrayList <VOReportContactRecord> contact_records = consolidated_record.vorep_contacts; 
				for (VOReportContactRecord contact_record : contact_records ) {
					contact_record.vo_report_name_id = vorepname_record.id;
				}
				vorepcontact_model.update(old_contact_records,contact_records);
			}
			// report names themselves
			vorepname_model.update(vorepname_model.getAllByVOID(rec.id),vorepname_records);	
			
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
	
	public ArrayList<VORecord> getAll() throws SQLException
	{
		ArrayList<VORecord> list = new ArrayList<VORecord>();
		for(RecordBase it : getCache()) {
			list.add((VORecord)it);
		}
		return list;
	}
	

}

