package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MailingListRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String email;
	
	//load from existing record
	public MailingListRecord(ResultSet rs) throws SQLException { super(rs); }
	
	//for creating new record
	public MailingListRecord() {}
}
