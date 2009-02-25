package edu.iu.grid.oim.view.form;

import edu.iu.grid.oim.view.View;

abstract public class FormElementBase implements View {
	String name;
	String label;
	String value;
	String error;
	
	public void setValue(String _value) { value = _value; }
	public void setError(String _error) { error = _error; }
	
	public FormElementBase(String _name, String _label)
	{
		name = _name;
		label = _label;
	}
}
