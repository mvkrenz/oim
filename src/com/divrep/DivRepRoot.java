package com.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//Each session contains a single DivRepRoot instance.

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
	
	public DivRepPage initPage(String pagekey)
	{
		//search and clear previous page with same pagekey
		for(DivRep divrep : childnodes) {
			DivRepPage page = (DivRepPage)divrep;
			if(page.getPageKey().equals(pagekey)) {
				//remove references
				this.remove(page);
				break;
			}
		}
		
		//if there are too many pages open, then close the last accessed one
		if(childnodes.size() > 4) {
			//find oldest page
			DivRepPage last = null;
			for(DivRep divrep : childnodes) {
				DivRepPage page = (DivRepPage)divrep;
				if(last == null || page.getLastAccessed().compareTo(last.getLastAccessed()) < 0) {
					last = page;
				}
			}
			this.remove(last);
			System.out.println(last.getPageKey() + "(accessed "+last.getLastAccessed().toString()+") has been removed from this session due to too many pages.");
		}
		
		//insert new page and return
		DivRepPage page = new DivRepPage(this, pagekey);
		return page;
	}

	protected void onEvent(DivRepEvent e) {
		//root doesn't handle any event
	}

	public void render(PrintWriter out) {
		//root doesn't display anything
	}
}
