package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divrep.common.Static;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.FormBase;
import com.webif.divrep.common.TextArea;
import com.webif.divrep.common.Text;
import com.webif.divrep.validator.UniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class SCFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(SCFormDE.class); 
    private Context context;
   
	protected Authorization auth;
	private Integer id;
	
	private Text name;
	private Text long_name;
	private TextArea description;
	private TextArea community;
	private Text footprints_id;
	private CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	
	//contact types to edit
	private int contact_types[] = {
		1, //submitter
		4, //operational contact
		7, //notification contact
		2, //security contact
		5, //misc contact
	};
	private HashMap<Integer, ContactEditor> contact_editors = new HashMap();
	
	public SCFormDE(Context _context, SCRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		new Static(this, "<h2>Details</h2>");
		
		//pull SCs for unique validator
		HashMap<Integer, String> scs = getSCs();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			scs.remove(id);
		}
		name = new Text(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new UniqueValidator<String>(scs.values()));
		name.setRequired(true);
		name.setSampleValue("GOC");
		
		long_name = new Text(this);
		long_name.setLabel("Enter a Long Name for this SC");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true);
		long_name.setSampleValue("OpenScienceGrid Operations Center");
				
		description = new TextArea(this);
		description.setLabel("Enter a Description");
		description.setValue(rec.description);
		description.setRequired(true);

		// TODO agopu Is this really necessary in both VO and SC?
		community = new TextArea(this);
		community.setLabel("Enter the Community this SC supports");
		community.setValue(rec.community);
		community.setRequired(true);

		new Static(this, "<h2>Contact Information</h2>");
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
			ContactEditor editor = createContactEditor(scclist_grouped, ctmodel.get(contact_type_id));
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type_id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			contact_editors.put(contact_type_id, editor);
		}
		if(auth.allows("admin")) {
			new Static(this, "<h2>Administrative Tasks</h2>");
		}
		footprints_id = new Text(this);
		footprints_id.setLabel("Footprints ID");
		footprints_id.setValue(rec.footprints_id);
		footprints_id.setRequired(true);
		if(!auth.allows("admin")) {
			footprints_id.setHidden(true);
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
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<SCContactRecord>> scclist, ContactTypeRecord ctrec) throws SQLException
	{
		new Static(this, "<h3>" + ctrec.name + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
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
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
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
			ret = false;
		}
		context.close();
		return ret;
	}
	
	//retrieve contact records from the contact editor.
	//be aware that SCContactRecord's sc_id is not populated.. you need to fill it out with
	//appropriate sc_id later
	private ArrayList<SCContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<SCContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditor editor = contact_editors.get(type_id);
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
