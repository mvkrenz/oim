package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigWLCGMemberRecord extends RecordBase {
	
	@Key public Integer group_id;
	@Key public String primary_key;
	@Key public Integer service_id;
	
	//load from existing record
	public MeshConfigWLCGMemberRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigWLCGMemberRecord() {}
}
