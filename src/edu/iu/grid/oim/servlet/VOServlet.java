package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.common.DivRepStaticContent;

import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;

import edu.iu.grid.oim.model.db.ProjectModel;
import edu.iu.grid.oim.model.db.VOOasisUserModel;
import edu.iu.grid.oim.model.db.VOReportNameModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.VOOasisUserRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.ProjectView;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.URLView;
import edu.iu.grid.oim.view.TableView.Row;
import edu.iu.grid.oim.view.divrep.ViewWrapper;
import edu.iu.grid.oim.view.divrep.form.VOFormDE;

public class VOServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		UserContext context = new UserContext(request);
		//auth.check("f_my_vo");
		
		try {	
			//construct view
			BootMenuView menuview = new BootMenuView(context, "vo");
			ContentView contentview = null;
			//display either list, or a single resource
			VORecord rec = null;
			SideContentView sideview = null;
			String vo_id_str = request.getParameter("id");
			if(vo_id_str != null) {
				Integer vo_id = Integer.parseInt(vo_id_str);
				VOModel model = new VOModel(context);
				rec = model.get(vo_id);
				contentview = new ContentView(context);
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("Virtual Organization", "vo");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				//contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				if(rec.active == false) {
					contentview.add(new HtmlView("<div class=\"alert\">This Virtual Organization is currently inactive.</div>"));
				}
				if(rec.disable == true) {
					contentview.add(new HtmlView("<div class=\"alert\">This Virtual Organization is currently disabled.</div>"));
				}
				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				contentview.add(createVOContent(context, rec)); //false = no edit button	
				
				sideview = createSideView(context, rec);

			} else {
				contentview = createListContentView(context);
			}
			
			BootPage page = new BootPage(context, menuview, contentview, sideview);
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createListContentView(UserContext context) 
		throws ServletException, SQLException
	{

		VOModel model = new VOModel(context);
		ArrayList<VORecord> vos = (ArrayList<VORecord>) model.getAll();
		Collections.sort(vos, new Comparator<VORecord> (){
			public int compare(VORecord a, VORecord b) {
				return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
			}
		});
		
		ArrayList<VORecord> editable_vos = new ArrayList<VORecord>();
		ArrayList<VORecord> readonly_vos = new ArrayList<VORecord>();
		for(VORecord rec : vos) {
			if(model.canEdit(rec.id)) {
				editable_vos.add(rec);
			} else {
				readonly_vos.add(rec);
			}
		}
		
		ContentView contentview = new ContentView(context);
		
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<a href=\"voedit\" class=\"btn pull-right\"><i class=\"icon-plus-sign\"></i> Add New Virtual Organization</a>"));
			contentview.add(new HtmlView("<h2>My Virtual Organizations <!--<small>that I can edit</p></small>--></h2>"));
			if(editable_vos.size() == 0) {
				contentview.add(new HtmlView("<p>You currently are not listed as a contact of any contact type (exept submitter) on any virtual organization - therefore you are not authorized to edit any VOs.</p>"));
			}
			ItemTableView table = new ItemTableView(6);
			for(final VORecord rec : editable_vos) {
				String name = rec.name;
				String disable_css = "";
				String tag = "";
				if(rec.disable) {
					disable_css += " disabled";
					tag += " [Disabled]";
				}
				if(!rec.active) {
					disable_css += " inactive";
					tag += " [Inactive]";
				}
				table.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"voedit?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
			}
			contentview.add(table);
		}
		
		if(readonly_vos.size() != 0) {
			contentview.add(new HtmlView("<h2>Virtual Organizations</h2>"));
			//contentview.add(new HtmlView("<p>Following are the currently registered virtual organizations on OIM - you do not have edit access on these records.</p>"));
	
			ItemTableView table = new ItemTableView(5);
			for(final VORecord rec : readonly_vos) {
				String name = rec.name;
				GenericView vo = new GenericView();		
				String disable_css = "";
				String tag = "";
				if(rec.disable) {
					disable_css += " disabled";
					tag += " (Disabled)";
				}
				if(!rec.active) {
					disable_css += " inactive";
					tag += " (Inactive)";
				}
				vo.add(new HtmlView("<a class=\""+disable_css+"\" title=\""+StringEscapeUtils.escapeHtml(rec.long_name)+"\" href=\"vo?id="+rec.id+"\">"+StringEscapeUtils.escapeHtml(name)+tag+"</a>"));
				//vo.add(new HtmlView("<p>"+StringEscapeUtils.escapeHtml(rec.long_name)+"</p>"));
				table.add(vo);

			}
			contentview.add(table);
		}
		
		return contentview;
	}

	public DivRep createVOContent(UserContext context, final VORecord rec) {
		RecordTableView table = new RecordTableView();
		try {	

			table.addRow("Long Name", rec.long_name);
			if (rec.science_vo) {
				table.addRow("VO Type", "OSG User and Resource Provider (Provides services to the OSG and has/will have users who do OSG-dependent scientific research)");
			}
			else {
				table.addRow("VO Type", "Resource Provider Only (Provides services to the OSG but does not have users who do OSG-dependent scientific research)");
			}
			ToolTip cert_only_tip = new ToolTip("This VO is only used to issue user certificates to OSG community.");
			table.addRow("Certificate Issue Only " + cert_only_tip.render(), rec.cert_only);
			table.addRow("Description", rec.description);
			table.addRow("Community", rec.community);
			//table.addRow("Ex. Assignment ID", rec.external_assignment_id);
			
			//pull parent vo
			VOModel model = new VOModel(context);
			VORecord parent_vo_rec = model.getParentVO(rec.id);
			String parent_vo_name = null;
			if(parent_vo_rec != null) {
				parent_vo_name = parent_vo_rec.name;
			}
			else {
				parent_vo_name = "N/A";
			}
			ToolTip parent_vo_tip = new ToolTip("Sometimes a project grows large enough to include several offshoot projects in it. When this happens, such a VO would want to be its own registration but would want to cite the parent project on its registration. This item helps a VO manager make such a mapping.");
			table.addRow("Parent VO " + parent_vo_tip.render() + " ", parent_vo_name);
			table.addRow("Support Center", getSCName(context, rec.sc_id));

			ContactTypeModel ctmodel = new ContactTypeModel(context);
			//ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			
			//contacts (only shows contacts that are filled out)
			VOContactModel vocmodel = new VOContactModel(context);
			ArrayList<VOContactRecord> voclist = vocmodel.getByVOID(rec.id);
			HashMap<Integer, ArrayList<VOContactRecord>> voclist_grouped = vocmodel.groupByContactTypeID(voclist);

			for(ContactTypeRecord.Info contact_type : VOFormDE.ContactTypes) {
				if(voclist_grouped.containsKey(contact_type.id)) {
					ContactTypeRecord ctrec = ctmodel.get(contact_type.id);

					ArrayList<VOContactRecord> clist = voclist_grouped.get(contact_type.id);
					Collections.sort(clist, new Comparator<VOContactRecord> (){
						public int compare(VOContactRecord a, VOContactRecord b) {
							if (a.getRank() > b.getRank()) // We are comparing based on rank id 
								return 1; 
							return 0;
						}
					});
					String cliststr = "";

					for(VOContactRecord vcrec : clist) {
						ContactRecord person = pmodel.get(vcrec.contact_id);
						ContactRank rank = ContactRank.get(vcrec.contact_rank_id);
						cliststr += "<div class='contact_rank contact_"+rank+"'>";
						cliststr +=  StringEscapeUtils.escapeHtml(person.name.trim());
						cliststr += "</div>";
					}
					ToolTip tip = new ToolTip(contact_type.desc);
					table.addRow(ctrec.name + " " + tip.render(), new HtmlView(cliststr));
				}
			}			
			if (rec.science_vo) {
				table.addRow("App Description", rec.app_description);
				ToolTip field_of_science_tip = new ToolTip("VOs are often associated with one or more scientific fields. ");
				table.addRow("Field of Science " +field_of_science_tip.render()+ " ", getFieldOfScience(context, rec.id));
				table.addRow("Primary URL", new URLView(rec.primary_url));
				table.addRow("AUP URL", new URLView(rec.aup_url));
				table.addRow("Membership Services URL", new URLView(rec.membership_services_url));
				table.addRow("Purpose URL", new URLView(rec.purpose_url));
				table.addRow("Support URL", new URLView(rec.support_url));

				//VO Report Names
				VOReportNameModel vorepname_model = new VOReportNameModel(context);
				ArrayList<VOReportNameRecord> vorepname_records = vorepname_model.getAllByVOID(rec.id);
				GenericView vorepname_view = new GenericView();
				for(VOReportNameRecord vorepname_record : vorepname_records) {
					vorepname_view.add(createVOReportNameView(context, vorepname_record));
				}
				table.addRow("Reports", vorepname_view);
			}
			
			table.addRow("OASIS Enabled", rec.use_oasis);
			if (rec.use_oasis) {
				StringBuffer users_str = new StringBuffer();
				VOOasisUserModel vooumodel = new VOOasisUserModel(context);
				users_str.append("<ul>");
				for(VOOasisUserRecord users : vooumodel.getByVOID(rec.id)) {
					ContactRecord person = pmodel.get(users.contact_id);
					users_str.append("<li>" + StringEscapeUtils.escapeHtml(person.name.trim()) + "</li>");
				}
				users_str.append("</ul>");
				table.addRow("OASIS Managers", new HtmlView(users_str.toString()));
			}
			
			table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
	}
	
	private String getSCName(UserContext context, Integer sc_id) throws SQLException
	{
		SCModel model = new SCModel(context);
		SCRecord rec = model.get(sc_id);
		return rec.name;
	}
	
	private IView getFieldOfScience(UserContext context, Integer vo_id) throws SQLException
	{
		VOFieldOfScienceModel model = new VOFieldOfScienceModel(context);
		ArrayList<VOFieldOfScienceRecord> list = model.getByVOID(vo_id);
		
		if(list == null) {
			return null;
		}
		String out = "";
		FieldOfScienceModel fmodel = new FieldOfScienceModel(context);
		//out += "<ul>";
		for(VOFieldOfScienceRecord rec : list) {
			FieldOfScienceRecord keyrec = new FieldOfScienceRecord();
			keyrec.id = rec.field_of_science_id;
			FieldOfScienceRecord frec = fmodel.get(keyrec);
			//out += "<li>" + frec.name + "</li>";
			
			ContactRank rank = ContactRank.get(rec.contact_rank_id);
			out += "<div class='contact_rank contact_"+rank+"'>";
			out +=  StringEscapeUtils.escapeHtml(frec.name.trim());
			out += "</div>";
		}
		//out += "</ul>";
		return new HtmlView(out);
	}

	private IView createVOReportNameView(UserContext context, VOReportNameRecord record)
	{
		GenericView view = new GenericView();
		RecordTableView table = new RecordTableView("inner_table");
		
		try {
			table.addHeaderRow(record.name);
			
			table.addRow("Associated FQANs", new HtmlView (""));
			Row row = table.new Row();
			row.addCell(getVOReportNameFqans(context, record.id), 2);
			table.addRow(row);
			
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			//ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			
			//reporting contacts 
			VOReportContactModel vorc_model = new VOReportContactModel(context);
			Collection<VOReportContactRecord> vorc_list = vorc_model.getAllByVOReportNameID(record.id);
			String cliststr = "";
			for(VOReportContactRecord vrc_record : vorc_list) {
				ContactRecord person = pmodel.get(vrc_record.contact_id);
				// AG: Remove rank from VORC
				ContactRank rank = ContactRank.get(vrc_record.contact_rank_id);
				cliststr += "<div class='contact_rank contact_"+rank+"'>";
				cliststr += StringEscapeUtils.escapeHtml(person.name);
				cliststr += "</div>";
			}
			table.addRow("Report Subscribers", new HtmlView(cliststr));
			view.add(table);
			view.add(new HtmlView("</div>"));
			
		} catch (Exception e) {
			log.error(e);
		}
		return view;
	}
	
	private IView getVOReportNameFqans(UserContext context, int vo_report_name_id) throws SQLException
	{
		VOReportNameFqanModel vorepnamefqan_model = new VOReportNameFqanModel(context);
		Collection<VOReportNameFqanRecord> records = vorepnamefqan_model.getAllByVOReportNameID(vo_report_name_id);
		// I don't like spitting out non-CSS HTML here .. leaving it for now. -agopu
		RecordTableView table = new RecordTableView("fqan_table");
		Row header_row = table.new Row();
		header_row.addHeaderCell(new HtmlView("Group Name"));
		header_row.addHeaderCell(new HtmlView("Optional Role"));
		table.addRow(header_row);
		for(VOReportNameFqanRecord record : records) {
			Row row = table.new Row();
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(record.group_name)));
			row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(record.role)));
			table.addRow(row);
		}
		return table;
	}

	private SideContentView createSideView(UserContext context, VORecord rec)
	{
		SideContentView view = new SideContentView();
		
		VOModel model = new VOModel(context);
		if(model.canEdit(rec.id)) {
			view.add(new HtmlView("<p><a class=\"btn\" href=\"voedit?id=" + rec.id + "\">Edit</a></p>"));
		}
		
		view.addRARequest(context, rec);
		
		//pull projects
		try {
			ProjectModel pmodel = new ProjectModel(context);
			ArrayList<ProjectRecord> projects = pmodel.getByVOID(rec.id);
			view.add(new ProjectView(projects, new ContactModel(context)));
		} catch (SQLException e) {
			log.error("Failed to load projects", e);
		}
		
		view.addContactLegend();
		return view;
	}
}
