package com.webif.divex.form;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.Event;
import com.webif.divex.DivEx;

public class TextFormElementDE extends FormElementDEBase<String> {
	
	private int width = 400;
	public void setWidth(int _width)
	{
		width = _width;
	}
	
	public TextFormElementDE(DivEx parent) {
		super(parent);
	}
	
	public void render(PrintWriter out) {
		out.print("<div ");
		renderClass(out);
		out.write("id=\""+getNodeID()+"\">");
		if(!hidden) {
			if(label != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
			}
			String current_value = value;
			if(value == null) {
				current_value = "";
			}
			out.print("<input type='text' style='width: "+width+"px;' onchange='divex(\""+getNodeID()+"\", event, this.value);' value=\""+StringEscapeUtils.escapeHtml(current_value)+"\"/>");
			if(isRequired()) {
				out.print(" * Required");
			}
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void onEvent(Event e) {
		value = ((String)e.value).trim();
		validate();
	}
	
	public Double getValueAsDouble()
	{
		if(value == null) return null;
		if(value.length() == 0) return null;
		try {
			Double d = Double.parseDouble(value);
			return d;
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
