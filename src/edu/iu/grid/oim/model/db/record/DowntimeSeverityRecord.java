package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DowntimeSeverityRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String wlcg_name;
	public String description;
	
	//load from existing record
	public DowntimeSeverityRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DowntimeSeverityRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		DowntimeSeverityRecord you = (DowntimeSeverityRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
