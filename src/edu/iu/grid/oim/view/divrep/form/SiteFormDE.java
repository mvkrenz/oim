package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepLocationSelector;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprint;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;

import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

import java.lang.Double;

public class SiteFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(SiteFormDE.class); 
    
    private Context context;
	protected Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextBox long_name;
	private DivRepTextArea description;
	private DivRepTextBox address_line_1;
	private DivRepTextBox address_line_2;
	private DivRepTextBox city;
	private DivRepTextBox state;
	private DivRepTextBox zipcode;
	private DivRepTextBox country;
	private DivRepLocationSelector latlng;
	private DivRepSelectBox sc_id;
	private DivRepSelectBox facility_id;
	private DivRepCheckBox active;
	private DivRepCheckBox disable;
	
	public SiteFormDE(Context _context, SiteRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		new DivRepStaticContent(this, "<h2>Site Information</h2>");
		new DivRepStaticContent(this, "<p>Add/modify basic information about this site.<br>NOTE: A site represents a department or a sub-organization within a an instituition (like BNL, Fermilab, etc.) or a university, referred to as facility.</p>");

		//pull sites for unique validator
		LinkedHashMap<Integer, String> sites = getSites();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			sites.remove(id);
		}

		facility_id = new DivRepSelectBox(this, getFacilities());
		facility_id.setLabel("Select the facility this site is part of");
		facility_id.setValue(rec.facility_id);
		facility_id.setRequired(true);

		name = new DivRepTextBox(this);
		name.setLabel("Enter this Site's short Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(sites.values()));
		name.setRequired(true);
		
		long_name = new DivRepTextBox(this);
		long_name.setLabel("Enter Longer Name, if applicable");
		long_name.setValue(rec.long_name);
		long_name.setRequired(false);
				
		description = new DivRepTextArea(this);
		description.setLabel("Enter Site Description");
		description.setValue(rec.description);
		description.setRequired(false);

		sc_id = new DivRepSelectBox(this, getActiveNonDisabledSCs());
		sc_id.setLabel("Select Support Center that supports resources and services for this site");
		sc_id.setValue(rec.sc_id);
		sc_id.setRequired(true);

		new DivRepStaticContent(this, "<h2>Geographical Address Information</h2>");
		address_line_1 = new DivRepTextBox(this);
		address_line_1.setLabel("Street Address");
		address_line_1.setValue(rec.address_line_1);
		address_line_1.setRequired(false);

		address_line_2 = new DivRepTextBox(this);
		address_line_2.setLabel("Address Line 2");
		address_line_2.setValue(rec.address_line_2);
		address_line_2.setRequired(false);

		city = new DivRepTextBox(this);
		city.setLabel("City");
		city.setValue(rec.city);
		city.setRequired(true);//on DB, this is non-nullable

		// Need to make this dropdown? probably not.
		state = new DivRepTextBox(this);
		state.setLabel("State");
		state.setValue(rec.state);
		state.setRequired(true);

		zipcode = new DivRepTextBox(this);
		zipcode.setLabel("Zipcode");
		zipcode.setValue(rec.zipcode);
		zipcode.setRequired(true);

		// Need to make this drop down. -agopu
		country = new DivRepTextBox(this);
		country.setLabel("Country");
		country.setValue(rec.country);
		country.setRequired(true);
		
		//latlng = new LatLngSelector(this);
		latlng = new DivRepLocationSelector(this, StaticConfig.getApplicationBase()+"/images/target.png");
		latlng.setLabel("Latitude / Longitude");
		latlng.setValue(latlng.new LatLng(rec.latitude, rec.longitude));
		latlng.setRequired(true);
		
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
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
	
	private LinkedHashMap<Integer, String> getSites() throws AuthorizationException, SQLException
	{
		SiteModel model = new SiteModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(SiteRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private LinkedHashMap<Integer, String> getActiveNonDisabledSCs() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(SCRecord rec : model.getAllActiveNonDisabled()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	private LinkedHashMap<Integer, String> getFacilities() throws AuthorizationException, SQLException
	{
		FacilityModel model = new FacilityModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(FacilityRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
		// Moved try block beginning from line 208 to handled SQL exception.. -agopu
		try {
			//Construct SiteRecord
			SiteRecord rec = new SiteRecord();
			rec.id = id;
		
			rec.name = name.getValue();
			rec.long_name = long_name.getValue();
			rec.description = description.getValue();

			rec.address_line_1 = address_line_1.getValue();
			rec.address_line_2 = address_line_2.getValue();
			rec.city           = city.getValue();
			rec.state          = state.getValue();
			rec.zipcode        = zipcode.getValue();
			rec.country        = country.getValue();
			
			rec.latitude = latlng.getValue().latitude;
			rec.longitude = latlng.getValue().longitude;
			
			rec.sc_id = sc_id.getValue();
			rec.facility_id = facility_id.getValue();
	
			rec.disable = disable.getValue();
			// rec.active = active.getValue();
			if (rec.disable == true) {
				rec.active = false; // not using real active value, instead defaulting based on disable value
			} else {
				rec.active = true; 
			}

			SiteModel model = new SiteModel(context);
			if(rec.id == null) {
				model.insert(rec);
				
				//create footprint ticket
				Footprint fp = new Footprint(context);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}
}
