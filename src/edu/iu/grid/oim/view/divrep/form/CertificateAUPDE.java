package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;
import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;

import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;

public class CertificateAUPDE extends DivRepFormElement {
	String aup;
	DivRepCheckBox agreement;
	public CertificateAUPDE(DivRep parent) {
		super(parent);
		//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
		InputStream aup_stream = getClass().getResourceAsStream("osg.certificate.agreement.html");
		aup = ResourceReader.loadContent(aup_stream).toString();
		agreement = new DivRepCheckBox(this);
		agreement.setLabel("I agree");
		agreement.setRequired(true);
		agreement.addValidator(new MustbeCheckedValidator("You must agree to these policies"));	
		/*
		agreement.addValidator(new DivRepIValidator<Boolean>() {
			@Override
			public Boolean isValid(Boolean value) {
				return value;
			}

			@Override
			public String getErrorMessage() {
				return "You must agree to these policies";
			}
		});
		*/
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
		//out.write("<h3>OSG Policy Agreement</h3>");//"IGTF Certificate Subscriber Agreement" is in the aup itself
		out.write(aup);
		agreement.render(out);		
		out.write("</div>");
	}
}

