package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class CVMFSServiceDetails extends ServiceDetailsContent {

	private DivRepTextBox endpoint;
	private DivRepTextArea repo_config;
	
	public CVMFSServiceDetails(DivRep _parent, ServiceRecord srec) {
		super(_parent);
		
		endpoint = new DivRepTextBox(this);
		endpoint.setLabel("Service Endpoint");
		endpoint.setSampleValue("some.host.edu/something");
		endpoint.setRequired(true);
		
		repo_config = new DivRepTextArea(this);
		repo_config.setLabel("Repository Configuration");
		repo_config.setSampleValue("(configuration for list of repository that it monitors)");
		
		/*
		endpoint.addValidator(new DivRepIValidator<String>() {
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
		*/
	}
	
	public void setValues(HashMap<String, String> values) {
		if(values.containsKey("endpoint")) {
			endpoint.setValue(values.get("endpoint"));
		}
		if(values.containsKey("repo_config")) {
			repo_config.setValue(values.get("repo_config"));
		}
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		endpoint.render(out);
		repo_config.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();		
		map.put("endpoint", endpoint.getValue());
		map.put("repo_config", repo_config.getValue());	
		return map;
	}

	@Override
	public boolean validate() {
		return repo_config.validate() && endpoint.validate();
	}
}
