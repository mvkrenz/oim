package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;

import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;

@Deprecated
public class FieldOfScienceFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(FieldOfScienceFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	
	public FieldOfScienceFormDE(UserContext _context, FieldOfScienceRecord rec, String origin_url) throws AuthorizationException, SQLException
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
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(fields.values()));
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
		FieldOfScienceRecord rec = new FieldOfScienceRecord();
		rec.id = id;
		rec.name = name.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			FieldOfScienceModel model = new FieldOfScienceModel(context);
			if(rec.id == null) {
				model.insert(rec);
				context.message(MessageType.SUCCESS, "Successfully inserted new field of science.");
			} else {
				model.update(model.get(rec), rec);
				context.message(MessageType.SUCCESS, "Successfully updated a field of science.");
			}
			return true;
		 } catch (Exception e) {
			log.error(e);
			alert(e.getMessage());
			return false;
		}
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
