package edu.iu.grid.oim.view.divex.form;

import java.io.PrintWriter;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

import edu.iu.grid.oim.view.IView;

public class ViewWrapperDE extends DivEx {

	IView content;
	public ViewWrapperDE(DivEx _parent, IView _content) {
		super(_parent);
		content = _content;
	}

	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		content.render(out);
	}

}
