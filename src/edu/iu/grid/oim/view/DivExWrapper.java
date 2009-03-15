package edu.iu.grid.oim.view;

import com.webif.divex.DivEx;

public class DivExWrapper implements View {

	private DivEx de;
	
	public DivExWrapper(DivEx _de) {
		de = _de;
	}
	
	public String toHTML() {
		String out = "";
		out += de.render();
		return out;
	}
}
