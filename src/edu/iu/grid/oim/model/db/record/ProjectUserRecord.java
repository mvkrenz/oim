package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectUserRecord extends RecordBase  
{
	/*project_id / submit_uid are unique indexed*/
	
	@Key public Integer id;
	public Integer project_id;
	public String submit_uid; //UID used on the submit node
	public Integer contact_id; //OIM contact ID
	
	//load from existing record
	public ProjectUserRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ProjectUserRecord() {}
}
