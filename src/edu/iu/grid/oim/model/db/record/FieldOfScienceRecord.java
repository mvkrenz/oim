package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldOfScienceRecord extends RecordBase {
	@Key public Integer id;
	public String name;
	
	//load from existing record
	public FieldOfScienceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public FieldOfScienceRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		FieldOfScienceRecord you = (FieldOfScienceRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
