package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactRankRecord extends RecordBase {

	public Integer id;
	public String name;
		
	//load from existing record
	public ContactRankRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ContactRankRecord() {}
}
