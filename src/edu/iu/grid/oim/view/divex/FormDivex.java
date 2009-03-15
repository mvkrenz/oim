package edu.iu.grid.oim.view.divex;

import java.util.ArrayList;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.IFormElementDE;

abstract public class FormDivex extends DivEx {
	
	public ButtonDE submitbutton = new ButtonDE(this, "Submit");
	public ButtonDE cancelbutton;
	private String origin_url;
	
	//private String error;
	private Boolean valid;
	
	public FormDivex(DivEx parent, String _origin_url)
	{
		super(parent);
		//register button event listener
		submitbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { submit(); }
		});
		
		origin_url = _origin_url;
		cancelbutton = new ButtonDE(this, "Cancel");
		cancelbutton.setStyle(ButtonDE.Style.ALINK);
		cancelbutton.addEventListener(new EventListener() {
			public void handleEvent(Event e) { 
				redirect(origin_url);
			}
		});
	}
	
	public void setCancelUrl(String _url)
	{

	}
	
	private void submit()
	{
		if(isValid()) {
			if(doSubmit()) {
				alert("Your form has been submitted");
				redirect(origin_url);	
			} else {
				alert("Form submission has failed. Please try again.");
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

	public String toHTML() 
	{
		String out = "";
		
		out += "<div class='form'>";
		/*
		if(error != null) {
			out += "<p class='formerror'>" + error + "</p>";
		}
		*/
		
		for(DivEx child : childnodes) {
			if(child instanceof IFormElementDE) { 
				out += "<p class='element'>";
				out += child.render();
				out += "</p>\n";
			}
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
