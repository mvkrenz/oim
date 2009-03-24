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
			case read_resource:
			case read_facility:
			case read_vo:
				
			case read_person:
			case read_sccontact:
			case read_vocontact:
				return true;
			}
			return false;
		}
		switch(auth_type_id) {
		case 1: //OSG End User
			switch(action) {
			case read_resource:
			case read_facility:
			case read_vo:
				
			case read_person:
			case read_sccontact:
			case read_vocontact:
					return true;
			}
			break;
		case 3: //OSG Security Staff
			switch(action) {
			case read_resource:
			case read_facility:
			case read_vo:
				
			case read_person:
			case read_sccontact:
			case read_vocontact:
					return true;
			}
			break;
		case 4: //OSG GOC Staff
			switch(action) {
			case read_resource:
			case write_resource:
				
			case read_facility:
			case write_facility:
				
			case read_vo:
			case write_vo:
			case admin_vo:
				
			case read_sc:
			case write_sc:
			case admin_sc:
				
			case read_person:
			case write_person:
				
			case read_vocontact:
			case write_vocontact:
				
			case read_sccontact:
			case write_sccontact:
					return true;
			}
			break;
		default:
			log.warn("allows() called with unknown auth_type_id : " + auth_type_id);
		}
		return false;
	}
}
