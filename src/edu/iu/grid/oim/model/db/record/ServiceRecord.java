package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public Integer port;
	public Integer service_group_id;
	//public String type;
	
	//load from existing record
	public ServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ServiceRecord() {}

}
