package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigTestRecord extends RecordBase {

	@Key public Integer id;
	public Integer meshconfig_id;
	public Integer service_id;
	public Integer groupa_id;
	public Integer groupb_id;
	public Integer param_id;
	
	//load from existing record
	public MeshConfigTestRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigTestRecord() {}
}
