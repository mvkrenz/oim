package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOReportContactRecord extends RecordBase  
{
	public Integer contact_id;
	public Integer vo_report_name_id;
	public Integer contact_type_id;
	public Integer contact_rank_id;
	
	//load from existing record
	public VOReportContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOReportContactRecord() {}
}
