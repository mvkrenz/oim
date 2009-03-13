package edu.iu.grid.oim.view.form;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

public class SelectFormElement extends FormElementBase 
{	
	String value;
	HashMap<String, String> keyvalues;
	
	public void constructor(String _value, HashMap<String, String> _keyvalues) 
	{
		value = _value;
		keyvalues = _keyvalues;	
	}
	public SelectFormElement(String _name, String _label, Integer _value, HashMap<String, String> _keyvalues)
	{
		super(_name, _label);
		
		String _value_str;
		if(_value == null) {
			_value_str = "-1";
		} else {
			_value_str = _value.toString();
		}
		constructor(_value_str, _keyvalues);
	}
	
	public SelectFormElement(String _name, String _label, String _value, HashMap<String, String> _keyvalues)
	{
		super(_name, _label);
		if(value == null) {
			value = "-1";
		}	
		constructor(_value, _keyvalues);	
	}
	
	public String toHTML() {
		String out = "";
		out += "<span>"+label+":</span>";
		out += "<div>";
		out += "<select name=\""+name+"\">";
		for(String k : keyvalues.keySet()) {
			String v = keyvalues.get(k);
			String selected = "";
			if(v.compareTo(value) == 0) {
				selected = "selected=selected";
			}
			out += "<option value=\""+k+"\" "+selected+">"+v+"</option>";
		}
		out += "</select>";
		out += "</div>";
		return out;
	}
}
