package com.webif.divex.form;

import com.webif.divex.DivEx;

abstract public class FormElementDEBase extends DivEx {
	protected FormElementDEBase(DivEx parent) {
		super(parent);
	}
	
	private Boolean valid = true;
	public Boolean isValid() { 
		validate();
		return valid; 
	}
	public void setValid(Boolean b) { valid = b; }
	abstract public void validate();
	
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
