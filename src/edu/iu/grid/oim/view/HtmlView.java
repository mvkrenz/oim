package edu.iu.grid.oim.view;

import java.io.PrintWriter;

public class HtmlView extends View {

	private String html;
	public HtmlView(String _html) {
		html = _html;
	}
	public void render(PrintWriter out) {
		out.print(html);
	}

}
