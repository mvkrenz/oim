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
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class SCFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(SCFormDE.class); 
    private Context context;
   
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE long_name;
	private TextAreaFormElementDE description;
	private TextAreaFormElementDE community;
	private TextFormElementDE footprints_id;
	private CheckBoxFormElementDE active;
	private CheckBoxFormElementDE disable;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		4, //operational contact
		7, //notification contact
		2, //security contact
		5, //misc contact
	};
	private HashMap<Integer, ContactEditorDE> contact_editors = new HashMap();
	
	public SCFormDE(Context _context, SCRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getDivExRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		new StaticDE(this, "<h2>Details</h2>");
		
		//pull SCs for unique validator
		HashMap<Integer, String> scs = getSCs();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			scs.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(scs.values()));
		name.setRequired(true);
		
		long_name = new TextFormElementDE(this);
		long_name.setLabel("Long Name");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true);
				
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);

		community = new TextAreaFormElementDE(this);
		community.setLabel("Community");
		community.setValue(rec.community);
		community.setRequired(true);

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
		
		new StaticDE(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<SCContactRecord>> scclist_grouped = null;
		if(id != null) {
			SCContactModel sccmodel = new SCContactModel(context);
			ArrayList<SCContactRecord> scclist = sccmodel.getBySCID(id);
			scclist_grouped = sccmodel.groupByContactTypeID(scclist);
		} else {
			//set user's contact as submitter
			scclist_grouped = new HashMap<Integer, ArrayList<SCContactRecord>>();
			ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
			SCContactRecord submitter = new SCContactRecord();
			submitter.contact_id = auth.getContactID();
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			list.add(submitter);
			scclist_grouped.put(1/*submitter*/, list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		for(int contact_type_id : contact_types) {
			ContactEditorDE editor = createContactEditor(scclist_grouped, ctmodel.get(contact_type_id));
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type_id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			contact_editors.put(contact_type_id, editor);
		}
	}
	
	private ContactEditorDE createContactEditor(HashMap<Integer, ArrayList<SCContactRecord>> scclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h3>" + ctrec.name + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditorDE editor = new ContactEditorDE(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(scclist != null) {
			ArrayList<SCContactRecord> clist = scclist.get(ctrec.id);
			if(clist != null) {
				for(SCContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	private HashMap<Integer, String> getSCs() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(context);
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(SCRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		
		//Construct VORecord
		SCRecord rec = new SCRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.long_name = long_name.getValue();
		rec.description = description.getValue();
		rec.community = community.getValue();
		rec.footprints_id = footprints_id.getValue();
		rec.active = active.getValue();
		rec.disable = disable.getValue();
		
		ArrayList<SCContactRecord> contacts = getContactRecordsFromEditor();
		
		SCModel model = new SCModel(context);
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
	//be aware that SCContactRecord's sc_id is not populated.. you need to fill it out with
	//appropriate sc_id later
	private ArrayList<SCContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<SCContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditorDE editor = contact_editors.get(type_id);
			HashMap<ContactRecord, Integer> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				SCContactRecord rec = new SCContactRecord();
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
