package com.webif.divex.form;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.Event;
import com.webif.divex.DivEx;

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
		value = ((String)e.value).trim();
		modified(true);
		validate();
	}
	
	public void render(PrintWriter out) {
		out.write("<div ");
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
			String sample = StringEscapeUtils.escapeHtml(sample_value);
			if(sample_value == null) {
				sample = "";
			}
			int random = (int)(Math.random()*10000);
			out.print("<textarea id=\""+getNodeID()+"_input"+random+"\" style='width: "+width+"px;' onchange='divex(\""+getNodeID()+"\", event, this.value);' sample=\""+sample+"\">");
			out.print(StringEscapeUtils.escapeHtml(current_value));
			out.print("</textarea>");

			out.write("<script type=\"text/javascript\">\n");

			out.write("var input = $(\"#"+getNodeID()+"_input"+random+"\");\n");

			out.write("input.val(\""+StringEscapeUtils.escapeJavaScript(current_value)+"\");");
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
				out.print(" * Required");
			}
			error.render(out);
		}
		out.print("</div>");
	}
}
