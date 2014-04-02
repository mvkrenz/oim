package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CpuInfoRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public Float normalization_constant;
	public Float hepspec_normalization_constant;
	public String notes;

	//load from existing record
	public CpuInfoRecord(ResultSet rs) throws SQLException { super(rs); }
	public String getName() {
		return name;
	}
	//for creating new record
	public CpuInfoRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		CpuInfoRecord you = (CpuInfoRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
