package edu.iu.grid.oim.model.cert;

//Interface for certificate authority
public interface ICertificateSigner {
	
	class CertificateProviderException extends Exception {

		public CertificateProviderException(Exception e) {
			super(e);
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
	
	public Certificate signHostCertificate(String csr, String cn) throws CertificateProviderException;
	public Certificate signUserCertificate(String csr, String dn) throws CertificateProviderException;
}
