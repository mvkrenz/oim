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
import com.webif.divex.EventListener;
import com.webif.divex.form.FormDEBase;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.VOReport;

import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.VOReportNameModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOVOModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.ResourceServicesDE;
import edu.iu.grid.oim.view.divex.ResourceWLCGDE;
import edu.iu.grid.oim.view.divex.VOReportNamesDE;
import edu.iu.grid.oim.view.divex.VOReportNameFqanDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class VOFormDE extends FormDEBase 
{
    static Logger log = Logger.getLogger(VOFormDE.class); 
   
    private Context context;
	private Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE long_name;
	private TextAreaFormElementDE description;
	private TextFormElementDE primary_url;
	private TextFormElementDE aup_url;
	private TextFormElementDE membership_services_url;
	private TextFormElementDE purpose_url;
	private TextFormElementDE support_url;
	private TextAreaFormElementDE app_description;
	private TextAreaFormElementDE community;
	private TextFormElementDE footprints_id;
	private SelectFormElementDE sc_id;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	private HashMap<Integer, CheckBoxFormElementDE> field_of_science;
	private CheckBoxFormElementDE child_vo;
	private SelectFormElementDE parent_vo;
	
	private VOReport vorep_consolidator;
	private VOReportNamesDE vo_report_name_div;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		6, //vo manager
		3, //admin contact       -- Formerly operations contact for VOs
		2, //security contact
		5, //misc contact
	};
	private HashMap<Integer, ContactEditorDE> contact_editors = new HashMap();
	
	public VOFormDE(Context _context, VORecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getDivExRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		new StaticDE(this, "<h2>Basic VO Information</h2>");
		new StaticDE(this, "<p>Add/modify basic information about this VO.</p>");

		//pull vos for unique validator
		HashMap<Integer, String> vos = getVONames();
		if(id != null) { //if doing update, remove my own name (I can use my own name)
			vos.remove(id);
		}

		// Name is not an editable field except for GOC staff
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(vos.values()));
		name.setRequired(true);
		name.setSampleValue("CDF");
		if (auth.allows("admin")) {
			name.setDisabled(true);
		}

		long_name = new TextFormElementDE(this);
		long_name.setLabel("Enter the Long Name for this VO");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true); // TODO: agopu should this be required?
		long_name.setSampleValue("Collider Detector at Fermilab");

		sc_id = new SelectFormElementDE(this, getSCNames());
		sc_id.setLabel("Select a Support Center that will support this VO");
		sc_id.setValue(rec.sc_id);
		sc_id.setRequired(true);

		new StaticDE(this, "<h3>Extended Descriptions</h3>");
		description = new TextAreaFormElementDE(this);
		description.setLabel("Enter a Description for this VO");
		description.setValue(rec.description);
		description.setRequired(true);

		app_description = new TextAreaFormElementDE(this);
		app_description.setLabel("Enter an Application Description");
		app_description.setValue(rec.app_description);
		app_description.setRequired(true);

		community = new TextAreaFormElementDE(this);
		community.setLabel("Describe the Community this VO serves");
		community.setValue(rec.community);
		community.setRequired(true);

		new StaticDE(this, "<h2>Select Field Of Science(s) applicable to this VO</h2>");
		ArrayList<Integer/*field_of_science_id*/> fslist = new ArrayList();
		if(id != null) {
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(context);
			for(VOFieldOfScienceRecord fsrec : vofsmodel.getByVOID(id)) {
				fslist.add(fsrec.field_of_science_id);
			}
		}
		FieldOfScienceModel fsmodel = new FieldOfScienceModel(context);
		field_of_science = new HashMap();
		for(FieldOfScienceRecord fsrec : fsmodel.getAll()) {
			CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this);
			field_of_science.put(fsrec.id, elem);
			elem.setLabel(fsrec.name);
			if(fslist != null) {
				if(fslist.contains(fsrec.id)) {
					elem.setValue(true);	
				}
			}
		}

		new StaticDE(this, "<h2>Sub-VO Mapping.</h2>");
		child_vo = new CheckBoxFormElementDE(this);
		child_vo.setLabel("Is this a sub-VO of an existing VO? For example, FermilabMinos is a sub VO of the Fermilab VO.");

		//indent the parent VO stuff
		new StaticDE(this, "<div class=\"indent\">");
		parent_vo = new SelectFormElementDE(this, vos);
		parent_vo.setLabel("Select a Parent VO");
		hideParentVOSelector(true);

		child_vo.addEventListener(new EventListener() {
			public void handleEvent(Event e) {	
				if(((String)e.value).compareTo("true") == 0) {
					hideParentVOSelector(false);
				} else {
					hideParentVOSelector(true);
				}
			}
		});

		if(id != null) {
			VOModel model = new VOModel(context);
			VORecord parent_vo_rec = model.getParentVO(id);
			if(parent_vo_rec != null) {
				parent_vo.setValue(parent_vo_rec.id);
				child_vo.setValue(true);
				hideParentVOSelector(false);				
			}
			// AG: Need to clean this up; especially for VOs that are not child VOs of a parent
			// .. perhaps a yes/no first?
			}
			parent_vo.addEventListener(new EventListener () {
				public void handleEvent(Event e) {
					handleParentVOSelection(Integer.parseInt((String)e.value));
				}
			});
		new StaticDE(this, "</div>");

		new StaticDE(this, "<h2>Relevant URLs</h2>");
		primary_url = new TextFormElementDE(this);
		primary_url.setLabel("Primary URL");
		primary_url.setValue(rec.primary_url);
		primary_url.addValidator(UrlValidator.getInstance());
		primary_url.setRequired(true);

		aup_url = new TextFormElementDE(this);
		aup_url.setLabel("AUP URL");
		aup_url.setValue(rec.aup_url);
		aup_url.addValidator(UrlValidator.getInstance());
		aup_url.setRequired(true);

		membership_services_url = new TextFormElementDE(this);
		membership_services_url.setLabel("Membership Services (VOMS) URL");
		membership_services_url.setValue(rec.membership_services_url);
		membership_services_url.addValidator(UrlValidator.getInstance());
		membership_services_url.setRequired(true);

		purpose_url = new TextFormElementDE(this);
		purpose_url.setLabel("Purpose URL"); 
		purpose_url.setValue(rec.purpose_url);
		purpose_url.addValidator(UrlValidator.getInstance());
		purpose_url.setRequired(true);

		support_url = new TextFormElementDE(this);
		support_url.setLabel("Support URL"); 
		support_url.setValue(rec.support_url);
		support_url.addValidator(UrlValidator.getInstance());
		support_url.setRequired(true);

	
		new StaticDE(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<VOContactRecord>> voclist_grouped = null;
		if(id != null) {
			VOContactModel vocmodel = new VOContactModel(context);
			ArrayList<VOContactRecord> voclist = vocmodel.getByVOID(id);
			voclist_grouped = vocmodel.groupByContactTypeID(voclist);
		} else {
			//set user's contact as submitter
			voclist_grouped = new HashMap<Integer, ArrayList<VOContactRecord>>();

			ArrayList<VOContactRecord> submitter_list = new ArrayList<VOContactRecord>();
			VOContactRecord submitter = new VOContactRecord();
			submitter.contact_id = auth.getContactID();
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			submitter_list.add(submitter);
			voclist_grouped.put(1/*submitter*/, submitter_list);
			
			// Should we make a function for these steps and call it 4 times? -agopu
			ArrayList<VOContactRecord> manager_list = new ArrayList<VOContactRecord>();
			VOContactRecord manager = new VOContactRecord();
			manager.contact_id = auth.getContactID();
			manager.contact_rank_id = 1;//primary
			manager.contact_type_id = 6;//manager
			manager_list.add(manager);
			voclist_grouped.put(6/*manager*/, manager_list);

			ArrayList<VOContactRecord> admin_contact_list = new ArrayList<VOContactRecord>();
			VOContactRecord primary_admin = new VOContactRecord();
			primary_admin.contact_id = auth.getContactID();
			primary_admin.contact_rank_id = 1;//primary
			primary_admin.contact_type_id = 3;//admin
			admin_contact_list.add(primary_admin);
			voclist_grouped.put(3/*admin*/, admin_contact_list);
		
			ArrayList<VOContactRecord> security_contact_list = new ArrayList<VOContactRecord>();
			VOContactRecord primary_security_contact= new VOContactRecord();
			primary_security_contact.contact_id = auth.getContactID();
			primary_security_contact.contact_rank_id = 1;//primary
			primary_security_contact.contact_type_id = 2;//security_contact
			security_contact_list.add(primary_security_contact);
			voclist_grouped.put(2/*security_contact*/, security_contact_list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
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

		// Handle reporting names
		new StaticDE(this, "<h2>Reporting Names for your VO</h2>");
		ContactModel cmodel = new ContactModel (context);
		VOReportNameModel vorepname_model = new VOReportNameModel(context);
		VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(context);

		ArrayList<VOReportNameRecord> vorepname_records = vorepname_model.getAll();
		vo_report_name_div = new VOReportNamesDE(this, vorepname_records, cmodel);

		if(id != null) {
			for(VOReportNameRecord vorepname_rec : vorepname_model.getAllByVOID(id)) {
				
				VOReportContactModel vorcmodel = new VOReportContactModel(context);
				Collection<VOReportContactRecord> vorc_list = vorcmodel.getAllByVOReportNameID(vorepname_rec.id);
				Collection<VOReportNameFqanRecord> vorepnamefqan_list = vorepnamefqan_model.getAllByVOReportNameID(vorepname_rec.id);
				vo_report_name_div.addVOReportName(vorepname_rec,
							vorepnamefqan_list, vorc_list);
			}
		}
		else {
			// TODO agopu need code here - Chris green's request for version 2.1
			//new StaticDE(this, "<h3>NOTE: We are adding a default reporting names for this VO. You can modify it or add more.</h3>");		}
		}
		if(auth.allows("admin")) {
			new StaticDE(this, "<h2>Administrative Tasks</h2>");
		}
		footprints_id = new TextFormElementDE(this);
		footprints_id.setLabel("Footprints ID");
		footprints_id.setValue(rec.footprints_id);
		footprints_id.setRequired(true);
		if(!auth.allows("admin")) {
			footprints_id.setHidden(true);
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
	
	private void hideParentVOSelector(Boolean b)
	{
		parent_vo.setHidden(b);
		parent_vo.redraw();
	}

	private ContactEditorDE createContactEditor(HashMap<Integer, ArrayList<VOContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h3>" + ctrec.name + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditorDE editor = new ContactEditorDE(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(voclist != null) {
			ArrayList<VOContactRecord> clist = voclist.get(ctrec.id);
			if(clist != null) {
				for(VOContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	private HashMap<Integer, String> getSCNames() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SCRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private HashMap<Integer, String> getVONames() throws AuthorizationException, SQLException
	{
		//pull all VOs
		VOModel model = new VOModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(VORecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}

	private void handleParentVOSelection(Integer parent_vo_id) {
		VOModel model = new VOModel (context);
		try {
			VORecord parent_vo_rec = model.get(parent_vo_id);
			if ((primary_url.getValue() == null) || (primary_url.getValue().length() == 0)) {
				primary_url.setValue(parent_vo_rec.primary_url);
				primary_url.redraw();
			}
			if ((aup_url.getValue() == null) || (aup_url.getValue().length() == 0)) {
				aup_url.setValue(parent_vo_rec.aup_url);
				aup_url.redraw();
			}
			if ((membership_services_url.getValue() == null) || (membership_services_url.getValue().length() == 0)) {
				membership_services_url.setValue(parent_vo_rec.membership_services_url);
				membership_services_url.redraw();
			}
			if ((purpose_url.getValue() == null) || (purpose_url.getValue().length() == 0)) {
				purpose_url.setValue(parent_vo_rec.purpose_url);
				purpose_url.redraw();
			}
			if ((support_url.getValue() == null) || (support_url.getValue().length() == 0)) {
				support_url.setValue(parent_vo_rec.support_url);
				support_url.redraw();
			}
			if (sc_id.getValue() == null) {
				sc_id.setValue(parent_vo_rec.sc_id);
				sc_id.redraw();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Boolean doSubmit() 
	{
		VORecord rec = new VORecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.long_name = long_name.getValue();
		rec.description = description.getValue();
		rec.primary_url = primary_url.getValue();
		rec.aup_url = aup_url.getValue();
		rec.membership_services_url = membership_services_url.getValue();
		rec.purpose_url = purpose_url.getValue();
		rec.support_url = support_url.getValue();
		rec.app_description = app_description.getValue();
		rec.community = community.getValue();
		rec.sc_id = sc_id.getValue();
		rec.footprints_id = footprints_id.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ArrayList<VOContactRecord> contacts = getContactRecordsFromEditor();
		
		ArrayList<Integer> field_of_science_ids = new ArrayList();
		for(Integer id : field_of_science.keySet()) {
			CheckBoxFormElementDE elem = field_of_science.get(id);
			if(elem.getValue()) {
				field_of_science_ids.add(id);
			}
		}
		
		Boolean ret = true;
		VOModel model = new VOModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, 
						contacts, 
						parent_vo.getValue(), 
						field_of_science_ids,
						vo_report_name_div.getVOReports());
			} else {
				model.updateDetail(rec, 
						contacts, 
						parent_vo.getValue(), 
						field_of_science_ids,
						vo_report_name_div.getVOReports());
			}
		} catch (Exception e) {
			alert(e.getMessage());
			log.error(e);
			ret = false;
		}
		context.close();
		return ret;
	}
	
	//retrieve contact records from the contact editor.
	//be aware that VOContactRecord's vo_id is not populated.. you need to fill it out with
	//appropriate vo_id later
	private ArrayList<VOContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<VOContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditorDE editor = contact_editors.get(type_id);
			HashMap<ContactRecord, Integer> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				VOContactRecord rec = new VOContactRecord();
				Integer rank_id = contacts.get(contact);
				rec.contact_id = contact.id;
				rec.contact_type_id = type_id;
				rec.contact_rank_id = rank_id;
				list.add(rec);
			}
		}
		
		return list;
	}
}
