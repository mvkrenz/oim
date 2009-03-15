package com.webif.divex;

public class ChangeEvent extends Event {
	public String newvalue;
	public ChangeEvent(String _newvalue) {
		super(Type.CHANGE);
		newvalue = _newvalue;
	}
}
