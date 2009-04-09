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
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;

public class CpuInfoFormDE extends FormDE 
{
    static Logger log = Logger.getLogger(CpuInfoFormDE.class); 
    
    protected Connection con = null;
	protected Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE normalization_constant;
	private TextAreaFormElementDE notes;
	
	public CpuInfoFormDE(DivEx parent, CpuInfoRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		auth = _auth;
		
		id = rec.id;

		//pull osg_grid_types for unique validator
		HashMap<Integer, String> cpu_infos = getCpuInfos();
		if(id != null) {
			//if doing update, remove my own name (I can use my own name)
			cpu_infos.remove(id);
		}
		name = new TextFormElementDE(this);
		name.setLabel("Name");
		name.setValue(rec.name);
		name.setValidator(new UniqueValidator<String>(cpu_infos.values()));
		name.setRequired(true);
		
		normalization_constant = new TextFormElementDE(this);
		normalization_constant.setLabel ("Normalization Constant"); 
		normalization_constant.setValue(String.valueOf(rec.normalization_constant));
		normalization_constant.setRequired(true);
		
		notes = new TextAreaFormElementDE(this);
		notes.setLabel("Notes");
		notes.setValue(rec.notes);
		notes.setRequired(false);
	}
	
	private HashMap<Integer, String> getCpuInfos() throws AuthorizationException, SQLException
	{
		//pull all OsgGridTypes
		HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
		CpuInfoModel model = new CpuInfoModel(auth);
		for(CpuInfoRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() {
		
		//Construct OsgGridTypeRecord
		CpuInfoRecord rec = new CpuInfoRecord();
		rec.id = id;
		rec.name = name.getValue();
		rec.normalization_constant = Float.valueOf(normalization_constant.getValue());
		rec.notes = notes.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			CpuInfoModel model = new CpuInfoModel(auth);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
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

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
