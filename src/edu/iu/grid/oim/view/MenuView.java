package edu.iu.grid.oim.view;

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
	
	public String toHTML() {
		String out = "";
		
		out += "<div class=\"menu\">\n";
	    for(MenuItem item : menu) {
	    	String cls = "";
	    	if(item.url == current) {
	    		cls = "selected";
	    	}
	    	out += "<span class=\""+ cls +"\"><a href=\""+ ServletBase.BaseURL() + "/" + item.url+"\">"+item.name+"</a></span>\n";
	    }
		out += "</div>\n";
		return out;
		
	}

}
