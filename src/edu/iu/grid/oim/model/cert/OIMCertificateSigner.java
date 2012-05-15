package edu.iu.grid.oim.model.cert;

import java.io.IOException;
import java.math.BigInteger;

import javax.security.auth.x500.X500PrivateCredential;
import javax.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

public class OIMCertificateSigner implements ICertificateSigner {
    static Logger log = Logger.getLogger(OIMCertificateSigner.class);  
    /*
    X500PrivateCredential rootCredential;
    X500PrivateCredential interCredential;
    */
    public OIMCertificateSigner() {
    	/*
        //create the CA certificates - TODO - load it from the file
    	rootCredential = Utils.createRootCredential();
    	interCredential = Utils.createIntermediateCredential(rootCredential.getPrivateKey(), rootCredential.getCertificate());
    	*/
    }

	@Override
	public Certificate signHostCertificate(String csr, String cn) throws CertificateProviderException {
		try {
			PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(Base64.decode(csr));
			X500Name csr_name = pkcs10.getSubject();
	
			/*
			BigInteger serial = new BigInteger("12345");
			X500Name issuser = interCredential.getCertificate().
			
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, serial, null, null, null, null);
			certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
			certGen.setIssuerDN(caCert.getSubjectX500Principal());
			certGen.setNotBefore(new Date(System.currentTimeMillis()));
			certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
			certGen.setSubjectDN(request.getCertificationRequestInfo().getSubject());
			certGen.setPublicKey(request.getPublicKey("BC"));
			certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
			
			// provide some basic extensions and mark the certificate as appropriate for signing and encipherment
			certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
			SubjectPublicKeyInfo publicinfo = pkcs10.getSubjectPublicKeyInfo();
			certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, pkcs10.getSubject());
			certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
			certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
		    */
		} catch (IOException e) {
			log.error("Failed to construct pkcs10 object from csr");
		}
		return null;
	}

	@Override
	public Certificate signUserCertificate(String csr, String dn) throws CertificateProviderException {
		try {
			PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(Base64.decode(csr));
		} catch (IOException e) {
			log.error("Failed to construct pkcs10 object from csr");
		}
		return null;
	}

	@Override
	public void revokeHostCertificate(String serial_id)
			throws CertificateProviderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revokeUserCertificate(String serial_id)
			throws CertificateProviderException {
		// TODO Auto-generated method stub
		
	}

}
