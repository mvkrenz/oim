package edu.iu.grid.oim.view.divrep.form;


import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.webif.divrep.Event;
import com.webif.divrep.form.FormBase;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.view.divrep.ResourceDowntimeEditor;

public class ResourceDowntimeFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(ResourceDowntimeFormDE.class); 
    
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private ResourceDowntimeEditor downtime;
	
	public ResourceDowntimeFormDE(Context _context, String origin_url, int resource_id) throws AuthorizationException, SQLException
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = resource_id;		
		
		downtime = new ResourceDowntimeEditor(this, context, resource_id);
	}
	
	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		ResourceDowntimeModel model = new ResourceDowntimeModel(context);
		try {
			model.updateDetail(id, downtime.getResourceDowntimes());
		} catch (Exception e) {
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
