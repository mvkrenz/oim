package edu.iu.grid.oim.view.divex.form;

import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divex.Event;
import com.webif.divex.form.FormDEBase;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.DoubleValidator;
import com.webif.divex.form.validator.IntegerValidator;
import com.webif.divex.form.validator.UniqueValidator;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;

public class CpuInfoFormDE extends FormDEBase 
{
    static Logger log = Logger.getLogger(CpuInfoFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElementDE name;
	private TextFormElementDE normalization_constant;
	private TextAreaFormElementDE notes;
	
	public CpuInfoFormDE(Context _context, CpuInfoRecord rec, String origin_url) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
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
		name.addValidator(new UniqueValidator<String>(cpu_infos.values()));
		name.setRequired(true);
		
		normalization_constant = new TextFormElementDE(this);
		normalization_constant.setLabel ("Normalization Constant"); 
		normalization_constant.setValue(String.valueOf(rec.normalization_constant));
		normalization_constant.addValidator(new DoubleValidator());
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
		CpuInfoModel model = new CpuInfoModel(context);
		for(CpuInfoRecord rec : model.getAll()) {
			keyvalues.put(rec.id, rec.name);
		}
		return keyvalues;
	}
	
	protected Boolean doSubmit() 
	{	
		Boolean ret = true;
		CpuInfoRecord rec = new CpuInfoRecord();
		rec.id = id;
		rec.name = name.getValue();
		rec.normalization_constant = Float.valueOf(normalization_constant.getValue());
		rec.notes = notes.getValue();

		//Do insert / update to our DB
		try {
			auth.check("admin");
			
			CpuInfoModel model = new CpuInfoModel(context);
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
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
