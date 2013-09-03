package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class GridServiceDetails extends ServiceDetailsContent {

	private DivRepCheckBox hidden;
	private DivRepTextBox uri_override;
	
	public GridServiceDetails(DivRep _parent, ServiceRecord srec) {
		super(_parent);

		hidden = new DivRepCheckBox(this);
		hidden.setLabel("Hidden Service (Example: An internal gatekeeper inaccessible to the outside world)");
		
		uri_override = new DivRepTextBox(this);
		uri_override.setLabel("Service URI Override");
		uri_override.setSampleValue("some.host.edu:"+srec.port);
		//uri_override.setRequired(true);
		uri_override.addValidator(new DivRepIValidator<String>() {
			String message;
			public String getErrorMessage() {
				return message;
			}
			public Boolean isValid(String value) {
				//split the value into 2 segments
				String[]parts = value.split(":");
				if(parts.length != 1 && parts.length != 2) {
					message = "Please enter override in the form such as \"resource123.iu.edu:2119\"";
					return false;
				}
				/* -- not sure we have this validation
				//validate the url
				String url = parts[0];
				if(!parent.isValidResourceFQDN(url)) {
					message = "Please use FQDN of this resource, or one of FQDN aliases.";
					return false;
				}
				*/
				
				//validate port
				if(parts.length == 2) {
					try {
						Integer port = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						message = "The port number is invalid.";
						return false;
					}
				}
				
				return true;
			}});
	}
	
	public void setValues(HashMap<String, String> values) {
		if(values.containsKey("hidden")) {
			if(values.get("hidden").equals("True")) {
				hidden.setValue(true);
			}
		}
		if(values.containsKey("uri_override")) {
			uri_override.setValue(values.get("uri_override"));
		}
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		uri_override.render(out);
		hidden.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		if(hidden.getValue()) {
			map.put("hidden", "True");
		} else {
			map.put("hidden", "False");
		}
		
		map.put("uri_override", uri_override.getValue());
		
		return map;
	}

	@Override
	public boolean validate() {
		return uri_override.validate();
	}
}
