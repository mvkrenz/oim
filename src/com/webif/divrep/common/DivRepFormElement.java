package com.webif.divrep.common;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.validator.DivRepIValidator;

abstract public class DivRepFormElement<ValueType> extends DivRep {
	
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
	
	protected DivRepFormElement(DivRep parent) {
		super(parent);
	}
	
	//validation suite
	protected ArrayList<DivRepIValidator<ValueType>> validators = new ArrayList<DivRepIValidator<ValueType>>();
	public void addValidator(DivRepIValidator<ValueType> _validator) { validators.add(_validator); }
	protected ErrorDE error = new ErrorDE(this);
	protected class ErrorDE extends DivRep
	{
		public ErrorDE(DivRep _parent) {
			super(_parent);
			// TODO Auto-generated constructor stub
		}
		private String error;
		public void set(String _error) { error = _error; }
		public void clear() { error = null; }
		public String get() { return error; }
		protected void onEvent(DivRepEvent e) {
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
	
	//Override this to do custom validation (make sure to call super.validate() inside)
	//Also, you need to override this to do its own child element loop if one of the element is expected to be
	//dynamically removed. most likely, if you have dynamic elements, you are keeping up with your own list
	//of active elements. childnodes, on the other hand, doesn't handle removing of the element. Once it's there,
	//it's there forever. So unnecessary validation may occur unless you override this to only loop your own
	//elements
	public void validate()
	{
		boolean original = valid;
		
		//validate *all* child elements first
		boolean children_valid = true;
		for(DivRep child : childnodes) {
			if(child instanceof DivRepFormElement) { 
				DivRepFormElement element = (DivRepFormElement)child;
				if(element != null && !element.isHidden()) {
					if(!element.isValid()) {
						children_valid = false;
						//continue validating other children
					}
				}
			}
		}
		
		if(children_valid) {
			//if child is valid, then let's validate myself..
			error.set(null);
			valid = true;
			
			if(value == null || value.toString().trim().length() == 0) {
				if(isRequired()) {
					error.set("This is a required field. Please specify a value.");
					valid = false;
				} 
			} else {
				//then run the optional validation
				for(DivRepIValidator<ValueType> validator : validators) {
					if(!validator.isValid(value)) {
						//bad..
						error.set(validator.getErrorMessage());
						valid = false;
						break;
					}
				}
			}
		} else {
			valid = false;
		}
		
		if(original != valid) {
			error.redraw();
		}
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
