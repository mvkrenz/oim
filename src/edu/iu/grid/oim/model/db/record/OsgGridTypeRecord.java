package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OsgGridTypeRecord extends RecordBase {
	@Key public Integer id;
	public String name;
	public String description;
	
	//load from existing record
	public OsgGridTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public OsgGridTypeRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		OsgGridTypeRecord you = (OsgGridTypeRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
