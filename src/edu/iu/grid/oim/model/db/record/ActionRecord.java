package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ActionRecord extends RecordBase {

	@Key public Integer id;
	@Name("Action Name") public String name;
	public String description;
	
	//load from existing record
	public ActionRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ActionRecord() {}
}
