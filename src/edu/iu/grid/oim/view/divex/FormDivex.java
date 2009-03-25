package edu.iu.grid.oim.view.divex;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.IFormElementDE;

abstract public class FormDivex extends DivEx {
	
	//URL to go after cancel or submit button is selected
	private String origin_url;
	
	public ButtonDE submitbutton;
	public ButtonDE cancelbutton;
	
	//private String error;
	private Boolean valid;
	
	public FormDivex(DivEx parent, String _origin_url)
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
			public void handleEvent(Event e) { 
				redirect(origin_url);
			}
		});
	}
	
	private void submit()
	{
		if(isValid()) {
			if(doSubmit()) {
				alert("Your form has been submitted");
				redirect(origin_url);	
			}
		} else {
			//error = "Please correct the issues before submitting your form.";
			//redraw();
			//scrollToShow();
			alert("Please correct the issues before submitting your form.");
		}
	}
	
	abstract protected Boolean doSubmit();
	
	public void validate()
	{
		redraw();
		valid = true;
		
		//validate *all* elements
		for(DivEx child : childnodes) {
			if(child instanceof IFormElementDE) { 
				IFormElementDE element = (IFormElementDE)child;
				if(element != null) {
					if(!element.isValid()) {
						valid = false;
					}
				}
			}
		}
	}
	public Boolean isValid()
	{
		validate();
		return valid;
	}
	
	/*
	public IFormElementDE getElement(String name) {
		for(DivEx child : childnodes) {
			if(child instanceof IFormElementDE) { 
				IFormElementDE element = (IFormElementDE)child;
				if(element.getName().compareTo(name) == 0) {
					return element;
				}
			}
		}
		return null;
	}
	*/

	public String renderInside() 
	{
		String out = "";
		
		out += "<div class='form'>";	
		for(DivEx child : childnodes) {
			if(child == submitbutton || child == cancelbutton) continue;
			out += "<p>" + child.render() + "</p>";
		}

		out += submitbutton.render();
		if(cancelbutton != null) {
			out += " or ";
			out += cancelbutton.render();
		}
		
		out += "</div>";
		return out;
	}
}
