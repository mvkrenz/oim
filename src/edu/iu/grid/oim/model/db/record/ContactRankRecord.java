package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactRankRecord implements IRecord {

	public Integer id;
	public String name;
	public Integer max_no_contacts;
	
	public String getTableName()
	{
		return "contact_rank";
	}
	
	//load from existing record
	public ContactRankRecord(ResultSet rs) throws SQLException {
		id 				= rs.getInt("id");
		name 			= rs.getString("name");
	}
	
	//for creating new record
	public ContactRankRecord()
	{
	}
}
