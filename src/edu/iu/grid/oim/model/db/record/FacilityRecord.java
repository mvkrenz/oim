package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class FacilityRecord implements IRecord {

	public Integer id;
	public String name;
	public String description;
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public FacilityRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		description = rs.getString("description");
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");
	}
	
	//for creating new record
	public FacilityRecord()	throws SQLException {
	}
}
