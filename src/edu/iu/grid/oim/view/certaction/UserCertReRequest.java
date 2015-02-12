package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.servlet.CertificateUserServlet;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.ChoosePassword;

public class UserCertReRequest implements IView {
    static Logger log = Logger.getLogger(UserCertReRequest.class);
	
	UserContext context;
	Authorization auth;
	CertificateRequestUserRecord rec;
	GenericView pane;
	
	public UserCertReRequest(final UserContext context, final CertificateRequestUserRecord rec) {
		this.context = context;
		this.rec = rec;
		auth = context.getAuthorization();
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		pane = new GenericView();
		//allow guest to re-request with retrieval password
		final ChoosePassword pass = new ChoosePassword(context.getPageRoot(), context);
		if(!auth.isUser()) {
			pane.add(new HtmlView("<p class=\"help-block\">If you are the original requester of this request, you can re-request to issue another certificate with the same CN.</p>"));
			pane.add(pass);
		}
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setHeight(40);
		note.setLabel("Note");
		note.setSampleValue("Why are you re-requesting this certificate request?");
		note.setRequired(true);
		pane.add(note);
		
		final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Re-request</button>") {
			protected void onClick(DivRepEvent e) {
            	if(note.validate()) {
            		context.setComment(note.getValue());
            		
            		//guest must provide password
            		if(!auth.isUser()) {
            			if(!pass.validate()) return;
            		}
            		
            		try {
            			//check access again - request status might have changed
	        			if(model.canReRequest(rec)) {
	        				model.rerequest(rec, pass.getValue());
                			context.message(MessageType.SUCCESS, "Successfully re-requested a certificate request with ID: " + rec.id + " You will be notified when your RA/sponsors approves or rejects your request.");   
							js("location.reload();");
	        			} else {
	        				alert("Reques status has changed. Please reload.");
	        			}
            		} catch (CertificateRequestException ex) {
                		alert("Failed to re-request: " + ex.getMessage());
                	}
            	}					
			}
		};
		button.setStyle(DivRepButton.Style.HTML);
		button.addClass("inline");
		pane.add(button);
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<p>Although this request has been terminated, you can re-request to get this certificate approved and issued.</p>");
		pane.render(out);
	}
}
