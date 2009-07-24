package com.divrep.common;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

public class DivRepSelectBox extends DivRepFormElement<Integer>
{		
	TreeMap<String, TreeMap<Integer, String>> keyvalues_groups;
	
	public DivRepSelectBox(DivRep parent, TreeMap<Integer, String> keyvalues) {
		super(parent);
		keyvalues_groups = new TreeMap<String, TreeMap<Integer, String>>();
		keyvalues_groups.put("", keyvalues);
	}
	
	public DivRepSelectBox(DivRep parent)
	{
		super(parent);
		keyvalues_groups = new TreeMap<String, TreeMap<Integer, String>>();
	}
	public void addGroup(String name, TreeMap<Integer, String> keyvalues) {
		keyvalues_groups.put(name, keyvalues);
	}
	
	//show (Please Select) item with null value
	private Boolean hasnull = true;
	public void setHasNull(Boolean b) { hasnull = b; }
	
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
			if(hasnull) {
				out.write("<option value=\"\">(Please Select)</option>");
			}
			for(String groupname : keyvalues_groups.keySet()) {
				TreeMap<Integer, String> keyvalues = keyvalues_groups.get(groupname);
				if(groupname.length() != 0) {
					out.write("<optgroup label=\""+StringEscapeUtils.escapeHtml(groupname)+"\">");
				}
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
				if(groupname.length() != 0) {
					out.write("</optgroup>");
				}
			}
			out.print("</select>");
			if(isRequired()) {
				out.write(" * Required");
			}
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void onEvent(DivRepEvent event) {
		try {
			value = Integer.parseInt((String)event.value);
		} catch (NumberFormatException e) {
			value = null;
		}
		modified(true);
		validate();
	}
}
