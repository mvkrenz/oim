package com.webif.divex.form;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.webif.divex.DivEx;
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
	protected ArrayList<IFormElementValidator<ValueType>> validators = new ArrayList();
	public void addValidator(IFormElementValidator<ValueType> _validator) { validators.add(_validator); }
	protected String error;
	private Boolean valid = true;
	public Boolean isValid() { 
		validate();
		return valid; 
	}
	public void setValid(Boolean b) { valid = b; }
	public void validate()
	{
		redraw();
		
		//if required, run RequiredValidator
		if(isRequired()) {
			if(value == null || value.toString().trim().length() == 0) {
				error = "Please select an item.";
				setValid(false);
				return;
			}
		}
		
		//then run the optional validation
		for(IFormElementValidator<ValueType> validator : validators) {
			if(!validator.isValid(value)) {
				//bad..
				error = validator.getMessage();
				setValid(false);
				return;
			}
		}
		
		//all good..
		error = null;
		setValid(true);
	}
	
	protected ValueType value;
	public void setValue(ValueType _value) { value = _value; }
	public ValueType getValue() { return value; }
	
	protected String label;
	public void setLabel(String _label) { label = _label; }
	public String getLabel() { return label; }
	
	private Boolean hidden = false;
	public Boolean isHidden() { return hidden; }
	public void setHidden(Boolean b) { hidden = b; }
	
	private Boolean disabled = false;
	public Boolean isDisabled() { return disabled; }
	public void setDisabled(Boolean b) { disabled = b; }
	
	private Boolean required = false;
	public Boolean isRequired() { return required; }
	public void setRequired(Boolean b) { required = b; }
}
