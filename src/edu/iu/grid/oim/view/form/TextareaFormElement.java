package edu.iu.grid.oim.view.form;

import org.apache.commons.lang.StringEscapeUtils;

public class TextareaFormElement extends FormElementBase 
{	
	String value;
	
	public TextareaFormElement(String _name, String _label, String _value)
	{
		super(_name, _label);
		value = _value;
		if(value == null) {
			value = "";
		}
	}
	
	public String toHTML() {
		String out = "";
		out += "<span>"+label+":</span>";
		out += "<div>";
		out += "<textarea name=\""+name+"\">"+ StringEscapeUtils.escapeHtml(value) + "</textarea>";
		out += "</div>";
		return out;
	}
}
