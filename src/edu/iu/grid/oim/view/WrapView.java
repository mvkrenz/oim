package edu.iu.grid.oim.view;

import java.io.PrintWriter;

public class WrapView extends View {

	private String head;
	private View content;
	private String tail;
	public WrapView(String _head, View _content, String _tail) {
		head = _head;
		content = _content;
		tail = _tail;
	}
	public void render(PrintWriter out) {
		out.print(head);
		content.render(out);
		out.print(tail);
	}

}
