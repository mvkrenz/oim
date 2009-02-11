package edu.iu.grid.oim.view.divex;

import com.webif.divex.Div;

public class Counter extends Div {
	ApplicationView app;
	
	public Counter(ApplicationView _app) {
		app = _app;
	}
    public String toHtml() {
        return "Count is: " + app.count;   
    }
}
