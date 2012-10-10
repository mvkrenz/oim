package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DNModel extends SmallTableModelBase<DNRecord> {
    static Logger log = Logger.getLogger(DNModel.class);  
    
    public DNModel(UserContext context) 
    {
    	super(context, "dn");
    }
    public String getName()
    {
    	return "DN";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
    DNRecord createRecord() throws SQLException
	{
		return new DNRecord();
	}
	
	public DNRecord getByDNString(String dn_string) throws SQLException
	{
		if(dn_string != null) {
			for(RecordBase it : getCache()) 
			{
				DNRecord rec = (DNRecord)it;
				if(rec.dn_string.compareTo(dn_string) == 0) {
					return rec;
				}
			}
		}
		return null;
	}
	public ArrayList<DNRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<DNRecord> list = new ArrayList<DNRecord>();
		for(RecordBase it : getCache()) 
		{
			DNRecord rec = (DNRecord)it;
			if(rec.contact_id.compareTo(contact_id) == 0) {
				list.add(rec);
			}
		}
		return list;
	}
	public ArrayList<DNRecord> getEnabledByContactID(int contact_id) throws SQLException
	{
		ArrayList<DNRecord> list = new ArrayList<DNRecord>();
		for(RecordBase it : getCache()) 
		{
			DNRecord rec = (DNRecord)it;
			if(rec.contact_id.compareTo(contact_id) == 0 && rec.disable == false) {
				list.add(rec);
			}
		}
		return list;
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
	
	public void insertDetail(DNRecord rec, ArrayList<Integer> auth_types) throws SQLException
	{
		Connection conn = connectOIM();
		try {		
			//process detail information
			conn.setAutoCommit(false);
			
			//insert rec itself and get the new ID
			insert(rec);
			
			//insert auth_type
			DNAuthorizationTypeModel amodel = new DNAuthorizationTypeModel(context);
			ArrayList<DNAuthorizationTypeRecord> arecs = new ArrayList<DNAuthorizationTypeRecord>();
			for(Integer auth_type : auth_types) {
				DNAuthorizationTypeRecord arec = new DNAuthorizationTypeRecord();
				arec.dn_id = rec.id;
				arec.authorization_type_id = auth_type;
				arecs.add(arec);
			}
			amodel.insert(arecs);
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back DN detail insert transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			
			//re-throw original exception
			throw e;
		}	
	}
	
	public void updateDetail(DNRecord rec, ArrayList<Integer> auth_types) throws SQLException
	{
		//Do insert / update to our DB
		Connection conn = connectOIM();
		try {
			//process detail information
			conn.setAutoCommit(false);
			
			update(get(rec), rec);
			
			//update auth_type
			DNAuthorizationTypeModel amodel = new DNAuthorizationTypeModel(context);
			ArrayList<DNAuthorizationTypeRecord> arecs = new ArrayList<DNAuthorizationTypeRecord>();
			for(Integer auth_type : auth_types) {
				DNAuthorizationTypeRecord arec = new DNAuthorizationTypeRecord();
				arec.dn_id = rec.id;
				arec.authorization_type_id = auth_type;
				arecs.add(arec);
			}
			amodel.update(amodel.getAllByDNID(rec.id), arecs);
		
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.error(e);
			log.info("Rolling back DN detail update transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			//re-throw original exception
			throw e;
		}			
	}

	/* - removing DN causes octompus-removing issue
	public void removeDN(DNRecord rec) throws SQLException
	{
		//remove DN and all authorization associated with that DN
		updateDetail(rec, new ArrayList<Integer>());
		
		//set submitter_dn_id to null for removed dn
		ContactModel cmodel = new ContactModel(context);
		ArrayList<ContactRecord> crecs = cmodel.getBySubmitterDNID(rec.id);
		for(ContactRecord crec : crecs) {
			crec.submitter_dn_id = null;
			cmodel.update(crec);
			log.info("contact id: " + crec.id + " submitter_dn_id has been reset to null");
		}
		
		//set resource_downtime dn_id to null for removed dn
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);
		ArrayList<ResourceDowntimeRecord> drecs = dmodel.getByDNID(rec.id);
		for(ResourceDowntimeRecord drec : drecs) {
			drec.dn_id = null;
			dmodel.update(drec);
			log.info("downtime id: " + drec.id + " dn_id has been reset to null");
		}
		
		//set resource_downtime dn_id to null for removed dn
		SiteModel smodel = new SiteModel(context);
		ArrayList<SiteRecord> srecs = smodel.getByDNID(rec.id);
		for(SiteRecord srec : srecs) {
			srec.submitter_dn_id = null;
			smodel.update(srec);
			log.info("site id: " + srec.id + " submitter_dn_id has been reset to null");
		}
		
		//then remove the dn itself
		super.remove(rec);
	}
	*/
}