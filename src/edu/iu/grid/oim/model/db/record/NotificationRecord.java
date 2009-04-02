package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class NotificationRecord extends RecordBase {

	@Key public Integer id;
	public String type;
	public String table;
	public String key;
	public String format;
	public String frequency;
	public Integer contact_id;
	public Timestamp timestamp;	
	
	public NotificationRecord(ResultSet rs) throws SQLException { super(rs); }
	public NotificationRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		NotificationRecord you = (NotificationRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
