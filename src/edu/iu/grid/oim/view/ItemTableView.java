package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.servlet.LogServlet;

public class ItemTableView extends GenericView {
	private int cols;
	ArrayList<IView> views = new ArrayList<IView>();
	
	public ItemTableView(int cols) {
		this.cols = cols;
	}
	public void addView(IView view) {
		views.add(view);
	}
	public void render(PrintWriter out)
	{
		int width = (int) (100F / cols);
		out.write("<table class=\"layout\">");
		out.write("<tr>");
		
		int count = 0;
		for(IView view : views) {
			out.write("<td width=\""+width+"%\">");
			view.render(out);
			out.write("</td>");
			
			count++;
			if(count % cols == 0) {
				out.write("</tr><tr>");
			}
		}
		
		out.write("</tr>");
		out.write("</table>");
	}
}