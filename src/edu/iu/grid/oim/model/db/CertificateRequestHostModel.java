package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.cert.ICertificateSigner.Certificate;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class CertificateRequestHostModel extends CertificateRequestModelBase<CertificateRequestHostRecord> {
    static Logger log = Logger.getLogger(CertificateRequestHostModel.class);  
    
    public CertificateRequestHostModel(UserContext _context) {
		super(_context, "certificate_request_host");
	}
    
	//NO-AC
	public CertificateRequestHostRecord get(int id) throws SQLException {
		CertificateRequestHostRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE id = " + id)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		rec = new CertificateRequestHostRecord(rs);
			}
	    }	
	    stmt.close();
	    conn.close();
	    return rec;
	}
	
	//return requests that I have submitted
	public ArrayList<CertificateRequestHostRecord> getISubmitted(Integer id) throws SQLException {
		ArrayList<CertificateRequestHostRecord> ret = new ArrayList<CertificateRequestHostRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + id);	
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		ret.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return ret;
	}
	
	//return requests that I am GA
	public ArrayList<CertificateRequestHostRecord> getIApprove(Integer id) throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		
		//list all domains that user is gridadmin of
		StringBuffer cond = new StringBuffer();
		GridAdminModel model = new GridAdminModel(context);
		try {
			for(GridAdminRecord grec : model.getGridAdminsByContactID(id)) {
				if(cond.length() != 0) {
					cond.append(" OR ");
				}
				cond.append("cns LIKE '%"+StringEscapeUtils.escapeSql(grec.domain)+"</String>%'");
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup GridAdmin domains", e1);
		}	
		
		if(cond.length() != 0) {
			ResultSet rs = null;
			Connection conn = connectOIM();
			Statement stmt = conn.createStatement();
			stmt.execute("SELECT * FROM "+table_name + " WHERE "+cond.toString() + " AND status in ('REQUESTED','RENEW_REQUESTED','REVOKE_REQUESTED')");	
	    	rs = stmt.getResultSet();
	    	while(rs.next()) {
	    		recs.add(new CertificateRequestHostRecord(rs));
	    	}
		    stmt.close();
		    conn.close();
		}

	    return recs;
	}

	/*
	//NO-AC 
	//return pem encoded pkcs7s
	public String getPkcs7(CertificateRequestHostRecord rec) throws CertificateRequestException {
		StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
		if(pkcs7s.length() > idx) {
			return pkcs7s.get(idx);
		} else {
			throw new CertificateRequestException("Index is larger than the number of CSR requested");
		}
	}
	*/
	
	//NO-AC
	//issue all requested certs and store it back to DB
	//you can monitor request status by checking returned Certificate[]
	public void startissue(final CertificateRequestHostRecord rec) throws CertificateRequestException {
		// mark the request as "issuing.."
		try {
			rec.status = CertificateRequestStatus.ISSUING;
			context.setComment("Starting to issue certificates.");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to update certificate request status for request:" + rec.id);
			throw new CertificateRequestException("Failed to update certificate request status");
		};
		
		//reconstruct cert array from db
		final StringArray csrs = new StringArray(rec.csrs);
		final StringArray serial_ids = new StringArray(rec.cert_serial_ids);
		final StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
		final StringArray certificates = new StringArray(rec.cert_certificate);
		final StringArray intermediates = new StringArray(rec.cert_intermediate);
		final ICertificateSigner.Certificate[] certs = new ICertificateSigner.Certificate[csrs.length()];
		for(int c = 0; c < csrs.length(); ++c) {
			certs[c] = new ICertificateSigner.Certificate();
			certs[c].csr = csrs.get(c);
			certs[c].serial = serial_ids.get(c);
			certs[c].certificate = certificates.get(c);
			certs[c].intermediate = intermediates.get(c);
			certs[c].pkcs7 = pkcs7s.get(c);
		}

		new Thread(new Runnable() {
			public void failed(String message, Throwable e) {
				log.error(message, e);
				rec.status = CertificateRequestStatus.FAILED;
				rec.status_note = message + " :: " + e.getMessage();
				try {
					context.setComment(message + " :: " + e.getMessage());
					CertificateRequestHostModel.super.update(get(rec.id), rec);
				} catch (SQLException e1) {
					log.error("Failed to update request status while processing failed condition :" + message, e1);
				}
			}
			
			public void run() {
				CertificateManager cm = new CertificateManager();
				try {
					cm.signHostCertificates(certs, new IHostCertificatesCallBack() {
						@Override
						public void certificateSigned(Certificate cert, int idx) {
							/*
							//update certs db contents
							try {
								pkcs7s.set(idx,  cert.pkcs7);
								certificates.set(idx,  cert.certificate);
								intermediates.set(idx,  cert.intermediate);
								rec.cert_pkcs7 = pkcs7s.toXML();
								rec.cert_certificate = certificates.toXML();
								rec.cert_intermediate = intermediates.toXML();
								context.setComment("Certificate idx:"+idx+" has been issued");
								CertificateRequestHostModel.super.update(get(rec.id), rec);
							} catch (SQLException e) {
								log.error("Failed to update certificate update while monitoring issue progress:" + rec.id);
							};
							*/
							
							//pull some information from the cert for validation purpose
							java.security.cert.Certificate[] chain;
							try {
								chain = CertificateManager.parsePKCS7(cert.pkcs7);
								
								X509Certificate c0 = (X509Certificate)chain[0];
								Date cert_notafter = c0.getNotAfter();
								Date cert_notbefore = c0.getNotBefore();
								
								//do a bit of validation
								Calendar today = Calendar.getInstance();
								if(Math.abs(today.getTimeInMillis() - cert_notbefore.getTime()) > 1000*3600*24) {
									log.warn("Host certificate issued for request "+rec.id+"(idx:"+idx+") has cert_notbefore set too distance from current timestamp");
								}
								long dayrange = (cert_notafter.getTime() - cert_notbefore.getTime()) / (1000*3600*24);
								if(dayrange < 390 || dayrange > 400) {
									log.warn("Host certificate issued for request "+rec.id+ "(idx:"+idx+")  has valid range of "+dayrange+" days (too far from 395 days)");
								}
							
								//make sure dn starts with correct base
								X500Principal dn = c0.getSubjectX500Principal();
								String apache_dn = CertificateManager.X500Principal_to_ApacheDN(dn);
								String host_dn_base = StaticConfig.conf.getProperty("digicert.host_dn_base");
								if(!apache_dn.startsWith(host_dn_base)) {
									log.warn("Host certificate issued for request " + rec.id + "(idx:"+idx+")  has DN:"+apache_dn+" which doesn't have an expected DN base: "+host_dn_base);
								}
							} catch (CertificateException e1) {
								log.error("Failed to validate host certificate (pkcs7) issued. ID:" + rec.id+"(idx:"+idx+")", e1);
							} catch (CMSException e1) {
								log.error("Failed to validate host certificate (pkcs7) issued. ID:" + rec.id+"(idx:"+idx+")", e1);
							} catch (IOException e1) {
								log.error("Failed to validate host certificate (pkcs7) issued. ID:" + rec.id+"(idx:"+idx+")", e1);
							}

							
							//update status note
							try {
								rec.status_note = "Certificate idx:"+idx+" has been issued. Serial Number: " + cert.serial;
								context.setComment(rec.status_note);
								CertificateRequestHostModel.super.update(get(rec.id), rec);
							} catch (SQLException e) {
								log.error("Failed to update certificate update while monitoring issue progress:" + rec.id);
							}
						}
						
						@Override
						public void certificateRequested() {
							//update certs db contents
							try {
								for(int c = 0; c < certs.length; ++c) {
									Certificate cert = certs[c];
									serial_ids.set(c,  cert.serial);
								}
								rec.cert_serial_ids = serial_ids.toXML();
								context.setComment("Certificate requests has been sent.");
								CertificateRequestHostModel.super.update(get(rec.id), rec);
							} catch (SQLException e) {
								log.error("Failed to update certificate update while monitoring issue progress:" + rec.id);
							}
						}
					});

					//update records
					int idx = 0;
					StringArray cert_certificates = new StringArray(rec.cert_certificate);
					StringArray cert_intermediates = new StringArray(rec.cert_intermediate);
					StringArray cert_pkcs7s = new StringArray(rec.cert_pkcs7);
					StringArray cert_serial_ids = new StringArray(rec.cert_serial_ids);
					for(ICertificateSigner.Certificate cert : certs) {
						cert_certificates.set(idx, cert.certificate);
						rec.cert_certificate = cert_certificates.toXML();
						
						cert_intermediates.set(idx, cert.intermediate);
						rec.cert_intermediate = cert_intermediates.toXML();
						
						cert_pkcs7s.set(idx, cert.pkcs7);
						rec.cert_pkcs7 = cert_pkcs7s.toXML();
						
						cert_serial_ids.set(idx,  cert.serial);
						rec.cert_serial_ids = cert_serial_ids.toXML();
						
						++idx;
					}
					
					//update status
					try {
						rec.status = CertificateRequestStatus.ISSUED;
						context.setComment("All ceritificates has been issued.");
						CertificateRequestHostModel.super.update(get(rec.id), rec);
					} catch (SQLException e) {
						throw new CertificateRequestException("Failed to update status for certificate request: " + rec.id);
					}
					
					//update ticket
					Footprints fp = new Footprints(context);
					FPTicket ticket = fp.new FPTicket();
					Authorization auth = context.getAuthorization();
					if(auth.isUser()) {
						ContactRecord contact = auth.getContact();
						ticket.description = contact.name + " has issued certificate.";
					} else {
						ticket.description = "Someone with IP address: " + context.getRemoteAddr() + " has issued certificate";
					}
					ticket.status = "Resolved";
					fp.update(ticket, rec.goc_ticket_id);
				} catch (ICertificateSigner.CertificateProviderException e) {
					failed("Failed to sign certificate -- CertificateProviderException ", e);
				} catch(Exception e) {
					failed("Failed to sign certificate -- unhandled", e);	
				}
			}
		}).start();
	}
	
	//NO-AC
	//return true if success
    public void approve(CertificateRequestHostRecord rec) throws CertificateRequestException 
    {
    	//get number of certificate requested for this request
    	String [] cns = rec.getCNs();
    	int count = cns.length;
    	
    	//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canApproveHostCert(count)) {
    		throw new CertificateRequestException("You will exceed your host approval quota.");
    	}
    	
		rec.status = CertificateRequestStatus.APPROVED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
			quota.incrementHostCertApproval(count);
		} catch (SQLException e) {
			log.error("Failed to approve host certificate request: " + rec.id);
			throw new CertificateRequestException("Failed to update certificate request record");
		}
		
		//update ticket
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = "Dear " + rec.requester_name + ",\n\n";
		ticket.description += "Your host certificate request has been approved. \n\n";
		ticket.description += "To retrieve your certificate please visit " + getTicketUrl(rec) + " and click on Issue Certificate button.\n\n";
		ticket.description += "Or if you are using the command-line: osg-cert-retrieve -i "+rec.id+"\n\n";
		ticket.nextaction = "Requester to download certificate";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
		
    }
    
    //NO-AC (for authenticated user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsUser(String[] csrs, ContactRecord requester) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		//Date current = new Date();
    	rec.requester_contact_id = requester.id;
	 	rec.requester_name = requester.name;
    	rec.requester_email = requester.primary_email;
    	rec.requester_phone = requester.primary_phone;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		ticket.title = "Host Certificate Request by " + requester.name + "(OIM user)";
		ticket.metadata.put("SUBMITTER_NAME", requester.name);
		
    	return request(csrs, rec, ticket);
    }
    
    //NO-AC (for guest user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsGuest(String[] csrs, String requester_name, String requester_email, String requester_phone) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		//Date current = new Date();
	 	rec.requester_name = requester_name;
    	rec.requester_email = requester_email;
    	rec.requester_phone = requester_phone;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester_name;
		ticket.email = requester_email;
		ticket.phone = requester_phone;
		ticket.title = "Host Certificate Request by " + requester_name + "(Guest)";
		ticket.metadata.put("SUBMITTER_NAME", requester_name);
		
    	return request(csrs, rec, ticket);
    }
    
    private String getTicketUrl(CertificateRequestHostRecord rec) {
    	return "certificatehost?id=" + rec.id;
    }
    
    //NO-AC
	//return request record if successful, otherwise null (guest interface)
    private CertificateRequestHostRecord request(String[] csrs, CertificateRequestHostRecord rec, FPTicket ticket) throws CertificateRequestException 
    {
    	log.debug("request");
    	
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;
    	//rec.gridadmin_contact_id = null;
    	
    	log.debug("request init");
    	
    	GridAdminModel gmodel = new GridAdminModel(context);
    	StringArray csrs_sa = new StringArray(csrs.length);
    	StringArray cns_sa = new StringArray(csrs.length);
    	int idx = 0;
    	for(String csr_string : csrs) {
        	log.debug("processing csr: " + csr_string);
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));

	    		//pull CN
	    		X500Name name;
	    		RDN[] cn_rdn;
	    		try {
	    			name = csr.getSubject();
	    			cn_rdn = name.getRDNs(BCStyle.CN);
	    		} catch(Exception e) {
					throw new CertificateRequestException("Failed to decode CSR", e);
				}
	    		
	    		if(cn_rdn.length != 1) {
	    			throw new CertificateRequestException("Please specify exactly one CN containing the hostname. You have provided DN: " + name.toString());
	    		}
	    		cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
	    		
	        	log.debug("cn found: " + cn);
	    		
	    		cns_sa.set(idx, cn);
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new CertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new CertificateRequestException("Failed to base64 decode CSR", e);	
			}
			/*
			//lookup gridadmin
        	log.debug("looking up gridadmin");
			ContactRecord ga;
			try {
	        	log.debug("looking up gridadmin (2)");
				ga = gmodel.getGridAdminByFQDN(cn);
			} catch (SQLException e) {
				throw new CertificateRequestException("SQLException while looking up GridAdmin to approve host:" + cn, e);	
			}
			if(ga == null) {
				throw new CertificateRequestException("No GridAdmin can approve host:" + cn);	
			}
			
			//make sure single gridadmin approves all host
        	log.debug("validating gridadmin");
			if(rec.gridadmin_contact_id == null) {
				rec.gridadmin_contact_id = ga.id;
			} else {
				if(!rec.gridadmin_contact_id.equals(ga.id)) {
					throw new CertificateRequestException("All host must be approved by the same GridAdmin. Different for " + cn);	
				}
			}
			*/
				
			csrs_sa.set(idx++, csr_string);
    	}
    	
    	rec.csrs = csrs_sa.toXML();
    	rec.cns = cns_sa.toXML();
    	
    	StringArray ar = new StringArray(csrs.length);
    	rec.cert_certificate = ar.toXML();
    	rec.cert_intermediate = ar.toXML();
    	rec.cert_pkcs7 = ar.toXML();
    	rec.cert_serial_ids = ar.toXML();
    	
    	try {
        	log.debug("inserting request record");
			Integer request_id = super.insert(rec);
			
        	log.debug("request_id: " + request_id);
        	ContactRecord ga = findGridAdmin(rec);
			ticket.description = "Dear " + ga.name + " (GridAdmin), \n\n";
			ticket.description += "Host certificate request has been submitted.\n\n";
			ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + getTicketUrl(rec);
			ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.host.assignee"));
			ticket.ccs.add(ga.primary_email);
			
			ticket.nextaction = "RA/Sponsors to verify requester";
			Calendar nad = Calendar.getInstance();
			nad.add(Calendar.DATE, 7);
			ticket.nad = nad.getTime();
			
			//set metadata
			ticket.metadata.put("SUBMITTED_VIA", "OIM/CertManager(host)");
			if(auth.isUser()) {
				ticket.metadata.put("SUBMITTER_DN", auth.getUserDN());
			} 
			Footprints fp = new Footprints(context);
        	log.debug("opening footprints ticket");
			String ticket_id = fp.open(ticket);

        	log.debug("update request record with goc ticket id");
			rec.goc_ticket_id = ticket_id;
			context.setComment("Opened GOC Ticket " + ticket_id);
			super.update(get(request_id), rec);
		} catch (SQLException e) {
			throw new CertificateRequestException("Failed to insert host certificate request record", e);	
		}
		
    	return rec;
    }
    
    //find gridadmin who should process the request
    //null if none, or there are more than 1 
    public ContactRecord findGridAdmin(CertificateRequestHostRecord rec) throws CertificateRequestException {
		GridAdminModel gamodel = new GridAdminModel(context);
    	Integer gridadmin_contact_id = null;
    	int idx = 0;
    	for(String csr_string : rec.getCSRs()) {
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));

	    		//pull CN
	    		X500Name name = csr.getSubject();
	    		RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
	    		cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
	    	
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new CertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new CertificateRequestException("Failed to base64 decode CSR", e);	
			} catch(Exception e) {
				throw new CertificateRequestException("Failed to decode CSR", e);
			}
			
			//lookup gridadmin
			ContactRecord ga;
			try {
				ga = gamodel.getGridAdminByFQDN(cn);
			} catch (SQLException e) {
				throw new CertificateRequestException("Failed to lookup GridAdmin to approve host:" + cn, e);	
			}
			if(ga == null) {
				throw new CertificateRequestException("The hostname you have provided in the CSR/CN=" + cn + " does not match any domain OIM is currently configured to issue certificates for.\n\nPlease double check the CN you have specified. If you'd like to be a GridAdmin for this domain, please open GOC Ticket at https://ticket.grid.iu.edu ");	
			}
			
			//make sure single gridadmin approves all host
			if(gridadmin_contact_id == null) {
				gridadmin_contact_id = ga.id;
			} else {
				if(!gridadmin_contact_id.equals(ga.id)) {
					throw new CertificateRequestException("All host must be approved by the same GridAdmin. Different for " + cn);	
				}
			}
    	}
    	
    	if(gridadmin_contact_id == null) {
    		throw new CertificateRequestException("Failed to find gridadmin for user certificate request" + rec.id);
    	}
    	
		ContactModel cmodel = new ContactModel(context);
		ContactRecord gridadmin;
		try {
			gridadmin = cmodel.get(gridadmin_contact_id);
		} catch (SQLException e) {
			throw new CertificateRequestException("Failed to find user record for gridadmin id " + gridadmin_contact_id, e);
		}
		return gridadmin;
    }
    
	//NO-AC
	//return host rec if success
	public CertificateRequestHostRecord requestRenew(CertificateRequestHostRecord rec) throws CertificateRequestException {
		/*
    	//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canApproveHostCert()) {
    		throw new CertificateRequestException("Can't request any more host certificate.");
    	}
    	*/
    
		//lookup gridadmin first
		ContactRecord gridadmin = findGridAdmin(rec);
		
		rec.status = CertificateRequestStatus.RENEW_REQUESTED;
		try {
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request host certificate request renewal: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
		
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();		
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has requested renewal for this certificate request.";
		} else {
			ticket.description = "Guest user with IP:" + context.getRemoteAddr() + " has requested renewal of this certificate request.";		

		}
		ticket.description += "\n\n> " + context.getComment();
		ticket.description += "\n\nPlease approve / disapprove this request at " + getTicketUrl(rec);
		ticket.nextaction = "GridAdmin to verify and approve/reject"; //nad will be set to 7 days from today by default
		ticket.status = "Engineering"; //I need to reopen resolved ticket.
		
		//Update CC gridadmin (it might have been changed since last time request was made)
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ticket.ccs.add(gridadmin.primary_email);
		
		//reopen now
		fp.update(ticket, rec.goc_ticket_id);
		
		return rec;
	}
    
	//NO-AC
	public void requestRevoke(CertificateRequestHostRecord rec) throws CertificateRequestException {
		rec.status = CertificateRequestStatus.REVOCATION_REQUESTED;
		try {
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request revocation of host certificate: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
	
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has requested revocation of this certificate request.";
		} else {
			ticket.description = "Guest user with IP:" + context.getRemoteAddr() + " has requested revocation of this certificate request.";		
		}
		ticket.description += "\n\nPlease approve / disapprove this request at " + getTicketUrl(rec);
		ticket.nextaction = "Grid Admin to process request."; //nad will be set to 7 days from today by default
		ticket.status = "Engineering"; //I need to reopen resolved ticket.
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	//return true if success
	public void cancel(CertificateRequestHostRecord rec) throws CertificateRequestException {
		try {
			if(	rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
				rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
				rec.status = CertificateRequestStatus.ISSUED;
			} else {
				rec.status = CertificateRequestStatus.CANCELED;
			}
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to cancel host certificate request:" + rec.id);
			throw new CertificateRequestException("Failed to cancel request status", e);
		}
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has canceled this request.";
		} else {
			ticket.description = "guest shouldn't be canceling";
		}
		ticket.description += "\n\n> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	public void reject(CertificateRequestHostRecord rec) throws CertificateRequestException {
		if(	rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
				//go back to issued status if it's from renew_requested
				rec.status = CertificateRequestStatus.ISSUED;
			} else {
				//all others
				rec.status = CertificateRequestStatus.REJECTED;
			}
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to reject host certificate request:" + rec.id);
			throw new CertificateRequestException("Failed to reject request status", e);
		}
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has rejected this certificate request.\n\n";
		} else {
			throw new CertificateRequestException("Guest shouldn't be rejecting request");
		}
		ticket.description += "> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	public void revoke(CertificateRequestHostRecord rec) throws CertificateRequestException {
		
		//revoke
		CertificateManager cm = new CertificateManager();
		try {
			String[] cert_serial_ids = rec.getSerialIDs();
			for(String cert_serial_id : cert_serial_ids) {
				log.info("Revoking certificate with serial ID: " + cert_serial_id);
				cm.revokeHostCertificate(cert_serial_id);
			}
		} catch (CertificateProviderException e1) {
			log.error("Failed to revoke host certificate", e1);
			throw new CertificateRequestException("Failed to revoke host certificate", e1);
		}	
		
		rec.status = CertificateRequestStatus.REVOKED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to update host certificate status: " + rec.id);
			throw new CertificateRequestException("Failed to update host certificate status", e);
		}
	
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has revoked this certificate.";
		} else {
			throw new CertificateRequestException("Guest should'nt be revoking certificate");
		}
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//determines if user should be able to view request details, logs, and download certificate (pkcs12 is session specific)
	public boolean canView(CertificateRequestHostRecord rec) {
		return true;
		/*
		if(auth.isGuest()) {
			//right now, guest can't view any certificate requests
		} else if(auth.isUser()) {
			//super ra can see all requests
			if(auth.allows("admin_all_user_cert_requests")) return true;
			
			//is user the requester?
			ContactRecord contact = auth.getContact();
			if(rec.requester_id.equals(contact.id)) return true;
			
			//ra or sponsor for specified vo can view it
			VOContactModel model = new VOContactModel(context);
			ContactModel cmodel = new ContactModel(context);
			ArrayList<VOContactRecord> crecs;
			try {
				crecs = model.getByVOID(rec.vo_id);
				for(VOContactRecord crec : crecs) {
					ContactRecord contactrec = cmodel.get(crec.contact_id);
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(1)) { //primary
						if(contactrec.id.equals(contact.id)) return true;
					}
					
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(3)) { //sponsor
						if(contactrec.id.equals(contact.id)) return true;
					}
				}
			} catch (SQLException e1) {
				log.error("Failed to lookup RA/sponsor information", e1);
			}
		}
		return false;
		*/
	}
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			//^RA doesn't *approve* REVOKE_REQUESTED - RA just click on REVOKE button
			if(auth.isUser()) {
				//grid admin can appove it
				ContactRecord user = auth.getContact();
				try {
					ContactRecord gridadmin = findGridAdmin(rec);
					if(gridadmin.id.equals(user.id)) {
						return true;
					}
				} catch (CertificateRequestException e) {
					log.error("Failed to lookup gridadmin for " + rec.id + " while processing canApprove()", e);
				}
			}
		}
		return false;
	}    
	
	public boolean canReject(CertificateRequestHostRecord rec) {
		return canApprove(rec); //same rule as approval
	}	

	public boolean canCancel(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			//rec.status.equals(CertificateRequestStatus.APPROVED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				//requester can cancel one's own request
				if(rec.requester_contact_id != null) {//could be null if guest submitted it
					ContactRecord contact = auth.getContact();
					if(rec.requester_contact_id.equals(contact.id)) return true;
				}
				
				//grid admin can cancel it
				ContactRecord user = auth.getContact();
				try {
					ContactRecord gridadmin = findGridAdmin(rec);
					if(gridadmin.id.equals(user.id)) {
						return true;
					}
				} catch (CertificateRequestException e) {
					log.error("Failed to lookup gridadmin for " + rec.id + " while processing canCancel()", e);
				}
			}
		}
		return false;
	}
	
	//why can't we just issue certificate after it's been approved? because we might have to create pkcs12
	public boolean canIssue(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.APPROVED)) {		
			if(rec.requester_contact_id == null) {
				//anyone can issue guest request
				return true;
			} else {
				if(auth.isUser()) {
					ContactRecord contact = auth.getContact();
					//requester can issue
					if(rec.requester_contact_id.equals(contact.id)) return true;
				}
			}
		}
		return false;
	}
	
	public boolean canRequestRenew(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canRequestRevoke(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser() && canRevoke(rec)) {
				//if user can directly revoke it, no need to request it
				return false;
			}
			
			//all else, allow
			return true;
		}
		return false;
	}
	
	public boolean canRevoke(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			
			if(auth.isUser()) {
				/*
				if(auth.allows("revoke_all_certificate")) {
					return true;
				}
				*/
				
				//requester oneself can revoke it
				if(rec.requester_contact_id != null) {//could be null if guest submitted it
					ContactRecord contact = auth.getContact();
					if(rec.requester_contact_id.equals(contact.id)) return true;
				}
				
				//grid admin can revoke it
				ContactRecord user = auth.getContact();
				try {
					ContactRecord gridadmin = findGridAdmin(rec);
					if(gridadmin.id.equals(user.id)) {
						return true;
					}
				} catch (CertificateRequestException e) {
					log.error("Failed to lookup gridadmin for " + rec.id + " while processing canRevoke()", e);
				}
			}
		}
		return false;
	}
	
    //NO AC
	public CertificateRequestHostRecord getBySerialID(String serial_id) throws SQLException {
		CertificateRequestHostRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM "+table_name+ " WHERE cert_serial_ids like ?");
		pstmt.setString(1, "%>"+serial_id+"<%");
	    if (pstmt.executeQuery() != null) {
	    	rs = pstmt.getResultSet();
	    	if(rs.next()) {
	    		rec = new CertificateRequestHostRecord(rs);
			}
	    }	
	    pstmt.close();
	    conn.close();
	    return rec;
		
	}
	
	//pass null to not filter
	public ArrayList<CertificateRequestHostRecord> search(String cns_contains, String status, Date request_after, Date request_before) throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		String sql = "SELECT * FROM "+table_name+" WHERE 1 = 1";
		if(cns_contains != null) {
			sql += " AND cns like \"%"+StringEscapeUtils.escapeSql(cns_contains)+"%\"";
		}
		if(status != null) {
			sql += " AND status = \""+status+"\"";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(request_after != null) {
			sql += " AND request_time >= \""+sdf.format(request_after) + "\"";
		}
		if(request_before != null) {
			sql += " AND request_time <= \""+sdf.format(request_before) + "\"";
		}
		
		PreparedStatement stmt = conn.prepareStatement(sql);
	    rs = stmt.executeQuery();
	    while(rs.next()) {
    		recs.add(new CertificateRequestHostRecord(rs));
	    }
	    stmt.close();
	    conn.close();
	    return recs;
	}
	
	//prevent low level access - please use model specific actions
    @Override
    public Integer insert(RecordBase rec) throws SQLException
    { 
    	throw new UnsupportedOperationException("Please use model specific actions instead (request, approve, reject, etc..)");
    }
    @Override
    public void update(RecordBase oldrec, RecordBase newrec) throws SQLException
    {
    	throw new UnsupportedOperationException("Please use model specific actions insetead (request, approve, reject, etc..)");
    }
    @Override
    public void remove(RecordBase rec) throws SQLException
    {
    	throw new UnsupportedOperationException("disallowing remove cert request..");
    }
}
