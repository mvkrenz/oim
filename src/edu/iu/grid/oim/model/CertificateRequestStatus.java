package edu.iu.grid.oim.model;

public class CertificateRequestStatus {
	public final static String REQUESTED = "REQUESTED";
	public final static String REVOCATION_REQUESTED = "REVOCATION_REQUESTED";
	public final static String RENEW_REQUESTED = "RENEW_REQUESTED";
	public final static String APPROVED = "APPROVED";
	public final static String REJECTED = "REJECTED";
	public final static String CANCELED = "CANCELED";
	public final static String ISSUING = "ISSUING"; //used to indicate that certificate is being issued in a thread
	public final static String ISSUED = "ISSUED";
	public final static String EXPIRED = "EXPIRED";
	public final static String REVOKED = "REVOKED";
	public final static String FAILED = "FAILED";
}