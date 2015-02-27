package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.iu.grid.oim.lib.StaticConfig;

public class VORecord extends ConfirmableRecord {
	
	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String community;
	public Integer sc_id;
	public Boolean active;
	public Boolean disable;
	
	public Boolean science_vo;
	public String primary_url;
	public String aup_url;
	public String membership_services_url;
	public String purpose_url;
	public String support_url;
	public String app_description;

	public Boolean use_oasis;
	public String oasis_repo_urls;
	public Boolean cert_only;
	
	public String certificate_signer;

	//load from existing record
	public VORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VORecord() {
		confirmed = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getOASISRepoUrls() {
		ArrayList<String> urls = new ArrayList<String>();
		if(oasis_repo_urls != null) {
			for(String url : oasis_repo_urls.split("\\|")) {
				if(url.length() == 0) continue;
				urls.add(url);
			}
		}
		return urls;
	}
	public void setOASISRepoUrls(ArrayList<String> urls) {
		StringBuffer _urls = new StringBuffer();
		for(String url : urls) {
			_urls.append(url);
			_urls.append("|");
		}
		oasis_repo_urls = _urls.toString();
	}
}
