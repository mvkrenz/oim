package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
import edu.iu.grid.oim.model.db.CertificateQuotaModel;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.form.CertificateRequestUserForm;
import edu.iu.grid.oim.view.divrep.form.validator.DivRepPassStrengthValidator;

public class CertificateRequestUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateRequestUserServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		
		/*
		CertificateRequestUserRecord userrec;
		try {
			UserCertificateRequestModel umodel = new UserCertificateRequestModel(context);
			userrec = umodel.getCurrent();
		} catch(SQLException e) {
			throw new ServletException("Failed to load current user certificate", e);
		}
		*/
		
		BootMenuView menuview = new BootMenuView(context, "certificate");
		BootPage page = new BootPage(context, menuview, createContent(context), null);
		page.render(response.getWriter());
	}
	
	protected IView createContent(final UserContext context) throws ServletException
	{
		//final Authorization auth = context.getAuthorization();

		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				
 				CertificateMenuView menu = new CertificateMenuView(context, "certificaterequestuser");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				CertificateQuotaModel quota = new CertificateQuotaModel(context);
				if(quota.canRequestUserCert()) {
					CertificateRequestUserForm form = new CertificateRequestUserForm(context, "certificateuser");
					form.render(out);	
				} else {
					out.write("<div class=\"alert\">You have reached the maximum quota for user certificate request. Please contact GOC for more detail.</div>");
				}

				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>"); //content
			}
			

		};
	}

}
