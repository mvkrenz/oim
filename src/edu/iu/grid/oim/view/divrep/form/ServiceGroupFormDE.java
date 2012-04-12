package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.ResourceAlias;
import edu.iu.grid.oim.view.divrep.ResourceServices;

public class ServiceGroupFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ServiceGroupFormDE.class); 
    private Context context;
   
	protected Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	
	public ServiceGroupFormDE(Context _context, ServiceGroupRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		//new DivRepStaticContent(this, "<h2>Details</h2>");
		
		//pull vos for unique validator
		HashMap<Integer, String> resource_groups = getResourceGroups();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resource_groups.remove(id);
		}
		
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(resource_groups.values()));
		name.setRequired(true);
		
		description = new DivRepTextArea(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
		//Construct VORecord
		ServiceGroupRecord rec = new ServiceGroupRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		
		ServiceGroupModel model = new ServiceGroupModel(context);
		try {
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}
	
	private HashMap<Integer, String> getResourceGroups() throws SQLException
	{
		ResourceGroupModel model = new ResourceGroupModel(context);
		HashMap<Integer, String> resource_groups = new HashMap();
		for(ResourceGroupRecord rec : model.getAll()) {
			resource_groups.put(rec.id, rec.name);
		}
		return resource_groups;
	}
}
