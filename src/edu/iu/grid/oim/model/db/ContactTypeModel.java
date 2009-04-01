package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactTypeModel extends SmallTableModelBase<ContactTypeRecord> {

	public static HashMap<Integer, ContactTypeRecord> cache = null;
		
	public ContactTypeModel(Connection _con, Authorization _auth) {
		super(_con, _auth, "contact_type");
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
