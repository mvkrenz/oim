package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;

public class CounterDE extends DivEx {
	TestApplicationDE app;
	
	public CounterDE(TestApplicationDE _app) {
		app = _app;
	}
    public String toHtml() {
        return "Count is: " + app.count;   
    }
}
