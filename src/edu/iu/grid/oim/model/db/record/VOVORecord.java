package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//TODO - why can't this be just a field in VO table?
public class VOVORecord extends RecordBase 
{
	@Key public Integer child_vo_id;
	public Integer parent_vo_id;
	
	//load from existing record
	public VOVORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOVORecord() {}
}
