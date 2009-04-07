package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

public abstract interface IView {
/*
	protected ArrayList<GenericView> children = new ArrayList<GenericView>();
	
	public void add(GenericView v) {
		children.add(v);
	}
	
	public void add(DivEx de) {
		add(new DivExWrapper(de));
	}
	
	//depricate this - danger of XSS
	public void add(String html) {
		add(new HtmlView(html));
	}
*/	
	abstract public void render(PrintWriter out);
}
