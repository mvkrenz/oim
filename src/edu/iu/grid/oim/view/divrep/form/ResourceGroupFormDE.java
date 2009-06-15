package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divrep.DivRep;
import com.webif.divrep.Static;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextAreaFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.UniqueValidator;
import com.webif.divrep.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
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
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.OIMHierarchySelector;
import edu.iu.grid.oim.view.divrep.ResourceAlias;
import edu.iu.grid.oim.view.divrep.ResourceServices;

public class ResourceGroupFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(ResourceGroupFormDE.class); 
    private Context context;
    
	protected Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextAreaFormElement description;
	private OIMHierarchySelector site_id;
	private SelectFormElement osg_grid_type_id;
	private CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	
	public ResourceGroupFormDE(Context _context, ResourceGroupRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		new Static(this, "<h2>Resource Group Information</h2>");
		new Static(this, "<p>A resource group is a logical grouping of CEs, SEs, etc. that make up one unit. Resource groups are referred to as \"sites\" by many people on the OSG. </p>");
		
		//pull vos for unique validator
		HashMap<Integer, String> resource_groups = getResourceGroups();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resource_groups.remove(id);
		}
		
		name = new TextFormElement(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(resource_groups.values()));
		name.setRequired(true);
		
		site_id = new OIMHierarchySelector(this, context, OIMHierarchySelector.Type.SITE);
		site_id.setLabel("Site");
		site_id.setValue(rec.site_id);
		site_id.setRequired(true);
		
		OsgGridTypeModel omodel = new OsgGridTypeModel(context);
		HashMap<Integer, String> gridtype_kv = new HashMap();
		for(OsgGridTypeRecord site_rec : omodel.getAll()) {
			gridtype_kv.put(site_rec.id, site_rec.name);
		}
		osg_grid_type_id = new SelectFormElement(this, gridtype_kv);
		osg_grid_type_id.setLabel("OSG Grid Type");
		osg_grid_type_id.setRequired(true);
		if(id != null) {
			osg_grid_type_id.setValue(rec.osg_grid_type_id);
		}

		description = new TextAreaFormElement(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);

		if(auth.allows("admin")) {
			new Static(this, "<h2>Administrative Tasks</h2>");
		}

		active = new CheckBoxFormElement(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElement(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<ResourceContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new Static(this, "<h3>" + StringEscapeUtils.escapeHtml(ctrec.name) + "</h3>");
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
		Boolean ret = true;
		
		//Construct VORecord
		ResourceGroupRecord rec = new ResourceGroupRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.site_id = site_id.getValue();
		rec.osg_grid_type_id = osg_grid_type_id.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ResourceGroupModel model = new ResourceGroupModel(context);
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
