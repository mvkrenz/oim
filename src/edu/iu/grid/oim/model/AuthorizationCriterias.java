package edu.iu.grid.oim.model;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.view.divrep.EditableContent;

//used to provide reason for why user can or can't do certain things

public class AuthorizationCriterias {

	ArrayList<AuthorizationCriteria> criterias = new ArrayList<AuthorizationCriteria>();
	
	//individual test, sort of.
	abstract public class AuthorizationCriteria {
		Boolean pass;
		String name; //like.. "Does this user provided x509 certificate" 
		String help_id; //editable help doc to be shown if requested - set to non-null if you want to let support staff edit this.
		
		public AuthorizationCriteria(String name, String help_id) {
			this.name = name;
			this.help_id = help_id;
			pass = test(); //should I do lazy-testing instead? For now, I don't think we gain much
			criterias.add(this);
		}
		
		//put your test code here to return true or false
		abstract public Boolean test();	
	}
	
	public void addAll(AuthorizationCriterias others) {
		criterias.addAll(others.criterias);
	}

	public boolean passAll() {
		for(AuthorizationCriteria criteria : criterias) {
			if(!criteria.pass) return false; 
		}
		//all good
		return true; 
	}

	public void renderHtml(final UserContext context, PrintWriter out) {
		final ConfigModel config = new ConfigModel(context);
		final Authorization auth = context.getAuthorization();
		
		for(final AuthorizationCriteria criteria : criterias) {
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
			if(criteria.help_id != null) {
				out.write("<div style=\"clear: both;\">");		
				DivRepToggler help = new DivRepToggler(context.getPageRoot()) {
					@Override
					public DivRep createContent() {
						Config help_content = config.new Config(config, criteria.help_id, "edit me");
						if(auth.allows("admin2") || auth.allows("admin_ra2")) {
							EditableContent content = new EditableContent(context.getPageRoot(), context, help_content);
							return content;	
						} else {
							return new DivRepStaticContent(context.getPageRoot(), help_content.getString());
						}
					}
				};
				help.setShowHtml("<u class=\"pull-right\">Explain</u>");
				help.setHideHtml("");
				help.render(out);
				out.write("</div>");//pull-right
			}
			
		
			out.write("</div>");//span
			out.write("</div>");//row
		}
	}

	//call this to retest
	public void retestAll() {
		for(AuthorizationCriteria criteria : criterias) {
			criteria.pass = criteria.test();
		}
	}
}

