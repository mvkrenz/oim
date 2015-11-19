package edu.iu.grid.oim.model.cert;

import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;

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
	
	public class CertificateBase {
		public CertificateBase() {
		}
		public String serial; //isser specific serial
		public String csr; //csr used to request this certificate
		
		//returned by signer
		public String certificate; //pkcs7 string
		public String intermediate; //pkcs7 string
		public String pkcs7; //pkcs7 string
		
		//expiration dates
		public Date notbefore;
		public Date notafter;
	}
	
	interface IHostCertificatesCallBack {
		public void certificateRequested();
		public void certificateSigned(CertificateBase cert, int idx);
	}
	public void signHostCertificates(CertificateBase[] certs,  IHostCertificatesCallBack callback) throws CertificateProviderException;
	public CertificateBase signUserCertificate(String csr, String dn, String email_address) throws CertificateProviderException;
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException;
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException;
	
	public String getUserDNBase();
	public String getHostDNBase();
}
