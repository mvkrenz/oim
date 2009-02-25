package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.ButtonView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.View;
import edu.iu.grid.oim.view.divex.ContactsDE;
import edu.iu.grid.oim.view.form.FormElementBase;
import edu.iu.grid.oim.view.form.FormView;
import edu.iu.grid.oim.view.form.TextFormElement;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.UrlValidator;

public class VOEditServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOEditServlet.class);  
	
    public VOEditServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		showForm(request, response, false);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String action = request.getParameter("action");
		if(action.compareTo("update") == 0) {
			showForm(request, response, true);
		} else if(action.compareTo("new") == 0) {
			showForm(request, response, true);
		}
	}
	
	private VORecord getRecord(HttpServletRequest request)
	{
		VORecord rec = new VORecord();
		rec.name = request.getParameter("name");
		rec.primary_url = request.getParameter("primary_url");
		rec.aup_url = request.getParameter("aup_url");
		return rec;
	}
	
	/*
	private Boolean validate(VORecord rec) {
		Boolean valid = true;
	
	    String[] schemes = {"http","https"};
	    UrlValidator urlValidator = new UrlValidator(schemes);
		if (!urlValidator.isValid(rec.primary_url)) {
			valid = false;
			notes.put("primary_url", "Must be");
		}
	
		contentview.add("<div><input type=\"edit\" name=\"primary_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.primary_url)+"\"></input></div>");	
		
		return valid;
	}
	*/
	
	private void showForm(HttpServletRequest request, HttpServletResponse response, Boolean submit) throws ServletException, IOException
	{
		ContentView contentview;
		
		String vo_id_str = request.getParameter("vo_id");
		if(vo_id_str != null) {
			int vo_id = Integer.parseInt(vo_id_str);
			String action = ServletBase.baseURL()+"/voedit?action=update&vo_id=" + vo_id;
			FormView form = new FormView(action);
			VORecord vo;
			if(submit) {
				//edit record submitted
				vo = getRecord(request);
				populateForm(form, vo, true);
			} else {
				Authorization auth = new Authorization(request, con);
				VOModel model = new VOModel(con, auth);
				vo = model.getVO(vo_id);
				populateForm(form, vo, false);
			}
			contentview = createEditView(form);
		} else {
			String action = ServletBase.baseURL()+"/voedit?action=new";
			FormView form = new FormView(action);
			
			VORecord vo;
			if(submit) {
				//new record submitted
				vo = getRecord(request);
				populateForm(form, vo, true);
			} else {
				//new record - just create default record
				vo = new VORecord();
				populateForm(form, vo, false);
			}
			contentview = createNewView(form);
		}
	
		MenuView menuview = createMenuView(baseURL(), "vo");
		Page page = new Page(menuview, contentview);
		response.getWriter().print(page.toHTML());
	}
	
	private View populateForm(FormView form, VORecord vo, Boolean validate)
	{
		Boolean valid = true;
		FormElementBase elem = new TextFormElement("name", "Name");
		elem.setValue(vo.name);
		if(validate) {
		    String[] schemes = {"http","https"};
		    UrlValidator urlValidator = new UrlValidator(schemes);
			if (!urlValidator.isValid(vo.name)) {
				valid = false;
				elem.setError("Please enter valid URL");
			}
		}
		form.add(elem);
		
/*		
		contentview.add("<span>Long Name:</span>");
		contentview.add("<div><input type=\"edit\" name=\"long_name\" value=\""+
				StringEscapeUtils.escapeHtml(vo.long_name)+"\"></input></div>");
		
		contentview.add("<span>Description:</span>");
		contentview.add("<div><input type=\"edit\" name=\"description\" value=\""+
				StringEscapeUtils.escapeHtml(vo.description)+"\"></input></div>");
		
		contentview.add("<span>Primary URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"primary_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.primary_url)+"\"></input></div>");	
		
		contentview.add("<span>AUP URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"aup_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.aup_url)+"\"></input></div>");	
		
		contentview.add("<span>Membership Services URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"membership_services_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.membership_services_url)+"\"></input></div>");

		contentview.add("<span>Purpose URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"purpose_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.purpose_url)+"\"></input></div>");

		contentview.add("<span>Support URL:</span>");
		contentview.add("<div><input type=\"edit\" name=\"support_url\" value=\""+
				StringEscapeUtils.escapeHtml(vo.support_url)+"\"></input></div>");

		contentview.add("<span>App Description:</span>");
		contentview.add("<div><input type=\"edit\" name=\"app_description\" value=\""+
				StringEscapeUtils.escapeHtml(vo.app_description)+"\"></input></div>");
		
		contentview.add("<span>Community:</span>");
		contentview.add("<div><input type=\"edit\" name=\"community\" value=\""+
				StringEscapeUtils.escapeHtml(vo.community)+"\"></input></div>");
*/		
		return form;

	}
	
	protected ContentView createEditView(FormView form) throws ServletException
	{
		ContentView contentview = new ContentView();
		contentview.add("<h1>Edit Virtual Organization</h1>");
	
		contentview.add("<h2>Details</h2>");
		contentview.add(form);
		
		contentview.add("<h2>Contact Information</h2>");
		contentview.add(new ContactsDE());
		
		return contentview;
	}
	
	protected ContentView createNewView(FormView form) throws ServletException
	{
		ContentView contentview = new ContentView();
		contentview.add("<h1>Add Virtual Organization</h1>");	
	
		contentview.add("<h2>Details</h2>");
		contentview.add(form);
		
		contentview.add("<h2>Contact Information</h2>");
		contentview.add(new ContactsDE());
		
		return contentview;
	}
}
