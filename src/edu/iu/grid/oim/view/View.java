package edu.iu.grid.oim.view;

import java.util.ArrayList;

import com.webif.divex.DivEx;

public class View {
	private ArrayList<View> children = new ArrayList<View>();
	
	public void add(View v) {
		children.add(v);
	}
	
	public void add(DivEx de) {
		add(new DivExWrapper(de));
	}
	
	//depricate this - danger of XSS
	public void add(String html) {
		add(new HtmlView(html));
	}
	
	public String toHTML()
	{
		String out = "";
		//output child content
		for(View v : children) {
			out += v.toHTML();
		}
		return out;
	}
}
