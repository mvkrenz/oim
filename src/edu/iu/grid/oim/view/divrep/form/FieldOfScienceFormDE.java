package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divrep.Event;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.UniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;

import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;

public class FieldOfScienceFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(FieldOfScienceFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	
	public FieldOfScienceFormDE(Context _context, FieldOfScienceRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//fields of science for unique validator
		HashMap<Integer, String> fields = getFieldsOfScience();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			fields.remove(id);
		}
		name = new TextFormElement(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(fields.values()));
		name.setRequired(true);
	}
	
	private HashMap<Integer, String> getFieldsOfScience() throws AuthorizationException, SQLException
	{
		//pull all FieldsofScience
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		FieldOfScienceModel model = new FieldOfScienceModel(context);
		for(FieldOfScienceRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{	
		Boolean ret = true;
		FieldOfScienceRecord rec = new FieldOfScienceRecord();
		rec.id = id;
		rec.name = name.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			FieldOfScienceModel model = new FieldOfScienceModel(context);
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
