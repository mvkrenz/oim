package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot.DivExPage;

//use this to make DivEx object behaves like a GenericView
public class DivExWrapper implements IView {

	private DivEx de;
	
	public DivExWrapper(DivEx _de) {
		de = _de;
	}
	
	public void render(PrintWriter out) {
		de.render(out);
		
		//ouput JS too
		out.write("<script type=\"text/javascript\">");
		DivExPage page = de.getPageRoot();
		page.flushJS(out);
		out.write("</script>");
		
		//divex frameworks sets this at the end of update request, but since we are doing the initial-draw,
		//we should set this to false. incase some divex component setting its redraw flag to true during
		//initialization.
		de.setNeedupdate(false);
	}
}
