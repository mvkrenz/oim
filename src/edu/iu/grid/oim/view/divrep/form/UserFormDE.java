package edu.iu.grid.oim.view.divrep.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
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

public class UserFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(UserFormDE.class); 
    
    private UserContext context;
	private Authorization auth;
	private Integer id;
	
	private DivRepTextBox dn_string;
	private ContactEditor contact;
	private HashMap<Integer/*auth_type*/, DivRepCheckBox> auth_types = new HashMap();
	//private Integer usercert_request_id;
	
	public UserFormDE(UserContext _context, DNRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		dn_string = new DivRepTextBox(this);
		dn_string.setLabel("DN String");
		dn_string.setValue(rec.dn_string);
		dn_string.setRequired(true);

		new DivRepStaticContent(this, "<h3>Contact's Name</h3>");
		contact = new ContactEditor(this, new ContactModel(context), false, false);
		contact.setShowRank(false);
		contact.setMinContacts(ContactEditor.Rank.Primary, 1);
		ContactModel cmodel = new ContactModel(context);
		if (id != null) {
			ContactRecord crec = cmodel.get(rec.contact_id);
			contact.addSelected(crec, 1);//1 = is for primary (I know.. the api is not consistent with setMinContact() above)
		}
		
		new DivRepStaticContent(this, "<h3>Authorization Types</h3>");
		AuthorizationTypeModel atmodel = new AuthorizationTypeModel(context);
		DNAuthorizationTypeModel dnatmodel = new DNAuthorizationTypeModel(context);
		for(AuthorizationTypeRecord atrec : atmodel.getAll()) {
			if(atrec.id == 0) continue; //ignore guest
			DivRepCheckBox elem = new DivRepCheckBox(this);
			elem.setLabel(atrec.name);
			auth_types.put(atrec.id, elem);
		}
		if(id != null) {
			Collection<Integer> dnatrecs = dnatmodel.getAuthorizationTypesByDNID(rec.id);
			for(Integer auth_type : dnatrecs) {
				auth_types.get(auth_type).setValue(true);
			}
		}
		
		//for now, let's just pass through
		//usercert_request_id = rec.usercert_request_id;
	
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
		
		//passthru
		//rec.usercert_request_id = usercert_request_id;

		ArrayList<Integer/*auth_type*/> auths = new ArrayList();
		for(Integer auth_type : auth_types.keySet()) {
			DivRepCheckBox elem = auth_types.get(auth_type);
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
				//create footprint ticket
				Footprints fp = new Footprints(context);
			} else {
				model.updateDetail(rec, auths);
			}
		} catch (Exception e) {
			log.error(e);
			alert(e.getMessage());
			ret = false;
		}
		//context.close();
		return ret;
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
