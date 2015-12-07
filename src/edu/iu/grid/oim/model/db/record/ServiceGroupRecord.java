package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ServiceGroupRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	
	//load from existing record
	public ServiceGroupRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ServiceGroupRecord() {}

}
