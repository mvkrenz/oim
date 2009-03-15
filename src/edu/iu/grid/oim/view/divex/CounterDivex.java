package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;

public class CounterDivex extends DivEx {
	TestApplicationDivex app;
	
	public CounterDivex(TestApplicationDivex _app) {
		super(_app);
		app = _app;
	}
    public String toHTML() {
        return "Count is: " + app.count;   
    }
}
