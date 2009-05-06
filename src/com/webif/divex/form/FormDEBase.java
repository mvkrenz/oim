package com.webif.divex.form;

import java.io.PrintWriter;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;

abstract public class FormDEBase extends DivEx {
	
	//URL to go after cancel or submit button is selected
	private String origin_url;
	
	public ButtonDE submitbutton;
	public ButtonDE cancelbutton;
	
	//private String error;
	private Boolean valid;
	
	public FormDEBase(DivEx parent, String _origin_url)
	{
		super(parent);
		
		origin_url = _origin_url;
		
		submitbutton = new ButtonDE(this, "Submit");
		submitbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { submit(); }
		});
		
		cancelbutton = new ButtonDE(this, "Cancel");
		cancelbutton.setStyle(ButtonDE.Style.ALINK);
		cancelbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { redirect(origin_url); }
		});
	}
	
	private void submit()
	{
		validate();
		if(valid) {
			if(doSubmit()) {
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
		for(DivEx child : childnodes) {
			if(child instanceof FormElementDEBase) { 
				FormElementDEBase element = (FormElementDEBase)child;
				if(element != null && !element.isHidden()) {
					if(!element.isValid()) {
						valid = false;
					}
				}
			}
		}
	}
	
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) 
	{
		out.print("<div id=\""+getNodeID()+"\" class='form'>");	
		for(DivEx child : childnodes) {
			//we display submit / cancel button at the end
			if(child == submitbutton || child == cancelbutton) continue;
			
			if(child instanceof FormElementDEBase) {
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
