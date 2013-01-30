package edu.iu.grid.oim.model;

public class CertificateRequestStatus {
	public final static String REQUESTED = "REQUESTED";
	public final static String REVOCATION_REQUESTED = "REVOCATION_REQUESTED";
	//public final static String RENEW_REQUESTED = "RENEW_REQUESTED";
	public final static String APPROVED = "APPROVED";
	public final static String REJECTED = "REJECTED";
	public final static String CANCELED = "CANCELED";
	public final static String ISSUING = "ISSUING"; //used to indicate that certificate is being issued in a thread
	public final static String ISSUED = "ISSUED";
	public final static String EXPIRED = "EXPIRED";
	public final static String REVOKED = "REVOKED";
	public final static String FAILED = "FAILED";
	
	public final static int toInt(String status) {
		if(status.equals(REQUESTED)) return 0;
		if(status.equals(REVOCATION_REQUESTED)) return 1;
		//if(status.equals(RENEW_REQUESTED)) return 2;
		if(status.equals(APPROVED)) return 3;
		if(status.equals(REJECTED)) return 4;
		if(status.equals(CANCELED)) return 5;
		if(status.equals(ISSUING)) return 6;
		if(status.equals(ISSUED)) return 7;
		if(status.equals(EXPIRED)) return 8;
		if(status.equals(REVOKED)) return 9;
		if(status.equals(FAILED)) return 10;
		
		return -1;
	}
	public final static String toStatus(int i) {
		switch(i) {
		case 0: return REQUESTED;
		case 1: return REVOCATION_REQUESTED;
		//case 2: return RENEW_REQUESTED;
		case 3: return APPROVED;
		case 4: return REJECTED;
		case 5: return CANCELED;
		case 6: return ISSUING;
		case 7: return ISSUED;
		case 8: return EXPIRED;
		case 9: return REVOKED;
		case 10: return FAILED;
		}
		return null;
	}
}