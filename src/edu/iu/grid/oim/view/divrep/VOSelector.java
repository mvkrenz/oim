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
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOSelector extends DivRepSelectBox {
    static Logger log = Logger.getLogger(VOSelector.class);  

	public VOSelector(DivRep parent, UserContext context) {
		super(parent);
		VOModel vo_model = new VOModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				if(vo_rec.disable) continue;
				keyvalues.put(vo_rec.id, vo_rec.name);
			}	
			setValues(keyvalues);	
			setLabel("Virtual Organization");
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
	}

}
