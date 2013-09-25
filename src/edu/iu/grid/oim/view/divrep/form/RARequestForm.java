package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.BootDialogForm;

//assume user is logged  in
public class RARequestForm extends BootDialogForm {
    static Logger log = Logger.getLogger(CertificateRequestUserModel.class);  
    
	private DivRepTextArea desc;
	private UserContext context;
	private ContactRecord vomanager;
	private VORecord vorec;
	private SCRecord screc;
	
	public RARequestForm(UserContext context, ContactRecord vomanager, VORecord vorec, SCRecord screc) {
		super(context.getPageRoot());
		this.context = context;
		this.vomanager = vomanager;
		this.vorec = vorec;
		this.screc = screc;
		
		setTitle("RA Enrollment Request");
		
		new DivRepStaticContent(this, "<p class=\"help-block\">Please update following request template and submit.</p>");
		
		desc = new DivRepTextArea(this);
		desc.setLabel("Request Detail");
		
		//load request template
		InputStream template  = getClass().getResourceAsStream("osg.certificate.ra.txt");
		StringBuilder request = ResourceReader.loadContent(template);
		desc.setValue(request.toString());
		
		desc.setHeight(300);
		desc.setWidth(450);
		desc.setRequired(true);

	}
	
	@Override
	protected boolean doSubmit() {
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ContactRecord requester = auth.getContact();
			ticket.title = "RA Request for " + requester.name;
			ticket.name = requester.name;
			ticket.email = requester.primary_email;
			ticket.phone = requester.primary_phone;
			ticket.description = desc.getValue();
			ticket.metadata.put("SUBMITTED_VIA","OIM/RARequestForm");
			ticket.metadata.put("SUBMITTER_NAME", requester.name);
			ticket.metadata.put("SUBMITTER_DN", auth.getUserDN()); 
			ticket.metadata.put("ASSOCIATED_VO_ID", vorec.id.toString());
			ticket.metadata.put("ASSOCIATED_VO_NAME", vorec.name);	
			ticket.metadata.put("SUPPORTING_SC_ID", screc.id.toString());
			ticket.metadata.put("SUPPORTING_SC_NAME", screc.name);	
			ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.user.assignee"));
			if(vomanager != null) {
				ticket.ccs.add(vomanager.primary_email);
			}
			ticket.ccs.add("osg-ra@opensciencegrid.org"); //TODO - make this configurable
			ticket.nextaction = "OSG RA to process request";
			String ticket_id = fp.open(ticket);
			if(ticket_id == null) {
				alert("Sorry, we failed to submit your request. Please wait for a while, and try again later.");
				return false;
			} else {
				log.info("Opened GridAdmin Request ticket with ID:" + ticket_id);
				alert("Opened ticket ID:" + ticket_id);
				return true;
			}
		} else {
			alert("guest can't submit this.");
			return false;
		}
	}

}
