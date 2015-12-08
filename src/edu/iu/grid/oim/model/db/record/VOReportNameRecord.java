package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOReportNameRecord extends RecordBase  
{
	@Key public Integer id;
	public String name;
	public Integer vo_id;
	
	//load from existing record
	public VOReportNameRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOReportNameRecord() {}
}
