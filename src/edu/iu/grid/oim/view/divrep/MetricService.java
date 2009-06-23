package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.webif.divrep.common.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.FormElement;
import com.webif.divrep.common.Select;
import com.webif.divrep.common.Text;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor.ContactDE;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class MetricService extends FormElement {

	//ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();
	private Button add_button;
	TreeMap<Integer, String> metric_kv;
	
	class MetricEditor extends FormElement
	{
		private Select metric;
		private CheckBoxFormElement critical;
		private Button remove_button;
		private MetricEditor myself;
		
		protected MetricEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			metric = new Select(this, metric_kv);
			metric.setLabel("Metric Name");
			
			critical = new CheckBoxFormElement(this);
			critical.setLabel("This is a critical metric for this service");
			
			remove_button = new Button(this, "images/delete.png");
			remove_button.setStyle(Button.Style.IMAGE);
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeMetric(myself);	
				}
			});
		}

		public void setMetric(Integer metric_id) {
			metric.setValue(metric_id);
		}
		public Integer getMetric() {
			return metric.getValue();
		}
		public void setCritical(Boolean b) {
			critical.setValue(b);
		}
		public Boolean getCritical() {
			return critical.getValue();
		}
		public MetricServiceRecord getMetricServiceRecord()
		{
			MetricServiceRecord rec = new MetricServiceRecord();
			rec.critical = critical.getValue();
			rec.metric_id = metric.getValue();
			return rec;
		}

		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"metric_service_editor\">");
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");
			
			metric.render(out);
			out.write("<br/>");
			critical.render(out);
			out.write("</div>");
		}
	}
	
	public void removeMetric(MetricEditor metric)
	{
		remove(metric);
		redraw();
	}
	
	public void addMetric(MetricServiceRecord rec) { 
		MetricEditor elem = new MetricEditor(this);
		elem.setMetric(rec.metric_id);
		elem.setCritical(rec.critical);
		redraw();
	}
	
	public MetricService(DivRep parent, TreeMap<Integer, String> _metric_kv) {
		super(parent);
		metric_kv = _metric_kv;
		
		add_button = new Button(this, "Add New RSV Metric");
		add_button.setStyle(Button.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addMetric(new MetricServiceRecord());
			}	
		});
	}

	public ArrayList<MetricServiceRecord> getMetricServiceRecords()
	{
		ArrayList<MetricServiceRecord> recs = new ArrayList<MetricServiceRecord>();
		for(DivRep node : childnodes) {
			if(node instanceof MetricEditor) {
				MetricEditor se = (MetricEditor)node;
				recs.add(se.getMetricServiceRecord());
			}
		}
		return recs;
	}

	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(DivRep node : childnodes) {
			if(node instanceof MetricEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
