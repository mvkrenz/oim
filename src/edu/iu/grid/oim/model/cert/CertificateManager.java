package edu.iu.grid.oim.model.cert;

import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.cert.ICertificateSigner.Certificate;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;

public class CertificateManager {
	private ICertificateSigner cp;
	public CertificateManager() {
		cp  = new DigicertCertificateSigner();
	}

	/*
	public ICertificateSigner.Certificate signHostCertificates(X500Name x500) {
		//generate CSR for user
		GenerateCSR gcsr;
		try {
			
			gcsr = new GenerateCSR(x500);
			
			ICertificateSigner.Certificate cert = signHostCertificates(gcsr.getCSR());
			return cert;
			
		} catch (Exception e) {
			System.out.println("Failed to generate CSR");
			return null;
		}
	}
	*/
	
	//use user provided CSR
	public void signHostCertificates(Certificate[] certs, IHostCertificatesCallBack callback) throws CertificateProviderException {
		cp.signHostCertificates(certs, callback);
	}
	
	public ICertificateSigner.Certificate signUserCertificate(String csr, String cn, String email_address) throws CertificateProviderException {
		ICertificateSigner.Certificate cert = cp.signUserCertificate(csr, cn, email_address);
		return cert;
	}
	public void revokeUserCertificate(String serial_id) throws CertificateProviderException {
		cp.revokeUserCertificate(serial_id);
	}
	
	public void revokeHostCertificate(String serial_id) throws CertificateProviderException {
		cp.revokeHostCertificate(serial_id);
	}
	
	/*
    public static void main(String[] args) throws Exception {
  	  
    	CertificateManager m = new CertificateManager();    	
    	X500Name name = new X500Name("CN=\"Soichi Hayashi/emailAddress=hayashis@indiana.edu\", OU=PKITesting, O=OSG, L=Bloomington, ST=IN, C=United States");
    	ICertificateSigner.Certificate cert = m.signHostCertificates(name, "soichi.grid.iu.edu");
    	System.out.println(cert);
    }
    */
	
}
