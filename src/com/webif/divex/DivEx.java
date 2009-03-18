package com.webif.divex;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DivEx {

	private String nodeid;
	private Boolean needupdate = false;
	private String js = "";
	private HashMap<String, String> div_attr = new HashMap();
	
	protected ArrayList<DivEx> childnodes = new ArrayList();
	
	
	//set attr to apply to the div
	public void setAttr(String attr, String value)
	{
		div_attr.put(attr, value);
	}
	
	public void alert(String msg)
	{
		js += "alert('"+msg+"');";
	}
	public void js(String _js)
	{
		js += _js;
	}
	public void redirect(String url) {
		js += "document.location = '"+url+"';";
	}
	public void scrollToShow() {
		js += "var targetOffset = $(\"#"+nodeid+"\").offset().top;";
		js += "$('html,body').animate({scrollTop: targetOffset}, 500);";
	}

	private ArrayList<EventListener> event_listeners = new ArrayList();
	
	public String getNodeID() { return nodeid; }

	//DivEx Servlet calls this on *root* upon completion of event handler
	public String outputUpdatecode()
	{
		//start with user specified js code
		String code = js;
		js = "";
		
		//find child nodes who needs update
		if(needupdate) {
			String success = "";
			
			code += "$(\"#"+nodeid+"\").load(\"divex?action=load&nodeid="+nodeid+"\", function(){"+success+"});";
			
			//I don't need to update any of my child - parent will redraw all of it.
			setNeedupdate(false);
		}
		for(DivEx d : childnodes) {
			code += d.outputUpdatecode();
		}	
		
		return code;
	}
	
	//recursively set mine and my children's needupdate flag
	public void setNeedupdate(Boolean b)
	{
		needupdate = b;
		for(DivEx node : childnodes) {
			node.setNeedupdate(b);
		}
	}
	
	public DivEx(DivEx parent) {
		nodeid = DivExRoot.getNewNodeID();
		
		if(parent != null) {
			parent.addChild(this);
		}
		
		div_attr.put("class", "divex");
	}
	
	public void addChild(DivEx child)
	{
		childnodes.add(child);
	}
	
	//recursively do search
	public DivEx findNode(String _nodeid)
	{
		if(nodeid.compareTo(_nodeid) == 0) return this;
		for(DivEx child : childnodes) {
			DivEx node = child.findNode(_nodeid);
			if(node != null) return node;
		}
		return null;
	}
	
	//if you have a custom controller that uses more fundamental html element, override this
	//but don't forget to put all of the dynamic part of the content to toHTML()
	//divex.load action will only call toHTML to redraw the content
	//whatever you put here will remains until any of the parent redraws
	public String render() {
		String attrs = "";
		for(String attr : div_attr.keySet()) {
			String value = div_attr.get(attr);
			attrs += attr + "=\""+value+"\" ";
		}
		
		String html = "";
		html += "<div "+attrs+" id='"+nodeid+"' onclick='divex_click(this.id);'>";
		html += renderInside();
		html += "</div>";
		return html;
	}
	
	//override this to draw the inside of your div.
	public String renderInside()
	{
		String html = "";
		for(DivEx child : childnodes) {
			html += child.render();
		}
		return html;
	}
	
	public void addEventListener(EventListener listener)
	{
		event_listeners.add(listener);
	}
	
	private void notifyEventListeners(Event e)
	{
		for(EventListener listener : event_listeners) {
			listener.handleEvent(e);
		}
	}
	
	public void click(String value)
	{
		ClickEvent e = new ClickEvent(value);
		this.onClick(e);
		notifyEventListeners(e);
	}	
	
	public void change(String newvalue)
	{
		ChangeEvent e = new ChangeEvent(newvalue);
		this.onChange(e);
		notifyEventListeners(e);
	}
	
	//override these to handle local events (for remote events, use listener)
	protected void onClick(ClickEvent e) {}
	protected void onChange(ChangeEvent e) {}
	
	//only set needupdate on myself - since load will redraw all its children
	//once drawing is done, needudpate = false will be performec recursively
	public void redraw() {
		needupdate = true;
	}
	
	public static String encodeHTML(String s)
	{
	    StringBuffer out = new StringBuffer();
	    for(int i=0; i<s.length(); i++)
	    {
	        char c = s.charAt(i);
	        if(c > 127 || c=='"' || c=='\'' || c=='<' || c=='>')
	        {
	           out.append("&#"+(int)c+";");
	        }
	        else
	        {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
}
