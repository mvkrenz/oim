package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigOIMMemberRecord extends RecordBase {
	
	@Key public Integer group_id;
	@Key public Integer resource_id;
	@Key public Integer service_id;
	
	//load from existing record
	public MeshConfigOIMMemberRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigOIMMemberRecord() {}
}
