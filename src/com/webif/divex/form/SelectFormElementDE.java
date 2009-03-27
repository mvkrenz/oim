package com.webif.divex.form;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.validator.IFormElementValidator;

import edu.iu.grid.oim.view.divex.FormDE;

public class SelectFormElementDE extends DivEx implements IFormElementDE 
{	
	protected String label;
	protected Integer value;
	protected String error;
	
	private Boolean valid;
	protected Boolean required = false;
	protected IFormElementValidator<Integer> validator = null;
	
	HashMap<Integer, String> keyvalues;
	
	public SelectFormElementDE(DivEx parent, HashMap<Integer, String> _keyvalues) {
		super(parent);
		keyvalues = _keyvalues;
	}
	
	public String renderInside() 
	{
		String out = "";
		if(label != null) {
			out += "<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>";
		}
		out += "<select onchange='divex(\""+getNodeID()+"\", \"change\", this.value);'>";
		out += "<option value=\"\">(Please Select)</option>";

		for(Integer v : keyvalues.keySet()) {
			String name = keyvalues.get(v);
			String selected = "";
			if(v == value) {
				selected = "selected=selected";
			}
			out += "<option value=\""+v+"\" "+selected+">"+StringEscapeUtils.escapeHtml(name)+"</option>";
		}
		out += "</select>";
		if(required) {
			out += " * Required";
		}
		if(error != null) {
			out += "<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>";
		}
		return out;
	}
	
	public void setLabel(String _label) { label = _label; }
	public void setValidator(IFormElementValidator<Integer> _validator) { validator = _validator; }
	
	public void setValue(Integer _value)	
	{ 
		value = _value; 
	}
	public Integer getValue()
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
			if(value == null) {
				error = "Please select an item.";
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
	
	public void onEvent(Event event) {
		try {
			value = Integer.parseInt(event.getValue());
		} catch (NumberFormatException e) {
			value = null;
		}
		validate();
	}
}
