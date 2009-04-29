package edu.iu.grid.oim.lib;

import javax.xml.soap.*;

import edu.iu.grid.oim.model.Context;

public class Footprint 
{
	Context context;
	
	SOAPBody body;
	SOAPEnvelope env;
	SOAPConnection connection;
	SOAPMessage msg;
	
	//TODO - pull these from config
	String fp_uri = "https://tick.globalnoc.iu.edu/MRWebServices";
	String fp_url = "https://tick.globalnoc.iu.edu/MRcgi/MRWebServices.pl";
	String fp_username = "goc";
	String fp_password = "5be1499a577add53307142asdaq2WSagsASDGa";
	String fp_truststore = "jssecacerts";
	
	public Footprint(Context _context)
	{
		context = _context;
		
		//using program listed here > http://blogs.sun.com/andreas/entry/no_more_unable_to_find
		//to create the trusted keystore
		System.setProperty("javax.net.ssl.trustStore", fp_truststore);
		
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

	public void createTicket()
	{
		try
        {
            SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1",fp_uri) );
            
            // root parameters user/pass/extra/args
            SOAPElement username = invoke.addChildElement( env.createName("user") );
            username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            username.addTextNode(fp_username);
            SOAPElement password = invoke.addChildElement( env.createName("password") );
            password.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            password.addTextNode(fp_password);
            SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
            extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            SOAPElement args = invoke.addChildElement( env.createName("args") );
            args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            
            //Contat Information
            SOAPElement abfields = args.addChildElement( env.createName("abfields") );
            abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            SOAPElement arg4_3_2 = abfields.addChildElement( env.createName("Last__bName") );
            arg4_3_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_2.addTextNode("Doe");
            SOAPElement arg4_3_3 = abfields.addChildElement( env.createName("First__bName") );
            arg4_3_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_3.addTextNode("John");
            SOAPElement arg4_3_4 = abfields.addChildElement( env.createName("Email__baddress") );
            arg4_3_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_3_4.addTextNode("johndoe@nowhere.com");

            SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
            arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_7.addTextNode("109");       
            SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
            arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_9.addTextNode("OSG-GOC");
            
            SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
            arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_8.addTextNode("This title comes from Java");
            
            SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
            arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_1.addTextNode("4");
            
            SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
            arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_4.addTextNode("Place issue description here. From Java code.");

            SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
            arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
            arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string[1]" );//CHANGE [1] to [n] based on the number of assignee.. (why!!!)
            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_5_1.addTextNode("hayashis");
            /*
            SOAPElement arg4_5_2 = arg4_5.addChildElement( env.createName("item") );
            arg4_5_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_5_2.addTextNode("user2");
			*/
            
            /*
            SOAPElement arg4_6 = arg4.addChildElement( env.createName("projfields") );
            arg4_6.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
            SOAPElement arg4_6_1 = arg4_6.addChildElement( env.createName("Custom__bField__bOne") );
            arg4_6_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_6_1.addTextNode("Value of Custom Field One");
            SOAPElement arg4_6_2 = arg4_6.addChildElement( env.createName("Custom__bField__bTwo") );
            arg4_6_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_6_2.addTextNode("Value of Custom Field Two");
            */
            SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
            arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_2.addTextNode("Open");
            msg.saveChanges();

            SOAPMessage reply = connection.call( msg, fp_url );
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
