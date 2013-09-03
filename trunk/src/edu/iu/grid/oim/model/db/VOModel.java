package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VOOasisUserRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOModel extends SmallTableModelBase<VORecord>
{	
    static Logger log = Logger.getLogger(VOModel.class);  

    public class VOReport {
    	public VOReportNameRecord name;
    	public ArrayList<VOReportNameFqanRecord> fqans;
    	public ArrayList<ContactRecord> contacts;
    }

    public VOModel(UserContext context) 
    {
    	super(context, "vo");
    }
    VORecord createRecord() throws SQLException
	{
		return new VORecord();
	}
    public String getName()
    {
    	return "Virtual Organization";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("sc_id")) {
			SCModel model = new SCModel(context);
			SCRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
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
		if(auth.getContact() != null) {	
			VOContactModel model = new VOContactModel(context);
			Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContact().id);
			for(VOContactRecord rec : vcrecs) {
				if(rec.contact_type_id == 1) continue; //submitter contact can't edit.
				list.add(rec.vo_id);
			}
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
		if(auth.allows("admin_ra")) return true;
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
		VOVOModel model = new VOVOModel(context);
		VOVORecord vovo = model.get(child_vo_id);
		if(vovo == null) return null;
		return get(vovo.parent_vo_id);	
	}
	
	public void insertDetail(VORecord rec, 
			ArrayList<VOContactRecord> contacts, 
			Integer parent_vo_id, 
			ArrayList<Integer> field_of_science,
			ArrayList<VOReport> voreports,
			ArrayList<ContactRecord> oasis_users) throws Exception
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {			
			//insert VO itself and get the new ID
			insert(rec);
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(context);
			//reset vo_id on all contact records
			for(VOContactRecord vcrec : contacts) {
				vcrec.vo_id = rec.id;
			}
			cmodel.insert(contacts);
			
			//process parent_vo
			VOVOModel vvmodel = new VOVOModel(context);
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
			ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
			for(Integer fsid : field_of_science) {
				VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
				vfosrec.vo_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			}
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(context);
			vofsmodel.insert(list);
		
			updateVOReports(rec.id, voreports); //yes, we can use update function to do the insert
			
			//process oasis_users
			ArrayList<VOOasisUserRecord> olist = new ArrayList<VOOasisUserRecord>();
			for(ContactRecord crec : oasis_users) {
				VOOasisUserRecord orec = new VOOasisUserRecord();
				orec.vo_id = rec.id;
				orec.contact_id = crec.id;
				olist.add(orec);
			}
			VOOasisUserModel voumodel = new VOOasisUserModel(context);
			voumodel.insert(olist);
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			conn.rollback();
			//re-throw original exception
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	
	public void updateDetail(VORecord rec,
			ArrayList<VOContactRecord> contacts, 
			Integer parent_vo_id, 
			ArrayList<Integer> field_of_science, 
			ArrayList<VOReport> voreports,
			ArrayList<ContactRecord> oasis_users) throws Exception
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {
			update(get(rec), rec);
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(context);
			//reset vo_id on all contact records
			for(VOContactRecord vcrec : contacts) {
				vcrec.vo_id = rec.id;
			}
			Collection<VOContactRecord> old_vo_contacts = cmodel.getByVOID(rec.id);
			cmodel.update(old_vo_contacts, contacts);
			
			//process parent_vo
			VOVOModel vvmodel = new VOVOModel(context);
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
			ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
			for(Integer fsid : field_of_science) {
				VOFieldOfScienceRecord vfosrec = new VOFieldOfScienceRecord();
				vfosrec.vo_id = rec.id;
				vfosrec.field_of_science_id = fsid;
				list.add(vfosrec);
			
			}
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(context);
			vofsmodel.update(vofsmodel.getByVOID(rec.id), list);

			updateVOReports(rec.id, voreports);
			
			//process oasis_users
			ArrayList<VOOasisUserRecord> olist = new ArrayList<VOOasisUserRecord>();
			for(ContactRecord crec : oasis_users) {
				VOOasisUserRecord orec = new VOOasisUserRecord();
				orec.vo_id = rec.id;
				orec.contact_id = crec.id;
				olist.add(orec);
			}
			VOOasisUserModel voumodel = new VOOasisUserModel(context);
			voumodel.update(voumodel.getByVOID(rec.id), olist);
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back VO update transaction.");
			conn.rollback();
	
			//re-throw original exception
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	
	public void updateVOReports(int vo_id, ArrayList<VOReport> voreports) throws SQLException
	{
		//process voreport record itself
		ArrayList<VOReportNameRecord> voreport_recs = new ArrayList();
		for(VOReport voreport : voreports) {
			voreport.name.vo_id = vo_id;
			voreport_recs.add(voreport.name);
		}
		VOReportNameModel vorepname_model = new VOReportNameModel(context);
		vorepname_model.update(vorepname_model.getAllByVOID(vo_id), voreport_recs);
		//process child records
		VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(context);
		VOReportContactModel vorepcontact_model = new VOReportContactModel(context);
		for(VOReport voreport : voreports) {
			VOReportNameRecord voreport_rec = voreport.name;

			//process fqan records
			ArrayList<VOReportNameFqanRecord> fqans = voreport.fqans;
			for(VOReportNameFqanRecord fqan : fqans) {
				fqan.vo_report_name_id = voreport_rec.id;
				if (fqan.role == null) fqan.role = ""; // New FQANs will have null role but DB does not accept null value, so set it to "" 
			}
			vorepnamefqan_model.update(vorepnamefqan_model.getAllByVOReportNameID(voreport_rec.id), fqans);		
			
			//process subscriber records
			ArrayList<ContactRecord> subscribers = voreport.contacts;
			ArrayList<VOReportContactRecord> voreport_contacts = new ArrayList();
			for(ContactRecord subscriber : subscribers) {
				//construct voreport contact record
				VOReportContactRecord vocrec = new VOReportContactRecord();
				vocrec.contact_id = subscriber.id;
				vocrec.contact_rank_id = 1;
				vocrec.contact_type_id = 10;//TODO - watch it
				vocrec.vo_report_name_id = voreport_rec.id;
				voreport_contacts.add(vocrec);
			}
			vorepcontact_model.update(vorepcontact_model.getAllByVOReportNameID(voreport_rec.id), voreport_contacts);	
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
	
	public VORecord getByName(String name) throws SQLException
	{
		for(VORecord rec : getAll()) {
			if(rec.name.equals(name)) return rec;
		}
		return null;
	}
}