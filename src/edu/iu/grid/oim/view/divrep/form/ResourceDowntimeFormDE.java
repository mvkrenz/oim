package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.divrep.common.DivRepForm;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel.ResourceDowntime;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.view.divrep.ResourceDowntimeEditor;

public class ResourceDowntimeFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ResourceDowntimeFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer resource_id;
	private Integer downtime_id;
	
	private ResourceDowntimeEditor editor;
	
	public ResourceDowntimeFormDE(Context _context, String origin_url, Integer rid, Integer did, TimeZone _timezone) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		resource_id = rid;		
		downtime_id = did;
		
		ResourceDowntimeRecord downtime_rec;
		if(downtime_id == null) {
			//new record
			downtime_rec = new ResourceDowntimeRecord();
			downtime_rec.resource_id = resource_id;
		} else {
			//edit record
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);	
			downtime_rec = dmodel.get(downtime_id);
		}
		
		editor = new ResourceDowntimeEditor(this, downtime_rec, context, auth, _timezone);
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		ResourceDowntimeModel model = new ResourceDowntimeModel(context);
		try {
			ResourceDowntime info = editor.getResourceDowntime(model);
			if(downtime_id == null) {
				model.insertDowntime(info);
			} else {
				model.updateDowntime(info);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}
}
