package com.webif.divex.form;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.ChangeEvent;
import com.webif.divex.DivEx;
import com.webif.divex.form.validator.IFormElementValidator;
import com.webif.divex.form.validator.RequiredValidator;

import edu.iu.grid.oim.view.divex.FormDivex;

public class TextFormElementDE extends DivEx implements IFormElementDE {
	
	protected String label;
	protected String value;
	protected String error;
	
	private Boolean valid;
	protected Boolean required = false;
	
	protected IFormElementValidator<String> validator = null;
	
	public TextFormElementDE(DivEx parent) {
		super(parent);
	}
	
	public String renderInside() {
		String html = "";
		if(label != null) {
			html += "<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>";
		}
		String current_value = value;
		if(value == null) {
			current_value = "";
		}
		html += "<input type='text' onblur='divex_change(\""+getNodeID()+"\", this.value);' value=\""+StringEscapeUtils.escapeHtml(current_value)+"\"/>";
		if(required) {
			html += " * Required";
		}
		if(error != null) {
			html += "<p class='elementerror'>"+StringEscapeUtils.escapeHtml(error)+"</p>";
		}
		html += "</input>";
		return html;
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
	
	public void onChange(ChangeEvent e) {
		value = e.newvalue;
		validate();
	}
}
