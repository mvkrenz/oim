package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.BootDialogForm;

//assume user is logged  in
public class GridAdminRequestForm extends BootDialogForm {
    static Logger log = Logger.getLogger(CertificateRequestUserModel.class);  
    
    private DivRepSelectBox vo;
	private DivRepTextArea desc;
	private UserContext context;
	
	public GridAdminRequestForm(UserContext context) {
		super(context.getPageRoot());
		this.context = context;
		
		setTitle("GridAdmin Enrollment Request");
		
		new DivRepStaticContent(this, "<p class=\"help-block\">Please update following request template and submit.</p>");
		
		VOModel vo_model = new VOModel(context);
		VOContactModel model = new VOContactModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				/*
				//check if the VO has at least 1 ra(primary or secondary) specified
				ArrayList<VOContactRecord> crecs = model.getByVOID(vo_rec.id);
				boolean hasra = false;
				for(VOContactRecord crec : crecs) {
					if(crec.contact_type_id.equals(11) && //RA
						(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
						//ContactRecord contactrec = cmodel.get(crec.contact_id);
						hasra = true;
						break;
					}
				}
				if(hasra) {
					keyvalues.put(vo_rec.id, vo_rec.name);
				}
				*/
				keyvalues.put(vo_rec.id, vo_rec.name);
			}
			vo = new DivRepSelectBox(this, keyvalues);
			vo.setLabel("Virtual Organization");
			vo.setRequired(true);
			
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
		
		desc = new DivRepTextArea(this);
		desc.setLabel("Request Detail");
		
		//load request template
		InputStream template  = getClass().getResourceAsStream("osg.certificate.gridadmin.txt");
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
			ticket.name = requester.name;
			ticket.email = requester.primary_email;
			ticket.phone = requester.primary_phone;

			//compose description
			VOModel vo_model = new VOModel(context);
			String voname;
			try {
				VORecord vrec = vo_model.get(vo.getValue());
				voname = vrec.name;
			} catch (SQLException e) {
				voname = "unknown";
				log.error("Failed to lookup vo", e);
			}
			
			ticket.title = "GridAdmin Request for " + requester.name + " (VO:" + voname + ")";
			ticket.description = "Requesting GridAdmin careof VO:" + voname + "\n\n";
			ticket.description += desc.getValue();
			
			ticket.metadata.put("SUBMITTED_VIA","OIM/GridAdminRequestForm");
			ticket.metadata.put("SUBMITTER_NAME", requester.name);
			ticket.metadata.put("SUBMITTER_DN", auth.getUserDN()); 
			ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.user.assignee"));
			ticket.ccs.add("osg-ra@opensciencegrid.org");
			ticket.nextaction = "OSG RA to process request";
			String ticket_id = fp.open(ticket);
			
			log.info("Opened GridAdmin Request ticket with ID:" + ticket_id);
			alert("Opened ticket ID:" + ticket_id);
			return true;
		} else {
			alert("guest can't submit this.");
			return false;
		}
	}

}
