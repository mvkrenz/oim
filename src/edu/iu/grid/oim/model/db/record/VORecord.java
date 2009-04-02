package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VORecord extends RecordBase 
{
	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String primary_url;
	public String aup_url;
	public String membership_services_url;
	public String purpose_url;
	public String support_url;
	public String app_description;
	public String community;
	public Integer sc_id;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public VORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VORecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		VORecord you = (VORecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
