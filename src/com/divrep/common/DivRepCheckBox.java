package com.divrep.common;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import org.apache.commons.lang.StringEscapeUtils;

public class DivRepCheckBox extends DivRepFormElement<Boolean> {
	
	public DivRepCheckBox(DivRep parent) {
		super(parent);
		value = false;
	}
	
	public void render(PrintWriter out) {
		out.write("<div ");
		renderClass(out);
		out.write("id=\""+getNodeID()+"\">");
		if(!hidden) {
			String checked = "";
			if(value == true) { //value should never be null by design
				checked = "checked=checked";
			}
			out.print("<input type='checkbox' onclick='divrep(\""+getNodeID()+"\", event, this.checked);' "+checked+"/>");
			if(label != null) {
				out.print(" <label>"+StringEscapeUtils.escapeHtml(label)+"</label>");
			}
			error.render(out);
		}
		out.print("</div>");
	}

	//override setValue to handle null case..
	public void setValue(Boolean _value)	
	{ 
		if(_value == null) {
			//I went back and forth if I should throw exception, but since checkbox is the one
			//that deviates from the rest that handles "null state", so let's ignore it..
			return;
		}
		value = _value; 
	}
	
	public void onEvent(DivRepEvent e) {
		if(((String)e.value).compareTo("true") == 0) {
			value = true;
		} else {
			value = false;
		}
		modified(true);
		validate(); //I know checkbox almost never needs any validation, but just for consistency sake..
	}
}
