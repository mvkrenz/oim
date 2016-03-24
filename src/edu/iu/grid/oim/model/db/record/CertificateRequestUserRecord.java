package edu.iu.grid.oim.model.db.record;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

public class CertificateRequestUserRecord extends RecordBase {

	@Key public Integer id;
	
	//either one of these should be set
	public Integer requester_contact_id; //for oim user
	
	@NoLog public String requester_passphrase; //only used if guest submits request & user doesn't provide CSR
	@NoLog public String requester_passphrase_salt;
	public String csr; //only set if user provides csr
	public String cert_certificate;
	public String cert_intermediate;
	public String cert_pkcs7;
	public String cert_serial_id;
	public String dn; 
	public Timestamp request_time;
	public String status;
	public Integer vo_id;
	public String goc_ticket_id;
	public String signer;
	
	public Date cert_notbefore;
	public Date cert_notafter;
	
	//load from existing record
	public CertificateRequestUserRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestUserRecord() {}
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Utility functions
	//convert apache format (delimited by /) to comma delimited DN (RFC1779)
	private String ApacheDN_to_RFC1779(String dn) {
		String tokens[] = dn.split("/");
		tokens = (String[]) ArrayUtils.remove(tokens, 0);//remove first one which is empty
		String out = StringUtils.join(tokens, ",");
		return out;
	}
	public X500Name getX500Name() {
		String rfc_dn = ApacheDN_to_RFC1779(dn);
		return new X500Name(rfc_dn);
	}
	public String getCN() {
		X500Name name = getX500Name();
		RDN cn_rdn = name.getRDNs(BCStyle.CN)[0];
		String cn = cn_rdn.getFirst().getValue().toString(); //wtf?
		return cn;
	}
	

}
