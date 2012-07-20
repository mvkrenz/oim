package edu.iu.grid.oim.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;

public class TestServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(TestServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");

		//if(auth.isLocal()) {
			/*
			CertificateRequestUserModel model = new CertificateRequestUserModel(context);
			model._test();
			*/
		//URL url = new URL("http://localhost:8080/oim/rest?action=quota_info"); //use get
		//URL url = new URL("http://localhost:8080/oim/rest?action=find_expired_cert_request"); //use post
		//URL url = new URL("http://localhost:8080/oim/rest?action=reset_daily_quota"); //use post
		URL url = new URL("http://localhost:8080/oim/rest?action=reset_yearly_quota"); //use post
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		InputStream is = con.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		response.getWriter().write(writer.toString());
		//}
	}
	
}