package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;

import edu.iu.grid.oim.view.IView;

public class Wizard extends DivRep 
{
	public Wizard(DivRep _parent) {
		super(_parent);
	}

	private static final long serialVersionUID = 1L;

	public abstract class WizardPage extends DivRepForm {
		
		String name;
		
		public WizardPage(String name) {
			super(Wizard.this, null);
			this.name = name;
			// TODO Auto-generated constructor stub
			setSubmitLabel("Next");
			init();
			
			/* disable submit button until page is valid?
			if(!validate()) {
				submitbutton.setDisabled(false);
			}
			*/
			
			pages.add(this);
			//make first page added as active - by default
			if(active == null) {
				active = this;
			}
		}
		@Override
		protected Boolean doSubmit() {
			onNext();
			return false;
		}
		
		//override these
		protected abstract void onNext();
		protected abstract void init();
		public void disableNext() {
			submitbutton.setDisabled(true);
		}
		
	}
	ArrayList<WizardPage> pages  = new ArrayList<WizardPage>();
	
	WizardPage active = null;
	public void setActive(WizardPage page) {
		active = page;
		redraw();
	}
	
	public void render(PrintWriter out) {
		out.write("<div class=\"divrep_wizard\" id=\""+getNodeID()+"\">");
		
		//display tab
		out.write("<ul class=\"divrep_wizard_tabs\">");
		for(WizardPage page : pages) {
			String cls = "";
			if(active == page) {
				cls = "divrep_wizard_tab_active";
			}
			out.write("<li class=\""+cls+"\"><a nohref>"+StringEscapeUtils.escapeHtml(page.name)+"</a></li>");
		}
		out.write("</ul>");
		
		//display active page
		if(active != null) {
			active.render(out);
		}
		
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
