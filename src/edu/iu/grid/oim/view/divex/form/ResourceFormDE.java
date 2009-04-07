package edu.iu.grid.oim.view.divex.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
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
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class ResourceFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(ResourceFormDE.class); 
   
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	private TextFormElementDE fqdn;
	private TextFormElementDE url;
	private CheckBoxFormElementDE interop_bdii;
	private CheckBoxFormElementDE interop_monitoring;
	private CheckBoxFormElementDE interop_accounting;
	private TextFormElementDE wlcg_accounting_name;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	private SelectFormElementDE resource_group_id;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		2, //security contact
		3, //admin contact
		4, //operational contact
		5, //misc contact
		7, //notification contat
		9 //resource report contact
	};
	private HashMap<Integer, ContactEditorDE> contact_editors = new HashMap();
	
	public ResourceFormDE(DivEx parent, ResourceRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Details</h2>");
		
		//pull vos for unique validator
		HashMap<Integer, String> resources = getResources();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resources.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.setValidator(new UniqueValidator<String>(resources.values()));
		name.setRequired(true);
				
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		
		fqdn = new TextFormElementDE(this);
		fqdn.setLabel("FQDN");
		fqdn.setValue(rec.fqdn);
		fqdn.setValidator(new UniqueValidator<String>(resources.values()));
		fqdn.setRequired(true);

		url = new TextFormElementDE(this);
		url.setLabel("URL");
		url.setValue(rec.url);
		url.setValidator(UrlValidator.getInstance());
		url.setRequired(true);
		
		interop_bdii = new CheckBoxFormElementDE(this);
		interop_bdii.setLabel("Interop BDII");
		interop_bdii.setValue(rec.interop_bdii);
		
		interop_monitoring = new CheckBoxFormElementDE(this);
		interop_monitoring.setLabel("Interop Monitoring");
		interop_monitoring.setValue(rec.interop_monitoring);

		interop_accounting = new CheckBoxFormElementDE(this);
		interop_accounting.setLabel("Interop Accounting");
		interop_accounting.setValue(rec.interop_accounting);
		
		wlcg_accounting_name = new TextFormElementDE(this);
		wlcg_accounting_name.setLabel("WLCG Accounting Name");
		wlcg_accounting_name.setValue(rec.wlcg_accounting_name);
		
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
		
		ResourceGroupModel model = new ResourceGroupModel(auth);
		HashMap<Integer, String> resource_groups_kv = new HashMap();
		for(ResourceGroupRecord grec : model.getAll()) {
			resource_groups_kv.put(grec.id, grec.name);
		}
		resource_group_id = new SelectFormElementDE(this, resource_groups_kv);
		resource_group_id.setLabel("Resource Group");
		if(id != null) {
			ResourceGroupRecord resource_group_rec = model.get(rec.resource_group_id);
			if(resource_group_rec != null) {
				resource_group_id.setValue(rec.resource_group_id);
			}
		}
		
		new StaticDE(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<ResourceContactRecord>> voclist_grouped = null;
		if(id != null) {
			ResourceContactModel vocmodel = new ResourceContactModel(auth);
			ArrayList<ResourceContactRecord> voclist = vocmodel.getByResourceID(id);
			voclist_grouped = vocmodel.groupByContactTypeID(voclist);
		} else {
			//set user's contact as submitter
			voclist_grouped = new HashMap<Integer, ArrayList<ResourceContactRecord>>();
			ArrayList<ResourceContactRecord> list = new ArrayList<ResourceContactRecord>();
			ResourceContactRecord submitter = new ResourceContactRecord();
			submitter.contact_id = auth.getContactID();
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			list.add(submitter);
			voclist_grouped.put(1/*submitter*/, list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(auth);
		for(int contact_type_id : contact_types) {
			ContactEditorDE editor = createContactEditor(voclist_grouped, ctmodel.get(contact_type_id));
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type_id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			contact_editors.put(contact_type_id, editor);
		}
	}
	
	private ContactEditorDE createContactEditor(HashMap<Integer, ArrayList<ResourceContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h3>" + ctrec.name + "</h3>");
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
		ResourceRecord rec = new ResourceRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.fqdn = fqdn.getValue();
		rec.url = url.getValue();
		rec.interop_bdii = interop_bdii.getValue();
		rec.interop_monitoring = interop_monitoring.getValue();
		rec.interop_accounting = interop_accounting.getValue();
		rec.wlcg_accounting_name = wlcg_accounting_name.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		rec.resource_group_id = resource_group_id.getValue();
		
		ArrayList<ResourceContactRecord> contacts = getContactRecordsFromEditor();
		ResourceModel model = new ResourceModel(auth);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, contacts);
			} else {
				model.updateDetail(rec, contacts);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			return false;
		}
		return true;
	}
	
	//retrieve contact records from the contact editor.
	//be aware that VOContactRecord's vo_id is not populated.. you need to fill it out with
	//appropriate vo_id later
	private ArrayList<ResourceContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<ResourceContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditorDE editor = contact_editors.get(type_id);
			HashMap<ContactRecord, Integer> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				ResourceContactRecord rec = new ResourceContactRecord();
				Integer rank_id = contacts.get(contact);
				rec.contact_id = contact.id;
				rec.contact_type_id = type_id;
				rec.contact_rank_id = rank_id;
				list.add(rec);
			}
		}
		
		return list;
	}
	
	private HashMap<Integer, String> getResources() throws SQLException
	{
		ResourceModel model = new ResourceModel(auth);
		HashMap<Integer, String> resources = new HashMap();
		for(ResourceRecord rec : model.getAll()) {
			resources.put(rec.id, rec.name);
		}
		return resources;
	}
}
