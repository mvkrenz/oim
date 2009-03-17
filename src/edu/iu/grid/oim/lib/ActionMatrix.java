package edu.iu.grid.oim.lib;

import org.apache.log4j.Logger;

//This class defines which auth_type_id allows what actions.
public class ActionMatrix {
    static Logger log = Logger.getLogger(ActionMatrix.class);  
    
	static public Boolean allows(Action action, Integer auth_type_id)
	{
		if(auth_type_id == null) {
			//guest
			switch(action) {
			case select_resource:
			case select_facility:
			case log:
			case select_certificate_dn:
			case select_vo:
				return true;
			}
			return false;
		}
		switch(auth_type_id) {
		case 1: //OSG End User
			switch(action) {
			case select_resource:
			case select_facility:
			case log:
			case select_certificate_dn:
			case select_vo:
					return true;
			}
			break;
		case 3: //OSG Security Staff
			switch(action) {
			case select_resource:
			case select_facility:
			case log:
			case select_certificate_dn:
			case select_vo:
					return true;
			}
			break;
		case 4: //OSG GOC Staff
			switch(action) {
			case select_resource:
			case insert_resource:
			case update_resource:
				
			case select_facility:
			case insert_facility:
			case update_facility:
				
			case admin_vo:
			case select_vo:
			case insert_vo:
			case update_vo:
			case delete_vo:
				
			case select_sc:
			case insert_sc:
			case update_sc:
				
			case log:
			case select_certificate_dn:
					return true;
			}
			break;
		default:
			log.warn("allows() called with unknown auth_type_id : " + auth_type_id);
		}
		return false;
	}
}
