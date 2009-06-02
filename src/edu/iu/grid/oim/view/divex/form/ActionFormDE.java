package edu.iu.grid.oim.view.divex.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divex.Event;
import com.webif.divex.form.FormDEBase;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.record.ActionRecord;

public class ActionFormDE extends FormDEBase 
{
    static Logger log = Logger.getLogger(ActionFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	
	public ActionFormDE(Context _context, ActionRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//pull osg_grid_types for unique validator
		HashMap<Integer, String> cpu_infos = getActions();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			cpu_infos.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(cpu_infos.values()));
		name.setRequired(true);
	
		
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(false);
	}
	
	private HashMap<Integer, String> getActions() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		ActionModel model = new ActionModel(context);
		for(ActionRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{	
		Boolean ret = true;
		ActionRecord rec = new ActionRecord();
		rec.id = id;
		rec.name = name.getValue();
		rec.description = description.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			ActionModel model = new ActionModel(context);
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
