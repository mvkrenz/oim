package edu.iu.grid.oim.view.divrep.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.common.Static;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.FormBase;
import com.webif.divrep.common.Select;
import com.webif.divrep.common.TextArea;
import com.webif.divrep.common.Text;
import com.webif.divrep.validator.UniqueValidator;
import com.webif.divrep.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor;

public class UserFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(UserFormDE.class); 
    
    private Context context;
	private Authorization auth;
	private Integer id;
	
	private Text dn_string;
	private ContactEditor contact;
	private HashMap<Integer/*auth_type*/, CheckBoxFormElement> auth_types = new HashMap();
	
	public UserFormDE(Context _context, DNRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		dn_string = new Text(this);
		dn_string.setLabel("DN String");
		dn_string.setValue(rec.dn_string);
		dn_string.setRequired(true);

		new Static(this, "<h3>Contact Person Name</h3>");
		contact = new ContactEditor(this, new ContactModel(context), false, false);
		contact.setShowRank(false);
		contact.setMinContacts(ContactEditor.Rank.PRIMARY, 1);
		ContactModel cmodel = new ContactModel(context);
		ContactRecord crec = cmodel.get(rec.contact_id);
		contact.addSelected(crec, 1);//1 = is for primary (I know.. the api is not consistent with setMinContact() above)
		
		new Static(this, "<h3>Authorization Types</h3>");
		AuthorizationTypeModel atmodel = new AuthorizationTypeModel(context);
		DNAuthorizationTypeModel dnatmodel = new DNAuthorizationTypeModel(context);
		for(AuthorizationTypeRecord atrec : atmodel.getAll()) {
			CheckBoxFormElement elem = new CheckBoxFormElement(this);
			elem.setLabel(atrec.name);
			auth_types.put(atrec.id, elem);
		}
		if(id != null) {
			Collection<Integer> dnatrecs = dnatmodel.getAuthorizationTypesByDNID(rec.id);
			for(Integer auth_type : dnatrecs) {
				auth_types.get(auth_type).setValue(true);
			}
		}
	
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		
		//Construct OsgGridTypeRecord
		DNRecord rec = new DNRecord();
		rec.id = id;
		
		//just grab first contact record (always one contact per one dn)
		rec.dn_string = dn_string.getValue();
		Collection<ContactRecord> contact_recs = contact.getContactRecords().keySet();
		for(ContactRecord crec : contact_recs) {
			rec.contact_id = crec.id;
			break;
		}

		ArrayList<Integer/*auth_type*/> auths = new ArrayList();
		for(Integer auth_type : auth_types.keySet()) {
			CheckBoxFormElement elem = auth_types.get(auth_type);
			if(elem.getValue()) {
				auths.add(auth_type);
			}
		}
		
		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			DNModel model = new DNModel(context);
			if(rec.id == null) {
				model.insertDetail(rec, auths);
			} else {
				model.updateDetail(rec, auths);
			}
		} catch (Exception e) {
			log.error(e);
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
