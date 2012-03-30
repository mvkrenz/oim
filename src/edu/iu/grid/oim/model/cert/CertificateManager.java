package edu.iu.grid.oim.model.cert;

public class CertificateManager {
	
	private ICertificateProvider cp;
	public CertificateManager() {
		this.cp  = new DigicertCP();
	}
	
	public void requestCertificate() {
	
	}
}
