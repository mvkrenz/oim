package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SCRecord implements IRecord {

	public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String community;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public SCRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		description = rs.getString("description");
		long_name = rs.getString("long_name");
		community = rs.getString("community");
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");
		footprints_id = rs.getString("footprints_id");		 
	}
	
	//for creating new record
	public SCRecord()
	{
	}
}
