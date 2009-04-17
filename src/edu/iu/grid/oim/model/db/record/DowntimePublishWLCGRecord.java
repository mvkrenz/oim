package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DowntimePublishWLCGRecord extends RecordBase {

	@Key public Integer resource_downtime_id;
	@Key public Integer downtime_action_id;
	public Integer publish_status;
	public Timestamp timestamp;
	public Boolean disable;
	
	//load from existing record
	public DowntimePublishWLCGRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DowntimePublishWLCGRecord() {}

}
