package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DNAuthorizationTypeRecord extends RecordBase {

	@Key public Integer dn_id;
	@Key public Integer authorization_type_id;
	
	//load from existing record
	public DNAuthorizationTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DNAuthorizationTypeRecord() {}
}
