package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;

public class TestApplicationDE extends DivEx {
	public Integer count = 0;
	public String name = "Soichi";
	
	public DivEx plusbutton = new PlusButtonDE(this);
	public DivEx display = new CounterDE(this);

	public String toHTML() 
	{
		String out = "";
		out += plusbutton.render();
		out += "Hello " + name;
		out += display.render();
		return out;
	}
}
