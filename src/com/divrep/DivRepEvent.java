package com.divrep;

public class DivRepEvent<T> {
	public String action;
	public T value;
	public DivRepEvent(String _action, T _value) {
		action = _action;
		value = _value;
	}
}
