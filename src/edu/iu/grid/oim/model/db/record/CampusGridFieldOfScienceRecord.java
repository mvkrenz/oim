package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CampusGridFieldOfScienceRecord extends RecordBase  
{
	@Key public Integer campusgrid_id;
	@Key public Integer field_of_science_id;

	//load from existing record
	public CampusGridFieldOfScienceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public CampusGridFieldOfScienceRecord() {}
}
