package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOFieldOfScienceRecord extends RecordBase  
{
	@Key public Integer vo_id;
	@Key public Integer field_of_science_id;

	//load from existing record
	public VOFieldOfScienceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOFieldOfScienceRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		
		VOFieldOfScienceRecord you = (VOFieldOfScienceRecord)o;
		if(
				vo_id.compareTo(you.vo_id) == 0 &&
				field_of_science_id.compareTo(you.field_of_science_id) == 0
		) return 0;
		return 1;
	}
	*/
}
