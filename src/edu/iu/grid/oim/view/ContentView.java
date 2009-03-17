package edu.iu.grid.oim.view;

import java.util.ArrayList;

import com.webif.divex.DivEx;

public class ContentView implements IView {

	private ArrayList<IView> children = new ArrayList<IView>();
	
	public void add(IView v) {
		children.add(v);
	}
	
	public void add(DivEx de) {
		add(new DivExWrapper(de));
	}
	
	//depricate this - danger of XSS
	public void add(String html) {
		add(new HtmlView(html));
	}
	
	public String toHTML() {
		String out = "<div id=\"content\">\n";
		
		//output bread
		out += "<div id=\"breadcrumb\">You are here &gt; Somewhere &gt; Somewhere</div>";
		
		//output child content
		for(IView v : children) {
			out += v.toHTML();
		}
		out += "</div>\n";
		return out;
	}

}
