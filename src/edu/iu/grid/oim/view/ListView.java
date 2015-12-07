package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ListView implements IView {
	protected ArrayList<IView> children = new ArrayList<IView>();
	public void add(IView v) {
		children.add(v);
	}
	
	public void render(PrintWriter out) {
		out.print("<ul class=\"content\">");
		for(IView v : children) {
			out.write("<li>");
			v.render(out);
			out.write("</li>");
		}
		out.print("</ul>");		

	}

}
