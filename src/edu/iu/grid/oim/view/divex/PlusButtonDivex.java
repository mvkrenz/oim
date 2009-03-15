package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

public class PlusButtonDivex extends DivEx {
	TestApplicationDivex app;
	
	public PlusButtonDivex(TestApplicationDivex _app) {
		super(_app);
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
