package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.servlet.CertificateUserServlet;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.AuthorizationCriteriasView;
import edu.iu.grid.oim.view.divrep.Wizard;
import edu.iu.grid.oim.view.divrep.Wizard.WizardPage;
import edu.iu.grid.oim.view.divrep.form.CertificateAUPDE;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class UserCertRenew extends UserCertIssue {
    static Logger log = Logger.getLogger(UserCertRenew.class);
	
	WizardPage page_agreement;
	
	public UserCertRenew(UserContext context, CertificateRequestUserRecord rec, AuthorizationCriterias criterias) {
		super(context, rec, criterias);
		page_agreement = createAgreementPage();
		wizard.addPage(0, page_agreement);
	}

	private WizardPage createAgreementPage() {	
		return wizard.new WizardPage("Agreements") {
			@Override
			public void init() {
				//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
				//new DivRepStaticContent(this, "<p>You are eligible for renewing your user certificate.</p>");
				new DivRepStaticContent(this, "<p>Please agree to the following agreements in order to start the user certificate renewal process.</p>");
				new CertificateAUPDE(this);
				
				try {
					new DivRepFormElement(this) {
						CertificateRequestUserModel model = new CertificateRequestUserModel(context);
						ArrayList<VORecord> vos = model.getVOIApprove(auth.getContact().id);
						HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("ra_agreement.html"));
						DivRepCheckBox ra_agree = new DivRepCheckBox(this);
						
						@Override
						public void render(PrintWriter out) {
							out.write("<div id=\""+getNodeID()+"\">");
							//list VOs that this user is RA of
							if(vos.size() > 0) {
								out.write("<div class=\"well\">");
								out.write("<p class=\"muted\">You are currently RA for the following VOs: ");
								for(VORecord vo : vos) {
									out.write("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(vo.name)+"</span> ");
								}
								out.write("</p>");
								fileview.render(out);
								
								ra_agree.setLabel("I agree");
								ra_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));			
								ra_agree.render(out);
								
								out.write("</div>");//well
							}
							out.write("</div>");
						}
						@Override
						protected void onEvent(DivRepEvent e) {
							// TODO Auto-generated method stub
							
						}	
					};
					
					new DivRepFormElement(this) {
						GridAdminModel gamodel = new GridAdminModel(context);
						ArrayList<GridAdminRecord> gas = gamodel.getGridAdminsByContactID(auth.getContact().id);
						HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("ga_agreement.html"));
						DivRepCheckBox ga_agree = new DivRepCheckBox(this);
						
						@Override
						public void render(PrintWriter out) {
							out.write("<div id=\""+getNodeID()+"\">");
							if(gas.size() > 0) {
								out.write("<div class=\"well\">");
								out.write("<p class=\"muted\">You are currently GridAdmin for the following Domains: ");
								for(GridAdminRecord ga : gas) {
									out.write("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(ga.domain)+"</span>&nbsp;");
								}
								out.write("</p>");
								fileview.render(out);
								
								ga_agree.setLabel("I agree");
								ga_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));		
								ga_agree.render(out);
								
								out.write("</div>");
							}	
							out.write("</div>");
						}

						@Override
						protected void onEvent(DivRepEvent e) {
							// TODO Auto-generated method stub
							
						}						
					};	
						
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
			@Override
			protected void onNext() {
				wizard.setActive(page_password);
			}
		};
	}
	
	@Override
	public void render(PrintWriter out) {
		if(criterias.passAll()) {
			wizard.render(out);
		} else {
			out.write("<p>In order to renew this user certificate, the following criteria must be met.</p>");
			AuthorizationCriteriasView authview = new AuthorizationCriteriasView(context.getPageRoot(), context, criterias);
			authview.render(out);
		}
	}
	

	protected void doProcess(UserContext context, String pass) throws CertificateRequestException {
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		model.renew(rec, pass);
	}
}
