package edu.iu.grid.oim.view.form;

import org.apache.commons.lang.StringEscapeUtils;

public class TextFormElement extends FormElementBase {
	
	public TextFormElement(String _name, String _label)
	{
		super(_name, _label);
	}
	
	public String toHTML() {
		String out = "";
		out += "<span>Name:</span>";
		out += "<div>";
		out += "<input type=\"edit\" name=\"name\" value=\""+
				StringEscapeUtils.escapeHtml(value)+"\"></input>";
		out += "<p class=\"error\">" + error + "</p>";
		out += "</div>";
		return out;
	}
}
