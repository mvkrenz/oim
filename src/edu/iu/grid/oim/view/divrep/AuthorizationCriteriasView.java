package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.AuthorizationCriterias.AuthorizationCriteria;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;

public class AuthorizationCriteriasView extends DivRepLite {

	ArrayList<AuthorizationCriteriaView> criteria_views = new ArrayList<AuthorizationCriteriaView>();
	class AuthorizationCriteriaView extends DivRepLite {
		DivRepToggler help = null;
		AuthorizationCriteria criteria;
		@SuppressWarnings("serial")
		public AuthorizationCriteriaView(final UserContext context, DivRep _parent, final AuthorizationCriteria criteria) {
			super(_parent);
			this.criteria = criteria;
			
			final ConfigModel config = new ConfigModel(context);
			final Authorization auth = context.getAuthorization();
			
			if(criteria.help_id != null) {
				help = new DivRepToggler(context.getPageRoot()) {
					@Override
					public DivRep createContent() {
						Config help_content = config.new Config(config, criteria.help_id, "edit me");
						if(auth.allows("admin") || auth.allows("admin_ra")) {
							EditableContent content = new EditableContent(context.getPageRoot(), context, help_content);
							return content;	
						} else {
							return new DivRepStaticContent(context.getPageRoot(), help_content.getString());
						}
					}
				};
				help.setShowHtml("<u class=\"pull-right\">Explain</u>");
				help.setHideHtml("");
			}
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div class=\"row-fluid oim-criteria\">");
			out.write("<div class=\"span1\">");
			if(criteria.pass) {
				out.write("<i class=\"oim-criteria oim-criteria-pass\"></i>");
			} else {
				out.write("<i class=\"oim-criteria oim-criteria-fail\"></i>");
			}
			out.write("</div>");//span
			
			out.write("<div class=\"span11\">");
			out.write("<p class=\"pull-left oim-criteria-label\">"+StringEscapeUtils.escapeHtml(criteria.name)+"</p>");
			//help toggle
			if(help != null) {
				out.write("<div style=\"clear: both;\">");		
				help.render(out);
				out.write("</div>");//pull-right
			}
		
			out.write("</div>");//span
			out.write("</div>");//row
		}
		
	}
	
	public AuthorizationCriteriasView(final DivRep _parent, final UserContext context, AuthorizationCriterias criterias) {
		super(_parent);
		for(final AuthorizationCriteria criteria : criterias.getCriterias()) {
			criteria_views.add(new AuthorizationCriteriaView(context, this, criteria));
		}
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		for(AuthorizationCriteriaView view : criteria_views) {
			view.render(out);
		}
		out.write("</div>");
	}

}
