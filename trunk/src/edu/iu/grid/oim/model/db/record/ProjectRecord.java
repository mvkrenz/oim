package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ProjectRecord extends RecordBase  
{
	@Key public Integer id;
	public String name;
	public String desc; //aka. abstract_of_work
	public Integer pi_contact_id;
	public Integer vo_id;
	public Integer cg_id;
	public String organization;
	public String department;
	public Timestamp submit_time; //default to CURRENT_TIMESTAMP
	public Integer fos_id;
	
	//load from existing record
	public ProjectRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ProjectRecord() {}
}
