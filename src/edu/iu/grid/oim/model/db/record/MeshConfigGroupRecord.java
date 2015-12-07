package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigGroupRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public Integer service_id;
	
	//load from existing record
	public MeshConfigGroupRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigGroupRecord() {}
}
