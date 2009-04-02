package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;

//put each content under a side content header
public class SideContentView extends View {

	private ArrayList<View> children = new ArrayList<View>();
	
	public void add(String title, View v) {
		children.add(new HtmlView("<h3>"+title+"</h3>"));
		WrapView content = new WrapView("<p class=\"indent\">", v, "</p>");
		children.add(content);
	}
	
	public void add(String title, DivEx de) {
		add(title, new DivExWrapper(de));
	}
	
	//depricate this - danger of XSS
	public void add(String title, String html) {
		add(title, new HtmlView(html));
	}
	
	public void render(PrintWriter out)
	{
		out.println("<div id=\"sideContent\">");
		for(View v : children) {
			v.render(out);
		}
		out.println("</div>");
	}

}
