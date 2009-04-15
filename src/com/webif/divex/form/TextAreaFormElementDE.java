package com.webif.divex.form;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.Event;
import com.webif.divex.DivEx;
import com.webif.divex.form.validator.IFormElementValidator;
import com.webif.divex.form.validator.RequiredValidator;

public class TextAreaFormElementDE extends FormElementDEBase<String>  {
	
	private int width = 400;
	public void setWidth(int _width)
	{
		width = _width;
	}
	
	public TextAreaFormElementDE(DivEx parent) { 
		super(parent);
	}
	
	public void onEvent(Event e) {
		value = e.getValue().trim();
		validate();
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
		out.print("<textarea style='width: "+width+"px;' onchange='divex(\""+getNodeID()+"\", event, this.value);'>");
		out.print(StringEscapeUtils.escapeHtml(current_value));
		out.print("</textarea>");
		if(isRequired()) {
			out.print(" * Required");
		}
		if(error != null) {
			out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
		}
		out.print("</div>");
		/*
		out.print("<script type=\"text/javascript\">");
		out.print("$('#"+getNodeID()+" textarea').TextAreaResizer();");
		out.print("</script>");
		*/
	}
}
