package edu.iu.grid.oim.view.divex.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.FormDE;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;

public class MetricFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(MetricFormDE.class); 
    
    protected Connection con = null;
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE common_name;
	private TextFormElementDE abbrev;
	private TextAreaFormElementDE description;
	private TextFormElementDE time_interval;
	private TextFormElementDE fresh_for;
	private TextFormElementDE help_url;
	private TextFormElementDE wlcg_metric_type;
	
	
	public MetricFormDE(DivEx parent, MetricRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;

		//pull metric names for unique validator
		HashMap<Integer, String> metric_names = getMetricNames();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			metric_names.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(metric_names.values()));
		name.setRequired(true);
		
		common_name = new TextFormElementDE(this);
		common_name.setLabel("Common Name");
		common_name.setValue(rec.common_name);
		common_name.setRequired(true);
		
		
		abbrev = new TextFormElementDE(this);
		abbrev.setLabel("Abbreviation");
		abbrev.setValue(rec.abbrev);
		abbrev.setRequired(true);
		
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		
		time_interval = new TextFormElementDE(this);
		time_interval.setLabel("Time Interval");
		time_interval.setValue(rec.time_interval.toString());
		time_interval.setRequired(true);

		fresh_for = new TextFormElementDE(this);
		fresh_for.setLabel("Fresh For");
		fresh_for.setValue(rec.fresh_for.toString());
		fresh_for.setRequired(true);
		
		help_url = new TextFormElementDE(this);
		help_url.setLabel("Help URL");
		help_url.addValidator(UrlValidator.getInstance());
		help_url.setValue(rec.help_url);
		
		wlcg_metric_type = new TextFormElementDE(this);
		wlcg_metric_type.setLabel("WLCG Metric Type");
		wlcg_metric_type.setValue(rec.wlcg_metric_type.toString());
		wlcg_metric_type.setRequired(true);

	}
	
	private HashMap<Integer, String> getMetricNames() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		MetricModel model = new MetricModel(auth);
		for(MetricRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		try {
			auth.check("admin");
			
			MetricRecord rec = new MetricRecord();
			rec.id = id;
			rec.name = name.getValue();
			rec.common_name = common_name.getValue();
			rec.abbrev = abbrev.getValue();
			rec.description = description.getValue();
			rec.time_interval = Integer.parseInt(time_interval.getValue());
			rec.fresh_for = Integer.parseInt(fresh_for.getValue());
			rec.help_url = help_url.getValue();
			rec.wlcg_metric_type = wlcg_metric_type.getValue();
			
			MetricModel model = new MetricModel(auth);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		 } catch (AuthorizationException e) {
			log.error(e);
			return false;
		} catch (SQLException e) {
			log.error(e);
			alert(e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
