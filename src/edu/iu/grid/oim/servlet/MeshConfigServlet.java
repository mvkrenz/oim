package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.MeshConfigModel;
import edu.iu.grid.oim.model.db.MeshConfigTestModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.MeshConfigRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.IView;

public class MeshConfigServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(MeshConfigServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		BootMenuView menuview = new BootMenuView(context, "meshconfig");
		BootPage page = new BootPage(context, menuview, createContent(context), null);
		page.render(response.getWriter());		
	}
	
	void renderTest(PrintWriter out, MeshConfigTestRecord test) {
		out.write("type:"+test.service_id);
	}
	
	protected IView createConfigPane(final UserContext context) {
		return new IView(){
			public void render(PrintWriter out) {
				MeshConfigModel model = new MeshConfigModel(context);
				VOModel vomodel = new VOModel(context);
				MeshConfigTestModel testmodel = new MeshConfigTestModel(context);
				try {
					out.write("<table class=\"table\">");
					out.write("<thead><tr><th>Config Name</th><th>Desc.</th><th>VO</th><th>Tests</th><th></th></tr></thead>");
					out.write("<tbody>");
					for(MeshConfigRecord rec : model.getAll()) {
						out.write("<tr>");
						
						out.write("<td>"+StringEscapeUtils.escapeHtml(rec.name));
						if(rec.disable) {
							out.write(" <span class=\"label label-default\">Disabled</span>");
						}
						out.write("</td>");
						
						out.write("<td>"+StringEscapeUtils.escapeHtml(rec.description)+"</td>");						
						VORecord vorec = vomodel.get(rec.vo_id);
						out.write("<td>"+StringEscapeUtils.escapeHtml(vorec.name)+"</td>");
						
						//list tests
						out.write("<td>");
						for(MeshConfigTestRecord test : testmodel.getByMeshconfigID(rec.id)) {
							renderTest(out, test);
						}
						out.write("</td>");
						
						out.write("<td>");
						
						DivRepButton edit = new DivRepButton(context.getPageRoot(), "Edit") {
							@Override
							protected void onClick(DivRepEvent e) {
								alert("hi");
							}
						};
						edit.addClass("btn");
						edit.addClass("btn-mini");
						edit.render(out);
						
						out.write("</td>");
						out.write("</tr>");
					}
					out.write("</tbody></table>");
				} catch (SQLException e) {
					log.error("failed to load meshconfig records",e);
				}
			}
		};
	}
	
	protected IView createGroupPane(final UserContext context) {
		return new IView(){
			public void render(PrintWriter out) {
				out.write("Group Pane - TODO");
			}
		};
	}
	protected IView createParamPane(final UserContext context) {
		return new IView(){
			public void render(PrintWriter out) {
				out.write("PArameter Pane - TODO");
			}
		};
	}
	
	protected IView createContent(final UserContext context) throws ServletException {
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				
				out.write("<div id=\"content\">");
				out.write("<h2>MeshConfig Administrator</h2>");
				
				BootTabView tabview = new BootTabView();
				tabview.addtab("Configs/Tests", createConfigPane(context));
				tabview.addtab("Groups", createGroupPane(context));
				tabview.addtab("Parameters", createParamPane(context));
				tabview.render(out);
				
				out.write("</div>"); //content
			}
		};
	}
}
