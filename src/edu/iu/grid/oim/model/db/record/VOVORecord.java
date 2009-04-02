package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VOVORecord extends RecordBase 
{
	@Key public Integer child_vo_id;
	public Integer parent_vo_id;
	
	//load from existing record
	public VOVORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOVORecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		VOVORecord you = (VOVORecord)o;
		if(child_vo_id.compareTo(you.child_vo_id) == 0) return 0;
		return 1;
	}
	*/
}
