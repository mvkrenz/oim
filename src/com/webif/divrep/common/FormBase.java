package com.webif.divrep.common;

import java.io.PrintWriter;

import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;

abstract public class FormBase extends DivRep {
	
	//URL to go after cancel or submit button is selected
	private String origin_url;
	
	public Button submitbutton;
	public Button cancelbutton;
	
	//private String error;
	private Boolean valid;
	
	public FormBase(DivRep parent, String _origin_url)
	{
		super(parent);
		
		origin_url = _origin_url;
		
		submitbutton = new Button(this, "Submit");
		submitbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { submit(); }
		});
		submitbutton.addClass("divrep_submit");
		
		cancelbutton = new Button(this, "Cancel");
		cancelbutton.setStyle(Button.Style.ALINK);
		cancelbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { 
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
			if(child instanceof FormElement) { 
				FormElement element = (FormElement)child;
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
		for(DivRep child : childnodes) {
			//we display submit / cancel button at the end
			if(child == submitbutton || child == cancelbutton) continue;
			
			if(child instanceof FormElement) {
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
