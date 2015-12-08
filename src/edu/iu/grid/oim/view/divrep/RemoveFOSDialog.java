package edu.iu.grid.oim.view.divrep;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRep;

import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;

public class RemoveFOSDialog extends BootDialogForm {
	static Logger log = Logger.getLogger(RemoveFOSDialog.class);  
	FieldOfScienceRecord rec;
	UserContext context;
	
	public RemoveFOSDialog(DivRep parent, UserContext context) {
		super(parent);
		this.context = context;
		//setHasCancelButton(true);
		setTitle("Remove Field of Science");
		setSubmitLabel("Remove");
		new DivRepStaticContent(this, "Do you really want to remove this field of science?");
	}
	public void setRecord(FieldOfScienceRecord rec) {
		this.rec = rec;
		setTitle("Remove: "+rec.name);
	}

	@Override
	protected boolean doSubmit() {
		FieldOfScienceModel model = new FieldOfScienceModel(context);
		try {
			model.remove(rec);
			redirect("fieldofscience");
			return true;
		} catch (SQLException e) {
			alert(e.toString());
			log.error("Failed to remove", e);
			return false;
		}
	}
}