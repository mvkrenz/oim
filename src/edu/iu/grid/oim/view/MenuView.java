package edu.iu.grid.oim.view;

import java.util.ArrayList;

import edu.iu.grid.oim.model.MenuItem;

public class MenuView extends View {
	private ArrayList<MenuItem> menu;
	private String baseurl;
	private String current;
	
	public MenuView(String _baseurl, ArrayList<MenuItem> _menu, String _current) {
		menu = _menu;
		baseurl = _baseurl;
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
	    	out += "<span class=\""+ cls +"\"><a href=\""+ baseurl + "/" + item.url+"\">"+item.name+"</a></span>\n";
	    }
		out += "</div>\n";
		return out;
		
	}

}
