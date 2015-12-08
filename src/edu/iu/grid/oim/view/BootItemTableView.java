package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;

import edu.iu.grid.oim.servlet.LogServlet;

public class BootItemTableView extends GenericView {
	private int cols;
	private int totalspan = 12;
	ArrayList<IView> views = new ArrayList<IView>();
	
	public BootItemTableView(int cols) {
		this.totalspan = totalspan;
		this.cols = cols;
	}
	public void add(IView view) {
		views.add(view);
	}
	public void add(DivRep div) {
		add(new DivRepWrapper(div));
	}
	public void render(PrintWriter out)
	{
		int span = (int) (totalspan / cols);
		out.write("<div class=\"row-fluid\">");
		
		int count = 0;
		for(IView view : views) {
			out.write("<div class=\"span"+span+"\">");
			view.render(out);
			out.write("</div>");
			
			count++;
			if(count % cols == 0) {
				out.write("</div><div class=\"row-fluid\">");
			}
		}
		
		out.write("</div>");
	}
}