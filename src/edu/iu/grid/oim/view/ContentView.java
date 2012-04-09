package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ContentView extends GenericView {
	IView bread_crumb;
	public void setBreadCrumb(IView _crumb)
	{
		bread_crumb = _crumb;
	}
	
	public void render(PrintWriter out)
	{
		out.println("<div id=\"content\">");
		
		if(bread_crumb != null) {
			bread_crumb.render(out);
		}
		
		super.render(out);
		
		out.println("</div>");
	}

}
