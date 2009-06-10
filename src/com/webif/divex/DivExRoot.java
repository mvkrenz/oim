package com.webif.divex;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
		super(null); //null will indicate this is the DivExRoot
	}
	
	static public DivExPage initPageRoot(HttpServletRequest request) {
		DivExRoot root = DivExRoot.getInstance(request.getSession());
		DivExPage pageroot = root.initPage(request.getServletPath());
		return pageroot;
	}
	
	//for each session, there is only one DivExRoot.
	static public DivExRoot getInstance(HttpSession session)
	{
    	DivExRoot root  = (DivExRoot) session.getAttribute("divex");
    	if(root == null) {
    		root = new DivExRoot();
    		session.setAttribute("divex", root);
    	}
    	return root;
	}
	
	public class DivExPage extends DivEx
	{

		public DivExPage(DivEx _parent) {
			super(_parent);
			// TODO Auto-generated constructor stub
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
				out.write("setTimeout(divex_runjs, 0);");
				js = "";
			}
		}

		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			// TODO Auto-generated method stub
			
		}   
	}
	
	//DivExRoot contains the list of DivExPage that is the root for each pages
	HashMap<String, DivExPage> pages = new HashMap<String, DivExPage>();
	public DivExPage initPage(String url)
	{
		//clear old page (if exist)
		if(pages.containsKey(url)) {
			DivExPage oldpage = pages.get(url);
			//remove references
			pages.remove(url);
			this.remove(oldpage);
		}
		
		//insert new page and return
		DivExPage page = new DivExPage(this);
		pages.put(url, page);
		return page;
	}

	protected void onEvent(Event e) {
		//root doesn't handle any event
	}

	public void render(PrintWriter out) {
		//root doesn't display anything
	}
}
