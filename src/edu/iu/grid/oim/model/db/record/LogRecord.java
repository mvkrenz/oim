package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LogRecord extends RecordBase {
	@Key public Integer id;
	public String type;
	public String xml;
	public Timestamp timestamp;
	public Integer dn_id;
	public String model;
	public String comment;
	public String ip;
	public String key;
	public Integer contact_id;
	
	//load from existing record
	public LogRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public LogRecord() {}
}
