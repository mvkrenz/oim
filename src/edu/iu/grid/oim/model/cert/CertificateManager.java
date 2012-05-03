package edu.iu.grid.oim.model.cert;

import org.bouncycastle.asn1.x500.X500Name;

import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;

public class CertificateManager {
	
	
	private ICertificateSigner cp;
	public CertificateManager() {
		cp  = new DigicertCertificateSigner();
	}

	public ICertificateSigner.Certificate signHostCertificate(X500Name x500, String cn) {
		//generate CSR for user
		GenerateCSR gcsr;
		try {
			gcsr = new GenerateCSR(x500);
			//gcsr.saveDER("c:/trash");
			
			ICertificateSigner.Certificate cert = signHostCertificate(gcsr.getCSR(), cn);
			return cert;
			
		} catch (Exception e) {
			System.out.println("Failed to generate CSR");
			return null;
		}
	}
	
	//use user provided CSR
	public ICertificateSigner.Certificate signHostCertificate(String csr, String cn) throws CertificateProviderException {
		ICertificateSigner.Certificate cert = cp.signHostCertificate(csr, cn);
		return cert;
	}
	
	public ICertificateSigner.Certificate signUserCertificate(String csr, String dn) throws CertificateProviderException {
		ICertificateSigner.Certificate cert = cp.signUserCertificate(csr, dn);
		return cert;
	}
	
    public static void main(String[] args) throws Exception {
  	  
    	CertificateManager m = new CertificateManager();    	
    	X500Name name = new X500Name("CN=\"Soichi Hayashi/emailAddress=hayashis@indiana.edu\", OU=PKITesting, O=OSG, L=Bloomington, ST=IN, C=United States");
    	ICertificateSigner.Certificate cert = m.signHostCertificate(name, "soichi.grid.iu.edu");
    	System.out.println(cert);
    }
}
