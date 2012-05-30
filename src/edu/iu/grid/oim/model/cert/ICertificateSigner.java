package edu.iu.grid.oim.model.cert;

import edu.iu.grid.oim.lib.StringArray;

//Interface for certificate authority
public interface ICertificateSigner {
	
	class CertificateProviderException extends Exception {

		public CertificateProviderException(Exception e) {
			super(e);
		}
		public CertificateProviderException(String msg, Exception e) {
			super(msg, e);
		}
		public CertificateProviderException(String msg) {
			super(msg);
		}
		public CertificateProviderException() {	
		}
	};
	
	public class Certificate {
		public Certificate(String issuer) {
			this.issuer = issuer;
		}
		public String serial; //isser specific serial
		public String issuer;
		
		//returned by signer
		public String certificate; //pkcs7 string
		public String intermediate; //pkcs7 string
		public String pkcs7; //pkcs7 string
	}
	
	public Certificate[] signHostCertificates(StringArray csrs) throws CertificateProviderException;
	public Certificate signUserCertificate(String csr, String dn) throws CertificateProviderException;
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException;
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException;
	
}
