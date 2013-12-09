package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class PerfsonarServiceDetails extends ServiceDetailsContent {

	private DivRepTextBox endpoint;
	
	public PerfsonarServiceDetails(DivRep _parent, ServiceRecord srec) {
		super(_parent);
		
		endpoint = new DivRepTextBox(this);
		endpoint.setLabel("Service Endpoint");
		endpoint.setSampleValue("http://perfsonar.example.org");
		endpoint.setRequired(true);
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
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		endpoint.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();		
		map.put("endpoint", endpoint.getValue());
		
		return map;
	}

	@Override
	public boolean validate() {
		return endpoint.validate();
	}
}
