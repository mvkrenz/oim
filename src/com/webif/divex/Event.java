package com.webif.divex;

public class Event {
	public enum Type { CLICK, CHANGE };
	public Type type;
	public Event(Type _type) {
		type = _type;
	}
}
