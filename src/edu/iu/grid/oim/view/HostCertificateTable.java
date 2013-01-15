package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
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
		out.write("<table class=\"table certificate\">");
		out.write("<thead><tr><th width=\"40px\">ID</th><th width=\"100px\">Status</th><th width=\"80px\">GOC Ticket</th><th width=\"250px\">FQDNs</th><th>Grid Admins</th></tr></thead>");
		out.write("<tbody>");
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		for(CertificateRequestHostRecord rec : recs) {
			String url = "certificatehost?id="+rec.id;
			if(search_result) {
				url += "&search";
			}
			out.write("<tr onclick=\"document.location='"+url+"';\">");
			out.write("<td>"+rec.id+"</td>");
			out.write("<td>"+rec.status+"</td>");
			//TODO - use configured goc ticket URL
			out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");

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
			
			try {
				ArrayList<ContactRecord> gas = model.findGridAdmin(rec.getCSRs(), rec.approver_vo_id);
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
			out.write("</tr>");	
		}
		out.write("</tbody>");
		out.write("</table>");
	}
}
