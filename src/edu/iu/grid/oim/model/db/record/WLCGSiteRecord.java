package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WLCGSiteRecord extends RecordBase {

	@Key public String primary_key;
	public String short_name;
	public String official_name;
	public Double longitude;
	public Double latitude;
	public String contact_email;
	public String country;
	public String timezone;
	
	//load from existing record
	public WLCGSiteRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public WLCGSiteRecord() {}
}
