package edu.iu.grid.oim.view.form;

import edu.iu.grid.oim.view.View;

abstract public class FormElementBase implements View {
	String name;
	String label;

	public String getName() { return name; }
	
	public FormElementBase(String _name, String _label)
	{
		name = _name;
		label = _label;
	}
}
