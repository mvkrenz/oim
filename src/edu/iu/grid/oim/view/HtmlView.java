package edu.iu.grid.oim.view;

public class HtmlView extends View {

	private String html;
	public HtmlView(String _html) {
		html = _html;
	}
	public String toHTML() {
		return html;
	}

}
