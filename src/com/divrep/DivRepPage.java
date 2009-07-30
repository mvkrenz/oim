package com.divrep;

import java.io.PrintWriter;
import java.util.Date;

public class DivRepPage extends DivRep
{
	//page key logically organizes all the pages. 
	//For eample, page?id=1 and page?id=2 are different pages, but page?id=1&foo=bar and page?id=2&foo=hoge are the same.
	//In that case, one page will have key of "page?id=1" regardless of what other parameters are.
	//this mechanism is used to free previously accessed same page, or free last accessed page.
	private String pagekey;
	public String getPageKey() { return pagekey; }
	//used for DivRepRoot to identify which page is last accessed and free it necessary.
	private Date last_accessed;
	public void setAccessed() {
		last_accessed = new Date();
	}
	public Date getLastAccessed() {
		return last_accessed;
	}
	
	public DivRepPage(DivRep _parent, String _pagekey) {
		super(_parent);
		pagekey = _pagekey;
		setAccessed();
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