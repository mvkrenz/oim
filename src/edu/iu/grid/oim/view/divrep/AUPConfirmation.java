package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;

public class AUPConfirmation extends DivRepFormElement {

	DivRepCheckBox check;
	
	public AUPConfirmation(DivRep parent) {
		super(parent);
		check = new DivRepCheckBox(this);
		check.setLabel("I agree to these terms.");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<h2>Acceptable Use Policy (AUP) Agreement</h2>");
			out.write("<p>You must agree to following OSG AUP agreements before submitting this entity.</p>");
			out.write("<p>");
			out.write("<a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/0000/000086/009/OSG-UsersAUP-V2.pdf\">OSG User AUP</a><br/>");
			out.write("<a target=\"_blank\" href=\"http://osg-docdb.opensciencegrid.org/0000/000087/005/OSG-ServiceAUP-V104.pdf\">OSG Service AUP</a><br/>");
			out.write("</p>");
			check.render(out);
			error.render(out);	
			out.write("</div>");
		
	}

	//aup is invalid if user don't check it
	public boolean validate()
	{
		boolean previous_valid = super.validate();
		boolean valid = previous_valid;
		
		if(valid) {	
			if(!check.getValue()) {
				error.set("You must agree to AUP before submitting.");
				valid = false;
			}
		}
		
		//why valid == false? because sometime error message can change to something else while it's set to true
		if(previous_valid != valid || valid == false) {
			error.redraw();
		}
		
		setValid(valid);
		return valid;
	}
}
