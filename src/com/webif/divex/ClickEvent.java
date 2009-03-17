package com.webif.divex;

public class ClickEvent extends Event {
	public String value;
	public ClickEvent(String _value) {
		super(Type.CLICK);
		value = _value;
	}
}
