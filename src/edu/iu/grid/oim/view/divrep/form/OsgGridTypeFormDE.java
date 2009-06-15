package edu.iu.grid.oim.view.divrep.form;

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
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class OsgGridTypeFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(OsgGridTypeFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextAreaFormElement description;
	
	public OsgGridTypeFormDE(Context _context, OsgGridTypeRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//pull osg_grid_types for unique validator
		HashMap<Integer, String> osg_grid_types = getOsgGridTypes();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			osg_grid_types.remove(id);
		}
		name = new TextFormElement(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(osg_grid_types.values()));
		name.setRequired(true);
		
		description = new TextAreaFormElement(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);

	}
	
	private HashMap<Integer, String> getOsgGridTypes() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		OsgGridTypeModel model = new OsgGridTypeModel(context);
		for(OsgGridTypeRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
		//Construct OsgGridTypeRecord
		OsgGridTypeRecord rec = new OsgGridTypeRecord();
		rec.id = id;
		rec.name = name.getValue();
		rec.description = description.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			OsgGridTypeModel model = new OsgGridTypeModel(context);
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
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
