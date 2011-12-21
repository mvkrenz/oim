package edu.iu.grid.oim.lib;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.xml.soap.*;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.GOCTicket;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.HtmlFileView;

public class Footprint 
{
    static Logger log = Logger.getLogger(Footprint.class); 
    
	Context context;
	
	SOAPBody body;
	SOAPEnvelope env;
	SOAPConnection connection;
	SOAPMessage msg;
	
	ConfigModel config;
	
	public Footprint(Context context)
	{
		this.context = context;
		
		//using program listed here > http://blogs.sun.com/andreas/entry/no_more_unable_to_find
		//to create the trusted keystore
		System.setProperty("javax.net.ssl.trustStore", StaticConfig.getSSLTrustStorePath());
		
		config = new ConfigModel(context);
		
        //Init SOAP
		try {
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
	        connection = scf.createConnection();
	        MessageFactory msgFactory = MessageFactory.newInstance();
	        msg = msgFactory.createMessage();
	        env = msg.getSOAPPart().getEnvelope();
	        env.addNamespaceDeclaration( "xsi", "http://www.w3.org/1999/XMLSchema-instance" );
	        env.addNamespaceDeclaration( "xsd", "http://www.w3.org/1999/XMLSchema" );
	        env.addNamespaceDeclaration( "namesp2", "http://xml.apache.org/xml-soap" );
	        env.addNamespaceDeclaration( "SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/" );
	        body = env.getBody();
		} catch (UnsupportedOperationException e) {
			log.error("Failed to initialize Footprint object", e);
		} catch (SOAPException e) {
			log.error("Failed to initialize Footprint object", e);
		}
	}

	public void createNewResourceTicket(String resource_name, String sc_footprint_id, String vo_name)
	{
		log.debug("createNewResourceTicket .. beginning");
		
		HtmlFileView description = new HtmlFileView(config.ResourceFPTemplate.get());
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("##RESOURCE_NAME##", resource_name);
		description.applyParams(params);
		
		try
        {
            SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1",StaticConfig.getFootprintsUri()) );
            
            // root parameters user/pass/extra/args
            SOAPElement username = invoke.addChildElement( env.createName("user") );
            username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            username.addTextNode(StaticConfig.getFootprintsUsername());
            
            SOAPElement password = invoke.addChildElement( env.createName("password") );
            password.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            password.addTextNode(StaticConfig.getFootprintsPassword());
            
            SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
            extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            
            SOAPElement args = invoke.addChildElement( env.createName("args") );
            args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            //Contat Information
            SOAPElement abfields = args.addChildElement( env.createName("abfields") );
            abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            ContactRecord contact = context.getAuthorization().getContact();
            SOAPElement arg4_3_2 = abfields.addChildElement( env.createName("Last__bName") );
            arg4_3_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_2.addTextNode(contact.getLastName());
            
            SOAPElement arg4_3_3 = abfields.addChildElement( env.createName("First__bName") );
            arg4_3_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_3.addTextNode(contact.getFirstName());
            
            SOAPElement arg4_3_4 = abfields.addChildElement( env.createName("Email__baddress") );
            arg4_3_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_4.addTextNode(contact.primary_email);
            
            if(contact.primary_phone != null) {
	            SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
	            arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            arg4_3_5.addTextNode(contact.primary_phone);
            }

            //Basic Information
            SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
            arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_7.addTextNode(StaticConfig.getFootprintsProjectID().toString());       
            SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
            arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_9.addTextNode("OSG-GOC");
            
            SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
            arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_8.addTextNode("OIM Resource Registration - " + resource_name);
            
            SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
            arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_1.addTextNode("4");
            
            SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
            arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_4.addTextNode(description.toString());

    		if(StaticConfig.isDebug()) {
	            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
	            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[1]" );//CHANGE [1] to [n] based on the number of items
	            
		            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
		            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            arg4_5_1.addTextNode("hayashis");
		            
		            if(sc_footprint_id == null) {
		            	log.warn("Can't assign support center for the registration ticket since the information hasn't been filled out in OIM");
		            }
		            
		        log.debug("DEBUG: assigning hayashis - in reality following would have assigned: kagross, echism, " + sc_footprint_id);
    		} else {
	            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
	            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            
	            int count = 0;
	            
	            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
	            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            arg4_5_1.addTextNode("kagross");
	            count++;
	            
	            SOAPElement arg4_5_2 = arg4_5.addChildElement( env.createName("item") );
	            arg4_5_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            arg4_5_2.addTextNode("echism");
	            count++;
	            
	            if(sc_footprint_id != null) {
		            //assign the associated SC so that they will be notified of this resource registration
		            SOAPElement arg4_5_3 = arg4_5.addChildElement( env.createName("item") );
		            arg4_5_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            arg4_5_3.addTextNode(sc_footprint_id);
		            count++;
	            } else {
	            	log.warn("Couldn't assign support center for the registration ticket since the information hasn't been filled out in OIM");
	            }
		        arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string["+count+"]" );//CHANGE [1] to [n] based on the number of items    
    		}
	            
    		if(StaticConfig.isDebug()) {
    			log.debug("DEBUG: following people would have been CC-ed in production mode: oim-dev@, rquick@iu.edu, ruth@fnal.gov");
    		} else {
	            SOAPElement ccs = args.addChildElement( env.createName("permanentCCs") );
	            ccs.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            ccs.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[3]" );//CHANGE [1] to [n] based on the number of items
	            
		            SOAPElement cc1 = ccs.addChildElement( env.createName("item") );
		            cc1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc1.addTextNode("oim-dev@OPENSCIENCEGRID.ORG");
		            
		            SOAPElement cc2 = ccs.addChildElement( env.createName("item") );
		            cc2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc2.addTextNode("rquick@iu.edu");
		            
		            SOAPElement cc3 = ccs.addChildElement( env.createName("item") );
		            cc3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc3.addTextNode("ruth@fnal.gov");   
    		}
            //project fields
            SOAPElement projfields = args.addChildElement( env.createName("projfields") );
            projfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            SOAPElement orig_vo = projfields.addChildElement( env.createName("Originating__bVO__bSupport__bCenter") );
            orig_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            orig_vo.addTextNode("MIS");
            
            SOAPElement dest_vo = projfields.addChildElement( env.createName("Destination__bVO__bSupport__bCenter") );
            dest_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            if(vo_name == null) {
            	dest_vo.addTextNode("MIS");
            } else {
            	dest_vo.addTextNode(vo_name);
            }
            
            SOAPElement next_action = projfields.addChildElement( env.createName("ENG__bNext__bAction__bItem") );
            next_action.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            next_action.addTextNode("Verify Information and Add to Ops Meeting Agenda");
            
            SOAPElement nad = projfields.addChildElement( env.createName("ENG__bNext__bAction__bDate__fTime__b__PUTC__p") );
            nad.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            nad.addTextNode(format.format(new Date()));            
            
            SOAPElement type = projfields.addChildElement( env.createName("Ticket__uType") );
            type.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            type.addTextNode("Problem__fRequest");  
            
            SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
            arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_2.addTextNode("Engineering");
            msg.saveChanges();
            
    		log.debug("createNewResourceTicket .. all setup.. calling SOAP");
            call();
    		log.debug("createNewResourceTicket .. yeah! success");
        } catch(Exception e) {
			log.error("Failed to create resource ticket: ", e);
		}
		log.debug("createNewResourceTicket .. ending");
	}
	
	public void createNewSCTicket(String sc_name)
	{		
		HtmlFileView description = new HtmlFileView(config.SCFPTemplate.get());
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("##SC_NAME##", sc_name);
		description.applyParams(params);
		
		try
        {
            SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1",StaticConfig.getFootprintsUri()) );
            
            // root parameters user/pass/extra/args
            SOAPElement username = invoke.addChildElement( env.createName("user") );
            username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            username.addTextNode(StaticConfig.getFootprintsUsername());
            
            SOAPElement password = invoke.addChildElement( env.createName("password") );
            password.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            password.addTextNode(StaticConfig.getFootprintsPassword());
            
            SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
            extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            
            SOAPElement args = invoke.addChildElement( env.createName("args") );
            args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            //Contat Information
            SOAPElement abfields = args.addChildElement( env.createName("abfields") );
            abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            ContactRecord contact = context.getAuthorization().getContact();
            SOAPElement arg4_3_2 = abfields.addChildElement( env.createName("Last__bName") );
            arg4_3_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_2.addTextNode(contact.getLastName());
            
            SOAPElement arg4_3_3 = abfields.addChildElement( env.createName("First__bName") );
            arg4_3_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_3.addTextNode(contact.getFirstName());
            
            SOAPElement arg4_3_4 = abfields.addChildElement( env.createName("Email__baddress") );
            arg4_3_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_4.addTextNode(contact.primary_email);
            
            if(contact.primary_phone != null) {
            	SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
            	arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            	arg4_3_5.addTextNode(contact.primary_phone);	
            }
            	
            //Basic Information
            SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
            arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_7.addTextNode(StaticConfig.getFootprintsProjectID().toString());       
            SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
            arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_9.addTextNode("OSG-GOC");
            
            SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
            arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_8.addTextNode("OIM SC Registration - " + sc_name);
            
            SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
            arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_1.addTextNode("4");
            
            SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
            arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_4.addTextNode(description.toString());

    		if(StaticConfig.isDebug()) {
                SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
                arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
                arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[1]" );//CHANGE [1] to [n] based on the number of items
                
    	            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
    	            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
    	            arg4_5_1.addTextNode("hayashis");
    			log.debug("DEBUG: following people would have been assigned in production mode: kagross, echism");
    		} else {
                SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
                arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
                arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[2]" );//CHANGE [1] to [n] based on the number of items
                
    	            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
    	            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
    	            arg4_5_1.addTextNode("kagross");
    	            
    	            SOAPElement arg4_5_2 = arg4_5.addChildElement( env.createName("item") );
    	            arg4_5_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
    	            arg4_5_2.addTextNode("echism");    			
    		}
            	           
    		/*
            SOAPElement ccs = args.addChildElement( env.createName("permanentCCs") );
            ccs.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
            ccs.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[3]" );//CHANGE [1] to [n] based on the number of items
            
	            SOAPElement cc1 = ccs.addChildElement( env.createName("item") );
	            cc1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            cc1.addTextNode("oim-dev@OPENSCIENCEGRID.ORG");
	            
	            SOAPElement cc2 = ccs.addChildElement( env.createName("item") );
	            cc2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            cc2.addTextNode("rquick@iu.edu");
	            
	            SOAPElement cc3 = ccs.addChildElement( env.createName("item") );
	            cc3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            cc3.addTextNode("ruth@fnal.gov");   	     
	        */
    		if(StaticConfig.isDebug()) {
    			log.debug("DEBUG: following people would have been CC-ed in production mode: oim-dev@, rquick@iu.edu, ruth@fnal.gov");
    		} else {
	            SOAPElement ccs = args.addChildElement( env.createName("permanentCCs") );
	            ccs.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            ccs.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[3]" );//CHANGE [1] to [n] based on the number of items
	            
		            SOAPElement cc1 = ccs.addChildElement( env.createName("item") );
		            cc1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc1.addTextNode("oim-dev@OPENSCIENCEGRID.ORG");
		            
		            SOAPElement cc2 = ccs.addChildElement( env.createName("item") );
		            cc2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc2.addTextNode("rquick@iu.edu");
		            
		            SOAPElement cc3 = ccs.addChildElement( env.createName("item") );
		            cc3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc3.addTextNode("ruth@fnal.gov");   
    		}
	            
            //project fields
            SOAPElement projfields = args.addChildElement( env.createName("projfields") );
            projfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            SOAPElement orig_vo = projfields.addChildElement( env.createName("Originating__bVO__bSupport__bCenter") );
            orig_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            orig_vo.addTextNode("MIS");
            
            SOAPElement dest_vo = projfields.addChildElement( env.createName("Destination__bVO__bSupport__bCenter") );
            dest_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            dest_vo.addTextNode("MIS");
            
            SOAPElement next_action = projfields.addChildElement( env.createName("ENG__bNext__bAction__bItem") );
            next_action.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            next_action.addTextNode("Verify Information and Add to Ops Meeting Agenda");
            
            SOAPElement nad = projfields.addChildElement( env.createName("ENG__bNext__bAction__bDate__fTime__b__PUTC__p") );
            nad.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            nad.addTextNode(format.format(new Date()));            
            
            SOAPElement type = projfields.addChildElement( env.createName("Ticket__uType") );
            type.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            type.addTextNode("Problem__fRequest");  
            
            SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
            arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_2.addTextNode("Engineering");
            msg.saveChanges();

            call();
        } catch(Exception ex) {
			log.error("Failed to create sc ticket: ", ex);
        } 
	}
	
	public void createNewVOTicket(String vo_name, String sc_footprint_id)
	{	
		HtmlFileView description = new HtmlFileView(config.VOFPTemplate.get());	
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("##VO_NAME##", vo_name);
		description.applyParams(params);
		
		try
        {
            SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1",StaticConfig.getFootprintsUri()) );
            
            // root parameters user/pass/extra/args
            SOAPElement username = invoke.addChildElement( env.createName("user") );
            username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            username.addTextNode(StaticConfig.getFootprintsUsername());
            
            SOAPElement password = invoke.addChildElement( env.createName("password") );
            password.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            password.addTextNode(StaticConfig.getFootprintsPassword());
            
            SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
            extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            
            SOAPElement args = invoke.addChildElement( env.createName("args") );
            args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            //Contat Information
            SOAPElement abfields = args.addChildElement( env.createName("abfields") );
            abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            ContactRecord contact = context.getAuthorization().getContact();
            SOAPElement arg4_3_2 = abfields.addChildElement( env.createName("Last__bName") );
            arg4_3_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_2.addTextNode(contact.getLastName());
            
            SOAPElement arg4_3_3 = abfields.addChildElement( env.createName("First__bName") );
            arg4_3_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_3.addTextNode(contact.getFirstName());
            
            SOAPElement arg4_3_4 = abfields.addChildElement( env.createName("Email__baddress") );
            arg4_3_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_4.addTextNode(contact.primary_email);
            
            if(contact.primary_phone != null) {
            	SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
            	arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            	arg4_3_5.addTextNode(contact.primary_phone);
            }
            
            //Basic Information
            SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
            arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_7.addTextNode(StaticConfig.getFootprintsProjectID().toString());       
            SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
            arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_9.addTextNode("OSG-GOC");
            
            SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
            arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_8.addTextNode("OIM VO Registration - " + vo_name);
            
            SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
            arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_1.addTextNode("4");
            
            SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
            arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_4.addTextNode(description.toString());

    		if(StaticConfig.isDebug()) {
	            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
	            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[1]" );//CHANGE [1] to [n] based on the number of items
	            
		            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
		            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            arg4_5_1.addTextNode("hayashis");
		            
		        log.debug("DEBUG: assigning hayashis - in production following would have assigned: kagross, echism, " + sc_footprint_id);
    		} else {
	            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
	            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            int count = 0;
	            
	            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
	            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            arg4_5_1.addTextNode("kagross");
	            count++;
	            
	            SOAPElement arg4_5_2 = arg4_5.addChildElement( env.createName("item") );
	            arg4_5_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	            arg4_5_2.addTextNode("echism");
	            count++;
	            
	            if(sc_footprint_id != null) {
		            //assign the associated SC so that they will be notified of this resource registration
		            SOAPElement arg4_5_3 = arg4_5.addChildElement( env.createName("item") );
		            arg4_5_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            arg4_5_3.addTextNode(sc_footprint_id);
		            count++;
	            } else {
			        log.warn("Failed to set Support Center as assignee for VO registration form since sc_footprint_id hasn't been set in OIM yet");            	
	            }
		        arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string["+count+"]" );//CHANGE [1] to [n] based on the number of items
		        
    		}
	            
    		if(StaticConfig.isDebug()) {
    			log.debug("DEBUG: following people would have been CC-ed in production mode: oim-dev@, rquick@iu.edu, ruth@fnal.gov");
    		} else {
	            SOAPElement ccs = args.addChildElement( env.createName("permanentCCs") );
	            ccs.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
	            ccs.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[3]" );//CHANGE [1] to [n] based on the number of items
	            
		            SOAPElement cc1 = ccs.addChildElement( env.createName("item") );
		            cc1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc1.addTextNode("oim-dev@OPENSCIENCEGRID.ORG");
		            
		            SOAPElement cc2 = ccs.addChildElement( env.createName("item") );
		            cc2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc2.addTextNode("rquick@iu.edu");
		            
		            SOAPElement cc3 = ccs.addChildElement( env.createName("item") );
		            cc3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		            cc3.addTextNode("ruth@fnal.gov");   
    		} 
	            
            //project fields
            SOAPElement projfields = args.addChildElement( env.createName("projfields") );
            projfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            SOAPElement orig_vo = projfields.addChildElement( env.createName("Originating__bVO__bSupport__bCenter") );
            orig_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            orig_vo.addTextNode("MIS");
            
            SOAPElement dest_vo = projfields.addChildElement( env.createName("Destination__bVO__bSupport__bCenter") );
            dest_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            dest_vo.addTextNode("MIS");
            
            SOAPElement next_action = projfields.addChildElement( env.createName("ENG__bNext__bAction__bItem") );
            next_action.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            next_action.addTextNode("Verify Information and Add to Ops Meeting Agenda");
            
            SOAPElement nad = projfields.addChildElement( env.createName("ENG__bNext__bAction__bDate__fTime__b__PUTC__p") );
            nad.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            nad.addTextNode(format.format(new Date()));            
            
            SOAPElement type = projfields.addChildElement( env.createName("Ticket__uType") );
            type.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            type.addTextNode("Problem__fRequest");  
            
            SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
            arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_2.addTextNode("Engineering");
            msg.saveChanges();

            call();
        } catch(Exception e) {
			log.error("Failed to create vo ticket: ", e);
		}
	}
	
	public static String GetIndent(int num) {

		String s = "";
		for (int i = 0; i < num; i++) {
			s = s + " ";
		}
		return s;
	}
	
	public static void DumpSOAPElement(SOAPElement el, int indent)
	{
		java.util.Iterator it = el.getChildElements();
		while (it.hasNext())
		{
			String indstr = GetIndent(indent);
			Object obj = it.next();
			if (obj instanceof SOAPElement)
			{
				SOAPElement ele = (SOAPElement) obj;
				log.error(indstr + "-----------------------------");
				log.error(indstr + ele.getElementName().getLocalName());
				log.error(indstr + "-----------------------------");
				DumpSOAPElement(ele, indent + 4);
			}
			else if (obj instanceof Text)
			{
				Text txt = (Text) obj;
				log.error(indstr + "TextNode:" + txt.getValue() + "\n");
			} else {
				log.error(indstr + obj.toString());
			}
		}
	}
	
	void call() throws SOAPException
	{
		/*
 		if(StaticConfig.isDebug()) {
 			System.out.println("Dumpging request soap body");
 			DumpSOAPElement(msg.getSOAPBody(), 0);
 		}
 		*/
 		
		log.debug("Calling : " + StaticConfig.getFootprintsUrl());
        SOAPMessage reply = connection.call(msg, StaticConfig.getFootprintsUrl());
        connection.close();
        
        Iterator it = reply.getSOAPBody().getChildElements();
        if(it.hasNext()) {
        	SOAPElement item = (SOAPElement) it.next();
        	if(item.getElementName().getLocalName().equals("MRWebServices__createIssue_gocResponse")) {
        		int ticket_id = Integer.parseInt(item.getTextContent());
        		log.debug("Created Ticket: " + ticket_id);
        		
        		//storing metadata
				try {
		            ContactRecord contact = context.getAuthorization().getContact();
					GOCTicket gocticket = new GOCTicket();
					gocticket.setMetadata(ticket_id, "SUBMITTER_NAME", contact.getFirstName() + " " + contact.getLastName());
					gocticket.setMetadata(ticket_id, "SUBMITTER_DN", context.getAuthorization().getUserDN());
					gocticket.setMetadata(ticket_id, "SUBMITTED_VIA", "OIM/registration");
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	} else {
	        	log.error("SOAP did not return ticket ID.. dumping..");
	 			DumpSOAPElement(msg.getSOAPBody(), 0);
	        	DumpSOAPElement(reply.getSOAPBody(), 0);
	        }
        } else {
        	log.error("SOAP did not return anything at all...");
        }
	}
}
