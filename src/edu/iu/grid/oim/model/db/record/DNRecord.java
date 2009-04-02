package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DNRecord extends RecordBase {

	@Key public Integer id;
	public String dn_string;
	public Integer contact_id;
	
	//load from existing record
	public DNRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DNRecord() {}
}
