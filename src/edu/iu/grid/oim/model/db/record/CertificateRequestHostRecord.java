package edu.iu.grid.oim.model.db.record;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cms.CMSException;

import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.cert.CertificateManager;

public class CertificateRequestHostRecord extends RecordBase {

	@Key public Integer id;

	public Integer requester_contact_id; //null if it's submitted by guest
	
	//guest contact info (used for goc ticket)
	public String requester_name;
	public String requester_email;
	
	public String csrs; //StringArray xml
	public String cns; //StringArray xml
	
	//return from DigiCert
	public String cert_certificate; //StringArray xml
	public String cert_intermediate; //StringArray xml
	public String cert_pkcs7; //StringArray xml
	public String cert_serial_ids; //StringArray xml
	public String cert_statuses; //StringArray xml
	public Integer approver_vo_id;
	
	//book keeping
	public Timestamp request_time;
	public Timestamp update_time;
	
	public String status;
	public String status_note;
	public String goc_ticket_id;
	
	public Date cert_notbefore;
	public Date cert_notafter;
	
	//load from existing record
	public CertificateRequestHostRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestHostRecord() {}
	
	public String[] getCNs() {
		StringArray sa = new StringArray(cns);
		return sa.getAll();
	}
	public String[] getPKCS7s() {
		StringArray sa = new StringArray(cert_pkcs7);
		return sa.getAll();
	}
	public String[] getCertificates() {
		StringArray sa = new StringArray(cert_certificate);
		return sa.getAll();
	}
	public String[] getCSRs() {
		StringArray sa = new StringArray(csrs);
		return sa.getAll();
	}
	public String[] getSerialIDs() {
		StringArray sa = new StringArray(cert_serial_ids);
		return sa.getAll();
	}
	public String[] getStatuses() {
		StringArray sa = new StringArray(cert_statuses);
		return sa.getAll();
	}
	public String getSigner() {
		String issuer_dn = ""; //by default
		ArrayList<Certificate> chain = null;
		try {
			if (this.getPKCS7s()[0] != null) {
				chain = CertificateManager.parsePKCS7(this.getPKCS7s()[0]);
				X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
				X500Principal issuer = c0.getIssuerX500Principal();
				issuer_dn = CertificateManager.X500Principal_to_ApacheDN(issuer);
				log.debug("signer is " + issuer_dn);
			}
		} catch (CertificateException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (CMSException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			//e2.printStackTrace(); -this will error for cilogon
			
		}
		return issuer_dn;
	}
}
