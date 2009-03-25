package edu.iu.grid.oim.lib;

//these are the actions that are used by this application.
public enum Action 
{
	login,
	
	//in general, write access is for accessing records that the user is a contact of the record
	//in general, admin allows full read/write/delete access to any records within that table
	
	write_resource,
	admin_resource,
	
	write_facility,
	admin_facility,
		
	write_vo,
	admin_vo,
	
	write_sccontact,
	admin_sccontact,
	
	write_vocontact,
	admin_vocontact,

	write_sc,
	admin_sc,
	
	write_person,
	admin_person
}
