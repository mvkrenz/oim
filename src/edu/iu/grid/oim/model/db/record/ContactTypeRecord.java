package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactTypeRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public Boolean allow_secondary;
	public Boolean allow_tertiary;
	
	//load from existing record
	public ContactTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ContactTypeRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		ContactTypeRecord you = (ContactTypeRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
