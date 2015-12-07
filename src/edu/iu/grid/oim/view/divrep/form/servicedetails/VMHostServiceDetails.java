package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

public class VMHostServiceDetails extends ServiceDetailsContent {

	private DivRepTextBox vm_option1;
	private DivRepTextBox vm_option2;
	private DivRepTextArea vm_config;
	
	public VMHostServiceDetails(DivRep _parent) {
		super(_parent);
		
		vm_option1 = new DivRepTextBox(this);
		vm_option1.setLabel("VM Option1");
		vm_option1.setSampleValue("abc123");
		vm_option1.setRequired(true);
		
		vm_option2 = new DivRepTextBox(this);
		vm_option2.setLabel("VM Option2");
		vm_option2.setSampleValue("abc456");
		vm_option2.setRequired(true);
		
		vm_config = new DivRepTextArea(this);
		vm_config.setLabel("VM Config");
	}
	
	public void setValues(HashMap<String, String> values) {
		if(values.containsKey("vm_option1")) {
			vm_option1.setValue(values.get("vm_option1"));
		}
		if(values.containsKey("vm_option2")) {
			vm_option2.setValue(values.get("vm_option2"));
		}
		if(values.containsKey("vm_config")) {
			vm_config.setValue(values.get("vm_config"));
		}
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		vm_option1.render(out);
		vm_option2.render(out);
		vm_config.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getValues() {
		HashMap<String, String> map = new HashMap<String, String>();
	
		map.put("vm_option1", vm_option1.getValue());
		map.put("vm_option2", vm_option2.getValue());
		map.put("vm_config", vm_config.getValue());
		
		return map;
	}

	@Override
	public boolean validate() {
		return (vm_option1.validate() && vm_option2.validate() && vm_config.validate());
	}
}
