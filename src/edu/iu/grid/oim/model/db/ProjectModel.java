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
import edu.iu.grid.oim.model.db.VOModel.VOReport;
import edu.iu.grid.oim.model.db.record.CampusGridContactRecord;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectPublicationRecord;
import edu.iu.grid.oim.model.db.record.ProjectUserRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;

public class ProjectModel extends SmallTableModelBase<ProjectRecord>
{	
    static Logger log = Logger.getLogger(ProjectModel.class);  

    public ProjectModel(UserContext context) 
    {
    	super(context, "project");
    }
    ProjectRecord createRecord() throws SQLException
	{
		return new ProjectRecord();
	}
    public String getName()
    {
    	return "Project";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id") && value != null) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		if(field_name.equals("cg_id") && value != null) {
			CampusGridModel model = new CampusGridModel(context);
			CampusGridRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		if(field_name.equals("fos_id") && value != null) {
			FieldOfScienceModel model = new FieldOfScienceModel(context);
			FieldOfScienceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		if(field_name.equals("pi_contact_id") && value != null) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	} 
	public ProjectRecord get(int id) throws SQLException {
		ProjectRecord keyrec = new ProjectRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public Collection<ProjectRecord> getAllEditable() throws SQLException
	{	   
		ArrayList<ProjectRecord> list = new ArrayList();
		//only select record that is editable
	    for(RecordBase rec : getCache()) {
	    	ProjectRecord prec = (ProjectRecord)rec;
	    	if(canEdit(prec.id)) {
	    		list.add(prec);
	    	}
	    }	    	
	    return list;
	}
	
	//TODO - I don't like this algorithm a bit..
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		if(auth.getContact() != null) {
			VOContactModel model = new VOContactModel(context);
			Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContact().id);
			for(VOContactRecord rec : vcrecs) {
				if(rec.contact_type_id == 1) continue; //submitter contact can't edit.
				for(RecordBase it : getCache()) {
					ProjectRecord prec = (ProjectRecord)it;
					if(prec.vo_id != null && prec.vo_id.equals(rec.vo_id)) {
						list.add(prec.id);
					}
				}
			}
			CampusGridContactModel cgmodel = new CampusGridContactModel(context);
			Collection<CampusGridContactRecord> cgcrecs = cgmodel.getByContactID(auth.getContact().id);
			for(CampusGridContactRecord rec : cgcrecs) {
				if(rec.contact_type_id == 1) continue; //submitter contact can't edit.
				for(RecordBase it : getCache()) {
					ProjectRecord prec = (ProjectRecord)it;
					if(prec.cg_id != null && prec.cg_id.equals(rec.campusgrid_id)) {
						list.add(prec.id);
					}
				}
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
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(vo_id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
	
	public void insertDetail(ProjectRecord rec, ArrayList<ProjectPublicationRecord> publications, ArrayList<ProjectUserRecord> users) throws Exception
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {			
			//insert project itself and get the new ID
			insert(rec);
			
			updatePublications(rec.id, publications); //yes, we can use update function to do the insert
			updateUsers(rec.id, users); //yes, we can use update function to do the insert
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back Project insert transaction.");
			conn.rollback();
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	
	public void updateDetail(ProjectRecord rec, ArrayList<ProjectPublicationRecord> publications, ArrayList<ProjectUserRecord> users) throws Exception
	{
		Connection conn = connectOIM();
		conn.setAutoCommit(false);
		try {
			update(get(rec), rec);
			
			updatePublications(rec.id, publications);
			updateUsers(rec.id, users); 
			
			conn.commit();
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back Project update transaction.");
			conn.rollback();
	
			//re-throw original exception
			throw new Exception(e);
		} finally {
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	
	public ArrayList<ProjectRecord> getAll() throws SQLException
	{
		ArrayList<ProjectRecord> list = new ArrayList<ProjectRecord>();
		for(RecordBase it : getCache()) {
			list.add((ProjectRecord)it);
		}
		return list;
	}
	
	public ProjectRecord getByName(String name) throws SQLException
	{
		for(ProjectRecord rec : getAll()) {
			if(rec.name.equals(name)) return rec;
		}
		return null;
	}
	
	public void updatePublications(int id, ArrayList<ProjectPublicationRecord> publications) throws SQLException
	{
		//reset project ids
		for(ProjectPublicationRecord rec : publications) {
			rec.project_id = id;
		}
		ProjectPublicationModel ppmodel = new ProjectPublicationModel(context);
		ppmodel.update(ppmodel.getAllByProjectId(id), publications);
	}
	
	public void updateUsers(int id, ArrayList<ProjectUserRecord> users) throws SQLException
	{
		//reset project ids
		for(ProjectUserRecord rec : users) {
			rec.project_id = id;
		}
		ProjectUserModel pumodel = new ProjectUserModel(context);
		pumodel.update(pumodel.getAllByProjectId(id), users);
	}
}