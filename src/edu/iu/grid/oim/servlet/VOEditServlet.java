package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.SimpleValidator;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ButtonView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.View;
import edu.iu.grid.oim.view.divex.ContactsDE;
import edu.iu.grid.oim.view.form.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.*;
import org.apache.commons.validator.routines.AbstractFormatValidator;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	private String current_page = "vo";	
	
    public VOEditServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		setAuth(request);
		
		//load existing VO record information
		
		//if vo_id is provided then we are doing update, otherwise do new.
		Page page;
		VORecord rec;
		String vo_id_str = request.getParameter("vo_id");
		if(vo_id_str != null) {
			//pull record to update
			int vo_id = Integer.parseInt(vo_id_str);
			Authorization auth = new Authorization(request, con);
			VOModel model = new VOModel(con, auth);
			rec = model.getVO(vo_id);
			page = createPage(getTitleUpdate(), getFormActionUpdate(vo_id), rec, new HashMap<String, String>());
		} else {
			rec = new VORecord();
			page = createPage(getTitleNew(), getFormActionNew(), rec, new HashMap<String, String>());
		}
		
		response.getWriter().print(page.toHTML());
	}
	
	protected String getFormActionUpdate(int vo_id)
	{
		return ServletBase.baseURL()+"/voedit?action=update&vo_id=" + vo_id;
	}
	protected String getFormActionNew()
	{
		return ServletBase.baseURL()+"/voedit?action=new";
	}
	protected String getTitleUpdate()
	{
		return "Update Virtual Organization";
	}
	protected String getTitleNew()
	{
		return "New Virtual Organization";
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		
		VORecord rec = convertFormToRecord(request);
		HashMap<String, String> validation_errors = validate(rec);
		
		Page page;
		String action = request.getParameter("action");
		if(action.compareTo("update") == 0) {
			if(validation_errors.size() == 0) {
				//TODO - store it to db
				
				response.sendRedirect("success");
				return;
			} else {
				//validation error.. redisplay form
				page = createPage(getTitleUpdate(), getFormActionUpdate(rec.id), rec, validation_errors);		
			}
		} else if(action.compareTo("new") == 0) {

			if(validation_errors.size() == 0) {
				//TODO - store it to db
				
				response.sendRedirect("success");
				return;
			} else {
				page = createPage(getTitleNew(), getFormActionNew(), rec, validation_errors);		
			}
		} else {
			response.sendRedirect("error");
			return;
		}
		
		response.getWriter().print(page.toHTML());
	}
	
	HashMap<String, String> validate(VORecord rec)
	{
		HashMap<String, String> errors = new HashMap<String, String>();
	
		//list of validator http://commons.apache.org/validator/apidocs/org/apache/commons/validator/routines/package-summary.html
		String[] schemes = {"http","https"};	    
	    UrlValidator urlValidator = new UrlValidator(schemes);
	    
		if (!urlValidator.isValid(rec.primary_url)) {
			errors.put("primary_url", "Must be a valid URL.");
		}
	    
		if (!urlValidator.isValid(rec.aup_url)) {
			errors.put("aup_url", "Must be a valid URL.");
		}
		
		if(SimpleValidator.notempty(rec.name)) {
			errors.put("name", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.description)) {
			errors.put("description", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.membership_services_url)) {
			errors.put("membership_services_url", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.purpose_url)) {
			errors.put("purpose_url", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.support_url)) {
			errors.put("support_url", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.app_description)) {
			errors.put("app_description", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.community)) {
			errors.put("community", "Value is empty");
		}
		
		if(SimpleValidator.notempty(rec.footprints_id)) {
			errors.put("footprints_id", "Value is empty");
		}
		/*
		rec.sc_id = Integer.parseInt(request.getParameter("sc_id"));
		rec.parent_vo_id = Integer.parseInt(request.getParameter("parent_vo_id"));
		rec.active = (Integer.parseInt(request.getParameter("active")) == 1);
		rec.disable = (Integer.parseInt(request.getParameter("disable")) == 1);
		*/
		return errors;
	}
	
	private VORecord convertFormToRecord(HttpServletRequest request)
	{
		VORecord rec = new VORecord();
	
		rec.name = request.getParameter("name");
		rec.description = request.getParameter("description");
		rec.primary_url = request.getParameter("primary_url");
		rec.aup_url = request.getParameter("aup_url");
		rec.membership_services_url = request.getParameter("membership_services_url");
		rec.purpose_url = request.getParameter("purpose_url");
		rec.support_url = request.getParameter("support_url");
		rec.app_description = request.getParameter("app_description");
		rec.community = request.getParameter("community");
		rec.sc_id = Integer.parseInt(request.getParameter("sc_id"));
		rec.parent_vo_id = Integer.parseInt(request.getParameter("parent_vo_id"));
		rec.active = (Integer.parseInt(request.getParameter("active")) == 1);
		rec.disable = (Integer.parseInt(request.getParameter("disable")) == 1);
		rec.footprints_id = request.getParameter("footprints_id");
		
		return rec;
	}
	
	private Page createPage(String page_title, String action, VORecord vo, HashMap<String, String> validation_errors) 
		throws ServletException, IOException
	{	
		//create form
		FormView form = new FormView(action, validation_errors);
		try {
			populateForm(form, vo, validation_errors);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	
		//put the form in a view and display
		ContentView contentview = createView(page_title, form);
		MenuView menuview = createMenuView(baseURL(), current_page);
		Page page = new Page(menuview, contentview);
		
		return page;
	}
	
	private View populateForm(FormView form, VORecord vo, HashMap<String, String> validation_errors) throws AuthorizationException, SQLException
	{
		FormElementBase elem;
	    	
		elem = new TextFormElement("name", "Name", vo.name);
		form.add(elem);
		
		elem = new TextFormElement("long_name", "Long Name", vo.long_name);
		form.add(elem);
	
		elem = new TextareaFormElement("description", "Description", vo.description);
		form.add(elem);
		
		elem = new TextFormElement("primary_url", "Primary URL", vo.primary_url);
		form.add(elem);
		
		elem = new TextFormElement("aup_url", "AUP URL", vo.aup_url);
		form.add(elem);
		
		elem = new TextFormElement("membership_services_url", "Membership Services URL", vo.membership_services_url);
		form.add(elem);	
		
		elem = new TextFormElement("purpose_url", "Purpose URL", vo.purpose_url);
		form.add(elem);
		
		elem = new TextFormElement("support_url", "Support URL", vo.support_url);
		form.add(elem);

		elem = new TextareaFormElement("app_description", "App Description", vo.app_description);
		form.add(elem);

		elem = new TextareaFormElement("community", "Community", vo.community);
		form.add(elem);
		
		elem = new TextFormElement("footprints_id", "Footprints ID", vo.footprints_id);
		form.add(elem);
		
		HashMap<String, String> keyvalues = getSCs();
		elem = new SelectFormElement("sc_id", "Support Center", vo.sc_id, keyvalues);
		form.add(elem);
		
		return form;
	}
	
	private HashMap<String, String> getSCs() throws AuthorizationException, SQLException
	{
		//pull all SCs
		ResultSet scs = null;
		SCModel model = new SCModel(con, auth);
		scs = model.getAllSCs();
		HashMap<String, String> keyvalues = new HashMap<String, String>();
		while(scs.next()) {
			SCRecord rec = new SCRecord(scs);
			keyvalues.put(rec.id.toString(), rec.name);
		}
		return keyvalues;
	}
	
	protected ContentView createView(String title, FormView form) throws ServletException
	{
		ContentView contentview = new ContentView();
		contentview.add("<h1>"+title+"</h1>");	
	
		contentview.add("<h2>VO Details</h2>");
		contentview.add(form);
		
		contentview.add("<h2>Contact Information</h2>");
		contentview.add(new ContactsDE());
		
		return contentview;
	}
	
}