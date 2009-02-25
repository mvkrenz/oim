package edu.iu.grid.oim.view.form;

import java.util.ArrayList;

import com.webif.divex.DivEx;

import edu.iu.grid.oim.view.View;

public class FormView implements View {

	String action;
	
	private ArrayList<FormElementBase> elements = new ArrayList<FormElementBase>();
	
	public FormView(String _action) {
		action = _action;
	}
	public void add(FormElementBase v) {
		elements.add(v);
	}

	public String toHTML() {
		String out = "<form action=\""+action+"\" method=\"post\">\n";
		
		//output form elements
		for(FormElementBase v : elements) {
			out += v.toHTML();
		}
		
		out += "<input type=\"submit\" name=\"Update\"></input>";
		out += "</form>\n";
		return out;
	}

}
