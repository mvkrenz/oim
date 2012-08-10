package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;
import edu.iu.grid.oim.view.divrep.form.validator.DomainNameValidator;

public class GridAdminFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(GridAdminFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox domain;
	private ContactEditor contact;
	
	public GridAdminFormDE(UserContext _context, GridAdminRecord rec, String origin_url) throws SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		//pull osg_grid_types for unique validator
		HashMap<Integer, String> domains = getDomains();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			domains.remove(id);
		}
		domain = new DivRepTextBox(this);
		domain.setLabel("Domain");
		domain.setValue(rec.domain);
		domain.setSampleValue("grid.iu.edu");
		domain.addValidator(new DivRepUniqueValidator<String>(domains.values()));
		domain.addValidator(new DomainNameValidator());
		domain.setRequired(true);
		
		ContactModel cmodel = new ContactModel(context);
		
		contact = new ContactEditor(this, new ContactModel(context), false, false);
		contact.setLabel("Grid Admin");		
		contact.setMinContacts(Rank.Primary, 1);
		contact.setShowRank(false);
		if(rec.contact_id != null) {
			ContactRecord crec = cmodel.get(rec.contact_id);
			contact.addSelected(crec, 1);	
		}
	}
	
	private HashMap<Integer, String> getDomains() throws SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		GridAdminModel model = new GridAdminModel(context);
		for(GridAdminRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.domain);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{	
		GridAdminRecord rec = new GridAdminRecord();
		rec.id = id;
		rec.domain = domain.getValue();
		ArrayList<ContactRecord> contact_recs = contact.getContactRecordsByRank(1);
		if(contact_recs.size() == 1) {
			ContactRecord crec = contact_recs.get(0);
			rec.contact_id = crec.id;
		}

		//Do insert / update to our DB
		try {
			GridAdminModel model = new GridAdminModel(context);
			if(rec.id == null) {
				model.insert(rec);
				context.message(MessageType.SUCCESS, "Successfully registered new grid admin.");
			} else {
				model.update(model.get(rec), rec);
				context.message(MessageType.SUCCESS, "Successfully updated a grid admin.");
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
