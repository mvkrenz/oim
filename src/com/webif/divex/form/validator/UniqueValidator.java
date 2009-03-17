package com.webif.divex.form.validator;

import java.util.Collection;

public class UniqueValidator<T> implements IFormElementValidator<T>
{
	//no singleton - user must provide the list of prohibited values
	
	private Collection<T> prohibited;
	public UniqueValidator(Collection<T> _prohibited)
	{
		prohibited = _prohibited;
	}
	public Boolean isValid(T value) {
		return !(prohibited.contains(value));
	}
	
	public String getMessage()
	{
		return "This value is already used. Please enter different value.";
	}
}
