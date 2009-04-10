package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.validator.IFormElementValidator;

public class SelectFormElementDE extends FormElementDEBase 
{	
	protected String label;
	protected Integer value;
	protected String error;

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
			if (value != null) {
				if(v.compareTo(value) == 0) {
					selected = "selected=\"selected\"";
				}
			}
			out.print("<option value=\""+v+"\" "+selected+">"+StringEscapeUtils.escapeHtml(name)+"</option>");
		}
		out.print("</select>");
		if(isRequired()) {
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

	public void validate()
	{
		redraw();
		
		//if required, run RequiredValidator
		if(isRequired()) {
			if(value == null) {
				error = "Please select an item.";
				setValid(false);
				return;
			}
		}
		
		//then run the optional validation
		if(validator != null) {
			if(!validator.isValid(value)) {
				//bad..
				error = validator.getMessage();
				setValid(false);
				return;
			}
		}
		
		//all good..
		error = null;
		setValid(true);
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
