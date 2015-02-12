package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;

public class Wizard extends DivRep 
{
	public Wizard(DivRep _parent) {
		super(_parent);
	}

	private static final long serialVersionUID = 1L;

	public abstract class WizardPage extends DivRepForm {
		
		String name;
		Boolean initialized = false;
		public WizardPage(String name) {
			super(Wizard.this, null);//set origin to null
			this.name = name;
			// TODO Auto-generated constructor stub
			setSubmitLabel("Next");
			
			/* disable submit button until page is valid?
			if(!validate()) {
				submitbutton.setDisabled(false);
			}
			*/
			//pages.add(this);//use addPage instead
			//make first page added as active - by default
		}
		
		//why don't I let derived class override doSubmit()? 
		//Maybe I should.. It's just that, by returning true, the page will be set with modified(false).
		//otherwise, user will see "the form has been updated are you sure you want to navigate out of this page" alert when they 
		//try to go somewhere else. Since the semantics of "next" isn't exactly the same as "submit" on the normal form, I think
		//it's slightly more correct to override this and force each page to modified(false)..
		@Override
		protected Boolean doSubmit() {
			onNext();
			return true; 
		}
		
		//override these
		protected abstract void onNext();
		protected abstract void init();
		public void disableNext() {
			submitbutton.setDisabled(true);
		}
		public void hideNext() {
			submitbutton.setHidden(true);
		}	
	}
	
	ArrayList<WizardPage> pages  = new ArrayList<WizardPage>();
	public void addPage(int i, WizardPage page) {
		pages.add(i, page);
	}
	public void addPage(WizardPage page) {
		pages.add(page);
	}
	
	WizardPage active = null;
	public void setActive(WizardPage page) {
		active = page;
		redraw();
	}
	
	public void render(PrintWriter out) {
		out.write("<div class=\"divrep_wizard\" id=\""+getNodeID()+"\">");
		
		//select first page as active if not yet specified at this point
		if(active == null) {
			active = pages.get(0);
		}
		
		//display chevlons
		out.write("<ul class=\"divrep_chevs\">");
		for(WizardPage page : pages) {
			if(page.initialized == false) {
				page.init();
				page.initialized = true;
			}
			String cls = "";
			if(active == page) {
				cls = "active";
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
