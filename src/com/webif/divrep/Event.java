package com.webif.divrep;

public class Event {
	public String action;
	public Object value;
	public Event(String _action, Object _value) {
		action = _action;
		value = _value;
	}
}
