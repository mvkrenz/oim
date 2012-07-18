package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

public class ReportConfirmationServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ReportConfirmationServlet.class);  
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			//allow cron to access
			auth.check("read_report");
		}
		
		try {
			
			ContactModel cmodel = new ContactModel(context);
			ArrayList<ContactRecord> expired_recs = cmodel.getConfirmationExpiredPersonalContacts();
			
			if(request.getParameter("excel") != null) {
				//construct excel view
				try {
					response.setContentType("application/vnd.ms-excel");
					response.setHeader("Content-Disposition",
							"attachment; filename=ConfirmationReport.xls");
					HSSFWorkbook wb = createExcelView(context, expired_recs);	
					wb.write(response.getOutputStream());
	
				} catch (Exception e) {
					throw new ServletException(
							"Exception in Excel Sample Servlet", e);
				} 
			} else {
				//construct html view
				BootMenuView menuview = new BootMenuView(context, "reportconfirmation");
				ContentView contentview = createContentView(context, expired_recs);
				/*
				//set crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Reports",  "report");
				bread_crumb.addCrumb("Confirmation Report",  null);
				contentview.setBreadCrumb(bread_crumb);
				*/
				
				BootPage page = new BootPage(context, menuview, contentview, createSideView());
				PrintWriter out = response.getWriter();
				page.render(out);			
			}
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	private HSSFWorkbook createExcelView(UserContext context, ArrayList<ContactRecord> expired_recs) 
	{
		ArrayList<ContactRecord> normal_list = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> critical_list = new ArrayList<ContactRecord>();
		HashMap<Integer, ArrayList<String>> critical_details = new HashMap<Integer, ArrayList<String>>();
		createReport(context, expired_recs, normal_list, critical_list, critical_details);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyle cellstyle = wb.createCellStyle();
		cellstyle.setWrapText(true);
		cellstyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		HSSFSheet sheet = wb.createSheet("Security Contacts");
		HSSFRow row = sheet.createRow(0);
		row.createCell(0).setCellValue(new HSSFRichTextString("Name"));
		row.createCell(1).setCellValue(new HSSFRichTextString("Email"));
		row.createCell(2).setCellValue(new HSSFRichTextString("Phone"));
		row.createCell(3).setCellValue(new HSSFRichTextString("Confirmation Date (UTC)"));
		row.createCell(4).setCellValue(new HSSFRichTextString("Detail"));	
		int rownum = 1;
		for(ContactRecord rec : critical_list) {
			row = sheet.createRow(rownum++);
			HSSFCell cell;
			cell = row.createCell(0);
			cell.setCellStyle(cellstyle);
			cell.setCellValue(new HSSFRichTextString(rec.name));
			
			cell = row.createCell(1);
			cell.setCellStyle(cellstyle);
			cell.setCellValue(new HSSFRichTextString(rec.primary_email));
			
			cell = row.createCell(2);
			cell.setCellStyle(cellstyle);
			cell.setCellValue(new HSSFRichTextString(rec.primary_phone));
			
			cell = row.createCell(3);
			cell.setCellStyle(cellstyle);
			cell.setCellValue(new HSSFRichTextString(rec.confirmed.toString()));
			
			ArrayList<String> details = critical_details.get(rec.id);
			StringBuffer details_str = new StringBuffer();
			for(String detail: details) {
				details_str.append(detail + "\n");
			}
			cell = row.createCell(4);
			cell.setCellStyle(cellstyle);
			cell.setCellValue(new HSSFRichTextString(details_str.toString()));
		}
		sheet.autoSizeColumn((short) 0);
		sheet.autoSizeColumn((short) 1);
		sheet.autoSizeColumn((short) 2);
		sheet.autoSizeColumn((short) 3);
		sheet.autoSizeColumn((short) 4);
		
		sheet = wb.createSheet("Non-Security Contacts");
		row = sheet.createRow(0);
		row.createCell(0).setCellValue(new HSSFRichTextString("Name"));
		row.createCell(1).setCellValue(new HSSFRichTextString("Email"));
		row.createCell(2).setCellValue(new HSSFRichTextString("Phone Number"));	
		row.createCell(3).setCellValue(new HSSFRichTextString("Confirmation Date (UTC)"));
		rownum = 1;
		for(ContactRecord rec : normal_list) {
			row = sheet.createRow(rownum++);
			row.createCell(0).setCellValue(new HSSFRichTextString(rec.name));
			row.createCell(1).setCellValue(new HSSFRichTextString(rec.primary_email));
			row.createCell(2).setCellValue(new HSSFRichTextString(rec.primary_phone));
			row.createCell(3).setCellValue(new HSSFRichTextString(rec.confirmed.toString()));
		}	
		sheet.autoSizeColumn((short) 0);
		sheet.autoSizeColumn((short) 1);
		sheet.autoSizeColumn((short) 2);
		sheet.autoSizeColumn((short) 3);
		
		return wb;
		
	}
	protected ContentView createContentView(UserContext context, ArrayList<ContactRecord> expired_recs) throws ServletException, SQLException
	{	
		ContentView contentview = new ContentView(context);	
		contentview.add(new HtmlView("<h1>Confirmation Report</h1>"));
		
		ArrayList<ContactRecord> normal_list = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> critical_list = new ArrayList<ContactRecord>();
		HashMap<Integer, ArrayList<String>> critical_details = new HashMap<Integer, ArrayList<String>>();
		createReport(context, expired_recs, normal_list, critical_list, critical_details);
				
		contentview.add(new HtmlView("<div class=\"row-fluid\">"));
		
		contentview.add(new HtmlView("<div class=\"span6\">"));
		contentview.add(new HtmlView("<h2>Non-Security Contacts</h2>"));
		for(ContactRecord rec : normal_list) {
			contentview.add(new HtmlView("<b>"+rec.name + "</b><br/>"));
			contentview.add(new HtmlView("<div class=\"divrep_indent\">"));
			contentview.add(new HtmlView(rec.primary_email+"<br/>"+rec.primary_phone));
			contentview.add(new HtmlView("</div>"));
		}
		contentview.add(new HtmlView("</div>"));//span6
		
		contentview.add(new HtmlView("<div class=\"span6\">"));
		contentview.add(new HtmlView("<h2>Security Contacts</h2>"));
		for(ContactRecord rec : critical_list) {
			contentview.add(new HtmlView("<b>"+rec.name + "</b><br/>"));
			contentview.add(new HtmlView("<div class=\"divrep_indent\">"));
			contentview.add(new HtmlView("<p>"+rec.primary_email+"<br/>"+rec.primary_phone+"</p>"));
			ArrayList<String> details = critical_details.get(rec.id);
			StringBuffer details_str = new StringBuffer();
			for(String detail: details) {
				details_str.append("<p class=\"warning\">" + detail + "</p>");
			}
			contentview.add(new HtmlView(details_str.toString()));
			contentview.add(new HtmlView("</div>"));
		}	
		contentview.add(new HtmlView("</div>"));//span6
		
		contentview.add(new HtmlView("</div>")); //end row
		
		return contentview;
	}
	
	private void createReport(UserContext context,
			ArrayList<ContactRecord> expired_recs, 
			ArrayList<ContactRecord> normal_list, 
			ArrayList<ContactRecord> critical_list, 
			HashMap<Integer, ArrayList<String>> critical_details)
	{
		try {		
			ResourceModel rmodel = new ResourceModel(context);
			ResourceContactModel rcontactmodel = new ResourceContactModel(context);
			
			VOModel vomodel = new VOModel(context);
			VOContactModel vocontactmodel = new VOContactModel(context);
			
			SCModel scmodel = new SCModel(context);
			SCContactModel sccontactmodel = new SCContactModel(context);
			
			for(ContactRecord rec : expired_recs) {
	
				//determine if this person is a security contact in sc, resource, or vo
				ArrayList<String> critical_detail = new ArrayList<String>();
				
				ArrayList<ResourceContactRecord> rcrecs = rcontactmodel.getByContactID(rec.id);
				for(ResourceContactRecord rcrec : rcrecs) {
					if(rcrec.contact_type_id == 2) { //2 == security contact
						ResourceRecord rrec = rmodel.get(rcrec.resource_id);
						if(rrec.active && !rrec.disable) {
							critical_detail.add("Resource Security Contact for "+rrec.name);
						}
					}
				}
				
				ArrayList<VOContactRecord> vocrecs = vocontactmodel.getByContactID(rec.id);
				for(VOContactRecord vocrec : vocrecs) {
					if(vocrec.contact_type_id == 2) { //2 == security contact
						VORecord vorec = vomodel.get(vocrec.vo_id);
						if(vorec.active && !vorec.disable) {
							critical_detail.add("VO Security Contact for "+vorec.name);
						}
					}
				}		
			
				ArrayList<SCContactRecord> sccrecs = sccontactmodel.getByContactID(rec.id);
				for(SCContactRecord sccrec : sccrecs) {
					if(sccrec.contact_type_id == 2) { //2 == security contact
						SCRecord screc = scmodel.get(sccrec.sc_id);
						if(screc.active && !screc.disable) {
							critical_detail.add("SC Security Contact for "+screc.name);
						}
					}
				}	
				
				if(critical_detail.size() == 0) {
					normal_list.add(rec);
				} else {
					critical_list.add(rec);
					critical_details.put(rec.id, critical_detail);
				}
				
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		//String export = "<a class=\"btn\" href=\""+StaticConfig.getApplicationBase()+"/reportconfirmation?excel\">Export to Excel Format</a>";
		view.add(new HtmlView("<p><a class=\"btn\" href=\"reportconfirmation?excel\">Export to Excel Format</a></p>"));
		view.add(new HtmlView("<p>This pages shows lists of contacts who have not confirmed the content of OIM for more than "+StaticConfig.getConfirmationExpiration()+" days</p>"));
		view.add(new HtmlView("<p>This list only contains personal contact, and contact that are not disabled.</p>"));

		return view;
	}
}
