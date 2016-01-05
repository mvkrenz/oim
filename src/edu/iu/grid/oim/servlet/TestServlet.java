package edu.iu.grid.oim.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.UserContext;

import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;

//This class allows admin to run junit asserts 
public class TestServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(TestServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		String action = request.getParameter("action");
		if(action.equals("reset_host_statuses")) {
			if(!auth.isLocal()) {
				throw new ServletException("local only");
			}
			CertificateRequestHostModel model = new CertificateRequestHostModel(context);
			try {
				response.setContentType("text/plain");
				model.resetStatuses(response.getWriter());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(action.equals("cilogon")) {
			testCILogin();
		} else if(action.equals("parse_pkcs7bug")) {
			parsePkcs7Bug(context);
		}
	}
	
	private void parsePkcs7Bug(UserContext context) {
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			CertificateRequestUserRecord rec = model.get(1437);
			ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs = model.getLogs(CertificateRequestUserModel.class, 1437);
			AuthorizationCriterias criteria = model.canRenew(rec, logs);
			if(criteria.passAll()) {
				log.debug("passing testg");
			}
			
		} catch (SQLException e) {
			log.error(e);
		}
	}
	
	private void testCILogin() {
		
        System.setProperty("javax.net.ssl.keyStore", StaticConfig.conf.getProperty("cilogon.api.user.pkcs12"));
        System.setProperty("javax.net.ssl.keyStorePassword", StaticConfig.conf.getProperty("cilogon.api.user.pkcs12_password"));
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
	System.setProperty("https.protocols", "TLSv1");
		PostMethod post = new PostMethod("https://osg.cilogon.org/getusercert");
		HttpClient cl = new HttpClient();
		try {
			cl.executeMethod(post);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		URL url;
		try {
			url = new URL("https://osg.cilogon.org/getusercert");
			String  keyStore = "D:/btsync/laptop/soichi/ssh/soichi.2014-2015.p12";
			String   keyStorePassword = "xxxxxxxxxxxxxxxxx";    
			String  keyPassword = "xxxxxxxxxxx";    
			String   KeyStoreType= "PKCS12";    
			String   KeyManagerAlgorithm = "SunX509";    
			String   SSLVersion = "SSLv3";    
			try {
				HttpURLConnection con = getHttpsURLConnection(url, keyStore, keyStorePassword, keyPassword, KeyStoreType, KeyManagerAlgorithm, SSLVersion);
				con.connect();
			} catch (UnrecoverableKeyException | KeyManagementException
					| NoSuchAlgorithmException | KeyStoreException
					| CertificateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}   
		*/

	}
	
	public HttpURLConnection getHttpsURLConnection(URL url, String  keystore, String   keyStorePass,String  keyPassword, String  KeyStoreType
		    ,String KeyManagerAlgorithm, String  SSLVersion)
		    throws NoSuchAlgorithmException, KeyStoreException,
		        CertificateException, FileNotFoundException, IOException,
		        UnrecoverableKeyException, KeyManagementException {
		    System.setProperty("javax.net.debug","ssl,handshake,record");

		    SSLContext sslcontext = SSLContext.getInstance(SSLVersion);
		    KeyManagerFactory kmf =  KeyManagerFactory.getInstance(KeyManagerAlgorithm);
		    KeyStore ks = KeyStore.getInstance(KeyStoreType);
		    ks.load(new FileInputStream(keystore), keyStorePass.toCharArray());
		    kmf.init(ks, keyPassword.toCharArray());

		    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		    tmf.init(ks);
		    TrustManager[] tm = tmf.getTrustManagers();

		    sslcontext.init(kmf.getKeyManagers(), tm, null);
		    SSLSocketFactory sslSocketFactory = sslcontext.getSocketFactory();
		    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
		    HttpsURLConnection httpsURLConnection = ( HttpsURLConnection)url.openConnection();

		    return httpsURLConnection;
		}
	/*
	void testCNValidator(UserContext context) throws SQLException {
		CNValidator v = new CNValidator(CNValidator.Type.HOST);
		if(v.isValid("grid.iu.edu")) {
			
		}
	}
	void testGridAdminModel(UserContext context) throws SQLException {
		System.out.println("\tgetDomainByFQDN");
		GridAdminModel model = new GridAdminModel(context);
		assertEquals(model.getDomainByFQDN("soichi.iu.edu"), "iu.edu");
		assertEquals(model.getDomainByFQDN("rsv/soichi.iu.edu"), "iu.edu");
		assertEquals(model.getDomainByFQDN("iu.edu"), "iu.edu");
		assertEquals(model.getDomainByFQDN("soichi.grid.iu.edu"), "grid.iu.edu");
		assertEquals(model.getDomainByFQDN("test.sub1.grid.iu.edu"), "sub1.grid.iu.edu");
		assertEquals(model.getDomainByFQDN("fiu.edu"), "fiu.edu");
		assertEquals(model.getDomainByFQDN("giu.edu"), null);
		assertEquals(model.getDomainByFQDN("something.fiu.edu"), "fiu.edu");
		assertEquals(model.getDomainByFQDN("something.fnal.gov"), "fnal.gov");
		assertEquals(model.getDomainByFQDN("pansrv/pandawms.org"), "pandawms.org");
		assertEquals(model.getDomainByFQDN("pansrv/some.pandawms.org"), "pandawms.org");
		assertEquals(model.getDomainByFQDN("1.uchicago.edu"), "uchicago.edu");
	}
	*/
}
