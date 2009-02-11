package edu.iu.grid.oim.view.divex;

import com.webif.divex.Div;

public class ApplicationView extends Div {
	public Integer count = 0;
	public Div display = new Counter(this);
	public Div plusbutton = new PlusButton(this);
	
	public String toHtml() 
	{
		String out = "<h1>Counter Application</h1>";
		out += plusbutton.render();
		out += display.render();
		return out;
	}
}
