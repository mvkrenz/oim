package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;

public class TestApplicationDivex extends DivEx {
	public TestApplicationDivex(DivEx parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	public Integer count = 0;
	public String name = "Soichi";
	
	public DivEx plusbutton = new PlusButtonDivex(this);
	public DivEx display = new CounterDivex(this);

	public String toHTML() 
	{
		String out = "";
		out += plusbutton.render();
		out += "Hello " + name;
		out += display.render();
		return out;
	}
}
