package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SCContactRecord extends RecordBase {
	@Key public Integer contact_id;
	@Key public Integer sc_id;
	@Key public Integer type_id;
	@Key public Integer rank_id;
	
	//load from existing record
	public SCContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SCContactRecord() {}
}
