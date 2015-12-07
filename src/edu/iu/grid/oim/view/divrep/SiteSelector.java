package edu.iu.grid.oim.view.divrep;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.common.DivRepSelectBox;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
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
