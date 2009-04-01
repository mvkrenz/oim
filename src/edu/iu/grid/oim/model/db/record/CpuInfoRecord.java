package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CpuInfoRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public Float normalization_constant;
	public String notes;

	//load from existing record
	public CpuInfoRecord(ResultSet rs) throws SQLException { super(rs); }	
	//for creating new record
	public CpuInfoRecord() {}
}
