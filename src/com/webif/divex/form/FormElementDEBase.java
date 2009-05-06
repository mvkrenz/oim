package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.validator.IFormElementValidator;

abstract public class FormElementDEBase<ValueType> extends DivEx {
	
	//class used to render the parent div element (you can use it to render it in non-div-ish way like inline)
	//the derived element has to use this in order for it to actually take effect (of course)
	private ArrayList<String> classes = new ArrayList<String>();
	public void addClass(String _class) {
		classes.add(_class);
	}
	protected void renderClass(PrintWriter out) {
		out.write("class=\"");
		for(String _class : classes) {
			out.write(_class);
			out.write(" ");
		}
		out.write("\"");
	}
	
	protected FormElementDEBase(DivEx parent) {
		super(parent);
	}
	
	//validation suite
	protected ArrayList<IFormElementValidator<ValueType>> validators = new ArrayList<IFormElementValidator<ValueType>>();
	public void addValidator(IFormElementValidator<ValueType> _validator) { validators.add(_validator); }
	protected ErrorDE error = new ErrorDE(this);
	protected class ErrorDE extends DivEx
	{
		public ErrorDE(DivEx _parent) {
			super(_parent);
			// TODO Auto-generated constructor stub
		}
		private String error;
		public void set(String _error) { error = _error; }
		public String get() { return error; }
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}
		public void render(PrintWriter out) {
			out.write("<div class=\"inline\" id=\""+getNodeID()+"\">");
			if(error != null) {
				out.write("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
			}
			out.write("</div>");
		}
	}
	protected Boolean valid = true;
	public Boolean isValid() { 
		validate();
		return valid; 
	}
	
	//you need to override this to do its own child element loop if one of the element is expected to be
	//dynamically removed. most likely, if you have dynamic elements, you are keeping up with your own list
	//of active elements. childnodes, on the other hand, doesn't handle removing of the element. Once it's there,
	//it's there forever. So unnecessary validation may occur unless you override this to only loop your own
	//elements
	public void validate()
	{
		error.redraw();
		
		//validate *all* child elements first
		boolean children_valid = true;
		for(DivEx child : childnodes) {
			if(child instanceof FormElementDEBase) { 
				FormElementDEBase element = (FormElementDEBase)child;
				if(element != null && !element.isHidden()) {
					if(!element.isValid()) {
						children_valid = false;
						//continue validating other children
					}
				}
			}
		}
		if(!children_valid) {
			//error.set("Child element is invalid.");
			valid = false;
			return;
		}
		
		//if required, run RequiredValidator
		if(value == null || value.toString().trim().length() == 0) {
			if(isRequired()) {
				error.set("This is a required field. Please specify a value.");
				valid = false;
				return;
			} else {
				//the field is not-required and it's empty - no futher validation necessary
				valid = true;
				return;
			}
		}
		
		//then run the optional validation
		for(IFormElementValidator<ValueType> validator : validators) {
			if(!validator.isValid(value)) {
				//bad..
				error.set(validator.getMessage());
				valid = false;
				return;
			}
		}
		
		//all good..
		error.set(null);
		valid = true;
	}
	
	protected ValueType value;
	public void setValue(ValueType _value) { value = _value; }
	public ValueType getValue() { return value; }
	
	protected ValueType sample_value;
	public void setSampleValue(ValueType _sample_value) { sample_value = _sample_value; }
	
	protected String label;
	public void setLabel(String _label) { label = _label; }
	public String getLabel() { return label; }
	
	protected Boolean hidden = false;
	public Boolean isHidden() { return hidden; }
	public void setHidden(Boolean b) { hidden = b; }
	
	protected Boolean disabled = false;
	public Boolean isDisabled() { return disabled; }
	public void setDisabled(Boolean b) { disabled = b; }
	
	protected Boolean required = false;
	public Boolean isRequired() { return required; }
	public void setRequired(Boolean b) { required = b; }
}
