package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.Static;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextAreaFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.UniqueValidator;
import com.webif.divrep.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class FacilityFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(FacilityFormDE.class); 
   
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextAreaFormElement description;
	private CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	
	public FacilityFormDE(Context _context, FacilityRecord rec, String origin_url) throws AuthorizationException, SQLException
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

		new Static(this, "<h2>Facility Information</h2>");
		new Static(this, "<p>Add/modify basic information about this facility.<br>NOTE: A facility represents an instituition (like BNL, Fermilab, etc.) or a university.</p>");
		name = new TextFormElement(this);
		name.setLabel("Facility Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(sites.values()));
		name.setRequired(true);
		
		description = new TextAreaFormElement(this);
		description.setLabel("Short Description");
		description.setValue(rec.description);
		description.setRequired(false);
	
		if(auth.allows("admin")) {
			new Static(this, "<h2>Administrative Tasks</h2>");
			new Static(this, "<p>NOTE: These fields are editable only by GOC administrative staff.</p>");
		}
		active = new CheckBoxFormElement(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElement(this);
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