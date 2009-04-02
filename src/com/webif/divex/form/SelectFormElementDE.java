package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.validator.IFormElementValidator;

public class SelectFormElementDE extends DivEx implements IFormElementDE 
{	
	protected String label;
	protected Integer value;
	protected String error;
	
	private Boolean valid;
	private Boolean hidden = false;
	
	protected Boolean required = false;
	protected IFormElementValidator<Integer> validator = null;
	
	HashMap<Integer, String> keyvalues;
	
	public SelectFormElementDE(DivEx parent, HashMap<Integer, String> _keyvalues) {
		super(parent);
		keyvalues = _keyvalues;
	}
	
	public void render(PrintWriter out) 
	{
		out.print("<div id=\""+getNodeID()+"\">");
		if(label != null) {
			out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
		}
		out.print("<select onchange='divex(\""+getNodeID()+"\", \"change\", this.value);'>");
		out.print("<option value=\"\">(Please Select)</option>");

		for(Integer v : keyvalues.keySet()) {
			String name = keyvalues.get(v);
			String selected = "";
			if(v == value) {
				selected = "selected=\"selected\"";
			}
			out.print("<option value=\""+v+"\" "+selected+">"+StringEscapeUtils.escapeHtml(name)+"</option>");
		}
		out.print("</select>");
		if(required) {
			out.print(" * Required");
		}
		if(error != null) {
			out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
		}
		out.print("</div>");
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
	
	public void setHidden(Boolean _hidden)
	{
		hidden = _hidden;
	}
	public Boolean isHidden() {
		return hidden;
	}
}
