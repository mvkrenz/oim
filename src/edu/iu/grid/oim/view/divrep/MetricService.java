package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor.ContactDE;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class MetricService extends DivRepFormElement {

	//ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();
	private DivRepButton add_button;
	LinkedHashMap<Integer, String> metric_kv;
	
	class MetricEditor extends DivRepFormElement
	{
		private DivRepSelectBox metric;
		private DivRepCheckBox critical;
		private DivRepButton remove_button;
		private MetricEditor myself;
		
		protected MetricEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			metric = new DivRepSelectBox(this, metric_kv);
			metric.setLabel("Metric Name");
			
			critical = new DivRepCheckBox(this);
			critical.setLabel("This is a critical metric for this service");
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
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

		protected void onEvent(DivRepEvent e) {
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
	
	public MetricService(DivRep parent, LinkedHashMap<Integer, String> _metric_kv) {
		super(parent);
		metric_kv = _metric_kv;
		
		add_button = new DivRepButton(this, "Add New RSV Metric");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
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

	protected void onEvent(DivRepEvent e) {
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
