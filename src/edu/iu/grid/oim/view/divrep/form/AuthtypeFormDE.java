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
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;

public class AuthtypeFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(AuthtypeFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	
	public AuthtypeFormDE(UserContext _context, AuthorizationTypeRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//pull auth_types for unique validator
		HashMap<Integer, String> auth_types = getAuthTypes();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			auth_types.remove(id);
		}
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(auth_types.values()));
		name.setRequired(true);
	}
	
	private HashMap<Integer, String> getAuthTypes() throws AuthorizationException, SQLException
	{
		//pull all AuthTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		AuthorizationTypeModel model = new AuthorizationTypeModel(context);
		for(AuthorizationTypeRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{	
		Boolean ret = true;
		AuthorizationTypeRecord rec = new AuthorizationTypeRecord();
		rec.id = id;
		rec.name = name.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			AuthorizationTypeModel model = new AuthorizationTypeModel(context);
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
		//context.close();
		return ret;
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
