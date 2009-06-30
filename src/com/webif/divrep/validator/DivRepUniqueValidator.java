package com.webif.divrep.validator;

import java.util.Collection;

public class DivRepUniqueValidator<T> implements DivRepIValidator<T>
{
	//no singleton - user must provide the list of prohibited values
	
	private Collection<T> prohibited;
	public DivRepUniqueValidator(Collection<T> _prohibited)
	{
		prohibited = _prohibited;
	}
	public Boolean isValid(T value) {
		return !(prohibited.contains(value));
	}
	
	public String getErrorMessage()
	{
		return "This value is already used. Please enter different value.";
	}
}
