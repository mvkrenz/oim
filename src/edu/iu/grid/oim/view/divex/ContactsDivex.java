package edu.iu.grid.oim.view.divex;

import java.util.ArrayList;

import com.webif.divex.DivEx;
import com.webif.divex.Event;

public class ContactsDivex extends DivEx {
	public ContactsDivex(DivEx parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	ArrayList<Integer> contacts = new ArrayList<Integer>();
	
	DivEx addbutton = new AddButton(this);
	
	class AddButton extends DivEx
	{
		ContactsDivex parent;
		public AddButton(ContactsDivex _parent) {
			super(_parent);
			parent = _parent;
		}
	    public String toHTML() {
	        return "<b>Add</b>";   
	    }
		protected void onClick(Event e) {
			parent.add(12);
		}
	};
	
	public void add(Integer contact) {
		contacts.add(contact);
		redraw();
	}

	public String toHTML() 
	{
		String out = "";
		out += "Contact List<br/>";
		for(Integer contact : contacts) {
			out += contact+"<br/>";
		}
		out += addbutton.render();
		return out;
	}
}
