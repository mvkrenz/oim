package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GridAdminRecord extends RecordBase {

	@Key public Integer vo_id;
	@Key public String domain;
	@Key public Integer contact_id;
	
	//load from existing record
	public GridAdminRecord(ResultSet rs) throws SQLException { super(rs); }
	
	//for creating new record
	public GridAdminRecord() {}

}
