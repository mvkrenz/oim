package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class FacilityRecord extends RecordBase {

	public Integer id;
	public String name;
	public String description;
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public FacilityRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public FacilityRecord()	throws SQLException {}
}
