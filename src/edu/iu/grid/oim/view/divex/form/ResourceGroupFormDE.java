package edu.iu.grid.oim.view.divex.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
import com.webif.divex.form.FormDE;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
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
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.OIMHierarchySelector;
import edu.iu.grid.oim.view.divex.ResourceAliasDE;
import edu.iu.grid.oim.view.divex.ResourceServicesDE;

public class ResourceGroupFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(ResourceGroupFormDE.class); 
   
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	private OIMHierarchySelector site_id;
	private SelectFormElementDE osg_grid_type_id;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	
	public ResourceGroupFormDE(DivEx parent, ResourceGroupRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Details</h2>");
		
		//pull vos for unique validator
		HashMap<Integer, String> resource_groups = getResourceGroups();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resource_groups.remove(id);
		}
		
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(resource_groups.values()));
		name.setRequired(true);
		
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		
		site_id = new OIMHierarchySelector(this, OIMHierarchySelector.Type.SITE, auth);
		site_id.setLabel("Site");
		site_id.setValue(rec.site_id);
		site_id.setRequired(true);
		
		OsgGridTypeModel omodel = new OsgGridTypeModel(auth);
		HashMap<Integer, String> gridtype_kv = new HashMap();
		for(OsgGridTypeRecord site_rec : omodel.getAll()) {
			gridtype_kv.put(site_rec.id, site_rec.name);
		}
		osg_grid_type_id = new SelectFormElementDE(this, gridtype_kv);
		osg_grid_type_id.setLabel("OSG Grid Type");
		osg_grid_type_id.setRequired(true);
		if(id != null) {
			osg_grid_type_id.setValue(rec.osg_grid_type_id);
		}
		
		active = new CheckBoxFormElementDE(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElementDE(this);
		disable.setLabel("Disabled");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
	}
	
	private ContactEditorDE createContactEditor(HashMap<Integer, ArrayList<ResourceContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h3>" + StringEscapeUtils.escapeHtml(ctrec.name) + "</h3>");
		ContactModel pmodel = new ContactModel(auth);		
		ContactEditorDE editor = new ContactEditorDE(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
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
	
	protected Boolean doSubmit() {
		
		//Construct VORecord
		ResourceGroupRecord rec = new ResourceGroupRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.site_id = site_id.getValue();
		rec.osg_grid_type_id = osg_grid_type_id.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ResourceGroupModel model = new ResourceGroupModel(auth);
		try {
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			return false;
		}
		return true;
	}
	
	private HashMap<Integer, String> getResourceGroups() throws SQLException
	{
		ResourceGroupModel model = new ResourceGroupModel(auth);
		HashMap<Integer, String> resource_groups = new HashMap();
		for(ResourceGroupRecord rec : model.getAll()) {
			resource_groups.put(rec.id, rec.name);
		}
		return resource_groups;
	}
}
