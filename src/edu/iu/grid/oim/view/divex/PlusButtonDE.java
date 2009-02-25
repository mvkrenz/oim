package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

public class PlusButtonDE extends DivEx {
	TestApplicationDE app;
	
	public PlusButtonDE(TestApplicationDE _app) {
		app = _app;
	}
    public String toHTML() {
        return "<b>Plus</b>";   
    }
	protected void onClick(Event e) {
		app.count++;
		app.display.redraw();
	}
}
