package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot.DivRepPage;

//simple view that has list of children
public class GenericView implements IView {
	protected ArrayList<IView> children = new ArrayList<IView>();
	public void add(IView v) {
		children.add(v);
	}
	public void add(DivRep div) {
		children.add(new DivRepWrapper(div));
	}
	public void render(PrintWriter out) {
		out.write("<div>");
		for(IView child : children) {
			child.render(out);
		}
		out.write("</div>");
	}

}
