package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.DivRepPassStrengthValidator;
import edu.iu.grid.oim.model.db.HostCertificateRequestModel;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;

public class RestServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RestServlet.class);  
    
	static enum Status {OK, FAILED};
    class Reply {
    	Status status = Status.OK;
    	String detail = "Nothing to report";
    	HashMap<String, String> params = new HashMap<String, String>();
    }
    
    class RestException extends Exception {
    	public RestException(String message) {
    		super(message);
    	}
    	public RestException(String message, Exception e) {
    		super(message, e);
    	}
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/xml");
		Reply reply = new Reply();
		
		try {
			String action = request.getParameter("action");
			if(action.equals("host_certs_request")) {
				doHostCertsRequest(request, reply);
			} else if(action.equals("host_certs_renew")) {
				doHostCertsRenew(request, reply);
			} else if(action.equals("host_cert_retrieve")) {
				doHostCertRetrieve(request, reply);
			} else if(action.equals("approve_host_request")) {
				doApproveHostRequest(request, reply);
			}
		
		} catch (RestException e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();
		} catch(AuthorizationException e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();
		}
		
		//TODO - output reply
	}
 
	private void doHostCertsRequest(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		
		HashMap<String, String> fqdn_csrs = new HashMap<String, String>();
		HostCertificateRequestModel model = new HostCertificateRequestModel(context);
		
		String [] dirty_fqdn_csrs = request.getParameterValues("fqdn_csrs");
		for(String fqdn_csr : dirty_fqdn_csrs) {
			String []parts = fqdn_csr.split(":");
			String fqdn = parts[0];
			String csr = parts[1];
	
			ArrayList<Integer> gridadmins = model.lookupGridAdmins(fqdn);
			if(gridadmins.isEmpty()) {
				throw new RestException("There are no Gridadmin who can approve request for " + fqdn);
			}
			fqdn_csrs.put(fqdn, csr);
		}
		
		CertificateRequestHostRecord rec;
		try {
			rec = model.request(fqdn_csrs);
			if(rec == null) {
				throw new RestException("Failed to make request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while makeing request", e);
		}
	}
	
	private void doHostCertsRenew(HttpServletRequest request, Reply reply) throws AuthorizationException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
	}
	
	private void doHostCertRetrieve(HttpServletRequest request, Reply reply) throws AuthorizationException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
	}
	
	private void doApproveHostRequest(HttpServletRequest request, Reply reply) throws AuthorizationException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
	}
}