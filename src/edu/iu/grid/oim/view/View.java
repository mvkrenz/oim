package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

public abstract class View {

	protected ArrayList<View> children = new ArrayList<View>();
	
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
	
	abstract public void render(PrintWriter out);
	/*
	{
		//output child content
		for(View v : children) {
			v.render(out);
		}
	}
	*/
}
