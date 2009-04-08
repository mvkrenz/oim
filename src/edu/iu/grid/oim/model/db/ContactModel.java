package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactModel extends SmallTableModelBase<ContactRecord> {
    static Logger log = Logger.getLogger(ContactModel.class);  

    public ContactModel(Authorization _auth) 
    {
    	super(_auth, "contact");
    }
	ContactRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ContactRecord(rs);
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
	    if(auth.allows("admin")) {
	    	//admin can edit all scs
	    	for(RecordBase rec : getCache()) {
	    		list.add((ContactRecord)rec);
	    	}
	    } else {
	    	//only select record that is editable
		    for(RecordBase rec : getCache()) {
		    	ContactRecord vorec = (ContactRecord)rec;
		    	if(canEdit(vorec.id)) {
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
		for(ContactRecord rec : getAll()) {
			if(rec.submitter_dn_id.compareTo(auth.getDNID()) == 0)  {
				list.add(rec.id);
			}
		}
		return list;
	}
	
	public boolean canEdit(int vo_id)
	{
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(vo_id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
}
