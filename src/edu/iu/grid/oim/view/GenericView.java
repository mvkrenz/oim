package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

//simple view that has list of children
public class GenericView implements IView {
	protected ArrayList<IView> children = new ArrayList<IView>();
	public void add(IView v) {
		children.add(v);
	}
	public void add(DivEx div) {
		children.add(new DivExWrapper(div));
	}
	public void render(PrintWriter out) {
		out.write("<div>");
		for(IView child : children) {
			child.render(out);
		}
		out.write("</div>");
	}

}
