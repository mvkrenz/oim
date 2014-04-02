package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.divrep.AUPConfirmation;
import edu.iu.grid.oim.view.divrep.Confirmation;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class SCFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(SCFormDE.class); 
    private UserContext context;
   
	protected Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextBox long_name;
	private DivRepTextArea description;
	private DivRepTextArea community;
	private DivRepTextBox footprints_id;
	private DivRepTextBox external_assignment_id;
	private DivRepCheckBox active;
	private DivRepCheckBox disable;
	private Confirmation confirmation;
	private DivRepTextArea comment;
	
	static public ArrayList<ContactTypeRecord.Info> ContactTypes;
	static {
		ContactTypes = new ArrayList<ContactTypeRecord.Info>();
		ContactTypes.add(new ContactTypeRecord.Info(1, "A contact who has registered this support center"));
		ContactTypes.add(new ContactTypeRecord.Info(4, "GOC tickets when assigned to a support center are sent to the primary Operations contact's email address"));
		ContactTypes.add(new ContactTypeRecord.Info(7, "GOC notifications (also available via RSS) are sent to both primary and secondary notification contacts"));
		ContactTypes.add(new ContactTypeRecord.Info(2, "Security notifications sent out by the OSG security team are sent to these contacts"));
		ContactTypes.add(new ContactTypeRecord.Info(5, "Contacts who do not fall under any of the above types but would like to be able to edit this support center can be added as miscellaneous contact"));
	}	
	
	private HashMap<Integer, ContactEditor> contact_editors = new HashMap();
	
	public SCFormDE(UserContext _context, SCRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		
		id = rec.id;
		
		new DivRepStaticContent(this, "<h2>Details</h2>");
		
		//pull SCs for unique validator
		HashMap<Integer, String> scs = getSCs();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			scs.remove(id);
		}
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(scs.values()));
		name.setRequired(true);
		name.setSampleValue("GOC");
		
		long_name = new DivRepTextBox(this);
		long_name.setLabel("Enter a Long Name for this SC");
		long_name.setValue(rec.long_name);
		long_name.setRequired(true);
		long_name.setSampleValue("OpenScienceGrid Operations Center");
				
		description = new DivRepTextArea(this);
		description.setLabel("Enter a Description");
		description.setValue(rec.description);
		description.setRequired(true);

		// TODO agopu Is this really necessary in both VO and SC?
		community = new DivRepTextArea(this);
		community.setLabel("Enter the Community this SC supports");
		community.setValue(rec.community);
		community.setRequired(true);
		
		new DivRepStaticContent(this, "<h2>Ticket Exchange</h2>");
		new DivRepStaticContent(this, "<p>GOC-TX uses this information to populate necessary fields on the destination ticketing system for tickets sent to this support center.</p>");

		external_assignment_id = new DivRepTextBox(this);
		external_assignment_id.setLabel("External Assignment ID");
		external_assignment_id.setValue(rec.external_assignment_id);

		
		new DivRepStaticContent(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<SCContactRecord>> scclist_grouped = null;
		if(id != null) {
			SCContactModel sccmodel = new SCContactModel(context);
			ArrayList<SCContactRecord> scclist = sccmodel.getBySCID(id);
			scclist_grouped = sccmodel.groupByContactTypeID(scclist);
		} else {
			scclist_grouped = new HashMap<Integer, ArrayList<SCContactRecord>>();
			
			/*
			//set user's contact as submitter
			ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
			SCContactRecord submitter = new SCContactRecord();
			submitter.contact_id = auth.getContactID();
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			list.add(submitter);
			scclist_grouped.put(1, list);
			*/
			
			//prepopulate submitter
			ArrayList<SCContactRecord> submitter_list = new ArrayList<SCContactRecord>();
			SCContactRecord submitter = new SCContactRecord();
			submitter.contact_id = auth.getContact().id;
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			submitter_list.add(submitter);
			scclist_grouped.put(1/*submitter*/, submitter_list);
			
			//prepopulate operations contact
			ArrayList<SCContactRecord> operation_list = new ArrayList<SCContactRecord>();
			SCContactRecord operation = new SCContactRecord();
			operation.contact_id = auth.getContact().id;
			operation.contact_rank_id = 1;//primary
			operation.contact_type_id = 4;//operations
			operation_list.add(operation);
			scclist_grouped.put(4, operation_list);
			
			//prepopulate notification contact
			ArrayList<SCContactRecord> notification_list = new ArrayList<SCContactRecord>();
			SCContactRecord notification = new SCContactRecord();
			notification.contact_id = auth.getContact().id;
			notification.contact_rank_id = 1;//primary
			notification.contact_type_id = 7;//operations
			notification_list.add(notification);
			scclist_grouped.put(7, notification_list);
		
			//security contact
			ArrayList<SCContactRecord> security_list = new ArrayList<SCContactRecord>();
			SCContactRecord security = new SCContactRecord();
			security.contact_id = auth.getContact().id;
			security.contact_rank_id = 1;//primary
			security.contact_type_id = 2;//security
			security_list.add(security);
			scclist_grouped.put(2/*security*/, security_list);
			
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		for(ContactTypeRecord.Info contact_type : ContactTypes) {
			ToolTip tip = new ToolTip(contact_type.desc);
			ContactEditor editor = createContactEditor(scclist_grouped, ctmodel.get(contact_type.id), tip);
			//disable submitter editor if needed
			if(!auth.allows("admin")) {
				if(contact_type.id == 1) { //1 = Submitter Contact
					editor.setDisabled(true);
				}
			}
			if(contact_type.id != 5) { //5 = misc
				editor.setMinContacts(ContactRank.Primary, 1);
			}
			contact_editors.put(contact_type.id, editor);
		}
		
		new DivRepStaticContent(this, "<h2>Confirmation</h2>");
		confirmation = new Confirmation(this, rec, auth);
		
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}
		footprints_id = new DivRepTextBox(this);
		footprints_id.setLabel("Footprints ID");
		footprints_id.setValue(rec.footprints_id);
		footprints_id.setRequired(true);
		if(!auth.allows("admin")) {
			footprints_id.setHidden(true);
		}

		active = new DivRepCheckBox(this);
		active.setLabel("Active (Soon to be phased out - modification not allowed)");
		active.setDisabled(true);
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
		
		if(id == null) {
			AUPConfirmation aup = new AUPConfirmation(this);
		}
		
		comment = new DivRepTextArea(this);
		comment.setLabel("Update Comment");
		comment.setSampleValue("Please provide a reason for this update.");
	}
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<SCContactRecord>> scclist, ContactTypeRecord ctrec, ToolTip tip) throws SQLException
	{
		new DivRepStaticContent(this, "<h3>" + ctrec.name + " " + tip.render() + "</h3>");
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
		//Construct VORecord
		SCRecord rec = new SCRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.long_name = long_name.getValue();
		rec.description = description.getValue();
		rec.community = community.getValue();
		rec.footprints_id = footprints_id.getValue();
		rec.external_assignment_id = external_assignment_id.getValue();
		rec.disable = disable.getValue();
		if (rec.disable == true) {
			rec.active = false; // not using real active value, instead defaulting based on disable value
		} else {
			rec.active = true; 
		}
		rec.confirmed = confirmation.getTimestamp();
		
		context.setComment(comment.getValue());
		
		ArrayList<SCContactRecord> contacts = getContactRecordsFromEditor();
		
		SCModel model = new SCModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, contacts);
				context.message(MessageType.SUCCESS, "Successfully registered new support center.");
				
				try {
					//create footprint ticket
					Footprints fp = new Footprints(context);
					fp.createNewSCTicket(rec.name);
				} catch (Exception fpe) {
					log.error("Failed to open footprints ticket: ", fpe);
				}
			} else {
				model.updateDetail(rec, contacts);
				context.message(MessageType.SUCCESS, "Successfully updated a support center.");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
			log.error("Failed to insert/update record", e);
			return false;
		}

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
			HashMap<ContactRecord, ContactRank> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				SCContactRecord rec = new SCContactRecord();
				ContactRank rank = contacts.get(contact);
				rec.contact_id = contact.id;
				rec.contact_type_id = type_id;
				rec.contact_rank_id = rank.id;
				list.add(rec);
			}
		}
		
		return list;
	}
}
