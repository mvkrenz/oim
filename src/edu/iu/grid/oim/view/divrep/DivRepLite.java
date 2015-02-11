package edu.iu.grid.oim.view.divrep;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

abstract public class DivRepLite extends DivRep {

	public DivRepLite(DivRep _parent) {
		super(_parent);
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}

}
