package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SCRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String community;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public SCRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SCRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		SCRecord you = (SCRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
