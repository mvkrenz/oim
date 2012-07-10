package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMWriter;

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
							response.setHeader("Content-Disposition", "attachment; filename=user_certificate.U"+rec.id+".p7b");
							PrintWriter out = response.getWriter();
							out.write(rec.cert_pkcs7);
						} else if(download.equals("pkcs12")) {
							response.setContentType("application/x-pkcs12");
							response.setHeader("Content-Disposition", "attachment; filename=user_certificate_and_key.U"+rec.id+".p12");
							KeyStore p12 = model.getPkcs12(rec);
							if(p12 == null) {
								log.error("Failed to create pkcs12");
							} else {
								String password = model.getPassword(id);
								p12.store(response.getOutputStream(), password.toCharArray());
							}
						} else if(download.equals("pem7")) {
							PrintWriter out = response.getWriter();
							response.setContentType("application/pem-signature");
							response.setHeader("Content-Disposition", "attachment; filename=user_certificate.U"+rec.id+".pem");
							out.write(rec.cert_certificate);
						}/* else if(download.equals("pem12")) {
							PrintWriter out = response.getWriter();
							response.setContentType("application/pem-signature");
							response.setHeader("Content-Disposition", "attachment; filename=user_certificate_and_key.U"+rec.id+".pem");
							model.writeEncryptedRSAPrivateKeyInPEM(out, rec);
							out.write(rec.cert_certificate);
						}*/
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
							String[] cns = rec.getCNs();
							response.setContentType("application/pkcs7-signature");
							response.setHeader("Content-Disposition", "attachment; filename=host_certificate.H"+rec.id+"."+cns[idx]+".p7b");
							out.write(pkc7s[idx]);
						} else if(download.equals("pem")) {
							PrintWriter out = response.getWriter();
							String[] certificates = rec.getCertificates();
							String[] cns = rec.getCNs();
							response.setContentType("application/pem-signature");
							response.setHeader("Content-Disposition", "attachment; filename=host_certificate.H"+rec.id+"."+cns[idx]+".pem");
							out.write(certificates[idx]);
						}
					}
				} catch (SQLException e) {
					log.error("Failed to load certificate record", e);
				}
			}
		}
	}

}