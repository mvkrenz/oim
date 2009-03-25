package com.webif.divex.form;

import com.webif.divex.ChangeEvent;
import com.webif.divex.DivEx;
import edu.iu.grid.oim.view.divex.FormDivex;
import org.apache.commons.lang.StringEscapeUtils;

public class CheckBoxFormElementDE extends DivEx implements IFormElementDE {

	protected String label;
	protected Boolean value = false;
	
	public CheckBoxFormElementDE(DivEx parent) {
		super(parent);
	}
	
	public String renderInside() {
		String checked = "";
		if(value == true) { //value should never be null by design
			checked = "checked=checked";
		}
		String html = "";
		html += "<input type='checkbox' onchange='divex_change(\""+getNodeID()+"\", this.checked);' "+checked+"/>";
		if(label != null) {
			html += " <label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>";
		}
		return html;
	}

	public void setLabel(String _label) { label = _label; }
	public void setValue(Boolean _value)	
	{ 
		if(_value == null) {
			//I went back and forth if I should throw exception, but since checkbox is the one
			//that deviates from the rest that handles "null state", so let's ignore it..
			return;
		}
		value = _value; 
	}
	public Boolean getValue()
	{
		return value;
	}
	
	public Boolean isValid()
	{
		return true;
	}
	
	public void onChange(ChangeEvent e) {
		if(e.newvalue.compareTo("true") == 0) {
			value = true;
		} else {
			value = false;
		}
	}
}
