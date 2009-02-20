package edu.iu.grid.oim.view;

import java.util.ArrayList;

import com.webif.divex.DivEx;

public class ContentView implements View {

	private ArrayList<View> children = new ArrayList<View>();
	
	public void add(View v) {
		children.add(v);
	}
	
	public void add(DivEx de) {
		children.add(new DivExWrapper(de));
	}
	
	public void add(String html) {
		children.add(new HtmlView(html));
	}
	
	public String toHTML() {
		String out = "<div id=\"content\">\n";
		
		//output bread
		out += "<div id=\"breadcrumb\">You are here &gt; Somewhere &gt; Somewhere</div>";
		
		//output child content
		for(View v : children) {
			out += v.toHTML();
		}
		out += "</div>\n";
		return out;
	}

}
