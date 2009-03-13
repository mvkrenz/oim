package edu.iu.grid.oim.lib;

//these are the actions that are used by this application.
public enum Action {
	select_resource,
	insert_resource,
	update_resource,
	
	select_facility,
	insert_facility,
	update_facility,
	
	select_vo,
	insert_vo,
	update_vo,
	
	select_sc,
	insert_sc,
	update_sc,
	
	log, //insert entry to log table
	select_certificate_dn,
}
