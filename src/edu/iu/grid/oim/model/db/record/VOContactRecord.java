package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOContactRecord implements IRecord {

	public Integer contact_id;
	public Integer vo_id;
	public Integer contact_type_id;
	public Integer contact_rank_id;
	
	//load from existing record
	public VOContactRecord(ResultSet rs) throws SQLException {
		contact_id = rs.getInt("contact_id");
		vo_id = rs.getInt("vo_id");
		contact_type_id = rs.getInt("contact_type_id");
		contact_rank_id = rs.getInt("contact_rank_id");
	}
	
	//for creating new record
	public VOContactRecord()
	{
	}
}
