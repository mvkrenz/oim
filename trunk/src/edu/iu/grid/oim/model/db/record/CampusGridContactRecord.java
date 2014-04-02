package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CampusGridContactRecord extends RecordBase {

	@Key public Integer campusgrid_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	@Key public Integer contact_id;
	
	//load from existing record
	public CampusGridContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public CampusGridContactRecord() { }
	
	public Integer getRank() {
		return contact_rank_id;
	}
}
