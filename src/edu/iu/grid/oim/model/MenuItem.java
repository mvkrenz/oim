package edu.iu.grid.oim.model;

public class MenuItem {
	public String name;
	public String url;
	public String param = null;
	
	public MenuItem(String _name, String _url)
	{
		name = _name;
		url = _url;
	}
	public MenuItem(String _name, String _url, String _param)
	{
		this(_name, _url);
		param = _param;
	}
}
