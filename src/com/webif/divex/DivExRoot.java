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
		
		//post_replace_js will be executed *after* all the replaces are completed
		//we need to place alert(), prompt() javascript after or Firefox will lockup some controls such as select.
		//some script may also fail due to the new content not yet available.
	    private String post_replace_js = "";
	    public void addPostReplaceJS(String _js) { post_replace_js += _js; }
		public void flushPostReplaceJS(PrintWriter out)
		{
			if(post_replace_js.length() != 0) {
				out.write("divex_jscallback = function() {");
				out.write(post_replace_js);
				out.write("};");
				out.write("setTimeout(divex_runjs, 0);");
				post_replace_js = "";
			}
			
			/*
			out.write(post_replace_js);
			post_replace_js = "";
			*/
		}
		
		//set the page to be in "modified" state - used by form (and maybe other in the future)
		//this control if the redirect should happen immediaterly or not
		private Boolean modified = false;
		public void setModified(Boolean b) {
			modified = b;
		}
		public Boolean isModified() { return modified; }

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
