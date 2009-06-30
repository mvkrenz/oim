package com.webif.divrep;

public class DivRepEvent {
	public String action;
	public Object value;
	public DivRepEvent(String _action, Object _value) {
		action = _action;
		value = _value;
	}
}
