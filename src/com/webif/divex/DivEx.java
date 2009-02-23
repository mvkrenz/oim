package com.webif.divex;
import java.util.HashMap;

public abstract class DivEx {
	static private HashMap<String, DivEx> nodes = new HashMap<String, DivEx>();	
	static private int next_nodeid = 0;

	private String nodeid;
	private Boolean needupdate = false;
	
	public static String bootcode()
	{
		String boot = "<script type=\"text/javascript\">"+
		"function divex_click(node) {"+
		"$.getScript('divex?nodeid='+node.id+'&action=click');"+
		"}"+
		"</script>";
		return boot;
	}

	public static String outputUpdatecode()
	{
		String code = "";
		for(DivEx d : DivEx.nodes.values()) {
			if(d.needupdate) {
				code += "$(\"#"+d.nodeid+"\").load(\"divex?action=load&nodeid="+d.nodeid+"\");";
				d.needupdate = false;
			}
		}	
		return code;
	}
	public DivEx() {
		nodeid = "divex_"+next_nodeid;
		++next_nodeid;
		DivEx.nodes.put(nodeid, this);
	}
	
	final public String render() {
		String html = "";
		html += "<div class='divex' id='"+nodeid+"' onclick='divex_click(this);'>";
		html += toHTML();
		html += "</div>";
		return html;
	}
	
	abstract public String toHTML();
	
	protected void onClick(Event e) {
	}
	
	public void update() {
		needupdate = true;
	}


	public static String encodeHTML(String s)
	{
	    StringBuffer out = new StringBuffer();
	    for(int i=0; i<s.length(); i++)
	    {
	        char c = s.charAt(i);
	        if(c > 127 || c=='"' || c=='\'' || c=='<' || c=='>')
	        {
	           out.append("&#"+(int)c+";");
	        }
	        else
	        {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
	
	public static DivEx findNode(String nodeid) {
		DivEx node = nodes.get(nodeid);
		return node;
	}
}
