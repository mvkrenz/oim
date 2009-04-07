package edu.iu.grid.oim.view.divex.form;

import java.sql.SQLException;
import java.util.HashMap;

import com.webif.divex.DivEx;
import com.webif.divex.form.FormDE;
import com.webif.divex.form.SelectFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.notification.NotificationBase;
import edu.iu.grid.oim.notification.VONotification;

public class NotificationFormDE extends FormDE {
	
	FormDE notification_form;
	private SelectFormElementDE notification_type;
	HashMap<Integer, NotificationBase> notifications = new HashMap();
	
	public NotificationFormDE(DivEx parent, NotificationRecord rec, String origin_url, Authorization _auth) throws AuthorizationException, SQLException
	{	
		super(parent, origin_url);
		
		//list all of our notification classes
		notifications.put(0, new VONotification());
		
		HashMap<Integer, String> options = new HashMap();
		for(Integer id : notifications.keySet()) {
			NotificationBase inst = notifications.get(id);
			options.put(id, inst.getTitle());

		}
		notification_type = new SelectFormElementDE(this, options);
		notification_type.setRequired(true);
	}
	
	protected Boolean doSubmit() {
		// TODO Auto-generated method stub
		return null;
	}

}
