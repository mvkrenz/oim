package edu.iu.grid.oim.view.divex;

import com.webif.divex.Div;
import com.webif.divex.Event;

public class PlusButton extends Div {
	ApplicationView app;
	
	public PlusButton(ApplicationView _app) {
		app = _app;
	}
    public String toHtml() {
        return "<b>Plus</b>";   
    }
	protected void onClick(Event e) {
		app.count++;
		app.display.update();
	}
}
