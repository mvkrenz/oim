package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class ContactModel extends SmallTableModelBase<ContactRecord> {
    static Logger log = Logger.getLogger(ContactModel.class);  

    public ContactModel(Authorization _auth) 
    {
    	super(_auth, "contact");
    }
    public String getName()
    {
    	return "Contact";
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
	public Collection<ContactRecord> getAllEditable() throws SQLException
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
		DNModel dnmodel = new DNModel(auth);
		
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
