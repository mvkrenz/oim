package edu.iu.grid.oim.model.exceptions;
public class ConfigException extends Exception {
	private static final long serialVersionUID = 1L;
	public ConfigException(String message) {
		super(message);
	}
	public ConfigException(String message, Exception e) {
		super(message, e);
	}
}