package com.webif.divex.form;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.Event;
import com.webif.divex.DivEx;
import com.webif.divex.form.validator.IFormElementValidator;
import com.webif.divex.form.validator.RequiredValidator;

public class TextFormElementDE extends DivEx implements IFormElementDE {
	
	protected String label;
	protected String value;
	protected String error;
	
	private int width = 400;
	public void setWidth(int _width)
	{
		width = _width;
	}
	
	private Boolean valid;
	private Boolean hidden = false;
	protected Boolean required = false;
	
	protected IFormElementValidator<String> validator = null;
	
	public TextFormElementDE(DivEx parent) {
		super(parent);
	}
	
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		if(label != null) {
			out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
		}
		String current_value = value;
		if(value == null) {
			current_value = "";
		}
		out.print("<input type='text' style='width: "+width+"px;' onchange='divex(\""+getNodeID()+"\", \"change\", this.value);' value=\""+StringEscapeUtils.escapeHtml(current_value)+"\"/>");
		if(required) {
			out.print(" * Required");
		}
		if(error != null) {
			out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
		}
		out.print("</div>");
	}

	public void setLabel(String _label) { label = _label; }
	public void setValidator(IFormElementValidator<String> _validator) { validator = _validator; }
	public void setValue(String _value)	
	{ 
		value = _value; 
	}
	public String getValue()
	{
		return value;
	}
	public void setRequired(Boolean b) {
		required = b;
	}
	
	public Boolean isValid()
	{
		validate();
		return valid;
	}
	
	public void validate()
	{
		redraw();
		
		//if required, run RequiredValidator
		if(required == true) {
			RequiredValidator req = RequiredValidator.getInstance();
			if(value == null || !req.isValid(value)) {
				error = req.getMessage();
				valid = false;
				return;
			}
		}
		//then run the optional validation
		if(validator != null) {
			if(!validator.isValid(value)) {
				//bad..
				error = validator.getMessage();
				valid = false;
				return;
			}
		}
		
		//all good..
		error = null;
		valid = true;
	}
	
	public void onEvent(Event e) {
		value = e.getValue().trim();
		validate();
	}
	
	public void setHidden(Boolean _hidden)
	{
		hidden = _hidden;
	}
	public Boolean isHidden() {
		return hidden;
	}
}
