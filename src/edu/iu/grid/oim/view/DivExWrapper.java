package edu.iu.grid.oim.view;

import com.webif.divex.DivEx;

public class DivExWrapper extends View {

	private DivEx de;
	
	public DivExWrapper(DivEx _de) {
		de = _de;
	}
	
	public String toHTML() {
		String out = "";
		out += de.render();
		
		//divex frameworks sets this at the end of update request, but since we are doing the initial-draw,
		//we should set this to false. incase some divex component setting its redraw flag to true during
		//initialization.
		de.setNeedupdate(false);
		
		return out;
	}
}
