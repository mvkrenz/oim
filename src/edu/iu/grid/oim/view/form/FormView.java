package edu.iu.grid.oim.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.DivEx;

import edu.iu.grid.oim.view.View;

public class FormView implements View 
{
	String action;
	HashMap<String, String> validation_errors;
	
	private ArrayList<FormElementBase> elements = new ArrayList<FormElementBase>();
	
	public FormView(String _action, HashMap<String, String> _validation_errors) {
		action = _action;
		validation_errors = _validation_errors;
	}
	
	public void add(FormElementBase v) {
		elements.add(v);
	}

	public String toHTML() {
		String out = "<form action=\""+action+"\" method=\"post\">\n";
		
		//output form elements
		for(FormElementBase v : elements) {
			out += v.toHTML();
			
			//has validation error?
			String name = v.getName();
			if(validation_errors.containsKey(name)) {
				String error = validation_errors.get(name);
				out += "<p class=\"error\">" + error + "</p>";
			}
		}
		
		out += "<input type=\"submit\" name=\"Update\"></input>";
		out += "</form>\n";
		return out;
	}

}
