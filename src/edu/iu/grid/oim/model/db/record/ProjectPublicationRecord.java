package edu.iu.grid.oim.model.db.record;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectPublicationRecord extends RecordBase  
{
	@Key public Integer id;
	public Integer project_id;
	public Date date;
	public String name;
	public String desc;
	
	//load from existing record
	public ProjectPublicationRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ProjectPublicationRecord() {}
}
