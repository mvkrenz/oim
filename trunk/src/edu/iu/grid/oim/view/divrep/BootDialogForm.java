package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.i18n.Labels;
import org.apache.commons.lang.StringEscapeUtils;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;

//This DivRep requires bootstrap modal jquery plugin
abstract public class BootDialogForm extends DivRep {
	private String title = "(call setTitle)";
	public String getTitle() { return title; }
	public void setTitle(String t) { title = t; }
	
	abstract protected boolean doSubmit();
	
	public BootDialogForm(DivRep parent) {
		super(parent);
	}
	
	protected void onEvent(DivRepEvent e) {
		if(e.value.equals("cancel")) {
			modified(false);
		} else if(e.value.equals("submit")) {
			submit();
		}
	}
	
	protected void setFormModified() {
		modified(true);
	}
	
	private Boolean show = false; //hidden by default
	public void show() { 
		show = true;
		redraw();
	}
	
	//derived class can call this function to force submit.
	protected void submit()
	{
		if(validate()) {
			if(doSubmit()) {
				modified(false);
				js("$('#"+getNodeID()+"_modal').modal('hide');"); //show = false && redraw() leaves the backdrop..
			}
		}
	}
	
	public boolean validate()
	{
		boolean valid = true;
		
		//validate *all* elements
		for(DivRep child : childnodes) {
			if(child instanceof DivRepFormElement) { 
				DivRepFormElement element = (DivRepFormElement)child;
				if(element != null && !element.isHidden()) {
					if(!element.validate()) {
						valid = false;
					}
				}
			}
		}
		
		return valid;
	}

	
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		if(show) {
			out.write("<div id=\""+getNodeID()+"_modal\" class=\"modal fade\">");
			
			//display header
			out.write("<div class=\"modal-header\">");
			out.write("<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>");
			out.write("<h3>"+StringEscapeUtils.escapeHtml(title)+"</h3>");
			out.write("</div>");
			
			//display body
			out.write("<div class=\"modal-body\">");
			for(DivRep child : childnodes) {
				if(child instanceof DivRepFormElement) {
					out.print("<div class=\"control-group\">");
					child.render(out);
					out.print("</div>");
				} else {
					//non form element..
					child.render(out);
				}
			}
			out.write("</div>");
			
			//display footer
			out.write("<div class=\"modal-footer\">");
			out.write("<button class=\"btn btn-primary\" onclick=\"divrep('"+getNodeID()+"', this, 'submit'); return false; \">Submit</button>");
			out.write("<button data-dismiss=\"modal\" class=\"btn\">Cancel</button>");
			out.write("</div>");
			
			out.write("</div>");

			out.write("<script>");
			out.write("$('#"+getNodeID()+"_modal').modal();");
			out.write("$('#"+getNodeID()+"_modal').on('hide', function() { divrep('"+getNodeID()+"', this, 'cancel'); });");
			out.write("</script>");
		}
	
		out.write("</div>");
	}

}
