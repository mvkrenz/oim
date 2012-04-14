package edu.iu.grid.oim.model.cert;

import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import sun.security.x509.X500Name;

public class CertificateManager {
	
	
	private ICertificateSigner cp;
	public CertificateManager() {
		cp  = new DigicertCertificateSigner();
	}

	public ICertificateSigner.Certificate signHostCertificate(X500Name x500, String domain) {
		//generate CSR for user
		GenerateCSR gcsr;
		try {
			gcsr = new GenerateCSR(x500);
			gcsr.saveDER("c:/trash");
			
			ICertificateSigner.Certificate cert = signHostCertificate(gcsr.getCSR(), domain);
			return cert;
			
		} catch (Exception e) {
			System.out.println("Failed to generate CSR");
			return null;
		}
	}
	
	//use user provided CSR
	public ICertificateSigner.Certificate signHostCertificate(String csr, String domain) throws CertificateProviderException {
		ICertificateSigner.Certificate cert = cp.signHostCertificate(csr, domain);
		return cert;
	}
	
    public static void main(String[] args) throws Exception {
  	  
    	CertificateManager m = new CertificateManager();    	
    	X500Name name = new X500Name(
        		"soichi.grid.iu.edu/emailAddress=hayashis@iu.edu", //CN common name
        		"PKITesting", //Organization unit
        		"OSG", //Organization name
        		"Bloomington", "Indiana", "US" //location, state, country
        );
    	ICertificateSigner.Certificate cert = m.signHostCertificate(name, "soichi.grid.iu.edu");
    	System.out.println(cert);
    }
}
