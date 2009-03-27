package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CpuInfoRecord implements IRecord {

	public Integer id;
	public String name;
	public Float normalization_constant;
	public String notes;

	//load from existing record
	public CpuInfoRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		normalization_constant = rs.getFloat("normalization_constant");
		notes = rs.getString("notes");
	}	
	
	//for creating new record
	public CpuInfoRecord()
	{
	}
}
