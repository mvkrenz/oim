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
	
	//override it with public interface since this is root. user can call this at root to emit
	//all update code
	public String outputUpdatecode()
	{
		return super.outputUpdatecode();
	}
}
