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
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.GridAdmin;
import edu.iu.grid.oim.view.divrep.form.validator.DomainNameValidator;

public class GridAdminFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(GridAdminFormDE.class); 
    
    private UserContext context;
    private Authorization auth;
    private String in_domain = null;

	private DivRepTextBox domain;
	private GridAdmin gridadmin;
	
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
				
		gridadmin = new GridAdmin(this, context);
		if(in_domain != null) {
			GridAdminModel gamodel = new GridAdminModel(context);
			HashMap<VORecord, ArrayList<GridAdminRecord>> groups = gamodel.getByDomainGroupedByVO(in_domain);
			for(VORecord vo : groups.keySet()) {
				ArrayList<GridAdminRecord> recs = groups.get(vo);
				gridadmin.addGridAdmin(recs);
			}
		} else {
			//add new empty group
			gridadmin.addGridAdmin(null);
		}
	}
	
	private ArrayList<String> getDomains(String ignore) throws SQLException
	{
		//pull all OsgGridTypes
		ArrayList<String> domains = new ArrayList<String>();
		GridAdminModel model = new GridAdminModel(context);
		LinkedHashMap<String, ArrayList<GridAdminRecord>> list = model.getAll();
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
		ArrayList<GridAdminRecord> list = gridadmin.getGridAdminRecords(domain.getValue());
		
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
