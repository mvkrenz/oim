package edu.iu.grid.oim.lib;

//these are the actions that are used by this application.
public enum Action {
	//model access for resource table
	read_resource,
	write_resource,
	
	//model access for facility table
	read_facility,
	write_facility,
	
	//model access for vo / vocontact table
	read_vo,
	write_vo,
	admin_vo,

	read_sccontact,
	write_sccontact,
	
	read_vocontact,
	write_vocontact,
	
	//model access for supportcenter table
	read_sc,
	write_sc,
	admin_sc,
	
	read_person,
	write_person
}
