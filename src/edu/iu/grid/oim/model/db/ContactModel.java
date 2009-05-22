package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactModel extends SmallTableModelBase<ContactRecord> {
    static Logger log = Logger.getLogger(ContactModel.class);  

    public ContactModel(Context _context) 
    {
    	super(_context, "contact");
    }
    public String getName()
    {
    	return "Contact";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("submitter_dn_id")) {
			DNModel model = new DNModel(context);
			if(value.equals(LogModel.NULL_TOKEN)) return value;
			DNRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.dn_string + ")";
		} else if(field_name.equals("service_id")) {
			ServiceModel model = new ServiceModel(context);
			ServiceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	ContactRecord createRecord() throws SQLException
	{
		return new ContactRecord();
	}
    
	public ContactRecord get(int id) throws SQLException {
		ContactRecord keyrec = new ContactRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ContactRecord> getAll() throws SQLException
	{
		ArrayList<ContactRecord> list = new ArrayList<ContactRecord>();
		for(RecordBase it : getCache()) {
			list.add((ContactRecord)it);
		}
		return list;
	}
	public ContactRecord getByemail(String email) throws SQLException
	{
		for(RecordBase it : getCache()) {
			ContactRecord rec = (ContactRecord)it;
			if(rec.primary_email != null && rec.primary_email.equals(email)) {
				return rec;
			}
			if(rec.secondary_email != null && rec.secondary_email.equals(email)) {
				return rec;
			}
		}
		return null;
	}
	
	public ArrayList<ContactRecord> getAllEditable() throws SQLException
	{	   
		ArrayList<ContactRecord> list = new ArrayList();

    	//only select record that is editable
	    for(RecordBase rec : getCache()) {
	    	ContactRecord vorec = (ContactRecord)rec;
	    	if(canEdit(vorec.id)) {
	    		list.add(vorec);
	    	}
	    }	    	
	    return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		DNModel dnmodel = new DNModel(context);
		
		HashSet<Integer> list = new HashSet<Integer>();
		for(ContactRecord rec : getAll()) {
			//allow editing if user is submitter_dn
			if(rec.submitter_dn_id != null && rec.submitter_dn_id.compareTo(auth.getDNID()) == 0)  {
				//only allow editing if the contact is not yet associated with DN
				DNRecord dnrec = dnmodel.getByContactID(rec.id);
				if(dnrec == null) {
					list.add(rec.id);
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
}
