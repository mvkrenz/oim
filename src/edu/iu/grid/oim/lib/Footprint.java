package edu.iu.grid.oim.lib;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.soap.*;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ContactRecord;

public class Footprint 
{
	Context context;
	
	SOAPBody body;
	SOAPEnvelope env;
	SOAPConnection connection;
	SOAPMessage msg;
	
	public Footprint(Context _context)
	{
		context = _context;
		
		//using program listed here > http://blogs.sun.com/andreas/entry/no_more_unable_to_find
		//to create the trusted keystore
		System.setProperty("javax.net.ssl.trustStore", Config.getSSLTrustStorePath());
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createNewResourceTicket(String resource_name)
	{
		InputStream is = this.getClass().getResourceAsStream("footprints_new_resource_template.txt");
		String description = FileReader.loadContent(is);
		description = description.replaceAll("##RESOURCE_NAME##", resource_name);
		
		try
        {
            SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1",Config.getFootprintsUri()) );
            
            // root parameters user/pass/extra/args
            SOAPElement username = invoke.addChildElement( env.createName("user") );
            username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            username.addTextNode(Config.getFootprintsUsername());
            
            SOAPElement password = invoke.addChildElement( env.createName("password") );
            password.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            password.addTextNode(Config.getFootprintsPassword());
            
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
            
            SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
            arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_5.addTextNode(contact.primary_phone);

            //Basic Information
            SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
            arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_7.addTextNode("109");       
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
            arg4_4.addTextNode(description);

            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
            arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[1]" );//CHANGE [1] to [n] based on the number of items
            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_5_1.addTextNode("hayashis");
            
            SOAPElement ccs = args.addChildElement( env.createName("permanentCCs") );
            ccs.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
            ccs.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[2]" );//CHANGE [1] to [n] based on the number of items
            SOAPElement cc1 = ccs.addChildElement( env.createName("item") );
            cc1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            cc1.addTextNode("oim-dev@OPENSCIENCEGRID.ORG");
            SOAPElement cc2 = ccs.addChildElement( env.createName("item") );
            cc2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            cc2.addTextNode("rquick@iu.edu");
            
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
            arg4_2.addTextNode("Open");
            msg.saveChanges();

            SOAPMessage reply = connection.call( msg, Config.getFootprintsUrl() );
            connection.close();

            SOAPBody replybody = reply.getSOAPPart().getEnvelope().getBody();

            // Check for error
            if( replybody.hasFault() )
            {
                throw new Exception( replybody.getFault().getFaultString() );
            }

            // Iterate through the result body, extracting information

            java.util.Iterator it = replybody.getChildElements();
            while( it.hasNext() )
            {
                Object obj = it.next();
                if( obj instanceof SOAPElement )

                {

                    SOAPElement ele = (SOAPElement)obj;

   

                        java.util.Iterator it2 = ele.getChildElements();

                        while( it2.hasNext() )

                        {

                            Object obj2 = it2.next();

                            if( obj2 instanceof SOAPElement )

                            {

                                SOAPElement ele2 = (SOAPElement)obj2;

                                String s2 = ele2.getElementName().getLocalName();

                                if( s2.equals("return") )

                                {

                                    java.util.Iterator it3 = ele2.getChildElements();

                                    while( it3.hasNext() )

                                    {

                                        Object obj3 = it3.next();

                                        if( obj3 instanceof Text )

                                        {

                                            Text txt = (Text)obj3;

                                            System.out.println( "Issue " + txt.getValue() + " has been created." );

                                        }

                                    }

                                }

                            }

                        }

                   

                }

            }

        }

        catch( Exception ex )

        {

            ex.printStackTrace();

        }

	}
}
