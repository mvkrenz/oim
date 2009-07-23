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
