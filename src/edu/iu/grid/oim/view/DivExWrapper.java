package edu.iu.grid.oim.view;

import com.webif.divex.DivEx;

public class DivExWrapper implements View {

	private DivEx de;
	
	public DivExWrapper(DivEx _de) {
		de = _de;
	}
	
	public String toHTML() {
		String out = "";
		out += DivEx.bootcode();//TODO -- I have to do this only once per page.. how can I automate this?
		out += de.render();
		return out;
	}

}
