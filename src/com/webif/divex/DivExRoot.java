package com.webif.divex;

import java.io.PrintWriter;
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
	public void setRedirect(String url) { redirect_url = url; }
	public String getRedirect() { return redirect_url; }
	
	public DivExRoot() {
		super(null);
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
	
	//flush all JS code - this is for a view container to emit DivEx related JS script
    private String js = "";
    public void addJS(String _js) { js += _js; }
	public void flushJS(PrintWriter out)
	{
		if(js.length() != 0) {
			out.write("divex_jscallback = function() {");
			out.write(js);
			out.write("};");
			out.write("setTimeout(divex_runjs, 0)");
			js = "";
		}
	}

	protected void onEvent(Event e) {
		//root doesn't handle any event
	}

	public void render(PrintWriter out) {
		//root doesn't display anything
	}
}
