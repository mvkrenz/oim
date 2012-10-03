package edu.iu.grid.oim.view.divrep;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRep;

import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;

public class RemoveDowntimeDialog extends DivRepDialog {
	static Logger log = Logger.getLogger(RemoveDowntimeDialog.class);  
	ResourceDowntimeRecord rec;
	UserContext context;
	
	public RemoveDowntimeDialog(DivRep parent, UserContext context) {
		super(parent);
		this.context = context;
		setHasCancelButton(true);
		setTitle("Remove Downtime");
		new DivRepStaticContent(this, "Do you really want to remove this downtime?");
	}
	public void setRecord(ResourceDowntimeRecord rec) {
		this.rec = rec;
	}
	public void onCancel() {
		close();
	}

	public void onSubmit() {
		ResourceDowntimeModel model = new ResourceDowntimeModel(context);
		try {
			model.disableDowntime(rec);
		} catch (SQLException e) {
			alert(e.toString());
			log.error("Failed to remove", e);
		}
		redirect("resourcedowntime");
	}
}