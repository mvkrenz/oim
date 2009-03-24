package edu.iu.grid.oim.view.divex.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divex.DivEx;
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
	
	public VOFormDivex(DivEx parent, VORecord rec, String origin_url, Connection _con, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		con = _con;
		auth = _auth;
	
		parent.add("<h3>Details</h3>");
		
		id = rec.id;
		{
			//pull vos for unique validator
			HashMap<Integer, String> vos = getVOs();
			if(id != null) {
				//if doing update, remove my own name (I can use my own name)
				vos.remove(id);
			}
			TextFormElementDE elem = new TextFormElementDE(this, "name");
			elem.setLabel("Name");
			elem.setValue(rec.name);
			elem.setValidator(new UniqueValidator<String>(vos.values()));
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "long_name");
			elem.setLabel("Long Name");
			elem.setValue(rec.long_name);
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "description");
			elem.setLabel("Description");
			elem.setValue(rec.description);
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "primary_url");
			elem.setLabel("Primary URL");
			elem.setValue(rec.primary_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "aup_url");
			elem.setLabel("AUP URL");
			elem.setValue(rec.aup_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "membership_services_url");
			elem.setLabel("Membership Services URL");
			elem.setValue(rec.membership_services_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "purpose_url");
			elem.setLabel("Purpose URL"); 
			elem.setValue(rec.purpose_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "support_url");
			elem.setLabel("Support URL"); 
			elem.setValue(rec.support_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "app_description");
			elem.setLabel("App Description");
			elem.setValue(rec.app_description);
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "community");
			elem.setLabel("Community");
			elem.setValue(rec.community);
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "footprints_id");
			elem.setLabel("Footprints ID");
			elem.setValue(rec.footprints_id);
			elem.setRequired(true);
		}
		
		{
			SelectFormElementDE elem = new SelectFormElementDE(this, "sc_id", getSCs());
			elem.setLabel("Support Center");
			elem.setValue(rec.sc_id);
			elem.setRequired(true);
		}
		
		{
			//if vo_id is set (for update) remove that from possible value.
			HashMap<Integer, String> vos = getVOs();
			if(rec.id != null) {
				vos.remove(rec.id);
			}
			
			SelectFormElementDE elem = new SelectFormElementDE(this, "parent_vo_id", vos);
			elem.setLabel("Parent Virtual Organization");
			elem.setValue(rec.parent_vo_id);
		}
		
		{
			CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this, "active");
			elem.setLabel("Active");
			elem.setValue(rec.active);
		}
		
		{
			CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this, "disable");
			elem.setLabel("Disabled");
			elem.setValue(rec.disable);
		}	
		
		//contact information
		parent.add("<h3>Contact Information</h3>");
		
		//contacts
		VOContactModel vocmodel = new VOContactModel(con, auth);
		ContactTypeModel ctmodel = new ContactTypeModel(con, auth);
		PersonModel pmodel = new PersonModel(con, auth);
		HashMap<Integer, ArrayList<Integer>> voclist = vocmodel.get(rec.id);
		for(Integer type_id : voclist.keySet()) {
			ArrayList<Integer> clist = voclist.get(type_id);
			ContactTypeRecord ctrec = ctmodel.get(type_id);
			
			String cliststr = "";
			for(Integer person_id : clist) {
				PersonRecord person = pmodel.get(person_id);
				cliststr += person.getFullName() + "<br/>";
			}
			
			parent.add(ctrec.name + "<br/>");
			parent.add(cliststr);
		}
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
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("name");
			rec.name = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("long_name");
			rec.long_name = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("description");
			rec.description = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("primary_url");
			rec.primary_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("aup_url");
			rec.aup_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("membership_services_url");
			rec.membership_services_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("purpose_url");
			rec.purpose_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("support_url");
			rec.support_url = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("app_description");
			rec.app_description = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("community");
			rec.community = elem.getValue();
		}
		
		{
			SelectFormElementDE elem = (SelectFormElementDE) getElement("sc_id");
			rec.sc_id = elem.getValue();
		}
		
		{
			SelectFormElementDE elem = (SelectFormElementDE) getElement("parent_vo_id");
			rec.parent_vo_id = elem.getValue();
		}	
		
		{	
			TextFormElementDE elem = (TextFormElementDE) getElement("footprints_id");
			rec.footprints_id = elem.getValue();
		}	
		
		{	
			CheckBoxFormElementDE elem = (CheckBoxFormElementDE) getElement("active");
			rec.active = elem.getValue();
		}	
		
		{	
			CheckBoxFormElementDE elem = (CheckBoxFormElementDE) getElement("disable");
			rec.disable = elem.getValue();
		}	
		
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
