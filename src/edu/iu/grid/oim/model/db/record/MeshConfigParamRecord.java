package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigParamRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public Integer service_id; //service_id that this params are designed for
	public String params;
	
	//load from existing record
	public MeshConfigParamRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigParamRecord() {}
}
