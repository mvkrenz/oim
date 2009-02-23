package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOContactRecord implements IRecord {

	public Integer person_id;
	public Integer vo_id;
	public Integer type_id;
	public Integer rank_id;
	
	//load from existing record
	public VOContactRecord(ResultSet rs) throws SQLException {
		person_id = rs.getInt("person_id");
		vo_id = rs.getInt("vo_id");
		type_id = rs.getInt("type_id");
		rank_id = rs.getInt("rank_id");
	}
	
	//for creating new record
	public VOContactRecord()
	{
	}
}
