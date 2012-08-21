package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepLocationSelector;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepLocationSelector.LatLng;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CampusGridFieldOfScienceModel;
import edu.iu.grid.oim.model.db.CampusGridSubmitNodeModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ResourceAliasModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.CampusGridContactModel;
import edu.iu.grid.oim.model.db.CampusGridModel;
import edu.iu.grid.oim.model.db.record.CampusGridFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.CampusGridSubmitNodeRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;

import edu.iu.grid.oim.model.db.record.CampusGridRecord;

import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.CampusGridContactRecord;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.divrep.CampusGridSubmitNodes;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.FieldOfScience;
import edu.iu.grid.oim.view.divrep.ResourceAlias;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class CampusGridFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(CampusGridFormDE.class); 
   
    private UserContext context;
	private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextArea description;
	private DivRepTextBox gratia;
	private DivRepSelectBox maturity;	
	private FieldOfScience field_of_science_de;
	private DivRepLocationSelector latlng;
	private CampusGridSubmitNodes submithosts;
	private DivRepSelectBox gateway_submitnode;	
	private DivRepCheckBox disable;

	private DivRepTextArea comment;

	static public ArrayList<ContactTypeRecord.Info> ContactTypes;
	static {
		ContactTypes = new ArrayList<ContactTypeRecord.Info>();
		ContactTypes.add(new ContactTypeRecord.Info(1, "A contact who has registered this campus grid"));
		ContactTypes.add(new ContactTypeRecord.Info(3, "Contacts for ticketing and assorted issues. This is typically a user/application support person or a help desk"));
		ContactTypes.add(new ContactTypeRecord.Info(2, "Security notifications sent out by the OSG security team are sent to primary and secondary campus grid security contacts"));
		ContactTypes.add(new ContactTypeRecord.Info(5, "Contacts who do not fall under any of the above types but would like to be able to edit this campus grid can be added as miscellaneous contact"));
		//ContactTypes.add(new ContactTypeRecord.Info(11, "RA (Registration Authority) agent who can approve certificate requests."));
	}	
	private HashMap<Integer, ContactEditor> contact_editors = new HashMap();
	
	public HashMap<Integer, String> getMaturities() {
		HashMap<Integer, String> kvs = new HashMap<Integer, String>();
		kvs.put(1, "Level 1: No organized or coordinated campus grid effort.");
		kvs.put(2, "Level 2: Some localized organization around campus grids.");
		kvs.put(3, "Level 3: Campus wide organization and/or broad visibility to campus grids.");
		kvs.put(4, "Level 4: Campus wide organization or visibility of campus grids initiatives.");
		kvs.put(5, "Level 5: Campus grids are a 'way of life' for campus researchers.");
		return kvs;
	}

	public CampusGridFormDE(UserContext _context, CampusGridRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;
		
		//new DivRepStaticContent(this, "<h2>Basic CampusGrid Information</h2>");
		
		//pull vos for unique validator
		LinkedHashMap<Integer, String> campusgrids = getCampusGridNames();
		if(id != null) { //if doing update, remove my own name (I can't use my own name)
			campusgrids.remove(id);
		}
		name = new DivRepTextBox(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.addValidator(new DivRepUniqueValidator<String>(campusgrids.values()));
		name.setRequired(true);
		name.setSampleValue("CDF");

		description = new DivRepTextArea(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);
		description.setSampleValue("Collider Detector at Fermilab");
		
		gratia = new DivRepTextBox(this);
		gratia.setLabel("URL of Campus Grid Gratia Accounting");
		//gratia.setRequired(true);
		gratia.setSampleValue("gratiaweb.grid.iu.edu/gratia/xml/glidein_hours_bar_smry?probe=condor:glidein.unl.edu");
		gratia.setValue(rec.gratia);
		//gratia.setWidth(450);

		maturity = new DivRepSelectBox(this);
		maturity.setLabel("Maturity");
		maturity.setValues(getMaturities());
		maturity.setValue(rec.maturity);
		maturity.setRequired(true);
		
		//load submit node resources
		ResourceServiceModel rsmodel = new ResourceServiceModel(context);
		ResourceModel rmodel = new ResourceModel(context);
		LinkedHashMap<Integer, String> submitnodes = new LinkedHashMap<Integer, String>();
		for(ResourceServiceRecord rs : rsmodel.getByServiceID(109)) { //submit node (TODO - make this configurable)
			ResourceRecord r = rmodel.get(rs.resource_id);
			submitnodes.put(rs.resource_id, r.name);
		}
		
		
		gateway_submitnode = new DivRepSelectBox(this);
		gateway_submitnode.setLabel("Gateway Submit Node");
		gateway_submitnode.setValues(submitnodes);
		gateway_submitnode.setValue(rec.gateway_submitnode_id);
		
		new DivRepStaticContent(this, "<h3>Submit Node FQDNs</h3>");		
		
		//create submithost selector
		submithosts = new CampusGridSubmitNodes(this);
		CampusGridSubmitNodeModel cmodel = new CampusGridSubmitNodeModel(context);
		if(id != null) {
			for(CampusGridSubmitNodeRecord rarec : cmodel.getAllByCampusGridID(id)) {
				submithosts.addNode(rarec.fqdn);
			}
		}
		/*
		if(id == null) {
			submithosts.addNode(null); //add placeholder
		}
		*/
		
		ArrayList<Integer> fos_selected = new ArrayList<Integer>();
		if(id != null) {
			//populate field of science
			try {
				//select currently selected field of science
				CampusGridFieldOfScienceModel cgfsmodel = new CampusGridFieldOfScienceModel(context);
				for(CampusGridFieldOfScienceRecord fsrec : cgfsmodel.getByCampusGridID(rec.id)) {
					fos_selected.add(fsrec.field_of_science_id);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		field_of_science_de = new FieldOfScience(this, context, fos_selected);
		
		//latlng = new LatLngSelector(this);
		new DivRepStaticContent(this, "<h3>Latitude / Longitude</h3>");		
		latlng = new DivRepLocationSelector(this, "images/target.png", StaticConfig.conf.getProperty("gmapapikey"));
		latlng.setHttps(true);//should be always https
		//latlng.setLabel("Latitude / Longitude");
		int zoom = 10;
		if(rec.latitude == null || rec.longitude == null) {
			//set it to some *random* location
			rec.latitude = 37.401394D;
			rec.longitude = -116.867846D;
			zoom = 2;
		}
		latlng.setValue(latlng.new LatLng(rec.latitude, rec.longitude, zoom));
		latlng.setRequired(true);
		
		new DivRepStaticContent(this, "<h2>Contact Information</h2>");
		HashMap<Integer/*contact_type_id*/, ArrayList<CampusGridContactRecord>> cgclist_grouped = null;
		if(id != null) {
			CampusGridContactModel vocmodel = new CampusGridContactModel(context);
			ArrayList<CampusGridContactRecord> voclist = vocmodel.getByCampusGridID(id);
			cgclist_grouped = vocmodel.groupByContactTypeID(voclist);
		} else {
			//set user's contact as submitter
			cgclist_grouped = new HashMap<Integer, ArrayList<CampusGridContactRecord>>();

			ArrayList<CampusGridContactRecord> submitter_list = new ArrayList<CampusGridContactRecord>();
			CampusGridContactRecord submitter = new CampusGridContactRecord();
			submitter.contact_id = auth.getContact().id;
			submitter.contact_rank_id = 1;//primary
			submitter.contact_type_id = 1;//submitter
			submitter_list.add(submitter);
			cgclist_grouped.put(1/*submitter*/, submitter_list);
			
			ArrayList<CampusGridContactRecord> admin_contact_list = new ArrayList<CampusGridContactRecord>();
			CampusGridContactRecord primary_admin = new CampusGridContactRecord();
			primary_admin.contact_id = auth.getContact().id;
			primary_admin.contact_rank_id = 1;//primary
			primary_admin.contact_type_id = 3;//admin
			admin_contact_list.add(primary_admin);
			cgclist_grouped.put(3/*admin*/, admin_contact_list);
		
			ArrayList<CampusGridContactRecord> security_contact_list = new ArrayList<CampusGridContactRecord>();
			CampusGridContactRecord primary_security_contact= new CampusGridContactRecord();
			primary_security_contact.contact_id = auth.getContact().id;
			primary_security_contact.contact_rank_id = 1;//primary
			primary_security_contact.contact_type_id = 2;//security_contact
			security_contact_list.add(primary_security_contact);
			cgclist_grouped.put(2/*security_contact*/, security_contact_list);
		}
		ContactTypeModel ctmodel = new ContactTypeModel(context);
		for(ContactTypeRecord.Info contact_type : ContactTypes) {
			ToolTip tip = new ToolTip(contact_type.desc);
			ContactEditor editor = createContactEditor(cgclist_grouped, ctmodel.get(contact_type.id), tip);
			
			//only oim admin can edit submitter
			if(contact_type.id == 1) {//submitter
				if(!auth.allows("admin")) {
					editor.setDisabled(true);
				}
			}
			
			//request authority
			if(contact_type.id == 11) {
				//only admin_ra can edit ra
				if(!auth.allows("admin_ra")) {
					editor.setDisabled(true);
				}
				
				editor.setLabel(Rank.Primary, "Primary RA");
				editor.setLabel(Rank.Secondary, "Secondary RA");
				editor.setLabel(Rank.Tertiary, "Sponsors");
			}
			
			if(contact_type.id != 5 && contact_type.id != 10 && contact_type.id != 11) { //5 = misc, 9 = resource report, 11 = ra agent
				editor.setMinContacts(Rank.Primary, 1);
			}
			contact_editors.put(contact_type.id, editor);
		}
		
		new DivRepStaticContent(this, "<h2>Administrative</h2>");
		/*
		active = new DivRepCheckBox(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		*/
		
		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
		
		comment = new DivRepTextArea(this);
		comment.setLabel("Update Comment");
		comment.setSampleValue("Please provide a reason for this update.");
	}
	
	private ContactEditor createContactEditor(HashMap<Integer, ArrayList<CampusGridContactRecord>> cgclist, ContactTypeRecord ctrec, ToolTip tip) throws SQLException
	{
		new DivRepStaticContent(this, "<h3>" + ctrec.name + " " + tip.render() + "</h3>");
		ContactModel pmodel = new ContactModel(context);		
		ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
		
		//if provided, populate currently selected contacts
		if(cgclist != null) {
			ArrayList<CampusGridContactRecord> clist = cgclist.get(ctrec.id);
			if(clist != null) {
				for(CampusGridContactRecord rec : clist) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = rec.contact_id;
					ContactRecord person = pmodel.get(keyrec);
					editor.addSelected(person, rec.contact_rank_id);
				}
			}
		}
	
		return editor;
	}
	
	private LinkedHashMap<Integer, String> getSCNames() throws AuthorizationException, SQLException
	{
		SCModel model = new SCModel(context);
		ArrayList<SCRecord> recs = model.getAllActiveNonDisabled();
		Collections.sort(recs, new Comparator<SCRecord> () {
			public int compare(SCRecord a, SCRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(SCRecord rec : recs) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private LinkedHashMap<Integer, String> getCampusGridNames() throws AuthorizationException, SQLException
	{
		//pull all CampusGrids
		CampusGridModel model = new CampusGridModel(context);
		ArrayList<CampusGridRecord> recs = model.getAll();
		Collections.sort(recs, new Comparator<CampusGridRecord> () {
			public int compare(CampusGridRecord a, CampusGridRecord b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap<Integer, String>();
		for(CampusGridRecord rec : recs) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}


	protected Boolean doSubmit() 
	{
		CampusGridRecord rec = new CampusGridRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.description = description.getValue();
		rec.gratia = gratia.getValue();
		rec.maturity = maturity.getValue();
		rec.latitude = latlng.getValue().latitude;
		rec.longitude = latlng.getValue().longitude;
		rec.disable = disable.getValue();
		rec.gateway_submitnode_id = gateway_submitnode.getValue();
	
		context.setComment(comment.getValue());
		
		ArrayList<CampusGridContactRecord> contacts = getContactRecordsFromEditor();

		ArrayList<Integer> field_of_science_ids = new ArrayList();
		for(Integer id : field_of_science_de.getSciences().keySet()) {
			DivRepCheckBox elem = field_of_science_de.getSciences().get(id);
			if(elem.getValue()) {
				field_of_science_ids.add(id);
			}
		}
		
		CampusGridModel model = new CampusGridModel(context);
		try {
			if(rec.id == null) {
				model.insertDetail(rec, contacts, field_of_science_ids, submithosts.getSubmithosts());
				context.message(MessageType.SUCCESS, "Successfully registered new CampusGrid.");
			} else {
				model.updateDetail(rec, contacts, field_of_science_ids, submithosts.getSubmithosts());
				context.message(MessageType.SUCCESS, "Successfully updated a CampusGrid.");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
			log.error("Failed to insert/update record", e);
			return false;
		}
	}
	
	//retrieve contact records from the contact editor.
	//be aware that CampusGridContactRecord's vo_id is not populated.. you need to fill it out with
	//appropriate vo_id later
	private ArrayList<CampusGridContactRecord> getContactRecordsFromEditor()
	{
		ArrayList<CampusGridContactRecord> list = new ArrayList();
		
		for(Integer type_id : contact_editors.keySet()) 
		{
			ContactEditor editor = contact_editors.get(type_id);
			HashMap<ContactRecord, Integer> contacts = editor.getContactRecords();
			for(ContactRecord contact : contacts.keySet()) {
				CampusGridContactRecord rec = new CampusGridContactRecord();
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
