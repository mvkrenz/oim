package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOContactRecord extends RecordBase {

	@Key public Integer contact_id;
	@Key public Integer vo_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	
	//load from existing record
	public VOContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOContactRecord()
	{
	}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		
		VOContactRecord you = (VOContactRecord)o;
		if(
				contact_id.compareTo(you.contact_id) == 0 &&
				vo_id.compareTo(you.vo_id) == 0 &&
				contact_type_id.compareTo(you.contact_type_id) == 0 &&
				contact_rank_id.compareTo(you.contact_rank_id) == 0 
		) return 0;
		return 1;
	}
	*/
}
