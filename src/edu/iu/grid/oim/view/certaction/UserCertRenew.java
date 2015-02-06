package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;

import com.divrep.DivRep;

import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.Wizard;

public class UserCertRenew implements IView {
	Wizard wizard;
	public UserCertRenew(DivRep parent) {
		wizard = new Wizard(parent);
		
		Wizard.Step start = wizard.new Wizard.Step("Begin");
		wizard.addStep(start);
		
		Wizard.Step agreement = wizard.new Wizard.Step("Agreement");
		wizard.addStep(agreement);
	}

	@Override
	public void render(PrintWriter out) {
		wizard.render(out);
	}
}
