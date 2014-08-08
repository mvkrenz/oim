package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.MeshConfigGroupModel;
import edu.iu.grid.oim.model.db.MeshConfigParamModel;
import edu.iu.grid.oim.model.db.MeshConfigTestModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.VOSelector;


public class MeshConfigServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(MeshConfigServlet.class);  

    final Integer SERVICE_GROUP_PERFSONAR_MONIOTIRNG = 1003;
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		auth.check("admin");
		
		BootMenuView menuview = new BootMenuView(context, "meshconfig");
		BootPage page = new BootPage(context, menuview, createContent(context), null);
		page.render(response.getWriter());		
	}
	/*
	
	void renderTest(PrintWriter out, MeshConfigTestRecord test) {
		out.write("type:"+test.service_id);
	}
	*/
	
	protected IView createConfigPane(final UserContext context) {
			
		class TestDiv extends DivRepFormElement {
			
			LinkedHashMap<Integer, String> mesh_types = new LinkedHashMap();

			DivRepTextBox name;
			VOSelector vo;
			DivRepCheckBox disable;
			
			DivRepSelectBox type;
			DivRepSelectBox param;
			DivRepSelectBox group_a;
			DivRepSelectBox group_b;

			protected TestDiv(DivRep parent, MeshConfigTestRecord rec) {
				super(parent);
				
				mesh_types = new LinkedHashMap();
				mesh_types.put(0, "DISJOINT");
				mesh_types.put(1, "MESH");
				mesh_types.put(2, "STAR");		
				
				name = new DivRepTextBox(this);
				name.setLabel("Name");
				name.setRequired(true);
		
				vo = new VOSelector(this, context);
				vo.setRequired(true);
				
				disable = new DivRepCheckBox(this);
				disable.setLabel("Disable");
				
				type = new DivRepSelectBox(this);
				type.setLabel("Mesh Type");
				type.setRequired(true);
				type.setValues(mesh_types);
				type.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						meshtypeUpdated();
						TestDiv.this.redraw();
					}
				});
				
				//load group keyvalues
				LinkedHashMap<Integer, String> group_keyvalues = new LinkedHashMap();
				try {
					MeshConfigGroupModel gmodel = new MeshConfigGroupModel(context);
					ArrayList<MeshConfigGroupRecord> grecs = gmodel.getAll();
					Collections.sort(grecs, new Comparator<MeshConfigGroupRecord> () {
						public int compare(MeshConfigGroupRecord a, MeshConfigGroupRecord b) {
							return a.name.compareToIgnoreCase(b.name);
						}
					});
					for(MeshConfigGroupRecord grec : grecs) {
						//if(vo_rec.disable) continue;
						group_keyvalues.put(grec.id, grec.name);
					}	
				} catch (SQLException e) {
					log.error("failed to load service record for perfsonar monitoring group", e);
				}
				group_a = new DivRepSelectBox(this);
				group_a.setLabel("Group A");
				group_a.setValues(group_keyvalues);	
				
				group_b = new DivRepSelectBox(this);
				group_b.setLabel("Group B");
				group_b.setValues(group_keyvalues);	
				
				param = new DivRepSelectBox(this);
				param.setLabel("Parameters");
				param.setRequired(true);
				try {
					LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
					MeshConfigParamModel mmodel = new MeshConfigParamModel(context);
					ArrayList<MeshConfigParamRecord> precs = mmodel.getAll();
					Collections.sort(precs, new Comparator<MeshConfigParamRecord> () {
						public int compare(MeshConfigParamRecord a, MeshConfigParamRecord b) {
							return a.name.compareToIgnoreCase(b.name);
						}
					});
					for(MeshConfigParamRecord prec : precs) {
						//if(vo_rec.disable) continue;
						keyvalues.put(prec.id, prec.name);
					}	
					param.setValues(keyvalues);	
				} catch (SQLException e) {
					log.error("failed to load service record for perfsonar monitoring group", e);
				}
				
				if(rec != null) {
					name.setValue(rec.name);
					vo.setValue(rec.vo_id);
					disable.setValue(rec.disable);
					type.setValue(meshTypeStringToInteger(rec.type));
					param.setValue(rec.param_id);
					group_a.setValue(rec.groupa_id);
					group_b.setValue(rec.groupb_id);
				}
				
				meshtypeUpdated();
			}
			
			private Integer meshTypeStringToInteger(String type) {
				for(Integer id : mesh_types.keySet()) {
					String mtype = mesh_types.get(id);
					if(mtype.equals(type)) {
						return id;
					}
				}
				return null;
			}
			
			private void meshtypeUpdated() {
				//hide everything by default
				group_a.setRequired(false);
				group_a.setHidden(true);
				group_b.setRequired(false);
				group_b.setHidden(true);
				
				if(type.getValue() != null) {
					switch(mesh_types.get(type.getValue())) {
					case "MESH":
						//only show group A
						group_a.setRequired(true);
						group_a.setHidden(false);
						break;
					case "DISJOINT":
					case "STAR":
						//show both
						group_a.setRequired(true);
						group_a.setHidden(false);
						group_b.setRequired(true);
						group_b.setHidden(false);
						break;
					}
				}
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
				//out.write("<hr>");
				name.render(out);
				vo.render(out);
				
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span3\">");
					type.render(out);
				out.write("</div>");
				out.write("<div class=\"span3\">");
					param.render(out);
				out.write("</div>");
				out.write("<div class=\"span3\">");
					group_a.render(out);
				out.write("</div>");
				out.write("<div class=\"span3\">");
					group_b.render(out);			
				out.write("</div>");//sapn4
				out.write("</div>");//row-fluid
				
				disable.render(out);
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}		
		}
		
		class TestsDiv extends DivRepFormElement {
			DivRepButton add;
			ArrayList<TestDiv> tdivs = new ArrayList<TestDiv>();

			protected TestsDiv(DivRep parent) {
				super(parent);
				
				add = new DivRepButton(this, "Add New Test") {
					protected void onClick(DivRepEvent e) {
						tdivs.add(new TestDiv(TestsDiv.this, null));
						TestsDiv.this.redraw();
					}				
				};
				add.addClass("btn");
				add.addClass("pull-right");
				
				MeshConfigTestModel model = new MeshConfigTestModel(context);
				try {
					for(MeshConfigTestRecord rec : model.getAll()) {
						TestDiv tdiv = new TestDiv(this, rec);
						tdivs.add(tdiv);
					}
				} catch (SQLException e) {
					log.error("failed to load meshconfig tests", e);
				}
				
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				add.render(out);
				for(TestDiv div: tdivs) {
					div.render(out);
				}
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}
		}
		/*
		class ConfigDiv extends DivRepFormElement {
			
			DivRepButton edit;
			
			DivRepTextBox name;
			DivRepTextArea desc;
			VOSelector vo;
			DivRepCheckBox disable;
			
			TestsDiv tests;
			
			public ConfigDiv(DivRep parent, MeshConfigRecord rec) {
				super(parent);
				name = new DivRepTextBox(this);
				name.setLabel("Name");
				name.setRequired(true);
				desc = new DivRepTextArea(this);
				desc.setLabel("Description");
				vo = new VOSelector(this, context);
				vo.setRequired(true);
				
				edit = new DivRepButton(this, "Edit") {
					protected void onClick(DivRepEvent e) {
						setHidden(true);
						ConfigDiv.this.redraw();
					}
				};
				edit.setHidden(true); //hide by default (editable by default)
				edit.addClass("pull-right");
				edit.addClass("btn");
				
				if(rec != null) {
					name.setValue(rec.name);
					desc.setValue(rec.description);
					vo.setValue(rec.vo_id);
					//let's disable this for the moment..
					//edit.setHidden(false);
				}
				
				tests = new TestsDiv(this, rec);
				
				disable = new DivRepCheckBox(this);
				disable.setLabel("Disable");
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"well well-mini\">");
				if(edit.isHidden()) {				
					out.write("<div class=\"row-fluid\">");
					out.write("<div class=\"span6\">");
						name.render(out);
						vo.render(out);
						disable.render(out);
					out.write("</div>");
					out.write("<div class=\"span6\">");
						desc.render(out);
					out.write("</div>");
					out.write("</div>");//row-fluid
					
				} else {
					edit.render(out);
					out.write("<h2>"+StringEscapeUtils.escapeHtml(name.getValue()));
					if(disable.getValue()) {
						out.write("<span class=\"label label-info\">Disable</span>");
					}
					out.write("</h2>");
					out.write("<p>"+StringEscapeUtils.escapeHtml(name.getValue())+"</p>");
				}
				tests.render(out);
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}			
		}
		
		class ConfigDivs extends DivRepFormElement {
			
			DivRepButton add;

			ArrayList<ConfigDiv> cdivs = new ArrayList<ConfigDiv>();
			VOModel vomodel = new VOModel(context);
			
			public ConfigDivs(DivRep _parent) {
				super(_parent);
				
				add = new DivRepButton(this, "Add New Mesh Config") {
					protected void onClick(DivRepEvent e) {
						cdivs.add(new ConfigDiv(ConfigDivs.this, null));
						ConfigDivs.this.redraw();
					}				
				};
				add.addClass("btn");
				add.addClass("pull-right");
				
				//load all meshconfigs
				MeshConfigModel model = new MeshConfigModel(context);	
				try {
					for(MeshConfigRecord rec : model.getAll()) {
						ConfigDiv cdiv = new ConfigDiv(this, rec);
						cdivs.add(cdiv);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");

				add.render(out);
				out.write("<br>");

				//group cdivs into different vos
				HashMap<Integer, ArrayList<ConfigDiv>> groups = new HashMap<Integer, ArrayList<ConfigDiv>>();
				for(ConfigDiv cdiv : cdivs) {
					ArrayList<ConfigDiv> group;
					Integer vo_id = cdiv.vo.getValue();
					if(!groups.containsKey(vo_id)) {
						group = new ArrayList<ConfigDiv>();
						groups.put(vo_id, group);
					} else {
						group = groups.get(vo_id);
					}
					group.add(cdiv);
				}
				for(Integer vo_id : groups.keySet()) {
					ArrayList<ConfigDiv> group = groups.get(vo_id);
					VORecord vorec;
					try {
						if(vo_id != null) { //new meshconfig is set to null vo initially
							vorec = vomodel.get(vo_id);
							out.write("<h2>"+StringEscapeUtils.escapeHtml(vorec.name)+"</h2>");
						} else {
							out.write("<h2>(New)</h2>");
						}
					} catch (SQLException e) {
						log.error("failed to load VO with ID:"+vo_id);
					}
					
					for(ConfigDiv cdiv : group) {
						out.write("<div style=\"clear: both;\">"); //so that add button doesn't sit inside the new meshconfig editor
						cdiv.render(out);
						out.write("</div>");
					}	
				}
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		*/
		
		return new IView(){
			public void render(PrintWriter out) {
				TestsDiv tests = new TestsDiv(context.getPageRoot());
				tests.render(out);
				/*
				MeshConfigModel model = new MeshConfigModel(context);
				VOModel vomodel = new VOModel(context);
				MeshConfigTestModel testmodel = new MeshConfigTestModel(context);
				try {
					out.write("<table class=\"table\">");
					out.write("<thead><tr><th>Config Name</th><th>Desc.</th><th>VO</th><th>Tests</th><th></th></tr></thead>");
					out.write("<tbody>");
					for(MeshConfigRecord rec : model.getAll()) {
						out.write("<tr>");
						
						out.write("<td>"+StringEscapeUtils.escapeHtml(rec.name));
						if(rec.disable) {
							out.write(" <span class=\"label label-default\">Disabled</span>");
						}
						out.write("</td>");
						
						out.write("<td>"+StringEscapeUtils.escapeHtml(rec.description)+"</td>");						
						VORecord vorec = vomodel.get(rec.vo_id);
						out.write("<td>"+StringEscapeUtils.escapeHtml(vorec.name)+"</td>");
						
						//list tests
						out.write("<td>");
						for(MeshConfigTestRecord test : testmodel.getByMeshconfigID(rec.id)) {
							renderTest(out, test);
						}
						out.write("</td>");
						
						out.write("<td>");
						
						DivRepButton edit = new DivRepButton(context.getPageRoot(), "Edit") {
							@Override
							protected void onClick(DivRepEvent e) {
								alert("hi");
							}
						};
						edit.addClass("btn");
						edit.addClass("btn-mini");
						edit.render(out);
						
						out.write("</td>");
						out.write("</tr>");
					}
					out.write("</tbody></table>");
				} catch (SQLException e) {
					log.error("failed to load meshconfig records",e);
				}
				*/
			}
		};
	}
	
	protected IView createGroupPane(final UserContext context) {
		
		class GroupDiv extends DivRepFormElement {
			protected GroupDiv(DivRep parent, MeshConfigGroupRecord rec) {
				super(parent);
				service = new DivRepSelectBox(this);
				service.setLabel("Service Type");
				service.setRequired(true);
				try {
					LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
					ServiceModel smodel = new ServiceModel(context);
					ArrayList<ServiceRecord> srecs = smodel.getByServiceGroupID(SERVICE_GROUP_PERFSONAR_MONIOTIRNG);
					Collections.sort(srecs, new Comparator<ServiceRecord> () {
						public int compare(ServiceRecord a, ServiceRecord b) {
							return a.name.compareToIgnoreCase(b.name);
						}
					});
					for(ServiceRecord srec : srecs) {
						//if(vo_rec.disable) continue;
						keyvalues.put(srec.id, srec.name);
					}	
					service.setValues(keyvalues);	
				} catch (SQLException e) {
					log.error("failed to load service record for perfsonar monitoring group", e);
				}
			}
			DivRepTextBox name;
			DivRepSelectBox service;
			
			@Override
			public void render(PrintWriter out) {
				// TODO Auto-generated method stub
				
			}
			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		
		return new IView(){
			public void render(PrintWriter out) {
				out.write("Group Pane - TODO");
			}
		};
	}
	protected IView createParamPane(final UserContext context) {
		return new IView(){
			public void render(PrintWriter out) {
				out.write("PArameter Pane - TODO");
			}
		};
	}
	
	protected IView createContent(final UserContext context) throws ServletException {
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				
				out.write("<div id=\"content\">");
				out.write("<h2>MeshConfig Administrator</h2>");
				
				BootTabView tabview = new BootTabView();
				tabview.addtab("Tests", createConfigPane(context));
				tabview.addtab("Groups", createGroupPane(context));
				tabview.addtab("Parameters", createParamPane(context));
				tabview.render(out);
				
				out.write("</div>"); //content
			}
		};
	}
}
