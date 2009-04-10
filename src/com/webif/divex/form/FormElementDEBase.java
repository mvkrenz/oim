package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

abstract public class FormElementDEBase extends DivEx {
	
	//class used to render the parent div element (you can use it to render it in non-div-ish way like inline)
	//the derived element has to use this in order for it to actually take effect (of course)
	private ArrayList<String> classes = new ArrayList<String>();
	public void addClass(String _class) {
		classes.add(_class);
	}
	protected void renderClass(PrintWriter out) {
		out.write("class=\"");
		for(String _class : classes) {
			out.write(_class);
			out.write(" ");
		}
		out.write("\"");
	}
	
	protected FormElementDEBase(DivEx parent) {
		super(parent);
	}
	
	private Boolean valid = true;
	public Boolean isValid() { 
		validate();
		return valid; 
	}
	public void setValid(Boolean b) { valid = b; }
	abstract public void validate();
	
	private Boolean hidden = false;
	public Boolean isHidden() { return hidden; }
	public void setHidden(Boolean b) { hidden = b; }
	
	private Boolean disabled = false;
	public Boolean isDisabled() { return disabled; }
	public void setDisabled(Boolean b) { disabled = b; }
	
	private Boolean required = false;
	public Boolean isRequired() { return required; }
	public void setRequired(Boolean b) { required = b; }
}
