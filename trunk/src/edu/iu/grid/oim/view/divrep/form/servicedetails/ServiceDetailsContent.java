package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.util.HashMap;

import com.divrep.DivRep;

abstract public class ServiceDetailsContent extends DivRep {
	public ServiceDetailsContent(DivRep _parent) {
		super(_parent);
		// TODO Auto-generated constructor stub
	}
	abstract public void setValues(HashMap<String, String> values);
	abstract public HashMap<String, String> getValues();
	abstract public boolean validate();
}
