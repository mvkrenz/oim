package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class ServiceSelector extends DivRepSelectBox {

	static Logger log = Logger.getLogger(ServiceSelector.class);  
    private Context context;

    public ServiceSelector(DivRep parent, Context _context) {
		super(parent);
		context = _context;
		
		ServiceGroupModel sgmodel = new ServiceGroupModel(context);
		ServiceModel smodel = new ServiceModel(context);
		try {
			ArrayList<ServiceGroupRecord> sgrecs = sgmodel.getAll();
			for(ServiceGroupRecord sgrec : sgrecs) {
				ArrayList<ServiceRecord> srecs = smodel.getByServiceGroupID(sgrec.id);
				LinkedHashMap<Integer, String> services = new LinkedHashMap<Integer, String>();
				for(ServiceRecord rgrec : srecs) {
					services.put(rgrec.id, rgrec.name);
				}
				addGroup("(Service Group) " + sgrec.name, services);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
