package com.webif.divex;

import javax.servlet.http.HttpServletRequest;

public class DivExRoot extends DivEx
{
	static private int next_nodeid = 0;
	static public String getNewNodeID()
	{
		String nodeid = "divex_"+next_nodeid;
		++next_nodeid;
		return nodeid;
	}
	
	private String redirect_url;
	
	public DivExRoot() {
		super(null);
	}
	public void redirect(String url) 
	{
		redirect_url = url;
	}

	static public DivExRoot getInstance(HttpServletRequest request)
	{
    	DivExRoot root  = (DivExRoot) request.getSession().getAttribute("divex");
    	if(root == null) {
    		root = new DivExRoot();
    		request.getSession().setAttribute("divex", root);
    	}
    	return root;
	}
	
	//override it with public interface since this is root. user can call this at root to emit
	//all update code
	public String outputUpdatecode()
	{
		//if redirect is set, we don't need any updatecode - just jump!
		if(redirect_url != null) {
			String js = "document.location = '"+redirect_url+"';";
			redirect_url = null;
			return js;
		}
		return super.outputUpdatecode();
	}

	protected void onEvent(Event e) {
		//root doesn't handle any event
	}

	public String render() {
		//root doesn't display anything
		return null;
	}
}
