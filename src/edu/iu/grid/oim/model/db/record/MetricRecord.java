package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class MetricRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String common_name;
	public String abbrev;
	public String description;
	public Integer time_interval;
	public Integer fresh_for;
	public String help_url;
	public String wlcg_metric_type;
	
	//load from existing record
	public MetricRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MetricRecord() {}
}
