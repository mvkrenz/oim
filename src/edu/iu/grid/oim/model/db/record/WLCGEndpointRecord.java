package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WLCGEndpointRecord extends RecordBase {

	@Key public String primary_key;
	public String site_id;
	public String hostname;
	public String host_ip;
	public String service_type;
	public Integer service_id;
	public Boolean in_production; 
	public String roc_name;
	public String contact_email;
	
	//load from existing record
	public WLCGEndpointRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public WLCGEndpointRecord() {}
}
