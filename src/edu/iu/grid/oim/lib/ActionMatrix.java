package edu.iu.grid.oim.lib;

import org.apache.log4j.Logger;

//This class defines which auth_type_id allows what actions.
public class ActionMatrix {
    static Logger log = Logger.getLogger(ActionMatrix.class);  
    
	static public Boolean allows(Action action, int auth_type_id)
	{
		switch(auth_type_id) {
		case 0: //guest
			switch(action) {
			case select_resource:
			case select_facility:
				return true;
			}
		case 1: //OSG End User
			switch(action) {
			case select_resource:
			case select_facility:
					return true;
			}
		case 3: //OSG Security Staff
			switch(action) {
			case select_resource:
			case select_facility:
					return true;
			}
		case 4: //OSG GOC Staff
			switch(action) {
			case select_resource:
			case insert_resource:
			case update_resource:
			case select_facility:
			case insert_facility:
			case update_facility:
					return true;
			}
		default:
			log.warn("allows() called with unknown auth_type_id");
		}
		return false;
	}
}
