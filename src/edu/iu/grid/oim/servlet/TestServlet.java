package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;
import static org.junit.Assert.*;

//This class allows admin to run junit asserts 
public class TestServlet extends ServletBase {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(TestServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		/*
		try {
			System.out.println("Testing GridAdminModel");
			//testGridAdminModel(context);
			//testCNValidator(context);
			
			System.out.println("All good");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
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
	
}