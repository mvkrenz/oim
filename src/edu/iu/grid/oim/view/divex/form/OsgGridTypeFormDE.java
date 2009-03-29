package edu.iu.grid.oim.view.divex.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE;
import edu.iu.grid.oim.view.divex.FormDE;

public class OsgGridTypeFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(OsgGridTypeFormDE.class); 
    
    protected Connection con = null;
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextAreaFormElementDE description;
	
	public OsgGridTypeFormDE(DivEx parent, OsgGridTypeRecord rec, String origin_url, Connection _con, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		con = _con;
		auth = _auth;
	
		//new StaticDE(this, "<h2>Details</h2>");
		
		id = rec.id;

		//pull osg_grid_types for unique validator
		HashMap<Integer, String> osg_grid_types = getOsgGridTypes();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			osg_grid_types.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.setValidator(new UniqueValidator<String>(osg_grid_types.values()));
		name.setRequired(true);
		
		description = new TextAreaFormElementDE(this);
		description.setLabel("Description");
		description.setValue(rec.description);
		description.setRequired(true);

	}
	
	private HashMap<Integer, String> getOsgGridTypes() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		OsgGridTypeModel model = new OsgGridTypeModel(con, auth);
		HashMap<Integer, OsgGridTypeRecord> osg_grid_types = model.getAll();
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		for(OsgGridTypeRecord rec : osg_grid_types.values()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		
		//Construct OsgGridTypeRecord
		OsgGridTypeRecord rec = new OsgGridTypeRecord();
		rec.id = id;
		rec.name = name.getValue();
		rec.description = description.getValue();

		//Do insert / update to our DB
		try {
			OsgGridTypeModel model = new OsgGridTypeModel(con, auth);
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
