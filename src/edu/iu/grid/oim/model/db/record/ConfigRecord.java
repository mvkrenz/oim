package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigRecord extends RecordBase {

	@Key public String key;
	public String value;
	
	//load from existing record
	public ConfigRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ConfigRecord() {}
}
