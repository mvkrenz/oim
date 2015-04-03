package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class GridServiceDetails extends ServiceDetailsContent {

	private DivRepCheckBox hidden;
	private DivRepTextBox uri_override;
	private DivRepTextBox sam_uri;
	
	public GridServiceDetails(DivRep _parent, ServiceRecord srec) {
		super(_parent);

		hidden = new DivRepCheckBox(this);
		hidden.setLabel("Hidden Service (Example: An internal gatekeeper inaccessible to the outside world)");
		
		uri_override = new DivRepTextBox(this);
		uri_override.setLabel("Service URI Override");
		uri_override.setSampleValue("some.host.edu:"+srec.port);
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
			}
		});
		
		if(srec.id.equals(1)) { //only for CE
			sam_uri = new DivRepTextBox(this);
			sam_uri.setLabel("SAM URI");
			sam_uri.setSampleValue("htcondor://$HOSTNAME:$PORT/$SCHEDDNAME?option1=val1&option2=val2");
			sam_uri.addValidator(new DivRepIValidator<String>(){
				String[] schemes = {"htcondor"};
				private org.apache.commons.validator.UrlValidator urlvalidator = new org.apache.commons.validator.UrlValidator(schemes);

				public Boolean isValid(String value) {
					return (urlvalidator.isValid(value));
				}
				
				public String getErrorMessage()
				{
					return "Please specify a valid htcondor URL in a format such as: htcondor://$HOSTNAME:$PORT/$SCHEDDNAME?option1=val1&option2=val2";
				}
			});
			
			//TODO - add validation based on Brian's recommendation
			/*
			HOSTNAME: Regexp: (?:(?:(?:(?:[a-zA-Z0-9][-a-zA-Z0-9]{0,61})?[a-zA-Z0-9])[.])*(?:[a-zA-Z][-a-zA-Z0-9]{0,61}[a-zA-Z0-9]|[a-zA-Z])[.]?)
			  - (just a regex for valid hostnames from http://stackoverflow.com/questions/1418423/the-hostname-regex)
			
			SCHEDDNAME: traditionally a hostname, but actually can be any valid string.  I'd suggest the following regexp: [-A-Za-z0-9.@_]{0,128}
			
			PORT: Optional (defaults to 9619), but should be a valid number: [0-9]{1,5}
			
			Options: Should match [-A-Za-z0-9_]+
			 */
		}
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
		if(sam_uri != null && values.containsKey("sam_uri")) {
			sam_uri.setValue(values.get("sam_uri"));
		}
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		uri_override.render(out);
		
		if(sam_uri != null) {
			out.write("<p class=\"muted pull-right\">* Only For HTCondorCE. Please see <a href=\"https://twiki.grid.iu.edu/bin/view/Documentation/Release3/InstallHTCondorCESAMURI\" target=\"_blank\">TWiki Doc</a> for more detail.</p>");
			sam_uri.render(out);
		}
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
		if(sam_uri != null) {
			map.put("sam_uri", sam_uri.getValue());	
		}
		
		return map;
	}

	@Override
	public boolean validate() {
		boolean valid = true;
		valid &= uri_override.validate();
		if(sam_uri != null) {
			valid &= sam_uri.validate();
		}
		return valid;
	}
}
