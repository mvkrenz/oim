package com.webif.divrep.common;

import java.io.PrintWriter;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;

abstract public class DivRepForm extends DivRep {
	
	//URL to go after cancel or submit button is selected
	private String origin_url;
	
	public DivRepButton submitbutton;
	public DivRepButton cancelbutton;
	
	//private String error;
	private Boolean valid;
	
	public DivRepForm(DivRep parent, String _origin_url)
	{
		super(parent);
		
		origin_url = _origin_url;
		
		submitbutton = new DivRepButton(this, "Submit");
		submitbutton.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) { submit(); }
		});
		submitbutton.addClass("divrep_submit");
		
		cancelbutton = new DivRepButton(this, "Cancel");
		cancelbutton.setStyle(DivRepButton.Style.ALINK);
		cancelbutton.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) { 
				modified(false);
				redirect(origin_url); 
			}
		});
	}
	
	private void submit()
	{
		validate();
		if(valid) {
			if(doSubmit()) {
				modified(false);
				redirect(origin_url);	
			}
		} else {
			alert("Please correct the issues flagged above, and then resubmit!");
		}
	}
	
	abstract protected Boolean doSubmit();
	
	public void validate()
	{
		redraw();
		valid = true;
		
		//validate *all* elements
		for(DivRep child : childnodes) {
			if(child instanceof DivRepFormElement) { 
				DivRepFormElement element = (DivRepFormElement)child;
				if(element != null && !element.isHidden()) {
					if(!element.isValid()) {
						valid = false;
					}
				}
			}
		}
	}
	
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) 
	{
		out.print("<div id=\""+getNodeID()+"\" class='form'>");	
		for(DivRep child : childnodes) {
			//we display submit / cancel button at the end
			if(child == submitbutton || child == cancelbutton) continue;
			
			if(child instanceof DivRepFormElement) {
				out.print("<div class=\"form_element\">");
				child.render(out);
				out.print("</div>");
			
			} else {
				//non form element..
				child.render(out);
			}
		}

		submitbutton.render(out);
		if(cancelbutton != null) {
			out.print(" or ");
			cancelbutton.render(out);
		}
		
		out.print("</div>");
	}
}
