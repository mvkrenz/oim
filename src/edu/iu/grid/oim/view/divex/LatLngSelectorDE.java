package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.FormElementDEBase;

public class LatLngSelectorDE extends FormElementDEBase<LatLngSelectorDE.LatLng> {
	public class LatLng
	{
		public LatLng(Double lat, Double lng) {
			latitude = lat;
			longitude = lng;
		}
		public Double latitude;
		public Double longitude;
	}

	public LatLngSelectorDE(DivEx parent) {
		super(parent);
	}

	protected void onEvent(Event e) {
		String newval = (String)e.value;
		String coords[] = newval.split(",");
		value.latitude = Double.parseDouble(coords[0]);
		value.longitude = Double.parseDouble(coords[1]);
	}

	public void render(PrintWriter out) {
		out.write("<div ");
		renderClass(out);
		out.write("id=\""+getNodeID()+"\">");
		if(!hidden) {
			if(label != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
			}
			out.write("<div id=\"map_canvas_"+getNodeID()+"\" style=\"width: 500px; height: 300px\"></div>"); 
			out.write("<script type=\"text/javascript\">\n");
			out.write("$(document).ready(function() {");
			    out.write("if (GBrowserIsCompatible()) {");
			    out.write("    var map = new GMap2(document.getElementById(\"map_canvas_"+getNodeID()+"\"));");
			    out.write("    map.setCenter(new GLatLng("+value.latitude+", "+value.longitude+"), 18);");
			    out.write("    map.setUIToDefault();");
			    out.write("    map.setMapType(G_HYBRID_MAP);");
			    //out.write("    map.enableGoogleBar();");
			    out.write("    var prompt = new GScreenOverlay(\"http://localhost:8080/oim/images/target.png\",new GScreenPoint(0.5, 0.5, 'fraction', 'fraction'), new GScreenPoint(-50, -50), new GScreenSize(100, 100, 'pixel', 'pixel'));");
			    out.write("    map.addOverlay(prompt);");
			    out.write("    GEvent.addListener(map, 'moveend', function() {divex('"+getNodeID()+"', null, map.getCenter().toUrlValue());});");
			    out.write("}");
			out.write("});");
			out.write("</script>");
		
			if(isRequired()) {
				out.write(" * Required");
			}
			error.render(out);	
		}
		out.write("</div>");
	}

}
