package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

public class CNEditor extends DivRepFormElement<String> {

	boolean user_modified = false;
	public boolean hasUserModified() { return user_modified; }
	
	DivRepTextBox cn;
	public CNEditor(DivRep parent) {
		super(parent);
		cn = new DivRepTextBox(this);
		cn.addValidator(new DivRepIValidator<String>(){
			boolean valid = true;
			
			@Override
			public Boolean isValid(String value) {
				//I am not sure how effective this is..
				try {
					X500Name name = new X500Name("CN="+value);
				} catch(Exception e) {
					return false;
				}
				
				if(value.contains("/")) {
					//we can't use / in apache format.. which is the format stored in our DB
					return false;
				}
				
				return true;
			}

			@Override
			public String getErrorMessage() {
				return "Failed to validate DN";
			}});
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		if(!isHidden()) {
			if(getLabel() != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(getLabel())+"</label><br/>");
			}
			out.write("<div style=\"float: left;margin: 5px 4px\">/DC=com/DC=DigiCert-Grid/OU=People/CN=</div>");		
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