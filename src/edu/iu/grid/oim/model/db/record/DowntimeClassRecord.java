package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DowntimeClassRecord implements IRecord {

	public Integer id;
	public String name;
	public String description;
	
	//load from existing record
	public DowntimeClassRecord(ResultSet rs) throws SQLException {
		id			= rs.getInt("id");
		name 		= rs.getString("name");
		description = rs.getString("description");
	}
	
	//for creating new record
	public DowntimeClassRecord()
	{
	}
}
