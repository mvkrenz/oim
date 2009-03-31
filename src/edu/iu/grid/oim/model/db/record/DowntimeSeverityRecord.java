package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DowntimeSeverityRecord extends RecordBase {

	public Integer id;
	public String name;
	public String wlcg_name;
	public String description;
	
	//load from existing record
	public DowntimeSeverityRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DowntimeSeverityRecord() {}
}
