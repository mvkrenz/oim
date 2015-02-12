package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.servlet.CertificateUserServlet;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.UserCNEditor;

import org.bouncycastle.asn1.x500.X500Name;

public class UserCertApprove implements IView {
    static Logger log = Logger.getLogger(UserCertApprove.class);
	
	UserContext context;
	Authorization auth;
	CertificateRequestUserRecord rec;
	GenericView pane;
	
	public UserCertApprove(final UserContext context, final CertificateRequestUserRecord rec) {
		this.context = context;
		this.rec = rec;
		auth = context.getAuthorization();
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		pane = new GenericView();
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setHeight(40);
		note.setLabel("Note");
		note.setSampleValue("Reasons for this approval.");
		note.setRequired(true);
		pane.add(note);
		
		//pane.add(new DivRepStaticContent(context.getPageRoot(), "<p class=\"alert\">Please update this to requested CN</p>"));
		
		final UserCNEditor cn_override = new UserCNEditor(context.getPageRoot());
		cn_override.setLabel("CN Override");
		cn_override.setRequired(true);
		cn_override.setValue(rec.getCN());
		pane.add(cn_override);
		
		final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>") {
			protected void onClick(DivRepEvent e) {
            	if(note.validate()) {
            		context.setComment(note.getValue());
            		
            		//TODO - need to move this check to model.approve() - before we add REST interface
            		if(model.canOverrideCN(rec)) {
            			if(cn_override.validate()) {
	                		//Regenerate DN using provided CN
	                		X500Name name = model.generateDN(cn_override.getValue());
	                		rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
	                		
	                		//make sure we don't have duplicate CN requested already.
							try {
								DNModel dnmodel = new DNModel(context);
		                		DNRecord duplicate = dnmodel.getEnabledByDNString(rec.dn);
		                		if(duplicate != null/* && !duplicate.contact_id.equals(rec.requester_contact_id)*/) {
		                			alert("The same DN is already registered in OIM for user id:"+duplicate.contact_id + ". Please specify different CN");
		                			return;
		                		}
							} catch (SQLException e1) {
								log.error("Failed to test duplicate DN during approval process", e1);
								alert("Failed to test duplicate DN.");
								return;
							}	                		
            			} else {
            				alert("Failed to validate provided CN.");
            				return;
            			}
            		}
            			                	
            		try {
            			//check access again - request status might have changed
            			if(model.canApprove(rec)) {
            				model.approve(rec);
                			context.message(MessageType.SUCCESS, "Successfully approved a request with ID: " + rec.id);
							js("location.reload();");
            			} else {
            				alert("Reques status has changed. Please reload.");
            			}
            		} catch (CertificateRequestException ex) {
            			String message = "Failed to approve request: " + ex.getMessage();
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
