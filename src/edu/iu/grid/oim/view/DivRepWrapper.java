package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot.DivRepPage;

//use this to make DivRep object behaves like a GenericView
public class DivRepWrapper implements IView {

	private DivRep de;
	
	public DivRepWrapper(DivRep _de) {
		de = _de;
	}
	
	public void render(PrintWriter out) {
		de.render(out);
		
		//ouput JS too
		out.write("<script type=\"text/javascript\">");
		DivRepPage page = de.getPageRoot();
		page.flushPostReplaceJS(out);
		out.write("</script>");
		
		//divrep frameworks sets this at the end of update request, but since we are doing the initial-draw,
		//we should set this to false. incase some divrep component setting its redraw flag to true during
		//initialization.
		de.setNeedupdate(false);
	}
}
