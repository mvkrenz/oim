package com.webif.divrep;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class DivRepRoot extends DivRep
{    
	static private int next_nodeid = 0;
	static public String getNewNodeID()
	{
		String nodeid = "divrep_"+next_nodeid;
		++next_nodeid;
		return nodeid;
	}
	
	public DivRepRoot() {
		super(null); //null will indicate this is the DivRepRoot
	}
	
	static public DivRepPage initPageRoot(HttpServletRequest request) {
		DivRepRoot root = DivRepRoot.getInstance(request.getSession());
		DivRepPage pageroot = root.initPage(request.getServletPath());
		return pageroot;
	}
	
	//for each session, there is only one DivRepRoot.
	static public DivRepRoot getInstance(HttpSession session)
	{
    	DivRepRoot root  = (DivRepRoot) session.getAttribute("divrep");
    	if(root == null) {
    		root = new DivRepRoot();
    		session.setAttribute("divrep", root);
    	}
    	return root;
	}
	
	public class DivRepPage extends DivRep
	{

		public DivRepPage(DivRep _parent) {
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
				out.write("divrep_jscallback = function() {");
				out.write(post_replace_js);
				out.write("};");
				out.write("setTimeout(divrep_runjs, 50);");
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
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			// TODO Auto-generated method stub
			
		}   
		
		private String redirect_url;
		public void setRedirect(String url) { redirect_url = url; }
		public String getRedirect() { return redirect_url; }
	}
	
	//DivRepRoot contains the list of DivRepPage that is the root for each pages
	HashMap<String, DivRepPage> pages = new HashMap<String, DivRepPage>();
	public DivRepPage initPage(String url)
	{
		//clear old page (if exist)
		if(pages.containsKey(url)) {
			DivRepPage oldpage = pages.get(url);
			//remove references
			pages.remove(url);
			this.remove(oldpage);
		}
		
		//insert new page and return
		DivRepPage page = new DivRepPage(this);
		pages.put(url, page);
		return page;
	}

	protected void onEvent(DivRepEvent e) {
		//root doesn't handle any event
	}

	public void render(PrintWriter out) {
		//root doesn't display anything
	}
}
