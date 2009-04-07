package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ContentView extends GenericView {
	public void render(PrintWriter out)
	{
		out.println("<div id=\"content\">");
		
		//output bread
		out.println("<div id=\"breadcrumb\">You are here &gt; Somewhere &gt; Somewhere</div>");
		
		super.render(out);
		
		out.println("</div>");
	}

}
