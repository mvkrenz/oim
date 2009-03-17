package edu.iu.grid.oim.lib;

//these are the actions that are used by this application.
public enum Action {
	//model access for resource table
	select_resource,
	insert_resource,
	update_resource,
	
	//model access for facility table
	select_facility,
	insert_facility,
	update_facility,
	
	//model access for vo / vocontact table
	select_vo,
	insert_vo,
	update_vo,
	delete_vo,
	//allows full access to all VO records
	admin_vo, 
	
	
	//model access for supportcenter table
	select_sc,
	insert_sc,
	update_sc,
	
	log, //insert entry to log table
	select_certificate_dn,
}
