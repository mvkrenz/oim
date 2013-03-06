package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceContactRecord extends RecordBase {

	@Key public Integer resource_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	@Key public Integer contact_id;
	
	//load from existing record
	public ResourceContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceContactRecord() { }
	/*
	public Integer getRank() {
		return contact_rank_id;
	}
	*/
}
