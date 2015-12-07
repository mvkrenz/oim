package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOResourceOwnershipRecord extends RecordBase  
{
	@Key public Integer resource_id;
	@Key public Integer vo_id;
	public Double percent;
	
	//load from existing record
	public VOResourceOwnershipRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOResourceOwnershipRecord() {}
}
