package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;

import edu.iu.grid.oim.view.IView;

public class ViewWrapper extends DivRep {

	IView content;
	public ViewWrapper(DivRep _parent, IView _content) {
		super(_parent);
		content = _content;
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		content.render(out);
	}

}
