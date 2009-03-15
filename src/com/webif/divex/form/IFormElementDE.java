package com.webif.divex.form;

import com.webif.divex.ChangeEvent;
import com.webif.divex.DivEx;
import com.webif.divex.form.validator.IFormElementValidator;
import com.webif.divex.form.validator.RequiredValidator;

import edu.iu.grid.oim.view.divex.FormDivex;


abstract public interface IFormElementDE {
	public Boolean isValid();
	public String getName();
}
