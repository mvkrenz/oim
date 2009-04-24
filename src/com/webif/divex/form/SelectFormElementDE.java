package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

public class SelectFormElementDE extends FormElementDEBase<Integer>
{		
	HashMap<Integer, String> keyvalues;
	
	public SelectFormElementDE(DivEx parent, HashMap<Integer, String> _keyvalues) {
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
			//FireFox has a bug where it select box doesn't have "name" attribute, it will misbehave when page is redrawn
			out.write("<select name='"+getNodeID()+"_select' onchange='console.log(\"hello\");divex(\""+getNodeID()+"\", event, this.value);'>");
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
			if(error != null) {
				out.write("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
			}
		}
		out.print("</div>");
	}
	
	public void onEvent(Event event) {
		try {
			value = Integer.parseInt((String)event.value);
		} catch (NumberFormatException e) {
			value = null;
		}
		validate();
	}
}
