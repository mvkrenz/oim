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
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOReportNameModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView.Row;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class VOServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOServlet.class);  
	
    public VOServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		auth.check("edit_my_vo");
		
		try {	
			//construct view
			MenuView menuview = new MenuView(context, "vo");
			ContentView contentview = createContentView();
			Page page = new Page(context, menuview, contentview, createSideView());
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
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
		
		ContentView contentview = new ContentView();
		contentview.add(new HtmlView("<h1>Virtual Organizations</h1>"));
		if(editable_vos.size() == 0) {
			contentview.add(new HtmlView("<p>You currently don't have any virtual organization that list your contact in any of the contact types.</p>"));
		}
		for(VORecord rec : editable_vos) {
			String name = rec.name;
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
			contentview.add(showVO(rec, true)); //true = show edit button
		}
		
		if(readonly_vos.size() != 0) {
			contentview.add(new HtmlView("<br/><h1>Read-Only Virtual Organizations</h1>"));
			contentview.add(new HtmlView("<p>Following are the virtual organizations that are currently registered at OIM that you do not have edit access.</p>"));
	
			for(VORecord rec : readonly_vos) {
				String name = rec.name;
				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(name)+"</h2>"));
				contentview.add(showVO(rec, false)); //false = no edit button
			}
		}
		
		return contentview;
	}
	
	private DivRepToggler showVO(final VORecord rec, final boolean show_edit_button)
	{
		DivRepToggler toggler = new DivRepToggler(context.getPageRoot()) {
			public DivRep createContent() {
				RecordTableView table = new RecordTableView();
				try {	
	
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
					table.addRow("Parent VO", parent_vo_name);
					table.addRow("Support Center", getSCName(rec.sc_id));
					table.addRow("Long Name", rec.long_name);
					table.addRow("Description", rec.description);
					table.addRow("App Description", rec.app_description);
					table.addRow("Community", rec.community);
					table.addRow("Field of Science", getFieldOfScience(rec.id));
					table.addRow("Primary URL", new HtmlView("<a target=\"_blank\" href=\""+rec.primary_url+"\">"+rec.primary_url+"</a>"));
					table.addRow("AUP URL", new HtmlView("<a target=\"_blank\" href=\""+rec.aup_url+"\">"+rec.aup_url+"</a>"));
					table.addRow("Membership Services URL", new HtmlView("<a target=\"_blank\" href=\""+rec.membership_services_url+"\">"+rec.membership_services_url+"</a>"));
					table.addRow("Purpose URL", new HtmlView("<a target=\"_blank\" href=\""+rec.purpose_url+"\">"+rec.purpose_url+"</a>"));
					table.addRow("Support URL", new HtmlView("<a target=\"_blank\" href=\""+rec.support_url+"\">"+rec.support_url+"</a>"));
				
					ContactTypeModel ctmodel = new ContactTypeModel(context);
					ContactRankModel crmodel = new ContactRankModel(context);
					ContactModel pmodel = new ContactModel(context);
					
					//contacts (only shows contacts that are filled out)
					VOContactModel vocmodel = new VOContactModel(context);
					ArrayList<VOContactRecord> voclist = vocmodel.getByVOID(rec.id);
					HashMap<Integer, ArrayList<VOContactRecord>> voclist_grouped = vocmodel.groupByContactTypeID(voclist);
					for(Integer type_id : voclist_grouped.keySet()) {
						ContactTypeRecord ctrec = ctmodel.get(type_id);
		
						ArrayList<VOContactRecord> clist = voclist_grouped.get(type_id);
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
							ContactRankRecord rank = crmodel.get(vcrec.contact_rank_id);
		
							cliststr += "<div class='contact_rank contact_"+rank.name+"'>";
							cliststr += person.name;
							cliststr += "</div>";
						}
						table.addRow(ctrec.name, new HtmlView(cliststr));
						
					}			
		
					//VO Report Names
					VOReportNameModel vorepname_model = new VOReportNameModel(context);
					ArrayList<VOReportNameRecord> vorepname_records = vorepname_model.getAllByVOID(rec.id);
					GenericView vorepname_view = new GenericView();
					for(VOReportNameRecord vorepname_record : vorepname_records) {
						vorepname_view.add(createVOReportNameView(vorepname_record));
					}
					table.addRow("Reports", vorepname_view);
		
					if(auth.allows("admin_vo")) {
						table.addRow("Footprints ID", rec.footprints_id);
					}
					table.addRow("Active", rec.active);
					table.addRow("Disable", rec.disable);
							
					if(show_edit_button) {
						class EditButtonDE extends DivRepButton
						{
							String url;
							public EditButtonDE(DivRep parent, String _url)
							{
								super(parent, "Edit");
								url = _url;
							}
							protected void onEvent(DivRepEvent e) {
								redirect(url);
							}
						};
						table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), StaticConfig.getApplicationBase()+"/voedit?id=" + rec.id)));
					}
				} catch (SQLException e) {
					return new DivRepStaticContent(this, e.toString());
				}
				return new ViewWrapper(context.getPageRoot(), table);
			}
		};
		return toggler;
	}
	
	private String getSCName(Integer sc_id) throws SQLException
	{
		SCModel model = new SCModel(context);
		SCRecord rec = model.get(sc_id);
		return rec.name;
	}
	
	private IView getFieldOfScience(Integer vo_id) throws SQLException
	{
		VOFieldOfScienceModel model = new VOFieldOfScienceModel(context);
		ArrayList<VOFieldOfScienceRecord> list = model.getByVOID(vo_id);
		
		if(list == null) {
			return null;
		}
		String out = "";
		FieldOfScienceModel fmodel = new FieldOfScienceModel(context);
		for(VOFieldOfScienceRecord rec : list) {
			FieldOfScienceRecord keyrec = new FieldOfScienceRecord();
			keyrec.id = rec.field_of_science_id;
			FieldOfScienceRecord frec = fmodel.get(keyrec);
			out += frec.name + "<br/>";
		}
		return new HtmlView(out);
	}

	private IView createVOReportNameView(VOReportNameRecord record)
	{
		GenericView view = new GenericView();
		RecordTableView table = new RecordTableView("inner_table");
		
		try {
			table.addHeaderRow(record.name);
			
			table.addRow("Associated FQANs", new HtmlView (""));
			Row row = table.new Row();
			row.addCell(getVOReportNameFqans(record.id), 2);
			table.addRow(row);
			
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			ContactRankModel crmodel = new ContactRankModel(context);
			ContactModel pmodel = new ContactModel(context);
			
			//reporting contacts 
			VOReportContactModel vorc_model = new VOReportContactModel(context);
			Collection<VOReportContactRecord> vorc_list = vorc_model.getAllByVOReportNameID(record.id);
			String cliststr = "";
			for(VOReportContactRecord vrc_record : vorc_list) {
				ContactRecord person = pmodel.get(vrc_record.contact_id);
				// AG: Remove rank from VORC
				ContactRankRecord rank = crmodel.get(vrc_record.contact_rank_id);
				cliststr += "<div class='contact_rank contact_"+rank.name+"'>";
				cliststr += person.name;
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
	
	private IView getVOReportNameFqans(int vo_report_name_id) throws SQLException
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

	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New Virtual Organization");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "voedit"));
		//view.add("About", new HtmlView("This page shows a list of Virtual Organization that you have access to edit."));		
		view.addContactLegend();
		
		return view;
	}
}
