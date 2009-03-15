package com.webif.divex;
import java.util.ArrayList;

public abstract class DivEx {

	private String nodeid;
	private String redirect_url;
	private Boolean needupdate = false;
	private Boolean scrolltoshow = false;
	private String alert_message;
	protected ArrayList<DivEx> childnodes = new ArrayList();
	
	public void alert(String msg)
	{
		alert_message = msg;
	}
	public void redirect(String _url) {
		redirect_url = _url;
	}
	private ArrayList<EventListener> event_listeners = new ArrayList();
	
	public String getNodeID() { return nodeid; }

	public String outputUpdatecode()
	{
		String code = "";

		if(alert_message != null) {
			code += "alert('"+alert_message+"');";
			alert_message = null;
		}
		if(redirect_url != null) {
			code += "document.location = '"+redirect_url+"';";
			redirect_url = null;
		}
		if(scrolltoshow) {
			code += "var targetOffset = $(\"#"+nodeid+"\").offset().top;";
			code += "$('html,body').animate({scrollTop: targetOffset}, 500);";
			scrolltoshow = false;
		}
		if(needupdate) {
			String success = "";
			code += "$(\"#"+nodeid+"\").load(\"divex?action=load&nodeid="+nodeid+"\", function(){"+success+"});";
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
		String html = "";
		html += "<div class='divex' id='"+nodeid+"' onclick='divex_click(this);'>";
		html += toHTML();
		html += "</div>";
		return html;
	}
	
	//override this to put your dynamic content
	abstract public String toHTML();
	
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
	
	public void click()
	{
		ClickEvent e = new ClickEvent();
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
	public void scrollToShow() {
		scrolltoshow = true;
		System.out.print("scolling request on "+this.nodeid);
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
