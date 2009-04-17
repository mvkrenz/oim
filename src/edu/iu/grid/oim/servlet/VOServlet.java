package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
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
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivExWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;
import edu.iu.grid.oim.view.Utils;
import edu.iu.grid.oim.view.TableView.Row;

public class VOServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(VOServlet.class);  
	
    public VOServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		setAuth(request);
		
		//pull list of all vos
		Collection<VORecord> vos = null;
		VOModel model = new VOModel(auth);
		try {
			vos = model.getAllEditable();
		
			//construct view
			MenuView menuview = createMenuView("vo");
			DivExRoot root = DivExRoot.getInstance(request);
			ContentView contentview = createContentView(root, vos);
			Page page = new Page(menuview, contentview, createSideView(root));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(final DivExRoot root, Collection<VORecord> vos) 
		throws ServletException, SQLException
	{
		ContentView contentview = new ContentView();	
		contentview.add(new HtmlView("<h1>Virtual Organization</h1>"));
	
		for(VORecord rec : vos) {
			contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
			/*
			//RSS feed button
			contentview.add(new HtmlView("<div class=\"right\"><a href=\"http://oimupdate.blogspot.com/feeds/posts/default/-/vo_"+rec.id+"\" target=\"_blank\"/>"+
					"Subscribe to Updates</a></div>"));
			*/
			RecordTableView table = new RecordTableView();
			contentview.add(table);

			//pull parent VO
			VOModel model = new VOModel(auth);
			VORecord parent_vo_rec = model.getParentVO(rec.id);
			String parent_vo_name = null;
			if(parent_vo_rec != null) {
				parent_vo_name = parent_vo_rec.name;
			}
			table.addRow("Parent VO", parent_vo_name);
	
			table.addRow("Support Center", rec.toString(rec.sc_id, auth));

			table.addRow("Long Name", rec.long_name);
			table.addRow("Description", rec.description);
			table.addRow("App Description", rec.app_description);
			table.addRow("Community", rec.community);
			table.addRow("Field of Scicnce", getFieldOfScience(rec.id));

			table.addRow("Primary URL", new HtmlView("<a target=\"_blank\" href=\""+rec.primary_url+"\">"+rec.primary_url+"</a>"));
			table.addRow("AUP URL", new HtmlView("<a target=\"_blank\" href=\""+rec.aup_url+"\">"+rec.aup_url+"</a>"));
			table.addRow("Membership Services URL", new HtmlView("<a target=\"_blank\" href=\""+rec.membership_services_url+"\">"+rec.membership_services_url+"</a>"));
			table.addRow("Purpose URL", new HtmlView("<a target=\"_blank\" href=\""+rec.purpose_url+"\">"+rec.purpose_url+"</a>"));
			table.addRow("Support URL", new HtmlView("<a target=\"_blank\" href=\""+rec.support_url+"\">"+rec.support_url+"</a>"));
		
			ContactTypeModel ctmodel = new ContactTypeModel(auth);
			ContactRankModel crmodel = new ContactRankModel(auth);
			ContactModel pmodel = new ContactModel(auth);
			
			//contacts (only shows contacts that are filled out)
			VOContactModel vocmodel = new VOContactModel(auth);
			ArrayList<VOContactRecord> voclist = vocmodel.getByVOID(rec.id);
			HashMap<Integer, ArrayList<VOContactRecord>> voclist_grouped = vocmodel.groupByContactTypeID(voclist);
			for(Integer type_id : voclist_grouped.keySet()) {
				ArrayList<VOContactRecord> clist = voclist_grouped.get(type_id);
				ContactTypeRecord ctrec = ctmodel.get(type_id);
				
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
			VOReportNameModel vorepname_model = new VOReportNameModel(auth);
			ArrayList<VOReportNameRecord> vorepname_records = vorepname_model.getAllByVOID(rec.id);
			GenericView vorepname_view = new GenericView();
			for(VOReportNameRecord vorepname_record : vorepname_records) {
				vorepname_view.add(createVOReportNameView(vorepname_record));
			}
			table.addRow("Reporting Names", vorepname_view);

			if(auth.allows("admin_vo")) {
				table.addRow("Footprints ID", rec.footprints_id);
			}
			table.addRow("Active", rec.active);
			table.addRow("Deactivated", rec.disable);
			
			
			class EditButtonDE extends ButtonDE
			{
				String url;
				public EditButtonDE(DivEx parent, String _url)
				{
					super(parent, "Edit");
					url = _url;
				}
				protected void onEvent(Event e) {
					redirect(url);
				}
			};
			table.add(new DivExWrapper(new EditButtonDE(root, Config.getApplicationBase()+"/voedit?vo_id=" + rec.id)));

		}
		
		return contentview;
	}
	
	private IView getFieldOfScience(Integer vo_id) throws SQLException
	{
		VOFieldOfScienceModel model = new VOFieldOfScienceModel(auth);
		ArrayList<VOFieldOfScienceRecord> list = model.getByVOID(vo_id);
		
		if(list == null) {
			return null;
		}
		String out = "";
		FieldOfScienceModel fmodel = new FieldOfScienceModel(auth);
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
			table.addRow("Associated FQANs", new HtmlView (getVOReportNameFqans(record.id)));

			ContactTypeModel ctmodel = new ContactTypeModel(auth);
			ContactRankModel crmodel = new ContactRankModel(auth);
			ContactModel pmodel = new ContactModel(auth);
			
			//reporting contacts 
			VOReportContactModel vorc_model = new VOReportContactModel(auth);
			ArrayList<VOReportContactRecord> vorc_list = vorc_model.getByVOReportNameID(record.id);
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
	
	private String getVOReportNameFqans(int vo_report_name_id) throws SQLException
	{
		String html = "";
		VOReportNameFqanModel vorepnamefqn_model = new VOReportNameFqanModel(auth);
		ArrayList<VOReportNameFqanRecord> records = vorepnamefqn_model.getAllByVOReportNameID(vo_report_name_id);
		for(VOReportNameFqanRecord record : records) {
			html += StringEscapeUtils.escapeHtml(record.fqan) + "<br/>";
		}
		return html;
	}

	private SideContentView createSideView(DivExRoot root)
	{
		SideContentView view = new SideContentView();
		
		class NewButtonDE extends ButtonDE
		{
			String url;
			public NewButtonDE(DivEx parent, String _url)
			{
				super(parent, "Add New Virtual Organization");
				url = _url;
			}
			protected void onEvent(Event e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(root, "voedit"));
		view.add("About", new HtmlView("This page shows a list of Virtual Organization that you have access to edit."));		
		return view;
	}
}
