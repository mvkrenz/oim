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
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class SiteSelector extends DivRepSelectBox {

	static Logger log = Logger.getLogger(SiteSelector.class);  
    private UserContext context;

    public SiteSelector(DivRep parent, UserContext _context) {
		super(parent);
		context = _context;
		
		FacilityModel fmodel = new FacilityModel(context);
		SiteModel smodel = new SiteModel(context);
		try {
			ArrayList<FacilityRecord> frecs = fmodel.getAll();
			Collections.sort(frecs, new Comparator<FacilityRecord> (){
				public int compare(FacilityRecord a, FacilityRecord b) {
					return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
				}
			});
			for(FacilityRecord frec : frecs) {
				ArrayList<SiteRecord> srecs = smodel.getByFacilityID(frec.id);
				LinkedHashMap<Integer, String> sites = new LinkedHashMap<Integer, String>();
				for(SiteRecord srec : srecs) {
					sites.put(srec.id, srec.name);
				}
				addGroup("(Facility) " + frec.name, sites);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
