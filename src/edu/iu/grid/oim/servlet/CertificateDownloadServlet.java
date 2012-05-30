package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class CertificateDownloadServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateDownloadServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		
		String type = request.getParameter("type");
		String download = request.getParameter("download");
		String id_dirty = request.getParameter("id");
		if(id_dirty != null && type != null && download != null) {
			Integer id = Integer.parseInt(id_dirty);
			if(type.equals("user")) {
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				try {
					CertificateRequestUserRecord rec = model.get(id);
					if(model.canView(rec)) {
						if(download.equals("pkcs7")) {
							response.setContentType("application/pkcs7-signature");
							PrintWriter out = response.getWriter();
							out.write(rec.cert_pkcs7);
						} else if(download.equals("pkcs12")) {
							response.setContentType("application/x-pkcs12");
							KeyStore p12 = model.getPkcs12(rec);
							if(p12 == null) {
								log.error("Failed to create pkcs12");
							} else {
								String password = model.getPassword(id);
								p12.store(response.getOutputStream(), password.toCharArray());
							}
						}
					}
				} catch (SQLException e) {
					log.error("Failed to load certificate record", e);
				} catch (KeyStoreException e) {
					log.error("Failed to output pkcs12", e);
				} catch (NoSuchAlgorithmException e) {
					log.error("Failed to output pkcs12", e);
				} catch (CertificateException e) {
					log.error("Failed to output pkcs12", e);
				}
			} else if(type.equals("host")) {
				String idx_dirty = request.getParameter("idx");
				Integer idx = Integer.parseInt(idx_dirty);
					
				CertificateRequestHostModel model = new CertificateRequestHostModel(context);
				try {
					CertificateRequestHostRecord rec = model.get(id);
					if(model.canView(rec)) {
						if(download.equals("pkcs7")) {
							PrintWriter out = response.getWriter();
							String[] pkc7s = rec.getPKCS7s();
							response.setContentType("application/pkcs7-signature");
							out.write(pkc7s[idx]);
						}
					}
				} catch (SQLException e) {
					log.error("Failed to load certificate record", e);
				}
			}
		}
	}

}