package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationTypeRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	
	//load from existing record
	public AuthorizationTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public AuthorizationTypeRecord() {}
}
