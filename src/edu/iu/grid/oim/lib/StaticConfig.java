package edu.iu.grid.oim.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

//we now have *db* configuration (ConfigModel) now.. use it instead
public class StaticConfig {
	public static Properties conf = new Properties();
	
	static public String getVersion() { return "3.46"; } //version should be hardcoded.
	
	static Logger log = Logger.getLogger(StaticConfig.class);  
    
	//just use getProperty() directly.. exists for backward compatibility and to reduce error
	static public String getApplicationName() { return conf.getProperty("application.name"); }
	static public Boolean isDebug() { return conf.getProperty("debug").equals("true"); }	
	static public String getGMapAPIKey() { return conf.getProperty("gmapapikey"); }
	static public int getConfirmationExpiration() { return Integer.parseInt(conf.getProperty("confirmation.expiration")); }
	static public int getDowntimeEditableEndDays() { return Integer.parseInt(conf.getProperty("downtime.editable.endday")); }
    
	static 
	{
		try {
			InputStream is = StaticConfig.class.getResourceAsStream("/oim.conf");
			conf.load(is);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
