package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;

public class UserCNEditor extends DivRepFormElement<String> {
	boolean user_modified = false;
	public boolean hasUserModified() { return user_modified; }
	
	DivRepTextBox cn;
	public UserCNEditor(DivRep parent) {
		super(parent);
		cn = new DivRepTextBox(this);
		cn.addValidator(new CNValidator(CNValidator.Type.USER));
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div class=\"cneditor\" id=\""+getNodeID()+"\">");
		if(!isHidden()) {
			/*
			if(getLabel() != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(getLabel())+"</label><br/>");
			}
			String dn_base = StaticConfig.conf.getProperty("digicert.user_dn_base");
			out.write("<div style=\"float: left;margin: 5px 4px\">"+dn_base+"/CN=</div>");
			*/
			if(getLabel() != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(getLabel())+"</label><br/>");
			}
			cn.render(out);
		}
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		user_modified = true;
	}
	
	@Override
	public void setRequired(Boolean b) { 
		super.setRequired(b);
		cn.setRequired(true);
	}
	
	@Override
	public void setValue(String value) { cn.setValue(value); }
	
	@Override
	public String getValue() { return cn.getValue(); }
	
	@Override
	public boolean validate()
	{
		return cn.validate();
	}
	
	@Override 
	public void setDisabled(Boolean b) {
		cn.setDisabled(b);
	}
}