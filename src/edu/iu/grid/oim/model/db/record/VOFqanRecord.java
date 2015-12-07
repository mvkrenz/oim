package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOFqanRecord extends RecordBase  
{
	@Key public Integer id;
	public String fqan;
	public Integer vo_report_name_id;
	
	//load from existing record
	public VOFqanRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOFqanRecord() {}
}
