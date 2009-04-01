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

}
