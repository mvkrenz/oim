package edu.iu.grid.oim.view.divex.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
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
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.FormDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class VOFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(VOFormDE.class); 
    
    protected Connection con = null;
	protected Authorization auth;
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
	private SelectFormElementDE parent_vo;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		2, //security contact
		3, //admin contact
		4, //operational contact
		5, //misc contact
		6, //vo manager
		7, //notification contat
		10, //VO report contact
	};
	private HashMap<Integer, ContactEditorDE> contact_editors = new HashMap();
	
	public VOFormDE(DivEx parent, VORecord rec, String origin_url, Connection _con, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		con = _con;
		auth = _auth;
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Details</h2>");
		
		//pull vos for unique validator
		HashMap<Integer, String> vos = getVOs();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			vos.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.setValidator(new UniqueValidator<String>(vos.values()));
		name.setRequired(true);
		
		long_name = new TextFormElementDE(this);
		long_name.setLabel("Long Name");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true);
				
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);

		primary_url = new TextFormElementDE(this);
		primary_url.setLabel("Primary URL");
		primary_url.setValue(rec.primary_url);
		primary_url.setValidator(UrlValidator.getInstance());
		primary_url.setRequired(true);

		aup_url = new TextFormElementDE(this);
		aup_url.setLabel("AUP URL");
		aup_url.setValue(rec.aup_url);
		aup_url.setValidator(UrlValidator.getInstance());
		aup_url.setRequired(true);

		membership_services_url = new TextFormElementDE(this);
		membership_services_url.setLabel("Membership Services URL");
		membership_services_url.setValue(rec.membership_services_url);
		membership_services_url.setValidator(UrlValidator.getInstance());
		membership_services_url.setRequired(true);

		purpose_url = new TextFormElementDE(this);
		purpose_url.setLabel("Purpose URL"); 
		purpose_url.setValue(rec.purpose_url);
		purpose_url.setValidator(UrlValidator.getInstance());
		purpose_url.setRequired(true);

		support_url = new TextFormElementDE(this);
		support_url.setLabel("Support URL"); 
		support_url.setValue(rec.support_url);
		support_url.setValidator(UrlValidator.getInstance());
		support_url.setRequired(true);

		app_description = new TextAreaFormElementDE(this);
		app_description.setLabel("App Description");
		app_description.setValue(rec.app_description);
		app_description.setRequired(true);

		community = new TextAreaFormElementDE(this);
		community.setLabel("Community");
		community.setValue(rec.community);
		community.setRequired(true);

		footprints_id = new TextFormElementDE(this);
		footprints_id.setLabel("Footprints ID");
		footprints_id.setValue(rec.footprints_id);
		footprints_id.setRequired(true);

		sc_id = new SelectFormElementDE(this, getSCs());
		sc_id.setLabel("Support Center");
		sc_id.setValue(rec.sc_id);
		sc_id.setRequired(true);

		active = new CheckBoxFormElementDE(this);
		active.setLabel("Active");
		active.setValue(rec.active);

		disable = new CheckBoxFormElementDE(this);
		disable.setLabel("Disabled");
		disable.setValue(rec.disable);
		
		parent_vo = new SelectFormElementDE(this, vos);
		parent_vo.setLabel("Parent VO");
		if(id != null) {
			VOModel model = new VOModel(con, auth);
			VORecord parent_vo_rec = model.getParentVO(rec.id);
			if(parent_vo_rec != null) {
				parent_vo.setValue(parent_vo_rec.id);
			}
		}
		
		new StaticDE(this, "<h3>Field Of Science</h3>");
		ArrayList<FieldOfScienceRecord> fslist = null;
		if(id != null) {
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(con, auth);
			fslist = vofsmodel.get(id);
		}
		FieldOfScienceModel fsmodel = new FieldOfScienceModel(con, auth);
		HashMap<Integer, FieldOfScienceRecord> fs = fsmodel.getAll();
		field_of_science = new HashMap();
		for(Integer fsid : fs.keySet()) {
			FieldOfScienceRecord fsrec = fs.get(fsid);
			CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this);
			field_of_science.put(fsid, elem);
			elem.setLabel(fsrec.name);
			if(fslist != null) {
				if(fslist.contains(fsrec)) {
					elem.setValue(true);	
				}
			}
		}
		
		new StaticDE(this, "<h2>Contact Information</h2>");
		VOContactModel vocmodel = new VOContactModel(con, auth);
		HashMap<Integer, ArrayList<VOContactRecord>> voclist = vocmodel.get(id);
		ContactTypeModel ctmodel = new ContactTypeModel(con, auth);
		for(int contact_type_id : contact_types) {
			contact_editors.put(contact_type_id, createContactEditor(voclist, ctmodel.get(contact_type_id)));
		}
	}
	
	private ContactEditorDE createContactEditor(HashMap<Integer, ArrayList<VOContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h3>" + ctrec.name + "</h3>");
		ContactModel pmodel = new ContactModel(con, auth);		
		ContactEditorDE editor = new ContactEditorDE(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		ArrayList<VOContactRecord> clist = voclist.get(ctrec.id);
		if(clist != null) {
			for(VOContactRecord rec : clist) {
				ContactRecord person = pmodel.get(rec.contact_id);
				editor.addSelected(person, rec.contact_rank_id);
			}
		}
		return editor;
	}
	
	private HashMap<Integer, String> getSCs() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(con, auth);
		Collection<SCRecord> scs = model.getAll();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SCRecord rec : scs) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private HashMap<Integer, String> getVOs() throws AuthorizationException, SQLException
	{
		//pull all VOs
		VOModel model = new VOModel(con, auth);
		Collection<VORecord> vos = model.getAll();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(VORecord rec : vos) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		
		//Construct VORecord
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
		
		//Do insert / update to our DB
		try {
			//process detail information
			VOModel model = new VOModel(con, auth);
			if(rec.id == null) {
				rec.id = model.insert(rec);
			} else {
				model.update(rec);
			}
			
			//process contact information
			VOContactModel cmodel = new VOContactModel(con, auth);
			cmodel.update(rec.id, getContactRecords());
			
			//process parent_vo
			model.updateParentVOID(rec.id, parent_vo.getValue());
			
			//process field of science
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(con, auth);
			ArrayList<Integer> list = new ArrayList<Integer>();
			for(Integer fsid : field_of_science.keySet()) {
				CheckBoxFormElementDE elem = field_of_science.get(fsid);
				if(elem.getValue()) {
					list.add(fsid);
				}
			}
			vofsmodel.update(rec.id, list);
			
		} catch (AuthorizationException e) {
			log.error(e);
			alert(e.getMessage());
			return false;
		} catch (SQLException e) {
			log.error(e);
			alert(e.getMessage());
			return false;
		}

		return true;
	}
	
	//retrieve contact records from the contact editor
	private ArrayList<VOContactRecord> getContactRecords()
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


	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
