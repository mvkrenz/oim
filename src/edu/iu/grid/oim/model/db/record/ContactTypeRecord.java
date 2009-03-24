package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactTypeRecord implements IRecord {

	public Integer id;
	public String name;
	public Integer max_no_contacts;
	public Integer require_dn;
	
	public String getTableName()
	{
		return "certificate_dn";
	}
	
	//load from existing record
	public ContactTypeRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		max_no_contacts = rs.getInt("max_no_contacts");
		require_dn = rs.getInt("require_dn");
	}
	
	//for creating new record
	public ContactTypeRecord()
	{
	}
}
