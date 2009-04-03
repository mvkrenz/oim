package com.webif.divex.form;

import java.io.PrintWriter;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

import org.apache.commons.lang.StringEscapeUtils;

public class CheckBoxFormElementDE extends FormElementDEBase {

	private String label;
	private Boolean value = false;
	
	public CheckBoxFormElementDE(DivEx parent) {
		super(parent);
	}
	
	public void render(PrintWriter out) {
		String checked = "";
		if(value == true) { //value should never be null by design
			checked = "checked=checked";
		}
		out.print("<div id=\""+getNodeID()+"\">");
		out.print("<input type='checkbox' onchange='divex(\""+getNodeID()+"\", \"change\", this.checked);' "+checked+"/>");
		if(label != null) {
			out.print(" <label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
		}
		out.print("</div>");
	}

	public void setLabel(String _label) { label = _label; }
	public void setValue(Boolean _value)	
	{ 
		if(_value == null) {
			//I went back and forth if I should throw exception, but since checkbox is the one
			//that deviates from the rest that handles "null state", so let's ignore it..
			return;
		}
		value = _value; 
	}
	public Boolean getValue()
	{
		return value;
	}
	
	public void onEvent(Event e) {
		if(e.getValue().compareTo("true") == 0) {
			value = true;
		} else {
			value = false;
		}
	}
	
	public void validate() {
		//nothing to validate on checkbox
	}
}
