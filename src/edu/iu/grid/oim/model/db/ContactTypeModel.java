package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactTypeModel extends DBModel {


	//tablename - such as "sc_contact";
	//fkey - such as "sc_id" which maps the foreign key
	public ContactTypeModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
	}
	
	public ContactTypeRecord get(int id) throws SQLException
	{
		PreparedStatement stmt = null;

		String sql = "SELECT * from contact_type where id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, id);
	
		ResultSet rs = stmt.executeQuery();

		rs = stmt.executeQuery();
		if(rs.next()) {
			return new ContactTypeRecord(rs);
		}
		log.warn("Couldn't find contact_type where id = " + id);

		return null;
	}
}
