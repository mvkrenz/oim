package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

public class BootBreadCrumbView implements IView {
	
	ArrayList<Crumb> crumbs = new ArrayList();
	class Crumb implements IView
	{
		public String title;
		public String url;
		public void render(PrintWriter out) {
			if(url == null) {
				out.write("<li class=\"active\">");
				if(title == null) {
					out.write("New");
				} else {
					out.write(StringEscapeUtils.escapeHtml(title));
				}
				out.write("</li>");
			} else {
				//out.write("<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a>");
				out.write("<li><a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a><span class=\"divider\">/</span></li>");
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
		out.write("<ul class=\"breadcrumb\">");
		for(Crumb crumb : crumbs) {
			crumb.render(out);
		}
		out.write("</ul>");
	}
/*
<ul class="breadcrumb">
        <li><a href="#">Home</a> <span class="divider">/</span></li>
        <li><a href="#">Library</a> <span class="divider">/</span></li>
        <li class="active">Data</li>
      </ul>
 */
}
