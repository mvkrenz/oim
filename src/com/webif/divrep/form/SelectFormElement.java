package com.webif.divrep.form;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.Event;

public class SelectFormElement extends FormElementBase<Integer>
{		
	HashMap<Integer, String> keyvalues;
	
	public SelectFormElement(DivRep parent, HashMap<Integer, String> _keyvalues) {
		super(parent);
		keyvalues = _keyvalues;
	}
	
	public void render(PrintWriter out) 
	{
		out.write("<div ");
		renderClass(out);
		out.write("id=\""+getNodeID()+"\">");
		if(!hidden) {
			if(label != null) {
				out.write("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
			}
			int random = (int)(Math.random()*10000);
			out.write("<select id='"+random+"' onchange='divrep(\""+getNodeID()+"\", event, this.value);'>");
			out.write("<option value=\"\">(Please Select)</option>");
			
			for(Integer v : keyvalues.keySet()) {
				String name = keyvalues.get(v);
				String selected = "";
				if (value != null) {
					if(v.compareTo(value) == 0) {
						selected = "selected=\"selected\"";
					}
				}
				out.write("<option value=\""+v+"\" "+selected+">"+StringEscapeUtils.escapeHtml(name)+"</option>");
			}
			out.print("</select>");
			if(isRequired()) {
				out.write(" * Required");
			}
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void onEvent(Event event) {
		try {
			value = Integer.parseInt((String)event.value);
		} catch (NumberFormatException e) {
			value = null;
		}
		modified(true);
		validate();
	}
}
