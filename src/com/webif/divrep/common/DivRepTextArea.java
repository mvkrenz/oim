package com.webif.divrep.common;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;

public class DivRepTextArea extends DivRepFormElement<String>  {
	
	private int width = 400;
	public void setWidth(int _width) { width = _width; }
	private int height = 100;
	public void setHeight(int _height) { height = _height; }
	
	public DivRepTextArea(DivRep parent) { 
		super(parent);
	}
	
	public void onEvent(DivRepEvent e) {
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
			out.print("<textarea id=\""+getNodeID()+"_input"+random+"\" style='height: "+height+"px; width: "+width+"px;' onchange='divrep(\""+getNodeID()+"\", event, this.value);' sample=\""+sample+"\">");
			out.print(StringEscapeUtils.escapeHtml(current_value));
			out.print("</textarea>");

			out.write("<script type=\"text/javascript\">\n");

			out.write("var input = $(\"#"+getNodeID()+"_input"+random+"\");\n");

			out.write("input.val(\""+StringEscapeUtils.escapeJavaScript(current_value)+"\");");
			out.write("if(input.val() == \"\") {\n");
			out.write(" input.addClass(\"divrep_sample\");\n");
			out.write(" input.val(input.attr('sample'));\n");
			out.write("}\n");
			
			out.write("input.focus(function() {");
			out.write("	if($(this).hasClass(\"divrep_sample\")) {");
			out.write(" 	this.value = \"\";");
			out.write("		$(this).removeClass(\"divrep_sample\");");
			out.write(" }");
			out.write("});");
			
			out.write("input.blur(function() {");
			out.write(" if(this.value == \"\") {");
			out.write("		$(this).addClass(\"divrep_sample\");");
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
