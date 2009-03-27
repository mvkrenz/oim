package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldOfScienceRecord implements IRecord {

	public Integer id;
	public String name;
	
	//load from existing record
	public FieldOfScienceRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
	}
	
	//for creating new record
	public FieldOfScienceRecord()
	{
	}
}
