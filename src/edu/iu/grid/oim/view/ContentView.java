package edu.iu.grid.oim.view;

public class ContentView extends View {

	public String toHTML() {
		String out = "<div id=\"content\">\n";
		
		//output bread
		out += "<div id=\"breadcrumb\">You are here &gt; Somewhere &gt; Somewhere</div>";
		
		//show child content
		out += super.toHTML();
		
		out += "</div>\n";
		return out;
	}

}
