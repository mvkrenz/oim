package com.webif.divrep.form;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.Event;

public class TextFormElement extends FormElementBase<String> {
	
	private int width = 400;
	public void setWidth(int _width)
	{
		width = _width;
	}
	
	public TextFormElement(DivRep parent) {
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
			
			String current_value = StringEscapeUtils.escapeHtml(value);
			if(value == null) {
				current_value = "";
			}
			String sample = StringEscapeUtils.escapeHtml(sample_value);
			if(sample_value == null) {
				sample = "";
			}
			int random = (int)(Math.random()*10000);
			out.write("<input id=\""+getNodeID()+"_input"+random+"\" type=\"text\" style=\"width: "+width+"px;\" onchange=\"divrep('"+getNodeID()+"', event, this.value);\" sample=\""+sample+"\"/>");
			out.write("<script type=\"text/javascript\">\n");

				out.write("var input = $(\"#"+getNodeID()+"_input"+random+"\");\n");

				out.write("input.val(\""+current_value+"\");");
				out.write("if(input.val() == \"\") {\n");
				out.write(" input.addClass(\"sample\");\n");
				out.write(" input.val(input.attr('sample'));\n");
				out.write("}\n");
				
				out.write("input.focus(function() {");
				out.write("	if($(this).hasClass(\"sample\")) {");
				out.write(" 	this.value = \"\";");
				out.write("		$(this).removeClass(\"sample\");");
				out.write(" }");
				out.write("});");
				
				out.write("input.blur(function() {");
				out.write(" if(this.value == \"\") {");
				out.write("		$(this).addClass(\"sample\");");
				out.write(" 	$(this).val($(this).attr('sample'));");
				out.write(" }");
				out.write("});");
			out.write("</script>");
			
			if(isRequired()) {
				out.write(" * Required");
			}
			error.render(out);
		}
		out.write("</div>");
	}
	
	public void onEvent(Event e) {
		value = ((String)e.value).trim();
		modified(true);
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
