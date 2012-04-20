package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class FacilityFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(FacilityFormDE.class); 
   
    private UserContext context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	private DivRepCheckBox active;
	private DivRepCheckBox disable;
	
	public FacilityFormDE(UserContext _context, FacilityRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		//pull facilities for unique validator
		HashMap<Integer, String> sites = getFacilities();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			sites.remove(id);
		}

		new DivRepStaticContent(this, "<h2>Facility Information</h2>");
		name = new DivRepTextBox(this);
		name.setLabel("Facility Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(sites.values()));
		name.setRequired(true);
		
		description = new DivRepTextArea(this);
		description.setLabel("Short Description");
		description.setValue(rec.description);
		description.setRequired(false);
	
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
			new DivRepStaticContent(this, "<p>NOTE: These fields are editable only by GOC administrative staff.</p>");
		}
		active = new DivRepCheckBox(this);
		active.setLabel("Active (Soon to be phased out - modification not allowed)");
		active.setDisabled(true);
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private HashMap<Integer, String> getFacilities() throws AuthorizationException, SQLException
	{
		FacilityModel model = new FacilityModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(FacilityRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		try {
			//Construct SiteRecord
			FacilityRecord rec = new FacilityRecord();
			rec.id = id;
		
			rec.name = name.getValue();
			rec.description = description.getValue();
			rec.disable = disable.getValue();
			if (rec.disable == true) {
				rec.active = false; // active.getValue(); // not using real active value, instead defaulting based on disable value
			} else {
				rec.active = true; 
			}
			
			FacilityModel model = new FacilityModel(context);

			if(rec.id == null) {
				model.insert(rec);
				//create footprint ticket
				Footprints fp = new Footprints(context);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret =  false;
		}
		//context.close();
		return ret;
	}
}