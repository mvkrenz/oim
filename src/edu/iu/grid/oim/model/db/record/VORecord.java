package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import edu.iu.grid.oim.lib.StaticConfig;

public class VORecord extends ConfirmableRecord {
	
	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String community;
	public Integer sc_id;
	public Boolean active;
	public Boolean disable;
	//public String footprints_id;
	
	public Boolean science_vo;
	public String primary_url;
	public String aup_url;
	public String membership_services_url;
	public String purpose_url;
	public String support_url;
	public String app_description;

	//load from existing record
	public VORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VORecord() {
		confirmed = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	public String getName() {
		return name;
	}
}
