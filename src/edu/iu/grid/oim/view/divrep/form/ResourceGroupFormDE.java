package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.SiteSelector;

public class ResourceGroupFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ResourceGroupFormDE.class); 
    private UserContext context;
    
	protected Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	private SiteSelector site_id;
	private DivRepSelectBox osg_grid_type_id;
	
	private DivRepTextBox normalization_factor;
	private ArrayList<DivRepCheckBox> apel_resources;
	
	private DivRepCheckBox disable;
	
	public ResourceGroupFormDE(UserContext _context, ResourceGroupRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		//new DivRepStaticContent(this, "<h2>Resource Group Information</h2>");
		new DivRepStaticContent(this, "<p class=\"help-block\">A resource group is a logical grouping of CEs, SEs, etc. that make up one unit. Resource groups are referred to as \"sites\" by many people on the OSG. </p>");
		
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
		
		site_id = new SiteSelector(this, context);
		site_id.setLabel("Site");
		site_id.setValue(rec.site_id);
		site_id.setRequired(true);
		
		OsgGridTypeModel omodel = new OsgGridTypeModel(context);
		LinkedHashMap<Integer, String> gridtype_kv = new LinkedHashMap();
		for(OsgGridTypeRecord site_rec : omodel.getAll()) {
			gridtype_kv.put(site_rec.id, site_rec.name);
		}
		osg_grid_type_id = new DivRepSelectBox(this, gridtype_kv);
		osg_grid_type_id.setLabel("OSG Grid Type");
		osg_grid_type_id.setRequired(true);
		if(rec.osg_grid_type_id != null) {
			osg_grid_type_id.setValue(rec.osg_grid_type_id);
		}

		description = new DivRepTextArea(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}
		
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<ResourceContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new DivRepStaticContent(this, "<h3>" + StringEscapeUtils.escapeHtml(ctrec.name) + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(voclist != null) {
			ArrayList<ResourceContactRecord> clist = voclist.get(ctrec.id);
			if(clist != null) {
				for(ResourceContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	protected Boolean doSubmit() 
	{
		//Construct VORecord
		ResourceGroupRecord rec = new ResourceGroupRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.site_id = site_id.getValue();
		rec.osg_grid_type_id = osg_grid_type_id.getValue();
		//rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ResourceGroupModel model = new ResourceGroupModel(context);
		try {
			if(rec.id == null) {
				model.insert(rec);
				context.message(MessageType.SUCCESS, "Successfully registered new resource group.");
			} else {
				model.update(model.get(rec), rec);
				context.message(MessageType.SUCCESS, "Successfully updated a resource group.");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
			return false;
		}
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
