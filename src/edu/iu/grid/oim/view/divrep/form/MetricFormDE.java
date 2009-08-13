package edu.iu.grid.oim.view.divrep.form;

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
import com.divrep.validator.DivRepIntegerValidator;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
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
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class MetricFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(MetricFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextBox common_name;
	private DivRepTextBox abbrev;
	private DivRepTextArea description;
	private DivRepTextBox time_interval;
	private DivRepTextBox fresh_for;
	private DivRepTextBox help_url;
	private DivRepTextBox wlcg_metric_type;
	
	
	public MetricFormDE(Context _context, MetricRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//pull metric names for unique validator
		HashMap<Integer, String> metric_names = getMetricNames();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			metric_names.remove(id);
		}
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		if(id != null) {
		}
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(metric_names.values()));
		name.setRequired(true);
		
		common_name = new DivRepTextBox(this);
		common_name.setLabel("Common Name");
		if(id != null) {
			common_name.setValue(rec.common_name);
		}
		common_name.setRequired(true);
		
		abbrev = new DivRepTextBox(this);
		abbrev.setLabel("Abbreviation");
		if(id != null) {
			abbrev.setValue(rec.abbrev);
		}
		abbrev.setValue(rec.abbrev);
		abbrev.setRequired(true);
		
		description = new DivRepTextArea(this);
		description.setLabel("Description");
		if(id != null) {
			description.setValue(rec.description);
		}
		description.setRequired(true);
		
		time_interval = new DivRepTextBox(this);
		time_interval.setLabel("Time Interval");
		if(id != null) {
			time_interval.setValue(rec.time_interval.toString());
		}
		time_interval.setRequired(true);
		time_interval.addValidator(new DivRepIntegerValidator());

		fresh_for = new DivRepTextBox(this);
		fresh_for.setLabel("Fresh For");
		if(id != null) {
			fresh_for.setValue(rec.fresh_for.toString());
		}
		fresh_for.setRequired(true);
		fresh_for.addValidator(new DivRepIntegerValidator());
		
		help_url = new DivRepTextBox(this);
		help_url.setLabel("Help URL");
		help_url.addValidator(DivRepUrlValidator.getInstance());
		if(id != null) {
			help_url.setValue(rec.help_url);
		}
		
		wlcg_metric_type = new DivRepTextBox(this);
		wlcg_metric_type.setLabel("WLCG Metric Type");
		if(id != null) {
			wlcg_metric_type.setValue(rec.wlcg_metric_type.toString());
		}
		wlcg_metric_type.setRequired(true);
	}
	
	private HashMap<Integer, String> getMetricNames() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		MetricModel model = new MetricModel(context);
		for(MetricRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		Boolean ret = true;
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
			
			MetricModel model = new MetricModel(context);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		 } catch (Exception e) {
			log.error(e);
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
