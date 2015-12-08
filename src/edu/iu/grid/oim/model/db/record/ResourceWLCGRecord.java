package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ResourceWLCGRecord extends RecordBase {

	@Key public Integer resource_id;
	public Boolean interop_bdii;
	public Boolean interop_monitoring;
	public Boolean interop_accounting;
	public String accounting_name;
	public Double ksi2k_minimum;
	public Double ksi2k_maximum;
	public Double hepspec;
	public Double storage_capacity_minimum;
	public Double storage_capacity_maximum;
	public Double tape_capacity;
	public Double apel_normal_factor;

	//load from existing record
	public ResourceWLCGRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceWLCGRecord() {}
}
