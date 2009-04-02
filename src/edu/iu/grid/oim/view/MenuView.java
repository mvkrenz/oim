package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.servlet.ServletBase;

public class MenuView extends View {
	private ArrayList<MenuItem> menu;
	private String current;
	
	public MenuView(ArrayList<MenuItem> _menu, String _current) {
		menu = _menu;
		current = _current;
	}
	
	public void render(PrintWriter out) {		
		out.println("<div class=\"menu\">");
	    for(MenuItem item : menu) {
	    	String cls = "";
	    	if(item.url == current) {
	    		cls = "selected";
	    	}
	    	out.println("<span class=\""+ cls +"\"><a href=\""+ ServletBase.BaseURL() + "/" + item.url+"\">"+item.name+"</a></span>");
	    }
		out.println("</div>");
		
	}

}
