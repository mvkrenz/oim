package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;
import edu.iu.grid.oim.view.divrep.form.validator.DomainNameValidator;

public class GridAdminFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(GridAdminFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
    private String in_domain = null;

	private DivRepTextBox domain;
	private ContactEditor contact;
	
	public GridAdminFormDE(UserContext _context, String in_domain, String origin_url) throws SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		this.in_domain = in_domain;
		
		context = _context;
		auth = context.getAuthorization();

		//pull osg_grid_types for unique validator
		domain = new DivRepTextBox(this);
		domain.setLabel("Domain");
		domain.setValue(in_domain);
		domain.setSampleValue("grid.iu.edu");
		domain.addValidator(new DivRepUniqueValidator<String>(getDomains(in_domain)));
		domain.addValidator(new DomainNameValidator());
		domain.setRequired(true);
		
		ContactModel cmodel = new ContactModel(context);
		
		contact = new ContactEditor(this, new ContactModel(context), false, false);
		contact.setLabel("Grid Admin");		
		contact.setMinContacts(Rank.Primary, 0); //allow removal
		contact.setMaxContacts(Rank.Primary, 10);
		contact.setShowRank(false);
		if(in_domain != null) {
			//load currently selected contacts for this domain
			GridAdminModel model = new GridAdminModel(context);
			ArrayList<GridAdminRecord> recs = model.getByDomain(in_domain);
			for(GridAdminRecord rec : recs) {
				ContactRecord crec = cmodel.get(rec.contact_id);
				contact.addSelected(crec, 1);	
			}
		}
	}
	
	private ArrayList<String> getDomains(String ignore) throws SQLException
	{
		//pull all OsgGridTypes
		ArrayList<String> domains = new ArrayList<String>();
		GridAdminModel model = new GridAdminModel(context);
		LinkedHashMap<String, ArrayList<ContactRecord>> list = model.getAll();
		for(String domain : list.keySet()) {
			if(ignore != null && domain.equals(ignore)) continue;
			domains.add(domain);
		}
		return domains;
	}
	
	protected Boolean doSubmit() 
	{	
		/*
		GridAdminRecord rec = new GridAdminRecord();
		rec.domain = domain.getValue();
		ArrayList<ContactRecord> contact_recs = contact.getContactRecordsByRank(1);
		if(contact_recs.size() == 1) {
			ContactRecord crec = contact_recs.get(0);
			rec.contact_id = crec.id;
		}
		*/
		ArrayList<GridAdminRecord> list = new ArrayList();
		ArrayList<ContactRecord> contacts = contact.getContactRecordsByRank(1);
		for(ContactRecord contact : contacts) {
			GridAdminRecord rec = new GridAdminRecord();
			rec.contact_id = contact.id;
			rec.domain = domain.getValue();
			list.add(rec);
		}
		
		//Do insert / update to our DB
		try {
			GridAdminModel model = new GridAdminModel(context);
			/*
			if(rec.id == null) {
				model.insert(rec);
				context.message(MessageType.SUCCESS, "Successfully registered new grid admin.");
			} else {
				model.update(model.get(rec), rec);
				context.message(MessageType.SUCCESS, "Successfully updated a grid admin.");
			}
			*/
			if(in_domain == null) {
				model.insert(list);			
			} else {
				model.update(model.getByDomain(in_domain), list);
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
