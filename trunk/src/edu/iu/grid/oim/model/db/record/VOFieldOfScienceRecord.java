package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOFieldOfScienceRecord extends RecordBase  
{
	@Key public Integer vo_id;
	@Key public Integer rank_id;
	@Key public Integer field_of_science_id;

	//load from existing record
	public VOFieldOfScienceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOFieldOfScienceRecord() {}
}
