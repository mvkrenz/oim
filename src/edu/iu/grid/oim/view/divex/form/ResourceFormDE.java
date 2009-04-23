package edu.iu.grid.oim.view.divex.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.FormDE;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.DoubleValidator;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.OIMHierarchySelector;
import edu.iu.grid.oim.view.divex.ResourceAliasDE;
import edu.iu.grid.oim.view.divex.ResourceServicesDE;

public class ResourceFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(ResourceFormDE.class); 
   
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	private TextFormElementDE fqdn;
	private TextFormElementDE url;
	
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	private OIMHierarchySelector resource_group_id;
	private ResourceAliasDE aliases;
	//private ResourceDowntimeFormDE downtimes;
	private ResourceServicesDE resource_services;
	
	private CheckBoxFormElementDE wlcg;
	private CheckBoxFormElementDE interop_bdii;
	private CheckBoxFormElementDE interop_monitoring;
	private CheckBoxFormElementDE interop_accounting;
	private TextFormElementDE wlcg_accounting_name;
	private TextFormElementDE ksi2k_minimum;
	private TextFormElementDE ksi2k_maximum;
	private TextFormElementDE storage_capacity_minimum;
	private TextFormElementDE storage_capacity_maximum;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		3, //admin contact
		2, //security contact
		9, //resource report contact
		5 //misc contact
	};
	private HashMap<Integer, ContactEditorDE> contact_editors = new HashMap();
	
	public ResourceFormDE(DivEx parent, ResourceRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Basic Resource Information</h2>");
		new StaticDE(this, "<p>Add/modify basic information about this resource.</p>");
		
		//pull vos for unique validator
		HashMap<Integer, String> resources = getResources();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			resources.remove(id);
		}
		
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(resources.values()));
		name.setRequired(true);
		
		fqdn = new TextFormElementDE(this);
		fqdn.setLabel("Fully Qualified Domain Name (FQDN) of this resource");
		fqdn.setValue(rec.fqdn);
		fqdn.addValidator(new UniqueValidator<String>(resources.values()));
		fqdn.setRequired(true);

		resource_group_id = new OIMHierarchySelector(this, OIMHierarchySelector.Type.RESOURCE_GROUP, auth);
		resource_group_id.setLabel("Select Your Facility (Instituition), Site (Department), and Resource Group");
		resource_group_id.setRequired(true);
		if(id != null) {
			resource_group_id.setValue(rec.resource_group_id);
		}

		description = new TextAreaFormElementDE(this);
		description.setLabel("Short Description");
		description.setValue(rec.description);
		description.setRequired(true);
				
		url = new TextFormElementDE(this);
		url.setLabel("Information URL");
		url.setValue(rec.url);
		url.addValidator(UrlValidator.getInstance());
		url.setRequired(true);
		
		
		new StaticDE(this, "<h3>Resource FQDN Aliases (If Applicable)</h3>");
		new StaticDE(this, "<p>If you used a DNS alias as their main gatekeeper or SE head node FQDN (as defined above), then you can add real host name(s) here as reverse alias(es).</p>");
		aliases = new ResourceAliasDE(this);
		ResourceAliasModel ramodel = new ResourceAliasModel(auth);
		if(id != null) {
			for(ResourceAliasRecord rarec : ramodel.getAllByResourceID(id)) {
				aliases.addAlias(rarec.resource_alias);
			}
		}
		
		new StaticDE(this, "<h2>Resource Service(s)</h2>");
		new StaticDE(this, "<p>Add, remove, modify services associated with your resource. For example, a CE or an SRM.</p>");
		ServiceModel smodel = new ServiceModel(auth);
		resource_services = new ResourceServicesDE(this, smodel.getAll());
		ResourceServiceModel rsmodel = new ResourceServiceModel(auth);
		if(id != null) {
			for(ResourceServiceRecord rarec : rsmodel.getAllByResourceID(id)) {
				resource_services.addService(rarec);
			}
		}
		/*
		new StaticDE(this, "<h2>Future Downtime Schedule</h2>");
		downtimes = new ResourceDowntimeFormDE(this, auth, id, resource_services);
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);
		if(id != null) {
			for(ResourceDowntimeRecord drec : dmodel.getFutureDowntimesByResourceID(rec.id)) {
				downtimes.addDowntime(drec);
			}
		}
		*/
		
		new StaticDE(this, "<h2>Contact Information</h2>");
		new StaticDE(this, "<p>Add, remove, modify various types of contacts associated with your resource. These contacts have the authorization to modify this resource. Each contact entry field shows you a list of contacts as you type a name.</p>");
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
		
		new StaticDE(this, "<h2>WLCG Interoperability Information (If Applicable)</h2>");
		new StaticDE(this, "<p>Enable this section if your resource is part of the WLCG interoperability agreement. " + 
					"You can then provide more interoperability details for this resource, including KSI2K Limits " + 
					" and storage capacity min/max values. If you are not sure about any of these values, " + 
					" ask your Owner VO(s)!</p>");

		wlcg = new CheckBoxFormElementDE(this);
		wlcg.setLabel("This is a WLCG resource");
		wlcg.addEventListener(new EventListener() {
			public void handleEvent(Event e) {	
				if(((String)e.value).compareTo("true") == 0) {
					hideWLCGElements(false);
				} else {
					hideWLCGElements(true);
				}
			}
		});
		
		//indent the whole WCLG things
		new StaticDE(this, "<div class=\"indent\">");
		{
			interop_bdii = new CheckBoxFormElementDE(this);
			interop_bdii.setLabel("Should this resource part of WLCG Interop BDII?");
	
			interop_monitoring = new CheckBoxFormElementDE(this);
			interop_monitoring.setLabel("Should this resource part of WLCG Interop Monitoring?");
	
			interop_accounting = new CheckBoxFormElementDE(this);
			interop_accounting.setLabel("Should this resource part of WLCG Interop Accounting?");

			interop_accounting.addEventListener(new EventListener() {
				public void handleEvent(Event e) {	
					if(((String)e.value).compareTo("true") == 0) {
						hideWLCGAccountingName(false);
					} else {
						hideWLCGAccountingName(true);
					}
				}
			});

			wlcg_accounting_name = new TextFormElementDE(this);
			wlcg_accounting_name.setLabel("WLCG Accounting Name");

			ksi2k_minimum = new TextFormElementDE(this);
			ksi2k_minimum.setLabel("KSI2K Minimum");
			ksi2k_minimum.addValidator(DoubleValidator.getInstance());
			
			ksi2k_maximum = new TextFormElementDE(this);
			ksi2k_maximum.setLabel("KSI2K Maximum");
			ksi2k_maximum.addValidator(DoubleValidator.getInstance());
			
			storage_capacity_minimum = new TextFormElementDE(this);
			storage_capacity_minimum.setLabel("Storage Capacity Minimum (in TeraBytes)");
			storage_capacity_minimum.addValidator(DoubleValidator.getInstance());
			
			storage_capacity_maximum = new TextFormElementDE(this);
			storage_capacity_maximum.setLabel("Storage Capacity Maximum (in TeraBytes)");
			storage_capacity_maximum.addValidator(DoubleValidator.getInstance());
		}
		new StaticDE(this, "</div>");
		hideWLCGElements(true);
		ResourceWLCGModel wmodel = new ResourceWLCGModel(auth);
		if(id != null) {
			ResourceWLCGRecord wrec = wmodel.get(rec.id);
			if(wrec != null) {
				//if WLCG record exist, populate the values
				wlcg.setValue(true);
				interop_bdii.setValue(wrec.interop_bdii);
				interop_monitoring.setValue(wrec.interop_monitoring);
				interop_accounting.setValue(wrec.interop_accounting);
				wlcg_accounting_name.setValue(wrec.accounting_name);
				if(wrec.ksi2k_minimum != null) {
					ksi2k_minimum.setValue(wrec.ksi2k_minimum.toString());
				}
				if(wrec.ksi2k_maximum != null) {
					ksi2k_maximum.setValue(wrec.ksi2k_maximum.toString());
				}
				if(wrec.storage_capacity_minimum != null) {
					storage_capacity_minimum.setValue(wrec.storage_capacity_minimum.toString());
				}
				if(wrec.storage_capacity_maximum != null) {
					storage_capacity_maximum.setValue(wrec.storage_capacity_maximum.toString());
				}
				hideWLCGElements(false);
			}
		}

		new StaticDE(this, "<h2>Administrative Tasks</h2>");
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
	
	private void hideWLCGElements(Boolean b)
	{
		interop_bdii.setHidden(b);
		interop_bdii.redraw();
		interop_bdii.setRequired(!b);
		
		interop_monitoring.setHidden(b);
		interop_monitoring.redraw();
		interop_monitoring.setRequired(!b);
		
		interop_accounting.setHidden(b);
		interop_accounting.redraw();
		interop_accounting.setRequired(!b);
		
		wlcg_accounting_name.setHidden(b);
		wlcg_accounting_name.redraw();
		wlcg_accounting_name.setRequired(!b);
		
		ksi2k_minimum.setHidden(b);
		ksi2k_minimum.redraw();
		ksi2k_minimum.setRequired(!b);
		
		ksi2k_maximum.setHidden(b);
		ksi2k_maximum.redraw();
		ksi2k_maximum.setRequired(!b);
		
		storage_capacity_minimum.setHidden(b);
		storage_capacity_minimum.redraw();
		storage_capacity_minimum.setRequired(!b);
		
		storage_capacity_maximum.setHidden(b);
		storage_capacity_maximum.redraw();
		storage_capacity_maximum.setRequired(!b);
	}

	private void hideWLCGAccountingName(Boolean b)
	{
		wlcg_accounting_name.setHidden(b);
		wlcg_accounting_name.redraw();
		wlcg_accounting_name.setRequired(!b);
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
		ResourceRecord rec = new ResourceRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.fqdn = fqdn.getValue();
		rec.url = url.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		rec.resource_group_id = resource_group_id.getValue();
		
		//If WLCG is on, then create wlcg record
		ResourceWLCGRecord wrec = null;
		if(wlcg.getValue()) {
			wrec = new ResourceWLCGRecord();
			wrec.interop_bdii = interop_bdii.getValue();
			wrec.interop_monitoring = interop_monitoring.getValue();
			wrec.interop_accounting = interop_accounting.getValue();
			wrec.accounting_name = wlcg_accounting_name.getValue();
			wrec.ksi2k_minimum = ksi2k_minimum.getValueAsDouble();
			wrec.ksi2k_maximum = ksi2k_maximum.getValueAsDouble();
			wrec.storage_capacity_maximum = storage_capacity_maximum.getValueAsDouble();
			wrec.storage_capacity_minimum = storage_capacity_minimum.getValueAsDouble();
		}
		
		ResourceModel model = new ResourceModel(auth);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, 
						aliases.getAliases(), 
						getContactRecordsFromEditor(), 
						wrec,
						resource_services.getResourceServiceRecords()/*,
						downtimes.getDowntimeEditors(),
						downtimes.getAffectedServiceRecords()*/);
			} else {
				model.updateDetail(rec, 
						aliases.getAliases(), 
						getContactRecordsFromEditor(),
						wrec,
						resource_services.getResourceServiceRecords()/*,
						downtimes.getDowntimeEditors(),
						downtimes.getAffectedServiceRecords()*/);
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
