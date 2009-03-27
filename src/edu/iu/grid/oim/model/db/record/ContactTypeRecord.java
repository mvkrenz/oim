package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactTypeRecord implements IRecord {

	public Integer id;
	public String name;

	public Integer require_dn;

	public Boolean allow_secondary;
	public Boolean allow_tertiary;

	
	public String getTableName()
	{
		return "contact_type";
	}
	
	//load from existing record
	public ContactTypeRecord(ResultSet rs) throws SQLException {
		id   = rs.getInt("id");
		name = rs.getString("name");

		allow_secondary = rs.getBoolean("allow_secondary");
		allow_tertiary = rs.getBoolean("allow_tertiary");

	}
	
	//for creating new record
	public ContactTypeRecord()
	{
	}
}
