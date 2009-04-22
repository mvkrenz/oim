package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE.ContactDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;
import edu.iu.grid.oim.view.divex.form.ResourceDowntimeFormDE.DowntimeEditor;

public class MetricServiceDE extends FormElementDEBase {

	//ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();
	private ButtonDE add_button;
	HashMap<Integer, String> metric_kv;
	
	class MetricEditor extends FormElementDEBase
	{
		private SelectFormElementDE metric;
		private CheckBoxFormElementDE critical;
		private ButtonDE remove_button;
		private MetricEditor myself;
		
		protected MetricEditor(DivEx parent) {
			super(parent);
			myself = this;
			
			metric = new SelectFormElementDE(this, metric_kv);
			metric.setLabel("Metric Name");
			
			critical = new CheckBoxFormElementDE(this);
			critical.setLabel("This is a critical metric for this service");
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
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
	
	public MetricServiceDE(DivEx parent, HashMap<Integer, String> _metric_kv) {
		super(parent);
		metric_kv = _metric_kv;
		
		add_button = new ButtonDE(this, "Add New RSV Metric");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addMetric(new MetricServiceRecord());
			}	
		});
	}

	public ArrayList<MetricServiceRecord> getMetricServiceRecords()
	{
		ArrayList<MetricServiceRecord> recs = new ArrayList<MetricServiceRecord>();
		for(DivEx node : childnodes) {
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
		for(DivEx node : childnodes) {
			if(node instanceof MetricEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
