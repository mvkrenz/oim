package edu.iu.grid.oim.view.divrep.form;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIntegerValidator;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ConfigModel;

public class CertificateConfigFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(CertificateConfigFormDE.class); 
    
    private UserContext context;
	
	private DivRepTextArea banner;	
    
	private DivRepTextBox global_usercert_max;
	private DivRepTextBox global_usercert_count;
	
	private DivRepTextBox global_hostcert_max;
	private DivRepTextBox global_hostcert_count;
	
	private DivRepTextBox usercert_max_year;
	private DivRepTextBox hostcert_max_year;
	private DivRepTextBox hostcert_max_day;
	
	public CertificateConfigFormDE(UserContext _context) throws SQLException
	{	
		super(_context.getPageRoot(), null);
		context = _context;

		setSubmitLabel("Update");
		
		ConfigModel config = new ConfigModel(context);
		
		new DivRepStaticContent(this, "<h2>Global Quota</h2>");
		global_usercert_max = new DivRepTextBox(this);
		global_usercert_max.setLabel("Maximum number of user certificates OSG can request per year");
		global_usercert_max.setValue(config.QuotaGlobalUserCertYearMax.getString());
		global_usercert_max.setRequired(true);
		global_usercert_max.addValidator(new DivRepIntegerValidator());
		
		global_usercert_count = new DivRepTextBox(this);
		global_usercert_count.setLabel("Number of user certificate OSG has requested this year");
		global_usercert_count.setValue(config.QuotaGlobalUserCertYearCount.getString());
		global_usercert_count.setRequired(true);
		global_usercert_count.addValidator(new DivRepIntegerValidator());
		new DivRepStaticContent(this, "<p>* This counter will be reset to 0 on January 1st of every year.</p>");
		
		global_hostcert_max = new DivRepTextBox(this);
		global_hostcert_max.setLabel("Maximum number of host certificates OSG can approve this year");
		global_hostcert_max.setValue(config.QuotaGlobalHostCertYearMax.getString());
		global_hostcert_max.setRequired(true);
		global_hostcert_max.addValidator(new DivRepIntegerValidator());
		
		global_hostcert_count = new DivRepTextBox(this);
		global_hostcert_count.setLabel("Number of host certificate OSG has approved this year");
		global_hostcert_count.setValue(config.QuotaGlobalHostCertYearCount.getString());
		global_hostcert_count.setRequired(true);
		global_hostcert_count.addValidator(new DivRepIntegerValidator());
		new DivRepStaticContent(this, "<p>* This counter will be reset to 0 on January 1st of every year.</p>");
		
		new DivRepStaticContent(this, "<h2>Per User Quota</h2>");
		usercert_max_year = new DivRepTextBox(this);
		usercert_max_year.setLabel("Maximum number of user certificates each user can request this year");
		usercert_max_year.setValue(config.QuotaUserCertYearMax.getString());
		usercert_max_year.setRequired(true);
		usercert_max_year.addValidator(new DivRepIntegerValidator());
		
		hostcert_max_year = new DivRepTextBox(this);
		hostcert_max_year.setLabel("Maximum number of host certificates each user can approve this year");
		hostcert_max_year.setValue(config.QuotaUserHostYearMax.getString());
		hostcert_max_year.setRequired(true);
		hostcert_max_year.addValidator(new DivRepIntegerValidator());
		
		hostcert_max_day = new DivRepTextBox(this);
		hostcert_max_day.setLabel("Maximum number of host certificates each user can approve each day");
		hostcert_max_day.setValue(config.QuotaUserHostDayMax.getString());
		hostcert_max_day.setRequired(true);
		hostcert_max_day.addValidator(new DivRepIntegerValidator());
		new DivRepStaticContent(this, "<p>* Per user counters are stored in user contact record.</p>");
		
		new DivRepStaticContent(this, "<h2>Page Banner</h2>");
		new DivRepStaticContent(this, "<p class='muted'>Banner displayed on top of all certificate related pages</p>");
		banner = new DivRepTextArea(this);
		banner.setValue(config.CertificatePageBanner.getString());
		banner.setHeight(50);
		banner.setSampleValue("No Banner");
	}
	
	protected Boolean doSubmit() 
	{		
		ConfigModel config = new ConfigModel(context);
		try {
			config.QuotaGlobalUserCertYearMax.set(global_usercert_max.getValue());
			config.QuotaGlobalUserCertYearCount.set(global_usercert_count.getValue());
			
			config.QuotaGlobalHostCertYearMax.set(global_hostcert_max.getValue());
			config.QuotaGlobalHostCertYearCount.set(global_hostcert_count.getValue());
			
			config.QuotaUserCertYearMax.set(usercert_max_year.getValue());
			config.QuotaUserHostDayMax.set(hostcert_max_day.getValue());
			config.QuotaUserHostYearMax.set(hostcert_max_year.getValue());
			
			config.CertificatePageBanner.set(banner.getValue());
			
			context.message(MessageType.SUCCESS, "Successfully updated configuration.");
			this.redirect("certificate");
		} catch (SQLException e) {
			log.error("Failed to update quota config", e);
			alert(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
