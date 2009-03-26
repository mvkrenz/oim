package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DowntimeSeverityRecord implements IRecord {

	public Integer id;
	public String name;
	public String wlcg_name;
	public String description;
	
	public String getTableName()
	{
		return "downtime_severity";
	}
	
	//load from existing record
	public DowntimeSeverityRecord(ResultSet rs) throws SQLException {
		id			= rs.getInt("id");
		name 		= rs.getString("name");
		wlcg_name 	= rs.getString("wlcg_name");
		description = rs.getString("description");
	}
	
	//for creating new record
	public DowntimeSeverityRecord()
	{
	}
}
