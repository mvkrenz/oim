package edu.iu.grid.oim.view;


public class ButtonView implements View {

	private String name;
	private String url;
	
	public ButtonView(String _name, String _url)
	{
		name = _name;
		url = _url;
	}
	
	public String toHTML() {
		return "<input type='button' value='&nbsp;&nbsp;&nbsp;"+name+"&nbsp;&nbsp;&nbsp;' onclick=\"document.location='"+url+"';\"/>";
	}
}
