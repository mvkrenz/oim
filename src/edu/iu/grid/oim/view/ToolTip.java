package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;

public class ToolTip implements IView {
	private String tip;
	
	public ToolTip(String tip) {
		this.tip = tip;
	}
	
	public String render() {
		return "<img style=\"cursor: pointer;\" align=\"top\" src=\"images/help.png\" alt=\""+StringEscapeUtils.escapeHtml(tip)+"\" title=\""+StringEscapeUtils.escapeHtml(tip)+"\"/>";
	}

	public void render(PrintWriter out) {
		out.write(render());
	}
	
}
