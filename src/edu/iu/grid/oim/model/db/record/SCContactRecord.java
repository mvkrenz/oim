package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SCContactRecord extends RecordBase {
	public Integer contact_id;
	public Integer sc_id;
	public Integer type_id;
	public Integer rank_id;
	
	//load from existing record
	public SCContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SCContactRecord() {}
}
