package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactRankModel extends SmallTableModelBase<ContactRankRecord> {	
	public ContactRankModel(Connection _con, Authorization _auth) {
		super(_con, _auth, "contact_rank");
	}
	ContactRankRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ContactRankRecord(rs);
	}
	
	public ContactRankRecord get(int id) throws SQLException {
		ContactRankRecord keyrec = new ContactRankRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}
