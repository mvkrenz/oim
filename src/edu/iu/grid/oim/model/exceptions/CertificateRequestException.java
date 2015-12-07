package edu.iu.grid.oim.model.exceptions;
public class CertificateRequestException extends Exception {
	private static final long serialVersionUID = 1L;
	public CertificateRequestException(String message) {
		super(message);
	}
	public CertificateRequestException(String message, Exception e) {
		super(message, e);
	}
}