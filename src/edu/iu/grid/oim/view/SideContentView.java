package edu.iu.grid.oim.view;

import java.util.ArrayList;

import com.webif.divex.DivEx;

//put each content under a side content header
public class SideContentView extends View {

	private ArrayList<View> children = new ArrayList<View>();
	
	public void add(String title, View v) {
		children.add(new HtmlView("<h3>"+title+"</h3>"));
		children.add(v);
	}
	
	public void add(String title, DivEx de) {
		add(title, new DivExWrapper(de));
	}
	
	//depricate this - danger of XSS
	public void add(String title, String html) {
		add(title, new HtmlView(html));
	}
	
	public String toHTML() {
		String out = "<div id=\"sideContent\">\n";
		for(View v : children) {
			out += v.toHTML();
		}
		out += "</div>";
		return out;
	}

}
