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
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import org.apache.commons.lang.StringEscapeUtils;
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
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
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
				
				//update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = "Failed to issue certificate.\n\n";
				ticket.description += message+"\n\n";
				ticket.description += e.getMessage()+"\n\n";
				ticket.description += "The alert has been sent to GOC alert for further actions on this issue.";
				ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.fail.assignee"));
				ticket.nextaction = "GOC developer to investigate";
				fp.update(ticket, rec.goc_ticket_id);
			}
			
			//should we use Quartz instead?
			public void run() {
				CertificateManager cm = new CertificateManager();
				try {
					cm.signHostCertificates(certs, new IHostCertificatesCallBack() {
						@Override
						public void certificateSigned(Certificate cert, int idx) {
							
							log.info("host cert issued by digicert: serial_id:" + cert.serial);
							log.info("pkcs7:" + cert.pkcs7);
							
							//pull some information from the cert for validation purpose
							java.security.cert.Certificate[] chain;
							try {
								chain = CertificateManager.parsePKCS7(cert.pkcs7);
								
								X509Certificate c0 = (X509Certificate)chain[0];
								cert.notafter = c0.getNotAfter();
								cert.notbefore = c0.getNotBefore();
								
								//do a bit of validation
								Calendar today = Calendar.getInstance();
								if(Math.abs(today.getTimeInMillis() - cert.notbefore.getTime()) > 1000*3600*24) {
									log.warn("Host certificate issued for request "+rec.id+"(idx:"+idx+") has cert_notbefore set too distance from current timestamp");
								}
								long dayrange = (cert.notafter.getTime() - cert.notbefore.getTime()) / (1000*3600*24);
								if(dayrange < 390 || dayrange > 405) {
									log.warn("Host certificate issued for request "+rec.id+ "(idx:"+idx+")  has invalid range of "+dayrange+" days (too far from 395 days)");
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
					
					//set cert expiriation dates using the first certificate issued
					ICertificateSigner.Certificate cert = certs[0];
					rec.cert_notafter = cert.notafter;
					rec.cert_notbefore = cert.notbefore;
					
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
						ticket.description = contact.name + " has issued certificate. Resolving this ticket.";
					} else {
						ticket.description = "Someone with IP address: " + context.getRemoteAddr() + " has issued certificate. Resolving this ticket.";
					}
					ticket.status = "Resolved";
					
					//suppressing notification if submitter is GA
					ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id); 
					boolean submitter_is_ga = false;
					for(ContactRecord ga : gas) {
						if(ga.id.equals(rec.requester_contact_id)) {
							submitter_is_ga = true;
							break;
						}
					}
					if(submitter_is_ga) {
						ticket.mail_suppression_assignees = true;
						ticket.mail_suppression_submitter = true;
						ticket.mail_suppression_ccs = true;
					}
					
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
    		throw new CertificateRequestException("You will exceed your host certificate quota.");
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
		
		//find if requester is ga
		ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id); 
		boolean submitter_is_ga = false;
		for(ContactRecord ga : gas) {
			if(ga.id.equals(rec.requester_contact_id)) {
				submitter_is_ga = true;
				break;
			}
		}
		
		//update ticket
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		if(submitter_is_ga) {
			ticket.description = rec.requester_name + " has approved this host certificate request.\n\n";
			ticket.mail_suppression_assignees = false;
			ticket.mail_suppression_ccs = true;
			ticket.mail_suppression_submitter = true;
		} else {
			ticket.description = "Dear " + rec.requester_name + ",\n\n";
			ticket.description += "Your host certificate request has been approved. \n\n";
		}
		ticket.description += "To retrieve the certificate please visit " + getTicketUrl(rec.id) + " and click on Issue Certificate button.\n\n";
    	if(StaticConfig.isDebug()) {
    		ticket.description += "Or if you are using the command-line: osg-cert-retrieve -T -i "+rec.id+"\n\n";
    	} else {
    		ticket.description += "Or if you are using the command-line: osg-cert-retrieve -i "+rec.id+"\n\n";  		
    	}
		ticket.nextaction = "Requester to download certificate";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
		
    }
    
    //NO-AC (for authenticated user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsUser(
    		ArrayList<String> csrs, 
    		ContactRecord requester, 
    		String request_comment, 
    		String[] request_ccs, 
    		Integer approver_vo_id) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		//Date current = new Date();
    	rec.requester_contact_id = requester.id;
	 	rec.requester_name = requester.name;
	 	rec.approver_vo_id = approver_vo_id;
    	//rec.requester_email = requester.primary_email;
    	//rec.requester_phone = requester.primary_phone;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		ticket.title = "Host Certificate Request by " + requester.name + "(OIM user)";
		ticket.metadata.put("SUBMITTER_NAME", requester.name);
		if(request_ccs != null) {
			for(String cc : request_ccs) {
				ticket.ccs.add(cc);
			}
		}
		
    	return request(csrs, rec, ticket, request_comment);
    }
    
    //NO-AC (for guest user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsGuest(
    		ArrayList<String> csrs, 
    		String requester_name, 
    		String requester_email, 
    		String requester_phone, 
    		String request_comment, 
    		String[] request_ccs,
    		Integer approver_vo_id) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		//Date current = new Date();
    	rec.approver_vo_id = approver_vo_id;
	 	rec.requester_name = requester_name;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester_name;
		ticket.email = requester_email;
		ticket.phone = requester_phone;
		ticket.title = "Host Certificate Request by " + requester_name + "(Guest)";
		ticket.metadata.put("SUBMITTER_NAME", requester_name);
		if(request_ccs != null) {
			for(String cc : request_ccs) {
				ticket.ccs.add(cc);
			}
		}
		
    	return request(csrs, rec, ticket, request_comment);
    }
    
    private String getTicketUrl(Integer request_id) {
    	String base;
    	//this is not an exactly correct assumption, but it should be good enough
    	if(StaticConfig.isDebug()) {
    		base = "https://oim-itb.grid.iu.edu/oim/";
    	} else {
    		base = "https://oim.grid.iu.edu/oim/";
    	}
    	return base + "certificatehost?id=" + request_id;
    }
    
    //NO-AC
	//return request record if successful, otherwise null (guest interface)
    private CertificateRequestHostRecord request(
    		ArrayList<String> csrs, 
    		CertificateRequestHostRecord rec, 
    		FPTicket ticket, 
    		String request_comment) throws CertificateRequestException 
    {
    	//log.debug("request");
    	
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;

		if(request_comment != null) {
			rec.status_note = request_comment;
			context.setComment(request_comment);
		}
		
    	//store CSRs / CNs to record
    	StringArray csrs_sa = new StringArray(csrs.size());
    	StringArray cns_sa = new StringArray(csrs.size());
    	int idx = 0;
    	for(String csr_string : csrs) {
        	log.debug("processing csr: " + csr_string);
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = parseCSR(csr_string);
	    		cn = pullCNFromCSR(csr);
	    		
	    		//validate CN
	    		if(!cn.matches("^([-0-9a-zA-Z\\.]*/)?[-0-9a-zA-Z\\.]*$")) { //OSGPKI-255
					throw new CertificateRequestException("CN structure is invalid, or contains invalid characters.");
	    		}
	    		
	    		//check private key strength
	    		SubjectPublicKeyInfo pkinfo = csr.getSubjectPublicKeyInfo();
	    		RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkinfo);	
	    		int keysize = rsa.getModulus().bitLength();
	    		if(keysize < 2048) {
	    			throw new CertificateRequestException("Please use RSA keysize greater than or equal to 2048 bits.");
	    		}
	    		
	    		cns_sa.set(idx, cn);
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new CertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new CertificateRequestException("Failed to base64 decode CSR", e);	
			}				
			csrs_sa.set(idx++, csr_string);
    	}
    	rec.csrs = csrs_sa.toXML();
    	rec.cns = cns_sa.toXML();
    	
    	StringArray ar = new StringArray(csrs.size());
    	rec.cert_certificate = ar.toXML();
    	rec.cert_intermediate = ar.toXML();
    	rec.cert_pkcs7 = ar.toXML();
    	rec.cert_serial_ids = ar.toXML();
    	
    	try {			
			ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id); 
			//find if submitter is ga
			boolean submitter_is_ga = false;
			for(ContactRecord ga : gas) {
				if(ga.id.equals(rec.requester_contact_id)) {
					submitter_is_ga = true;
					break;
				}
			}
			
			//now submit - after this, we are commited.
			Integer request_id = super.insert(rec);
			
			if(submitter_is_ga) {
				ticket.description = "Host certificate request has been submitted by a GridAdmin.\n\n";
				ticket.mail_suppression_assignees = true;
				ticket.mail_suppression_submitter = true;
				ticket.mail_suppression_ccs = true;
			} else {
	        	ticket.description = "Dear GridAdmin; ";
				for(ContactRecord ga : gas) {
					ticket.description += ga.name + ", ";
					ticket.ccs.add(ga.primary_email);
				}
				ticket.description += "\n\n";
				ticket.description += "Host certificate request has been submitted. ";
				ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + getTicketUrl(request_id) + "\n\n";
			}	
			
			Authorization auth = context.getAuthorization();
			ticket.description += "Requester IP:" + context.getRemoteAddr() + "\n";
			if(auth.isUser()) {
				ContactRecord user = auth.getContact();
				//ticket.description += "OIM User Name:" + user.name + "\n";
				ticket.description += "Submitter is OIM authenticated with DN:" + auth.getUserDN() + "\n";
			}
			
			if(request_comment != null) {
				ticket.description += "Requester Comment: "+request_comment;
			}
			ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.host.assignee"));
			
			ticket.nextaction = "GridAdmin to verify requester";
			Calendar nad = Calendar.getInstance();
			nad.add(Calendar.DATE, 7);
			ticket.nad = nad.getTime();
			
			//set metadata
			ticket.metadata.put("SUBMITTED_VIA", "OIM/CertManager(host)");
			if(auth.isUser()) {
				ticket.metadata.put("SUBMITTER_DN", auth.getUserDN());
			} 
			
			//all ready to submit request
			
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
    
	public PKCS10CertificationRequest parseCSR(String csr_string) throws IOException {
		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));
		return csr;
	}
    public String pullCNFromCSR(PKCS10CertificationRequest csr) throws CertificateRequestException {
		//pull CN from pkcs10
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
		String cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
		
    	//log.debug("cn found: " + cn);
    	return cn;
    }
    
    //find gridadmin who should process the request - identify domain from csrs
    //if there are more than 1 vos group, then user must specify whic hone via approver_vo_id (it could be null for gridadmin with only 1 vo group)
    //null if none, or there are more than 1 
    public ArrayList<ContactRecord> findGridAdmin(String[] csrs, Integer approver_vo_id) throws CertificateRequestException {
		GridAdminModel gamodel = new GridAdminModel(context);
    	String gridadmin_domain = null;
    	int idx = 0;
    
    	if(csrs.length == 0) {
    		throw new CertificateRequestException("No CSR");
    	}
    	
    	for(String csr_string : csrs) {
    		//parse CSR and pull CN
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = parseCSR(csr_string);
	    		cn = pullCNFromCSR(csr);
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new CertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new CertificateRequestException("Failed to base64 decode CSR", e);	
			} catch(Exception e) {
				throw new CertificateRequestException("Failed to decode CSR", e);
			}
			
			//lookup registered gridadmin domain
			String domain = null;
			try {
				domain = gamodel.getDomainByFQDN(cn);
			} catch (SQLException e) {
				throw new CertificateRequestException("Failed to lookup GridAdmin to approve host:" + cn, e);	
			}
			if(domain == null) {
				throw new CertificateRequestException("The hostname you have provided in the CSR/CN=" + cn + " does not match any domain OIM is currently configured to issue certificates for.\n\nPlease double check the CN you have specified. If you'd like to be a GridAdmin for this domain, please open GOC Ticket at https://ticket.grid.iu.edu ");	
			}
			
			//make sure same set of gridadmin approves all host
			if(gridadmin_domain == null) {
				gridadmin_domain = domain;
			} else {
				if(!gridadmin_domain.equals(domain)) {
					throw new CertificateRequestException("All host certificates must be approved by the same set of gridadmins. Different for " + cn);	
				}
			}
    	}
		try {
			HashMap<VORecord, ArrayList<GridAdminRecord>> groups = gamodel.getByDomainGroupedByVO(gridadmin_domain);
			if(groups.size() == 0) {
				throw new CertificateRequestException("No gridadmin exists for domain: " + gridadmin_domain);
			}
			if(groups.size() == 1 && approver_vo_id == null) {
				//just return the first group
				for(VORecord vo : groups.keySet()) {
					ArrayList<GridAdminRecord> gas = groups.get(vo);
					return GAsToContacts(gas);
				}
			}
			//use a group user specified - must match
			String vonames = "";
			for(VORecord vo : groups.keySet()) {
				vonames += vo.name + ", "; //just in case we might need to report error message later
				if(vo.id.equals(approver_vo_id)) {
					//found a match.. return the list
					ArrayList<GridAdminRecord> gas = groups.get(vo);
					return GAsToContacts(gas);
				}
			}
			//oops.. didn't find specified vo..
			throw new CertificateRequestException("Couldn't find GridAdmin group under specified VO. Please use one of following VOs:" + vonames);
		} catch (SQLException e) {
			throw new CertificateRequestException("Failed to lookup gridadmin contacts for domain:" + gridadmin_domain, e);
		}
    }
    
    public ArrayList<ContactRecord> GAsToContacts(ArrayList<GridAdminRecord> gas) throws SQLException {
		//convert contact_id to contact record 
		ArrayList<ContactRecord> contacts = new ArrayList<ContactRecord>();
		ContactModel cmodel = new ContactModel(context);
		for(GridAdminRecord ga : gas) {
			contacts.add(cmodel.get(ga.contact_id));
		}
		return contacts;
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
		ticket.description += "\n\nPlease approve / disapprove this request at " + getTicketUrl(rec.id);
		ticket.nextaction = "Grid Admin to process request."; //nad will be set to 7 days from today by default
		ticket.status = "Engineering"; //I need to reopen resolved ticket.
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	//return true if success
	public void cancel(CertificateRequestHostRecord rec) throws CertificateRequestException {
		try {
			if(	//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
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
			ticket.description = contact.name + " has canceled this request.\n\n";
		} else {
			//Guest can still cancel by providing the password used to submit the request.
		}
		ticket.description += "\n\n> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	public void reject(CertificateRequestHostRecord rec) throws CertificateRequestException {	
		if(	//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
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
			ticket.description = contact.name + " has revoked this certificate.\n\n";
		} else {
			throw new CertificateRequestException("Guest shouldn't be revoking certificate");
		}
		ticket.description += "> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//determines if user should be able to view request details, logs, and download certificate (pkcs12 is session specific)
	public boolean canView(CertificateRequestHostRecord rec) {
		return true;
	}
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REQUESTED)) {
			//^RA doesn't *approve* REVOKE_REQUESTED - RA just click on REVOKE button
			if(auth.isUser()) {
				//grid admin can appove it
				ContactRecord user = auth.getContact();
				try {
					ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id);
					for(ContactRecord ga : gas) {
						if(ga.id.equals(user.id)) {
							return true;
						}
					}
				} catch (CertificateRequestException e) {
					log.error("Failed to lookup gridadmin for " + rec.id + " while processing canApprove()", e);
				}
			}
		}
		return false;
	}    
	
	public LogDetail getLastApproveLog(ArrayList<LogDetail> logs) {
		for(LogDetail log : logs) {
			if(log.status.equals("APPROVED")) {
				return log;
			}
		}
		return null;
	}
	/*
	//true if user can approve request
	public boolean canRenew(CertificateRequestHostRecord rec, ArrayList<LogDetail> logs) {
		if(!canView(rec)) return false;
		
		//only issued request can be renewed
		if(!rec.status.equals(CertificateRequestStatus.ISSUED)) return false;
		
		//logged in?
		if(!auth.isUser()) return false;
		
		//original requester or gridadmin?
		ContactRecord contact = auth.getContact();
		if(!rec.requester_contact_id.equals(contact.id) && !canApprove(rec)) return false;

		//approved within 5 years?
		LogDetail last = getLastApproveLog(logs);
		if(last == null) return false; //never approved
		Calendar five_years_ago = Calendar.getInstance();
		five_years_ago.add(Calendar.YEAR, -5);
		if(last.time.before(five_years_ago.getTime())) return false;
	
		
		//TODO -- will expire in less than 6 month? (can I gurantee that all certificates has the same expiration date?)
		//Calendar six_month_future = Calendar.getInstance();
		//six_month_future.add(Calendar.MONTH, 6);
		//if(rec.cert_notafter.after(six_month_future.getTime())) return false;
		
		
		//all good
		return true;
	}
	*/
	
	public boolean canReject(CertificateRequestHostRecord rec) {
		return canApprove(rec); //same rule as approval
	}	

	public boolean canCancel(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.APPROVED) || //if renew_requesterd > approved cert is canceled, it should really go back to "issued", but currently it doesn't.
			//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				if(auth.allows("admin_gridadmin")) return true; //if user has admin_gridadmin priv (probably pki staff), then he/she can cancel it

				//requester can cancel one's own request
				if(rec.requester_contact_id != null) {//could be null if guest submitted it
					ContactRecord contact = auth.getContact();
					if(rec.requester_contact_id.equals(contact.id)) return true;
				}
				
				//grid admin can cancel it
				ContactRecord user = auth.getContact();
				try {
					ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id);
					for(ContactRecord ga : gas) {
						if(ga.id.equals(user.id)) {
							return true;
						}
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
	
	/*
	public boolean canRequestRenew(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser()) {
				return true;
			}
		}
		return false;
	}
	*/
	
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
				if(auth.allows("admin_gridadmin")) return true; //if user has admin_gridadmin priv (probably pki staff), then he/she can revoke it
				
				//requester oneself can revoke it
				if(rec.requester_contact_id != null) {//could be null if guest submitted it
					ContactRecord contact = auth.getContact();
					if(rec.requester_contact_id.equals(contact.id)) return true;
				}
				
				//grid admin can revoke it
				ContactRecord user = auth.getContact();
				try {
					ArrayList<ContactRecord> gas = findGridAdmin(rec.getCSRs(), rec.approver_vo_id);
					for(ContactRecord ga : gas) {
						if(ga.id.equals(user.id)) {
							return true;
						}
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
    public Integer insert(CertificateRequestHostRecord rec) throws SQLException
    { 
    	throw new UnsupportedOperationException("Please use model specific actions instead (request, approve, reject, etc..)");
    }
    @Override
    public void update(CertificateRequestHostRecord oldrec, CertificateRequestHostRecord newrec) throws SQLException
    {
    	throw new UnsupportedOperationException("Please use model specific actions insetead (request, approve, reject, etc..)");
    }
    @Override
    public void remove(CertificateRequestHostRecord rec) throws SQLException
    {
    	throw new UnsupportedOperationException("disallowing remove cert request..");
    }

	@Override
	CertificateRequestHostRecord createRecord() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<CertificateRequestHostRecord> findUpdatedIn(Integer morethan, Integer lessthan) throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE status = 'ISSUED' AND -----condition for expiration date-------");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}
	
	public void notifyExpiringIn(Integer days_less_than) throws SQLException {
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		ContactModel cmodel = new ContactModel(context);
		
		//process host certificate requests
		for(CertificateRequestHostRecord rec : findExpiringIn(days_less_than)) {
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			
			//user can't renew host certificate, so instead of keep notifying, let's just notify only once by limiting the time window
			//when notification can be sent out
			Calendar today = Calendar.getInstance();
			if((rec.cert_notafter.getTime() - today.getTimeInMillis()) < 1000*3600*24*23) {
				log.info("Aborting expiration notification for host certificate " + rec.id + " - it's expiring in less than 23 days");
				continue;
			}
			
			//send notification
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = "Dear " + requester.name + ",\n\n";
			ticket.description += "Your host certificates requested in this ticket will expire in "+days_less_than+" days\n\n";
			
			//ArrayList<CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail> logs = getLogs(CertificateRequestHostModel.class, rec.id);
			ticket.description += "Please request for new host certificate(s) for replacements.\n\n";
			
			ticket.description += "Please visit "+getTicketUrl(rec.id)+" for more details.\n\n";
			
			//TODO - clear CC list (or suppress cc-email)
			
			//I don't have to reopen the ticket since this is one time notification
			if(StaticConfig.isDebug()) {
				log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expiring host certificate");
				log.debug(ticket.description);
			} else {
				fp.update(ticket, rec.goc_ticket_id);
				log.info("updated goc ticket : " + rec.goc_ticket_id + " to notify expiring host certificate");
			}

		}
	}
	
	public ArrayList<CertificateRequestHostRecord> findExpiringIn(Integer days) throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE status = '"+CertificateRequestStatus.ISSUED+"' AND CURDATE() > DATE_SUB( cert_notafter, INTERVAL "+days+" DAY )");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}

	public void processCertificateExpired() throws SQLException {
		
		//search for expired certificates
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE status = '"+CertificateRequestStatus.ISSUED+"' AND cert_notafter < CURDATE()")) {
	    	rs = stmt.getResultSet();
	    	
	    	while(rs.next()) {
	    		CertificateRequestHostRecord rec = new CertificateRequestHostRecord(rs);
    			rec.status = CertificateRequestStatus.EXPIRED;
	    		context.setComment("Certificate is no longer valid.");
    			super.update(get(rec.id), rec);
    			
				// update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = "Certificate(s) has been expired.";
				fp.update(ticket, rec.goc_ticket_id);
				
				log.info("sent expiration notification for user certificate request: " + rec.id + " (ticket id:"+rec.goc_ticket_id+")");
			}
	    }	
	    stmt.close();
	    conn.close();
	}
	
	//search for approved request that is too old (call this every day)
	public void processStatusExpired() throws SQLException {
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		//approved in exactly 15 days ago
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE status = '"+CertificateRequestStatus.APPROVED+"' AND DATEDIFF(NOW() ,update_time) = 15")) {
	    	rs = stmt.getResultSet();
	    	while(rs.next()) {
	    		CertificateRequestHostRecord rec = new CertificateRequestHostRecord(rs);
    			
				// update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				
				ContactModel cmodel = new ContactModel(context);
				ContactRecord requester = cmodel.get(rec.requester_contact_id);
				
				//send notification
				ticket.description = "Dear " + requester.name + ",\n\n";
				ticket.description += "Your host certificate (id: "+rec.id+") was approved 15 days ago. The request is scheduled to be automatically canceled within another 15 days. Please take this opportunity to download your approved certificate at your earliest convenience. If you are experiencing any trouble with the issuance of your certificate, please feel free to contact the GOC for further assistance. Please visit "+getTicketUrl(rec.id)+" to issue your host certificate.\n\n";
				
				fp.update(ticket, rec.goc_ticket_id);
				log.info("sent approval expiration warning notification for host certificate request: " + rec.id + " (ticket id:"+rec.goc_ticket_id+")");
			}
	    }	
	    
	    //approved 30 days ago
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE status = '"+CertificateRequestStatus.APPROVED+"' AND DATEDIFF(NOW() ,update_time) = 30")) {
	    	rs = stmt.getResultSet();
	    	
	    	while(rs.next()) {
	    		CertificateRequestHostRecord rec = new CertificateRequestHostRecord(rs);
    			rec.status = CertificateRequestStatus.CANCELED;
	    		context.setComment("Certificate was not issued within 30 days after approval.");
    			super.update(get(rec.id), rec);

				// update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				
				ContactModel cmodel = new ContactModel(context);
				ContactRecord requester = cmodel.get(rec.requester_contact_id);
				
				//send notification
				ticket.description = "Dear " + requester.name + ",\n\n";
				ticket.description += "You did not issue your host certificate (id: "+rec.id+") within 30 days from the approval. In compliance with OSG PKI policy, the request is being canceled. You are welcome to re-request if necessary at "+getTicketUrl(rec.id)+".\n\n";
				ticket.status = "Resolved";
				fp.update(ticket, rec.goc_ticket_id);
				
				log.info("sent approval calelation notification for host certificate request: " + rec.id + " (ticket id:"+rec.goc_ticket_id+")");
			}
	    }	
	    
	    stmt.close();
	    conn.close();		
	}

	public  ArrayList<CertificateRequestHostRecord>  findNullIssuedExpiration() throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE status = '"+CertificateRequestStatus.ISSUED+"' AND (cert_notafter is NULL or cert_notbefore is NULL)");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}
}
