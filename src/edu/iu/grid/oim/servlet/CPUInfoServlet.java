package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CpuInfoModel;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.TableView;

public class CPUInfoServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(CPUInfoServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("edit_measurement"); 
		
		try {
			//construct view
			BootMenuView menuview = new BootMenuView(context, "cpuinfo");;
			ContentView contentview = createContentView(context);
			
			BootPage page = new BootPage(context, menuview, contentview, null);
			page.render(response.getWriter());				
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView(UserContext context) 
		throws ServletException, SQLException
	{
		CpuInfoModel model = new CpuInfoModel(context);
		ArrayList<CpuInfoRecord> cpus = model.getAll();
		Collections.sort(cpus, new Comparator<CpuInfoRecord> (){
			public int compare(CpuInfoRecord a, CpuInfoRecord b) {
				return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
			}
		});

		ContentView contentview = new ContentView();	
		if(context.getAuthorization().isUser()) {
			contentview.add(new HtmlView("<a class=\"btn pull-right\" href=\"cpuinfoedit\"><i class=\"icon-plus-sign\"></i> Add New CPU Info</a>"));
		}
		contentview.add(new HtmlView("<h2>CPU Information</h2>"));
		
		contentview.add(new HtmlView("<table class=\"table\">"));
		contentview.add(new HtmlView("<thead><tr><th>Name</th><th>Normalization&nbsp;Constant</th><th>HEPSPEC&nbsp;Normalization&nbsp;Constant</th><th>Notes</th><th></th></tr></thead>"));	

		contentview.add(new HtmlView("<tbody>"));
		for(CpuInfoRecord rec : cpus) {
			//contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));
				
			contentview.add(new HtmlView("<tr>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.name)+"</td>"));	
			contentview.add(new HtmlView("<td>"+rec.normalization_constant.toString()+"</td>"));	
			contentview.add(new HtmlView("<td>"+rec.hepspec_normalization_constant.toString()+"</td>"));	
			contentview.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.notes)+"</td>"));	
			
			contentview.add(new HtmlView("<td>"));
			if(context.getAuthorization().isUser()) {
				contentview.add(new HtmlView("<a class=\"btn\" href=\"cpuinfoedit?cpu_info_id="+rec.id+"\">Edit</a>"));
			}
			contentview.add(new HtmlView("</td>"));
			
			contentview.add(new HtmlView("</tr>"));	

		}
		contentview.add(new HtmlView("</tbody>"));
		contentview.add(new HtmlView("</table>"));
		
		return contentview;
	}
	
	private SideContentView createSideView()
	{
		SideContentView view = new SideContentView();
		/*
		class NewButtonDE extends DivRepButton
		{
			String url;
			public NewButtonDE(DivRep parent, String _url)
			{
				super(parent, "Add New CPU Info record");
				url = _url;
			}
			protected void onEvent(DivRepEvent e) {
				redirect(url);
			}
		};
		view.add("Operation", new NewButtonDE(context.getPageRoot(), "cpuinfoedit"));
		*/
		return view;
	}
}
