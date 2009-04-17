package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOReportNameFqanRecord extends RecordBase  
{
	@Key public Integer vo_report_name_id;
	@Key public String fqan;
	
	//load from existing record
	public VOReportNameFqanRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOReportNameFqanRecord() {}
}
