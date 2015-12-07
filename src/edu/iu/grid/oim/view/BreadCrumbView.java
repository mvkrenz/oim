package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

public class BreadCrumbView implements IView {
	
	ArrayList<Crumb> crumbs = new ArrayList();
	class Crumb implements IView
	{
		public String title;
		public String url;
		public void render(PrintWriter out) {
			out.write(" &gt; ");
			if(url == null) {
				if(title == null) {
					out.write("New");
				} else {
					out.write(StringEscapeUtils.escapeHtml(title));
				}
			} else {
				out.write("<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a>");
			}
		}
	}
	
	public void addCrumb(String _title, String _url)
	{
		Crumb crumb = new Crumb();
		crumb.title = _title;
		crumb.url = _url;
		crumbs.add(crumb);
	}

	public void render(PrintWriter out) {
		//output bread
		out.write("<div id=\"breadcrumb\">You are here");
		for(Crumb crumb : crumbs) {
			crumb.render(out);
		}
		out.write("</div>");
	}

}
