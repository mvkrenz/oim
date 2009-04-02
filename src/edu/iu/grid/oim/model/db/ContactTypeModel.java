package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;

public class ContactTypeModel extends SmallTableModelBase<ContactTypeRecord> {

	public static HashMap<Integer, ContactTypeRecord> cache = null;
		
	public ContactTypeModel(Authorization _auth) {
		super(_auth, "contact_type");
	}
	ContactTypeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ContactTypeRecord(rs);
	}
	
	public ContactTypeRecord get(int id) throws SQLException {
		ContactTypeRecord keyrec = new ContactTypeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}
