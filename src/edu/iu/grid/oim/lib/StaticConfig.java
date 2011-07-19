package edu.iu.grid.oim.lib;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;


//we now have *dynamic* configuration (ConfigModel) now.. use it instead
public class StaticConfig {
	static public String getVersion() { return "2.36"; } //version shouldn't be configurable
	
	static Logger log = Logger.getLogger(StaticConfig.class);  
    
    private String appbase;
	static public String getApplicationBase() { return config.appbase; }
    
	private String staticbase;
	static public String getStaticBase() { return config.staticbase; }

    private String appname;
	static public String getApplicationName() { return config.appname; }

	private Boolean debug;
	static public Boolean isDebug() { return config.debug; }	

	private String gmap_api_key;
	static public String getGMapAPIKey() { return config.gmap_api_key; }
	
	private int confirmation_expiration;
	static public int getConfirmationExpiration() { return config.confirmation_expiration; }
	
	private int downtime_editable_end_datys;
	static public int getDowntimeEditableEndDays() { return config.downtime_editable_end_datys; }
	
	//Footprints
	private String footprints_uri;
	static public String getFootprintsUri() { return config.footprints_uri; }
	private String footprints_url;
	static public String getFootprintsUrl() { return config.footprints_url; }
	private String footprints_username;
	static public String getFootprintsUsername() { return config.footprints_username; }
	private String footprints_password;
	static public String getFootprintsPassword() { return config.footprints_password; }
	private Integer footprints_projectid;
	static public Integer getFootprintsProjectID() { return config.footprints_projectid; }

	private String doe_cn;
	static public String getDOECN() { return config.doe_cn; }
	
	private String ssl_truststore;
	static public String getSSLTrustStorePath() { return config.ssl_truststore; }
    
	//why private? - don't allow client instantiation (use static getters)
	private StaticConfig() 
	{
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			InputStream is = getClass().getResourceAsStream("/config.xml");
			InputSource config_xml = new InputSource(is);	

			appbase = (String)xpath.evaluate("//ApplicationBase", config_xml);
			is.reset();
			
			staticbase = (String)xpath.evaluate("//StaticBase", config_xml);
			is.reset();
			
			appname = (String)xpath.evaluate("//ApplicationName", config_xml);
			is.reset();
			
			debug = ((String)xpath.evaluate("//Debug", config_xml)).compareTo("true") == 0;
			is.reset();
	
			gmap_api_key = (String)xpath.evaluate("//GMapAPIKey", config_xml);
			is.reset();
			
			footprints_uri = (String)xpath.evaluate("//Footprints/URI", config_xml);
			is.reset();
			
			footprints_url = (String)xpath.evaluate("//Footprints/URL", config_xml);
			is.reset();
			
			footprints_username = (String)xpath.evaluate("//Footprints/Username", config_xml);
			is.reset();

			footprints_password = (String)xpath.evaluate("//Footprints/Password", config_xml);
			is.reset();

			footprints_projectid = Integer.parseInt(((String)xpath.evaluate("//Footprints/ProjectID", config_xml)));
			is.reset();
			
			ssl_truststore = (String)xpath.evaluate("//SSLTruststorePath", config_xml);
			is.reset();
			
			doe_cn = (String)xpath.evaluate("//DOECN", config_xml);
			is.reset();
			
			Double d = (Double)xpath.evaluate("//ConfirmationExpiration", config_xml, XPathConstants.NUMBER);
			confirmation_expiration = d.intValue();
			is.reset();
			
			downtime_editable_end_datys = Integer.parseInt(((String)xpath.evaluate("//DowntimeEditableEndDays", config_xml)));
			is.reset();
			
		} catch (XPathExpressionException e) {
			log.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//singleton
	static private StaticConfig config = new StaticConfig();

}
