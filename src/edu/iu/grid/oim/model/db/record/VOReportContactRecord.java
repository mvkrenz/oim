package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOReportContactRecord extends RecordBase  
{
	@Key public Integer vo_report_name_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	@Key public Integer contact_id;
	
	//load from existing record
	public VOReportContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOReportContactRecord() {}
	
}
