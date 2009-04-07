package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

//put each content under a side content header
public class SideContentView implements IView {

	private ArrayList<IView> children = new ArrayList<IView>();
	
	public void add(IView v) {
		children.add(v);
	}
	
	public void add(DivEx de) {
		add(new DivExWrapper(de));
	}
	
	public void add(String html) {
		add(new HtmlView(html));
	}
	public void add(String title, IView v) {
		children.add(new HtmlView("<h3>"+title+"</h3>"));
		children.add(new HtmlView("<p class=\"indent\">"));
		children.add(v);
		children.add(new HtmlView("</p>"));
	}
	
	public void add(String title, DivEx de) {
		add(title, new DivExWrapper(de));
	}
	
	public void add(String title, String html) {
		add(title, new HtmlView(html));
	}
	
	public void render(PrintWriter out)
	{
		out.println("<div id=\"sideContent\">");
		for(IView v : children) {
			v.render(out);
		}
		out.println("</div>");
	}

}
