package edu.iu.grid.oim.view;

import java.io.PrintWriter;

public class ContentView extends View {

	public void render(PrintWriter out)
	{
		out.println("<div id=\"content\">");
		
		//output bread
		out.println("<div id=\"breadcrumb\">You are here &gt; Somewhere &gt; Somewhere</div>");
		
		//show child content using View's render
		for(View child : children) {
			child.render(out);
		}
		
		out.println("</div>");
	}

}
