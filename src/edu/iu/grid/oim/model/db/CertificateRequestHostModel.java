package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.Certificate;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
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
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateBase;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.cert.ICertificateSigner.IHostCertificatesCallBack;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;

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
		HashSet<Integer> vos = new HashSet<Integer>();
		GridAdminModel model = new GridAdminModel(context);
		try {
			for(GridAdminRecord grec : model.getGridAdminsByContactID(id)) {
				if(cond.length() != 0) {
					cond.append(" OR ");
				}
				cond.append("cns LIKE '%"+StringEscapeUtils.escapeSql(grec.domain)+"</String>%'");
				vos.add(grec.vo_id);
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup GridAdmin domains", e1);
		}	
		
		String vos_list = "";
		for(Integer vo : vos) {
			if(vos_list.length() != 0) {
				vos_list += ",";
			}
			vos_list += vo;
		}
		
		if(cond.length() != 0) {
			ResultSet rs = null;
			Connection conn = connectOIM();
			Statement stmt = conn.createStatement();
			System.out.println("Searching SQL: SELECT * FROM "+table_name + " WHERE ("+cond.toString() + ") AND approver_vo_id in ("+vos_list+") AND status in ('REQUESTED','RENEW_REQUESTED','REVOKE_REQUESTED')");

			stmt.execute("SELECT * FROM "+table_name + " WHERE ("+cond.toString() + ") AND approver_vo_id in ("+vos_list+") AND status in ('REQUESTED','RENEW_REQUESTED','REVOKE_REQUESTED')");	
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
		final StringArray statuses = new StringArray(rec.cert_statuses);
		
		final CertificateBase[] certs = new CertificateBase[csrs.length()];
		for(int c = 0; c < csrs.length(); ++c) {
			certs[c] = new CertificateBase();
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
				final CertificateManager cm = CertificateManager.Factory(context, rec.approver_vo_id);				
				try {
					//lookup requester contact information
					String requester_email = rec.requester_email; //for guest request
					if(rec.requester_contact_id != null) {
						//for user request
						ContactModel cmodel = new ContactModel(context);
						ContactRecord requester = cmodel.get(rec.requester_contact_id);
						requester_email = requester.primary_email;
					}
	
					log.debug("Starting signing process");
					cm.signHostCertificates(certs, new IHostCertificatesCallBack() {
						
						//called once all certificates are requested (and approved) - but not yet issued
						@Override
						public void certificateRequested() {
							log.debug("certificateRequested called");
							//update certs db contents
							try {
								for(int c = 0; c < certs.length; ++c) {
									CertificateBase cert = certs[c];
									serial_ids.set(c,  cert.serial); //really just order ID (until the certificate is issued)
									statuses.set(c, CertificateRequestStatus.ISSUING);
								}
								rec.cert_serial_ids = serial_ids.toXML();
								rec.cert_statuses = statuses.toXML();
								context.setComment("All certificate requests have been sent.");
								CertificateRequestHostModel.super.update(get(rec.id), rec);
							} catch (SQLException e) {
								log.error("Failed to update certificate update while monitoring issue progress:" + rec.id);
							}
						}
						
						//called for each certificate issued
						@Override
						public void certificateSigned(CertificateBase cert, int idx) {
							
							log.info("host cert issued by digicert: serial_id:" + cert.serial);
							log.info("pkcs7:" + cert.pkcs7);
							
							//pull some information from the cert for validation purpose
							try {
								ArrayList<Certificate> chain = CertificateManager.parsePKCS7(cert.pkcs7);
								
								X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
								cert.notafter = c0.getNotAfter();
								cert.notbefore = c0.getNotBefore();
								
								//do a bit of validation
								Calendar today = Calendar.getInstance();
								if(Math.abs(today.getTimeInMillis() - cert.notbefore.getTime()) > 1000*3600*24) {
									log.warn("Host certificate issued for request "+rec.id+"(idx:"+idx+") has cert_notbefore set too distance from current timestamp");
								}
								
								long dayrange = (cert.notafter.getTime() - cert.notbefore.getTime()) / (1000*3600*24);
								if(dayrange < 350 || dayrange > 450) {
									log.warn("Host certificate issued for request "+rec.id+ "(idx:"+idx+")  has invalid range of "+dayrange+" days (too far from 395 days)");
								}
								
								//make sure dn starts with correct base
								X500Principal dn = c0.getSubjectX500Principal();
								String apache_dn = CertificateManager.X500Principal_to_ApacheDN(dn);
								if(!apache_dn.startsWith(cm.getHostDNBase())) {
									log.error("Host certificate issued for request " + rec.id + "(idx:"+idx+")  has DN:"+apache_dn+" which doesn't have an expected DN base: "+cm.getHostDNBase());
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
								statuses.set(idx, CertificateRequestStatus.ISSUED);
								rec.cert_statuses = statuses.toXML();
								
								rec.status_note = "Certificate idx:"+idx+" has been issued. Serial Number: " + cert.serial;
								context.setComment(rec.status_note);
								CertificateRequestHostModel.super.update(get(rec.id), rec);
							} catch (SQLException e) {
								log.error("Failed to update certificate update while monitoring issue progress:" + rec.id);
							}
						}
					}, requester_email);
					
					log.debug("Finishing up issue process");

					//update records
					int idx = 0;
					StringArray cert_certificates = new StringArray(rec.cert_certificate);
					StringArray cert_intermediates = new StringArray(rec.cert_intermediate);
					StringArray cert_pkcs7s = new StringArray(rec.cert_pkcs7);
					StringArray cert_serial_ids = new StringArray(rec.cert_serial_ids);
					for(CertificateBase cert : certs) {
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
					
					//set cert expiriation dates using the first certificate issued (out of many *requests*)
					CertificateBase cert = certs[0];
					rec.cert_notafter = cert.notafter;
					rec.cert_notbefore = cert.notbefore;
					
					//log.debug("Updating status");
					
					//update status
					try {
						rec.status = CertificateRequestStatus.ISSUED;
						context.setComment("All ceritificates has been issued.");
						CertificateRequestHostModel.super.update(get(rec.id), rec);
					} catch (SQLException e) {
						throw new CertificateRequestException("Failed to update status for certificate request: " + rec.id);
					}
					
					log.debug("Updating ticket");
					
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
					ArrayList<ContactRecord> gas = findGridAdmin(rec); 
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
					
				} catch (CertificateProviderException e) {
					failed("Failed to sign certificate -- CertificateProviderException ", e);
				} catch (SQLException e) {
					failed("Failed to sign certificate -- most likely couldn't lookup requester contact info", e);
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
		ArrayList<ContactRecord> gas = findGridAdmin(rec); 
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
			ticket.mail_suppression_assignees = false; //Per Von/Alain's request, we will send notification to Alain when request is approved
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
    	rec.requester_email = requester.primary_email;
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
	 	rec.requester_email = requester_email;
    	
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
		
    	//set all host cert status to REQUESTED
    	StringArray statuses = new StringArray(csrs.size());
		for(int c = 0; c < csrs.size(); ++c) {
			statuses.set(c, CertificateRequestStatus.REQUESTED);
		}
    	rec.cert_statuses = statuses.toXML();
		
		if(request_comment != null) {
			rec.status_note = request_comment;
			context.setComment(request_comment);
		}
		
    	//store CSRs / CNs to record
    	StringArray csrs_sa = new StringArray(csrs.size());
    	StringArray cns_sa = new StringArray(csrs.size());
    	int idx = 0;
    	CNValidator cnv = new CNValidator(CNValidator.Type.HOST);
    	for(String csr_string : csrs) {
        	log.debug("processing csr: " + csr_string);
    		String cn;
    		ArrayList<String> sans;
			try {
	    		PKCS10CertificationRequest csr = CertificateManager.parseCSR(csr_string);
	    		cn = CertificateManager.pullCNFromCSR(csr);
	    		sans = CertificateManager.pullSANFromCSR(csr);
	    		
	    		//validate CN
	    		//if(!cn.matches("^([-0-9a-zA-Z\\.]*/)?[-0-9a-zA-Z\\.]*$")) { //OSGPKI-255
				//	throw new CertificateRequestException("CN structure is invalid, or contains invalid characters.");
	    		//}
	    		if(!cnv.isValid(cn)) {
	    			throw new CertificateRequestException("CN specified is invalid: " + cn + " .. " + cnv.getErrorMessage());
	    		}
	    		for(String san : sans) {
		    		if(!cnv.isValid(san)) {
		    			throw new CertificateRequestException("SAN specified is invalid: " + san + " .. " + cnv.getErrorMessage());
		    		}
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
    	
    	StringArray empty = new StringArray(csrs.size());
    	String empty_xml = empty.toXML();
    	rec.cert_certificate = empty_xml;
    	rec.cert_intermediate = empty_xml;
    	rec.cert_pkcs7 = empty_xml;
    	rec.cert_serial_ids = empty_xml;
    	
    	try {			
			ArrayList<ContactRecord> gas = findGridAdmin(rec); 
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
			
			ticket.description += "CNs requested:\n";
			for(String cn : cns_sa.getAll()) {
				ticket.description += "/CN=" + cn + "\n";
			}
			
			Authorization auth = context.getAuthorization();
			ticket.description += "Requester IP:" + context.getRemoteAddr() + "\n";
			if(auth.isUser()) {
				ContactRecord user = auth.getContact();
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
    
    //find gridadmin who should process the request - identify domain from csrs
    //if there are more than 1 vos group, then user must specify approver_vo_id 
    //   * it could be null for gridadmin with only 1 vo group, and approver_vo_id will be reset to the correct VO ID
    public ArrayList<ContactRecord> findGridAdmin(CertificateRequestHostRecord rec) throws CertificateRequestException 
    {
		GridAdminModel gamodel = new GridAdminModel(context);
    	String gridadmin_domain = null;
    	int idx = 0;
    	ArrayList<String> domains = new ArrayList<String>();
    
    	String[] csrs = rec.getCSRs();
    	if(csrs.length == 0) {
    		throw new CertificateRequestException("No CSR");
    	}
    	for(String csr_string : csrs) {
    		//parse CSR and pull CN
    		String cn;
    		ArrayList<String> sans;
			try {
	    		PKCS10CertificationRequest csr = CertificateManager.parseCSR(csr_string);
	    		cn = CertificateManager.pullCNFromCSR(csr);
	    		sans = CertificateManager.pullSANFromCSR(csr);
	    		
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
				log.debug("first domain is " + gridadmin_domain);
				
			} else {
				if(!gridadmin_domain.equals(domain)) {
					//throw new CertificateRequestException("All host certificates must be approved by the same set of gridadmins. Different for " + cn);
					domains.add(domain);
					log.debug("Next domain is " + domain);
				}
			}
			
			//make sure SANs are also approved by the same domain or share a common GridAdmin
			for(String san : sans) {
				try {
					String san_domain = gamodel.getDomainByFQDN(san);
					if(!gridadmin_domain.equals(san_domain)) {
						//throw new CertificateRequestException("All SAN must be approved by the same set of gridadmins. Different for " + san);	
						domains.add(san_domain);
						log.debug("san domain is " + san_domain);
					}
				} catch (SQLException e) {
					throw new CertificateRequestException("Failed to lookup GridAdmin for SAN:" + san, e);	
				}
			}
    	}
		try {
			//for first domain in the list, add the grid admins. For each additional domain remove all non-matching grid-admins
			ArrayList<ContactRecord> gas = new ArrayList<ContactRecord> ();
			HashMap<VORecord, ArrayList<GridAdminRecord>> groups = gamodel.getByDomainGroupedByVO(gridadmin_domain);
			if(groups.size() == 0) {
				throw new CertificateRequestException("No gridadmin exists for domain: " + gridadmin_domain);
			}
			if(groups.size() == 1 && rec.approver_vo_id == null) {
				//set approver_vo_id to the one and only one vogroup's vo id
				Iterator<VORecord> it = groups.keySet().iterator();  
				VORecord vorec = it.next();
				rec.approver_vo_id = vorec.id;
			}
			String vonames = "";
			for(VORecord vo : groups.keySet()) {
				vonames += vo.name + ", "; //just in case we might need to report error message later
				if(vo.id.equals(rec.approver_vo_id)) {
					log.debug("found a match.. return the list " + vo.name);
					 gas.addAll(GAsToContacts(groups.get(vo)));
					
			
				}
			}
			for (String domain : domains) {
				//HashMap<VORecord, ArrayList<GridAdminRecord>> groups = gamodel.getByDomainGroupedByVO(gridadmin_domain);
				log.debug("looking up approvers for domain " + domain);
				groups = gamodel.getByDomainGroupedByVO(domain);
				if(groups.size() == 0) {
					throw new CertificateRequestException("No gridadmin exists for domain: " + domain);
				}
				if(groups.size() == 1 && rec.approver_vo_id == null) {
					
					Iterator<VORecord> it = groups.keySet().iterator();  
					VORecord vorec = it.next();
					rec.approver_vo_id = vorec.id;

					log.debug("set approver_vo_id to the one and only one vogroup's vo id " + rec.approver_vo_id);
				}
				vonames = "";
				for(VORecord vo : groups.keySet()) {
					vonames += vo.name + ", "; //just in case we might need to report error message later
					if(vo.id.equals(rec.approver_vo_id)) {
						log.debug("found a match.. return the list " + vo.name);
						ArrayList<ContactRecord> newgas = GAsToContacts(groups.get(vo));
						for(ContactRecord contact: newgas) {
							log.debug("checking contact name " + contact.name);
							if (!gas.contains(contact)) {
								newgas.remove(contact);
								log.debug("removing " + contact.name);
							}
	
							
						}
						gas = newgas; 
						//return GAsToContacts(gas);
					}
				}
			}
			if (!gas.isEmpty()) {
				return gas;
			}
			else {
			//oops.. didn't find specified vo..
				throw new CertificateRequestException("Couldn't find GridAdmin group under specified VO. Please use one of the following VOs:" + vonames);
			}
		} catch (SQLException e) {
			throw new CertificateRequestException("Failed to lookup gridadmin contacts for domain:" + gridadmin_domain, e);
		}
    }
    
    public ArrayList<ContactRecord> GAsToContacts(ArrayList<GridAdminRecord> gas) throws SQLException {
		//convert contact_id to contact record 
		ArrayList<ContactRecord> contacts = new ArrayList<ContactRecord>();
		ContactModel cmodel = new ContactModel(context);
		for(GridAdminRecord ga : gas) {
			log.debug("adding contact id " + ga.contact_id);
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
		//CertificateManager cm = CertificateManager.Factory(context, rec.approver_vo_id);

		

	
		ArrayList<Certificate> chain = null;
		try {
			chain = CertificateManager.parsePKCS7(rec.cert_pkcs7);
		} catch (CertificateException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (CMSException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
		X500Principal issuer = c0.getIssuerX500Principal();
		String issuer_dn = CertificateManager.X500Principal_to_ApacheDN(issuer);

		CertificateManager cm = CertificateManager.Factory(issuer_dn);

		try {
			String[] cert_serial_ids = rec.getSerialIDs();
			StringArray statuses = new StringArray(rec.cert_statuses);
			for(int i = 0;i < cert_serial_ids.length; ++i) {
			//for(String cert_serial_id : cert_serial_ids) {
				//only revoke ones that are not yet revoked
				if(statuses.get(i).equals(CertificateRequestStatus.ISSUED)) {
					String cert_serial_id = cert_serial_ids[i];
					log.info("Revoking certificate with serial ID: " + cert_serial_id);
					cm.revokeHostCertificate(cert_serial_id);
					statuses.set(i, CertificateRequestStatus.REVOKED); //TODO - how do I know the revocation succeeded or not?
				}
			}
			rec.cert_statuses = statuses.toXML();
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
	
	//NO-AC
	public void revoke(CertificateRequestHostRecord rec, int idx) throws CertificateRequestException {		
		//make sure we have valid idx
		String[] cert_serial_ids = rec.getSerialIDs();
		String cert_serial_id = cert_serial_ids[idx];
		StringArray statuses = new StringArray(rec.cert_statuses);
		if(idx >= cert_serial_ids.length) {
			throw new CertificateRequestException("Invalid certififcate index:"+idx);
		}
		
		//revoke one
		CertificateManager cm = CertificateManager.Factory(context, rec.approver_vo_id);
		try {
			log.info("Revoking certificate with serial ID: " + cert_serial_id);
			cm.revokeHostCertificate(cert_serial_id);
			statuses.set(idx, CertificateRequestStatus.REVOKED); //TODO - how do I know the revocation succeeded or not?
			rec.cert_statuses = statuses.toXML();
		} catch (CertificateProviderException e1) {
			log.error("Failed to revoke host certificate", e1);
			throw new CertificateRequestException("Failed to revoke host certificate", e1);
		}
		
		//set rec.status to REVOKED if all certificates are revoked
		boolean allrevoked = true;
		for(int i = 0;i < cert_serial_ids.length; ++i) {
			if(!statuses.get(i).equals(CertificateRequestStatus.REVOKED)) {
				allrevoked = false;
				break;
			}
		}
		if(allrevoked) {
			rec.status = CertificateRequestStatus.REVOKED;
		}
		
		try {
			//context.setComment("Revoked certificate with serial ID:"+cert_serial_id);
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
			ticket.description = contact.name + " has revoked a certificate with serial ID:"+cert_serial_id+".\n\n";
		} else {
			throw new CertificateRequestException("Guest shouldn't be revoking certificate!");
		}
		ticket.description += "> " + context.getComment();
		//ticket.status = "Resolved";
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
					ArrayList<ContactRecord> gas = findGridAdmin(rec);
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
					ArrayList<ContactRecord> gas = findGridAdmin(rec);
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
					ArrayList<ContactRecord> gas = findGridAdmin(rec);
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
	
	//canRevokeOne
	public boolean canRevoke(CertificateRequestHostRecord rec, int idx) {
		if(!canRevoke(rec)) return false;
		StringArray statuses = new StringArray(rec.cert_statuses);
		return statuses.get(idx).equals(CertificateRequestStatus.ISSUED);
	}
	
    //NO AC
	public CertificateRequestHostRecord getBySerialID(String serial_id) throws SQLException {
		serial_id = normalizeSerialID(serial_id);
		
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
			sql += " AND status = \""+StringEscapeUtils.escapeSql(status)+"\"";
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
    	throw new UnsupportedOperationException("Please use model specific actions instead (request, approve, reject, etc..)");
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
	
	public void notifyExpiringIn(Integer days_less_than) throws SQLException {
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		ContactModel cmodel = new ContactModel(context);
		
		//process host certificate requests
		log.debug("Looking for host certificate expiring in " + days_less_than + " days");
		for(CertificateRequestHostRecord rec : findExpiringIn(days_less_than)) {
			log.debug("host cert: " + rec.id + " expires on " + dformat.format(rec.cert_notafter));
			Date expiration_date = rec.cert_notafter;
			
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
			ticket.description = "Dear " + rec.requester_name + ",\n\n";
			ticket.description += "Your host certificates will expire on "+dformat.format(expiration_date)+"\n\n";
			
			//list CNs - per Horst's request.
			for(String cn : rec.getCNs()) {
				ticket.description += cn+"\n";
			}
			ticket.description+= "\n";
			
			ticket.description += "Please request for new host certificate(s) for replacements.\n\n";		
			ticket.description += "Please visit "+getTicketUrl(rec.id)+" for more details.\n\n";
			
			//don't send to CCs
			ticket.mail_suppression_ccs = true;
			if(StaticConfig.isDebug()) {
				log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expiring host certificate");
				log.debug(ticket.description);
				log.debug(ticket.status);
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
	        	
	    		StringArray statuses = new StringArray(rec.csrs.length());
	    		for(int c = 0; c < rec.csrs.length(); ++c) {
	    			statuses.set(c, CertificateRequestStatus.EXPIRED);
	    		}
	    		rec.cert_statuses = statuses.toXML();
    			
	    		rec.status = CertificateRequestStatus.EXPIRED;
	    		context.setComment("Certificate is no longer valid.");
    			super.update(get(rec.id), rec);
    			
				// update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = "Certificate(s) has been expired.";
				if(StaticConfig.isDebug()) {
					log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expired host certificate");
					log.debug(ticket.description);
					log.debug(ticket.status);
				} else {
					fp.update(ticket, rec.goc_ticket_id);
					log.info("updated goc ticket : " + rec.goc_ticket_id + " to notify expired host certificate");
				}
				
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

				//send notification
				ticket.description = "Dear " + rec.requester_name + ",\n\n";
				ticket.description += "Your host certificate (id: "+rec.id+") was approved 15 days ago. The request is scheduled to be automatically canceled within another 15 days. Please take this opportunity to download your approved certificate at your earliest convenience. If you are experiencing any trouble with the issuance of your certificate, please feel free to contact the GOC for further assistance. Please visit "+getTicketUrl(rec.id)+" to issue your host certificate.\n\n";
				
				if(StaticConfig.isDebug()) {
					log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expiring status for host certificate");
					log.debug(ticket.description);
					log.debug(ticket.status);
				} else {
					fp.update(ticket, rec.goc_ticket_id);
					log.info("updated goc ticket : " + rec.goc_ticket_id + " to notify expiring status for host certificate");
				}
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

				//send notification
				ticket.description = "Dear " + rec.requester_name + ",\n\n";
				ticket.description += "You did not issue your host certificate (id: "+rec.id+") within 30 days from the approval. In compliance with OSG PKI policy, the request is being canceled. You are welcome to re-request if necessary at "+getTicketUrl(rec.id)+".\n\n";
				ticket.status = "Resolved";

				if(StaticConfig.isDebug()) {
					log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expired status for host certificate");
					log.debug(ticket.description);
					log.debug(ticket.status);
				} else {
					fp.update(ticket, rec.goc_ticket_id);
					log.info("updated goc ticket : " + rec.goc_ticket_id + " to notify expired status for host certificate");
				}
				
				log.info("sent approval calelation notification for host certificate request: " + rec.id + " (ticket id:"+rec.goc_ticket_id+")");
			}
	    }	
	    
	    stmt.close();
	    conn.close();		
	}
	
	//used by RestServlet only once to reset approver_vo_id
	public  ArrayList<CertificateRequestHostRecord>  findNullVO() throws SQLException {
		ArrayList<CertificateRequestHostRecord> recs = new ArrayList<CertificateRequestHostRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE approver_vo_id is NULL order by id");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}
	
	//one time function to reset cert_statuses field for all records
	public void resetStatuses(PrintWriter out) throws SQLException {
		out.write("CertificateRequestHostModel::resetStatuses\n");
	
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE cert_statuses is NULL");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		CertificateRequestHostRecord rec = new CertificateRequestHostRecord(rs);
    		System.out.println("resetting statuses for rec id:"+rec.id);
    		
    		out.write("rec id:"+rec.id+"\n");
    		out.write("\t certcounts:"+rec.getCNs().length+"\n");
    		StringArray statuses = new StringArray(rec.getCNs().length);
    		for(int i = 0;i<rec.getCNs().length;++i) {
    			statuses.set(i, rec.status);
    		}
    		rec.cert_statuses = statuses.toXML();
			super.update(get(rec.id), rec);
    	}
	    stmt.close();
	    conn.close();
	}
}
