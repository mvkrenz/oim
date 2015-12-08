package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ConfigRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.exceptions.ConfigException;

import javax.sql.DataSource;

public class ConfigModel extends ModelBase<ConfigRecord> {
	
    static Logger log = Logger.getLogger(ConfigModel.class);  
    
    //protected UserContext context;
	//protected Authorization auth;
	
	public class Config {
		String key;
		String def; //default is a java keyword..
		ConfigModel model;
		public Config(ConfigModel model, String key, String def) {
			this.model = model;
			this.key = key;
			this.def = def;
		}
		
		//return current config value. returns default is not set yet
		public String getString() {
			try {
				return model.get(key);
			} catch (ConfigException e) {
				return def;
			} catch (SQLException e) {
				log.error("Failed to obtain value for " + key, e);
				return null;
			}
		}
		public Integer getInteger() {
			String v = getString();
			return Integer.parseInt(v);
		}
		
		public void set(String value) throws SQLException {
			model.set(key, value);
		}
		public String getKey() {
			return key;
		}
	}
	
	//Footprints Templates
	public Config ResourceFPTemplate = new Config(this, "resource_fp_template", "here is my default fp template");
	public Config VOFPTemplate = new Config(this, "resource_vo_template", "here is my default vo template");
	public Config SCFPTemplate = new Config(this, "resource_sc_template", "here is my default sc template");
	
	//Certificate Request Global Quotas
	public Config QuotaGlobalUserCertYearMax = new Config(this, "QuotaGlobalUserCertYearMax", "3500");
	public Config QuotaGlobalUserCertYearCount = new Config(this, "QuotaGlobalUserCertYearCount", "0");
	public Config QuotaGlobalUserCertTotalCount = new Config(this, "QuotaGlobalUserCertTotalCount", "0");
	
	public Config QuotaGlobalHostCertYearMax = new Config(this, "QuotaGlobalHostCertYearMax", "11500");
	public Config QuotaGlobalHostCertYearCount = new Config(this, "QuotaGlobalHostCertYearCount", "0");
	public Config QuotaGlobalHostCertTotalCount = new Config(this, "QuotaGlobalHostCertTotalCount", "0");
	
	public Config QuotaUserCertYearMax = new Config(this, "QuotaUserCertYearMax", "3");
	public Config QuotaUserHostDayMax = new Config(this, "QuotaUserHostDayMax", "50");
	public Config QuotaUserHostYearMax = new Config(this, "QuotaUserHostYearMax", "1000");
	
	public Config CertificatePageBanner = new Config(this, "certificate_page_banner", "");
	
	public ConfigModel(UserContext context)
	{
		super(context, "config");
	}
	
	private ConfigRecord getRecord(String key) throws SQLException, ConfigException {
		Connection connection = context.getConnection();
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM config WHERE `key` = ?");
		stmt.setString(1, key);
		ConfigRecord rec = null;
		ResultSet rs = stmt.executeQuery();
		if(rs != null) {
	    	if(rs.next()) {
	    		rec = new ConfigRecord(rs);
			} else {
				stmt.close();
				connection.close();
		    	throw new ConfigException("No such config");
		    }
	    } else {
	    	log.error("ConfigModel::get didn't return result set");
	    }
	    stmt.close();
	    connection.close();
	    return rec;
	}
	
	private String get(String key) throws SQLException, ConfigException {
		ConfigRecord rec = getRecord(key);
	    return rec.value;
	}
	
	private void set(String key, String value) throws SQLException {
		Connection connection = context.getConnection();
		//int affected;
		try {
			//test get to see if the value exist?
			get(key);
			
			//update the value
			/*
			PreparedStatement stmt = connection.prepareStatement("UPDATE config SET `value` = ? WHERE `key` = ?");
			stmt.setString(1, value);
			stmt.setString(2, key);
			affected = stmt.executeUpdate();
			stmt.close();
			*/
			ConfigRecord oldrec = getRecord(key);
			ConfigRecord newrec = getRecord(key);
			newrec.value = value;
			update(oldrec, newrec);
		} catch (ConfigException e) {
			//never been set before.. insert
			/*
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO config (`key`, `value`) VALUES (?,?)");
			stmt.setString(1, key);
			stmt.setString(2, value);	
			affected = stmt.executeUpdate();
			*/
			ConfigRecord rec = new ConfigRecord();
			rec.key = key;
			rec.value = value;
			insert(rec);
		} 
		/*
		if(affected != 1) {
			log.error("Failed to set " + key + " with value " + value);
		}
		*/
		connection.close();
	}

	@Override
	ConfigRecord createRecord() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	

}

