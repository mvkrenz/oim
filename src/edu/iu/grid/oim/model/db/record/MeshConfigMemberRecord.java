package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigMemberRecord extends RecordBase {
	
	@Key public Integer group_id;
	@Key public Integer resource_id;
	@Key public Integer service_id;
	
	//load from existing record
	public MeshConfigMemberRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigMemberRecord() {}
}
