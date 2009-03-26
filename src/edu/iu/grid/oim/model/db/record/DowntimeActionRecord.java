package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DowntimeActionRecord implements IRecord {

	public Integer id;
	public String name;
	public String description;
	
	public String getTableName()
	{
		return "downtime_action";
	}
	
	//load from existing record
	public DowntimeActionRecord(ResultSet rs) throws SQLException {
		id			= rs.getInt("id");
		name 		= rs.getString("name");
		description = rs.getString("description");
	}
	
	//for creating new record
	public DowntimeActionRecord()
	{
	}
}
