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
import edu.iu.grid.oim.view.IView;

public class UserCertCancel implements IView {
    static Logger log = Logger.getLogger(UserCertCancel.class);
	
	UserContext context;
	Authorization auth;
	CertificateRequestUserRecord rec;
	GenericView pane;
	
	public UserCertCancel(final UserContext context, final CertificateRequestUserRecord rec) {
		this.context = context;
		this.rec = rec;
		auth = context.getAuthorization();
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		pane = new GenericView();
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setHeight(40);
		note.setLabel("Note");
		note.setSampleValue("Why are you canceling this request?");
		note.setRequired(true);
		pane.add(note);
		
		final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn\">Cancel Request</button>") {
			protected void onClick(DivRepEvent e) {
            	if(note.validate()) {
            		context.setComment(note.getValue());
                	try {
            			//check access again - request status might have changed
	        			if(model.canCancel(rec)) {
	                    	model.cancel(rec);
                			context.message(MessageType.SUCCESS, "Successfully canceled a certificate request with ID: " + rec.id);
							js("location.reload();");
	        			} else {
	        				alert("Reques status has changed. Please reload.");
	        			}
                	} catch(CertificateRequestException ex) {
                		log.warn("CertificateRequestException while canceling certificate request:", ex);
            			String message = "Failed to cancel request: " + ex.getMessage();
            			if(ex.getCause() != null) {
            				message += "\n\n" + ex.getCause().getMessage();
            			}
                		alert(message);
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
		pane.render(out);
	}
}
