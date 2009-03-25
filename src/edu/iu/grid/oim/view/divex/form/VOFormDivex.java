package edu.iu.grid.oim.view.divex.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
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
import edu.iu.grid.oim.model.db.PersonModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.PersonRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divex.FormDivex;

public class VOFormDivex extends FormDivex 
{
    static Logger log = Logger.getLogger(VOFormDivex.class); 
    
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
	
	public VOFormDivex(DivEx parent, VORecord rec, String origin_url, Connection _con, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		con = _con;
		auth = _auth;
	
		new StaticDE(this, "<h3>Details</h3>");
		
		id = rec.id;

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
		
		//contact information
		new StaticDE(this, "<h3>Contact Information</h3>");
		
		//contacts
		VOContactModel vocmodel = new VOContactModel(con, auth);
		HashMap<Integer, ArrayList<Integer>> voclist = vocmodel.get(id);
		ContactTypeModel ctmodel = new ContactTypeModel(con, auth);
		renderContactEditor(voclist, ctmodel.get(1));//submitter
		renderContactEditor(voclist, ctmodel.get(2));//security contact
		renderContactEditor(voclist, ctmodel.get(3));//admin contact
		renderContactEditor(voclist, ctmodel.get(5));//misc contact
		renderContactEditor(voclist, ctmodel.get(6));//vo manager
		renderContactEditor(voclist, ctmodel.get(7));//notification contact
		renderContactEditor(voclist, ctmodel.get(10));//VO report contact
	}
	
	private void renderContactEditor(HashMap<Integer, ArrayList<Integer>> voclist, ContactTypeRecord ctrec) throws SQLException
	{
		new StaticDE(this, "<h4>" + ctrec.name + "</h4>");
		
		ArrayList<Integer> clist = voclist.get(ctrec.id);
		if(clist != null) {
			PersonModel pmodel = new PersonModel(con, auth);
			for(Integer person_id : clist) {
				PersonRecord person = pmodel.get(person_id);
				TextFormElementDE text = new TextFormElementDE(this);
				text.setValue(person.getFullName());
			}
		}
		TextFormElementDE newcontact = new TextFormElementDE(this);
	}
	
	private HashMap<Integer, String> getSCs() throws AuthorizationException, SQLException
	{
		//pull all SCs
		ResultSet scs = null;
		SCModel model = new SCModel(con, auth);
		scs = model.getAll();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		while(scs.next()) {
			SCRecord rec = new SCRecord(scs);
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	private HashMap<Integer, String> getVOs() throws AuthorizationException, SQLException
	{
		//pull all VOs
		ResultSet vos = null;
		VOModel model = new VOModel(con, auth);
		vos = model.getAll();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		while(vos.next()) {
			VORecord rec = new VORecord(vos);
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
			VOModel model = new VOModel(con, auth);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(rec);
			}
		} catch (AuthorizationException e) {
			log.error(e);
			return false;
		} catch (SQLException e) {
			log.error(e);
			alert(e.getMessage());
			return false;
		}

		return true;
	}
}
