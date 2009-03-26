package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MailingListRecord implements IRecord {

	public Integer id;
	public String name;
	public String email;
	
	public String getTableName()
	{
		return "mailing_list";
	}
	
	//load from existing record
	public MailingListRecord(ResultSet rs) throws SQLException {
		id 	  = rs.getInt("id");
		name  = rs.getString("name");
		email = rs.getString("email");
	}
	
	//for creating new record
	public MailingListRecord()
	{
	}
}
