package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class MetricServiceRecord extends RecordBase {

	@Key public Integer metric_id;
	@Key public Integer service_id;
	public Boolean critical;

	//load from existing record
	public MetricServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MetricServiceRecord() {}
}
