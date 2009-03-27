package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SCContactRecord implements IRecord {

	public Integer contact_id;
	public Integer sc_id;
	public Integer type_id;
	public Integer rank_id;
	
	//load from existing record
	public SCContactRecord(ResultSet rs) throws SQLException {
		contact_id = rs.getInt("contact_id");
		sc_id = rs.getInt("sc_id");
		type_id = rs.getInt("type_id");
		rank_id = rs.getInt("rank_id");
	}
	
	//for creating new record
	public SCContactRecord()
	{
	}
}
