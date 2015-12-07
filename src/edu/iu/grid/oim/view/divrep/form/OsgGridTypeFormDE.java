package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;

public class OsgGridTypeFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(OsgGridTypeFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	
	public OsgGridTypeFormDE(UserContext _context, OsgGridTypeRecord rec, String origin_url) throws AuthorizationException, SQLException
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
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(osg_grid_types.values()));
		name.setRequired(true);
		
		description = new DivRepTextArea(this);
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
				context.message(MessageType.SUCCESS, "Successfully registered new OSG Grid Type.");
			} else {
				model.update(model.get(rec), rec);
				context.message(MessageType.SUCCESS, "Successfully updated a OSG Grid Typec.");
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
