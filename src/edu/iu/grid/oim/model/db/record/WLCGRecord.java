package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class WLCGRecord extends RecordBase {

	@Key public Integer resource_id;
	public Boolean interop_bdii;
	public Boolean interop_monitoring;
	public Boolean interop_accounting;
	public String accounting_name;
	public Double ksi2k_minimum;
	public Double ksi2k_maximum;
	public Double storage_capacity_minimum;
	public Double storage_capacity_maximum;

	//load from existing record
	public WLCGRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public WLCGRecord() {}
}
