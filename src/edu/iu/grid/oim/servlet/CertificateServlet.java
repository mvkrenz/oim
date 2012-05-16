package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.divrep.form.validator.DivRepPassStrengthValidator;

public class CertificateServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		
		//redirect to the most appropriate page
		if(auth.isGuest()) {
			response.sendRedirect("certificaterequestuser");
			return;
		} else if(auth.isUser()) {
			UserCertificateRequestModel model = new UserCertificateRequestModel(context);
			/*
			CertificateRequestUserRecord rec;
			try {
				rec = model.getCurrent();
				if(rec != null) {
					response.sendRedirect("certificateuser?id="+rec.id);
					return;
				}
			} catch (SQLException e) {
				throw new ServletException("Failed to load current dn", e);
			}
			*/
			try {
				ContactRecord crec = auth.getContact();
				ArrayList<CertificateRequestUserRecord> list = model.getMine(crec.id);
				if(list.size() == 0) {
					response.sendRedirect("certificaterequestuser"); //request new
					return;
				} else if(list.size() == 1) {
					CertificateRequestUserRecord urec = list.get(0);
					response.sendRedirect("certificateuser?id="+urec.id); //show detail
					return;
				} 
				
				//all else
				response.sendRedirect("certificateuser"); //show list
				
			} catch (SQLException e) {
				throw new ServletException("Failed to load my certificate", e);
			}
		}
	}
}
