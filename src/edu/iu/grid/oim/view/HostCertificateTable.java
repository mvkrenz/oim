package edu.iu.grid.oim.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang.StringEscapeUtils;
import org.bouncycastle.cms.CMSException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class HostCertificateTable implements IView {
	
	ArrayList<CertificateRequestHostRecord> recs;	
	UserContext context;
	boolean search_result;
	
	public HostCertificateTable(UserContext context, ArrayList<CertificateRequestHostRecord> recs, boolean search_result) {
		this.context = context;
		this.recs = recs;
		this.search_result = search_result;
	}

	public void render(PrintWriter out)  {
		out.write("<table class=\"table table-hover certificate\">");
		out.write("<thead><tr><th width=\"40px\">ID</th><th width=\"100px\">Status</th><th width=\"120px\">Request Date</th><th width=\"250px\">FQDNs</th><th>VO</th><th>Grid Admins</th><th>Signer</th></tr></thead>");
		out.write("<tbody>");
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat("MM/dd/yyyy");
		dformat.setTimeZone(auth.getTimeZone());
		
		VOModel vomodel = new VOModel(context);
		
		for(CertificateRequestHostRecord rec : recs) {
			String url = "certificatehost?id="+rec.id;
			if(search_result) {
				url += "&search";
			}
			out.write("<tr onclick=\"document.location='"+url+"';\">");
			out.write("<td>"+rec.id+"</td>");
			out.write("<td>"+rec.status+"</td>");
			//out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
			out.write("<td>"+dformat.format(rec.request_time)+"</td>");
					
			//fqdns
			String[] cns = rec.getCNs();
			int idx = 0;
			out.write("<td><ul>");
			for(String cn : cns) {
				out.write("<li>"+cn+"</li>");
				idx++;
				if(idx > 5) {
					out.write("<li>... <span class=\"badge badge-info\">Total "+cns.length+"</span></li>");
					break;
				}
				
			}
			out.write("</ul></td>");
			
			//voname
			String voname = "<span class=\"muted\">N/A</span>";
			if(rec.approver_vo_id != null) {
				try {
					VORecord vorec = vomodel.get(rec.approver_vo_id);
					if(vorec != null) {
						voname = vorec.name;
					}
				} catch (SQLException e) {
					System.out.println("Failed to find voname for request id:"+rec.id);
				}
			}
			out.write("<td>"+voname+"</td>");
			
			
			try {
				ArrayList<ContactRecord> gas = model.findGridAdmin(rec);
				out.write("<td>");
				boolean first = true;
				for(ContactRecord ga : gas) {
					if(first) {
						first = false;
					} else {
						out.write(" | ");
					}
					out.write(StringEscapeUtils.escapeHtml(ga.name));
				}
				out.write("</td>");
			} catch (CertificateRequestException ce) {
				out.write("<td><span class=\"label label-important\">No GridAdmin</span></td>");
			}
			String issuer_dn = ""; //by default
			ArrayList<Certificate> chain = null;
			try {
				if (rec.getPKCS7s()[0] != null) {
					chain = CertificateManager.parsePKCS7(rec.getPKCS7s()[0]);
					X509Certificate c0 = CertificateManager.getIssuedX509Cert(chain);
					X500Principal issuer = c0.getIssuerX500Principal();
					issuer_dn = CertificateManager.X500Principal_to_ApacheDN(issuer);
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
			if(issuer_dn.contains("DigiCert")) {
				out.write("<td style=\"color:red\">Digicert</td>");
			} else if (issuer_dn.contains("CILogon")){
				out.write("<td style=\"color:green\">CILogon</td>");
			}
			else {
				out.write("<td style=\"color:gray\">Not Issued</td>");
			}
			
			out.write("</tr>");	
		}
		out.write("</tbody>");
		out.write("</table>");
	}
}
