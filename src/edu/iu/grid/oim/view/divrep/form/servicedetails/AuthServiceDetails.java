package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class AuthServiceDetails extends ServiceDetailsContent {

	public AuthServiceDetails(DivRep _parent, ServiceRecord srec) {
		super(_parent);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\"");
		out.write("<p>No custom parameters requested for Auth service group yet.</p>");
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValues(HashMap<String, String> values) {
		//nothing to set
		
	}

	@Override
	public HashMap<String, String> getValues() {
		//nothing to get
		return null;
	}

	@Override
	public boolean validate() {
		//nothing to validate
		return true;
	}
}
