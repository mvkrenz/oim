package edu.iu.grid.oim.view.divrep.form.servicedetails;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.form.validator.ServiceKVKeyValidator;

public class MiscServiceDetails extends ServiceDetailsContent {
	
	public class KeyValue extends DivRep {
		
		public DivRepTextBox key;
		public DivRepTextBox value;
		public DivRepButton remove_button;
		
		public KeyValue(DivRep _parent, String _key, String _value) {
			super(_parent);
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					remove(KeyValue.this);
				}
			});
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.addClass("right");
			
			key = new DivRepTextBox(this);
			//key.setLabel("Key");
			key.setValue(_key);
			key.setRequired(true);
			key.addValidator(new ServiceKVKeyValidator());
			
			value = new DivRepTextBox(this);
			//value.setLabel("Value");
			value.addInputClass("input-xlarge");
			//value.setRequired(true);
			value.setValue(_value);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"row-fluid\">");
			
			out.write("<div class=\"span5\">");
			key.render(out);
			out.write("</div>");
			
			out.write("<div class=\"span6\">");
			value.render(out);
			out.write("</div>");
			
			out.write("<div class=\"span1\">");
			remove_button.render(out);
			out.write("</div>");
			
			out.write("</div>");
		}
		
		private void remove(KeyValue it) {
			kvs.remove(it);
			MiscServiceDetails.this.redraw();
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public boolean validate() {
			return key.validate() && value.validate();
		}
		
	}
	
	public DivRepButton add_button;
	ArrayList<KeyValue> kvs = new ArrayList<KeyValue>();

	public MiscServiceDetails(final DivRep _parent, ServiceRecord srec) {
		super(_parent);
		add_button = new DivRepButton(this, "Add New Key");
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			@Override
			public void handleEvent(DivRepEvent e) {
				kvs.add(new KeyValue(MiscServiceDetails.this, "", ""));
				redraw();
			}	
		});
		
		//add one empty one
		//kvs.add(new KeyValue(MiscServiceDetails.this, "", ""));
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		
		out.write("<div class=\"row-fluid\">");
		out.write("<div class=\"span5\">Key</div>");
		out.write("<div class=\"span6\">Value</div>");
		out.write("<div class=\"span1\"></div>");	
		out.write("</div>");
		
		for(KeyValue kv : kvs) {
			kv.render(out);
			//out.write("<hr>");
		}
		add_button.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValues(HashMap<String, String> values) {
		kvs.clear();
		for(String key : values.keySet()) {
			String value = values.get(key);
			KeyValue kv = new KeyValue(this, key, value);
			kvs.add(kv);
		}
	}

	@Override
	public HashMap<String, String> getValues() {
		HashMap<String, String> out = new HashMap<String, String>();
		for(KeyValue kv : kvs) {
			out.put(kv.key.getValue(), kv.value.getValue());
		}
		return out;
	}

	@Override
	public boolean validate() {
		boolean b = true;
		for(KeyValue kv : kvs) {
			b = b && kv.validate();
		}
		return b;
	}
}
