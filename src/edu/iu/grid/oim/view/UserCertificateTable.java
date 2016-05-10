package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class UserCertificateTable implements IView {
	
	ArrayList<CertificateRequestUserRecord> recs;	
	UserContext context;
	boolean search_result;
	
	public UserCertificateTable(UserContext context, ArrayList<CertificateRequestUserRecord> recs, boolean search_result) {
		this.context = context;
		this.recs = recs;
		this.search_result = search_result;
	}

	public void render(PrintWriter out)  {

		out.write("<table class=\"table table-hover certificate\">");
		out.write("<thead><tr><th>ID</th><th>Status</th><th width=\"120px\">Request Date</th><th>DN</th><th>VO</th><th>RA</th><th>Signer</th></tr></thead>");
		out.write("<tbody>");
		CertificateRequestUserModel usermodel = new CertificateRequestUserModel(context);
		
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat("MM/dd/yyyy");
		dformat.setTimeZone(auth.getTimeZone());
		
		for(CertificateRequestUserRecord rec : recs) {
			String url = "certificateuser?id="+rec.id;
			if(search_result) {
				url += "&search";
			}
			out.write("<tr onclick=\"document.location='"+url+"';\">");
			out.write("<td>"+rec.id+"</td>");
			out.write("<td>"+rec.status+"</td>");
			out.write("<td>"+dformat.format(rec.request_time)+"</td>");
			out.write("<td>"+rec.dn+"</td>");
			
			try {
				VOModel vomodel = new VOModel(context);
				VORecord vo = vomodel.get(rec.vo_id);
				out.write("<td>"+vo.name+"</td>");
			} catch (SQLException e) {
				out.write("<td>sql error</td>");
			}
			
			try {
				ArrayList<ContactRecord> ras = usermodel.findRAs(rec);
				if(ras.isEmpty()) {
					out.write("<td><span class=\"label label-important\">No RA</span></td>");
				} else {
					out.write("<td>");
					boolean first = true;
					for(ContactRecord ra : ras) {
						if(first) first = false;
						else out.write(" | ");
						out.write(ra.name);
					}
					out.write("</td>");	
				}
				
			} catch (SQLException e) {
				out.write("<td>sql error</td>");
			}
			if(rec.dn.contains("DigiCert-Grid")) {
				out.write("<td style=\"color:red\">Digicert</td>");
			} else if (rec.dn.contains("CILogon")){
				out.write("<td style=\"color:green\">CILogon</td>");
			}
			else {
				out.write("<td style=\"color:gray\">Not Issued</td>");
			}
	

			//this link causes search page to forward to detail page, and it becomes confusing 
			//out.write("<td><a target=\"_blank\" onclick=\"document.location='"+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"'; return false;\">"+rec.goc_ticket_id+"</a></td>");
		
			
			out.write("</tr>");	
		}
		out.write("</tbody>");
		out.write("</table>");
	}
}
