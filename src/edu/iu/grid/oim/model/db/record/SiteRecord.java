package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SiteRecord extends RecordBase 
{
	public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public String longitude, latitude; // Need to change this to float but it is text in the DB right now
	public Integer sc_id; 
	public Integer facility_id; 
	public Integer submitter_dn_id; 
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public SiteRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SiteRecord()	throws SQLException {}
}
