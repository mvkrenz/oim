package edu.iu.grid.oim.view.divex.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.FormDEBase;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class FacilityFormDE extends FormDEBase 
{
    static Logger log = Logger.getLogger(FacilityFormDE.class); 
   
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	
	public FacilityFormDE(Context _context, FacilityRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getDivExRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		//pull facilities for unique validator
		HashMap<Integer, String> sites = getFacilities();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			sites.remove(id);
		}

		new StaticDE(this, "<h2>Facility Information</h2>");
		new StaticDE(this, "<p>Add/modify basic information about this facility.<br>NOTE: A facility represents an instituition (like BNL, Fermilab, etc.) or a university.</p>");
		name = new TextFormElementDE(this);
		name.setLabel("Facility Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(sites.values()));
		name.setRequired(true);
		
		description = new TextAreaFormElementDE(this);
		description.setLabel("Short Description");
		description.setValue(rec.description);
		description.setRequired(false);
	
		if(auth.allows("admin")) {
			new StaticDE(this, "<h2>Administrative Tasks</h2>");
			new StaticDE(this, "<p>NOTE: These fields are editable only by GOC administrative staff.</p>");
		}
		active = new CheckBoxFormElementDE(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElementDE(this);
		disable.setLabel("Disabled");
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
			rec.active = active.getValue();
			rec.disable = disable.getValue();
			
			FacilityModel model = new FacilityModel(context);

			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret =  false;
		}
		context.close();
		return ret;
	}
}