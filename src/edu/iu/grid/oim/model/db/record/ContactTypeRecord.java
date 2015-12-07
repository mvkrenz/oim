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
	
	static public class Info {
		public int id;
		public String desc;
		public Info(int id, String desc) {
			this.id = id;
			this.desc = desc;
		}
	}
}
