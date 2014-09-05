package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.MeshConfigGroupModel;
import edu.iu.grid.oim.model.db.MeshConfigModel;
import edu.iu.grid.oim.model.db.MeshConfigOIMMemberModel;
import edu.iu.grid.oim.model.db.MeshConfigParamModel;
import edu.iu.grid.oim.model.db.MeshConfigContactModel;
import edu.iu.grid.oim.model.db.MeshConfigTestModel;
import edu.iu.grid.oim.model.db.MeshConfigWLCGMemberModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceDetailModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.WLCGEndpointModel;
import edu.iu.grid.oim.model.db.WLCGSiteModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigOIMMemberRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigContactRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigWLCGMemberRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.WLCGEndpointRecord;
import edu.iu.grid.oim.model.db.record.WLCGSiteRecord;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.ContactEditor;
import edu.iu.grid.oim.view.divrep.OIMResourceServiceListEditor;
import edu.iu.grid.oim.view.divrep.WLCGResourceServiceListEditor;

public class MeshConfigFormDE extends DivRepForm {
    static Logger log = Logger.getLogger(MeshConfigFormDE.class); 
	UserContext context;
	LinkedHashMap<Integer, String> service_types = new LinkedHashMap();
	
    final Integer SERVICE_GROUP_PERFSONAR_MONIOTIRNG = 1003;
	
	GroupsDiv groupsdiv;
	ParamsDiv paramsdiv;
	TestsDiv testsdiv;
	ConfigsDiv configsdiv;
	
	static public ArrayList<ContactTypeRecord.Info> ContactTypes;
	static {
		ContactTypes = new ArrayList<ContactTypeRecord.Info>();
		ContactTypes.add(new ContactTypeRecord.Info(1, "A contact who has registered this mesh config"));
		ContactTypes.add(new ContactTypeRecord.Info(3, "Contacts who should be listed as mesh administrators."));
		//ContactTypes.add(new ContactTypeRecord.Info(2, "Security notifications sent out by the OSG security team are sent to primary and secondary virtual organization security contacts"));
		//ContactTypes.add(new ContactTypeRecord.Info(5, "Contacts who do not fall under any of the above types."));
	}

	class TestDiv extends DivRepFormElement {
		
		LinkedHashMap<Integer, String> mesh_types = new LinkedHashMap();

		Integer id;
		
		DivRepTextBox name;
		DivRepSelectBox config;
		DivRepSelectBox service;
		DivRepSelectBox type;
		DivRepSelectBox param;
		DivRepSelectBox group_a;
		DivRepSelectBox group_b;
		
		DivRepCheckBox disable;
		DivRepButton remove;

		protected TestDiv(DivRep parent, MeshConfigTestRecord rec) {
			super(parent);
		
			mesh_types = new LinkedHashMap();
			mesh_types.put(0, "DISJOINT");
			mesh_types.put(1, "MESH");
			mesh_types.put(2, "STAR");		
					
			name = new DivRepTextBox(this);
			name.setLabel("Name");
			name.setRequired(true);
			name.setSampleValue("Intercloud OWAMP Mesh Test");
			/*
			vo = new VOSelector(this, context);
			vo.setRequired(true);
			*/
			disable = new DivRepCheckBox(this);
			disable.setLabel("Disable");
			
			service = new DivRepSelectBox(this);
			service.setLabel("Service Type");
			service.setRequired(true);
			service.setValues(service_types);
			service.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					load_keyvalues();
					
					//reset to null
					param.setValue(null); 
					group_a.setValue(null);
					group_b.setValue(null);
					
					showhide();
					TestDiv.this.redraw();
				}
			});
			
			type = new DivRepSelectBox(this);
			type.setLabel("Mesh Type");
			type.setRequired(true);
			type.setValues(mesh_types);
			type.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					showhide();
					TestDiv.this.redraw();
				}
			});
			
			group_a = new DivRepSelectBox(this);
			group_a.setLabel("Host Group A");
			
			group_b = new DivRepSelectBox(this);
			group_b.setLabel("Host Group B");
			
			param = new DivRepSelectBox(this);
			param.setLabel("Parameters");
			param.setRequired(true);
			
			config = new DivRepSelectBox(this);
			config.setLabel("Configuration to be part of");
			config.setRequired(true);
			
			remove = new DivRepButton(this, "images/delete.png") {
				@Override
				protected void onClick(DivRepEvent e) {
					//divrep keeps track of its children, and DivrepFormElement validates all children even if I remove it here.
					//instead of overriding validate(), I am trying this new approach by hiding the element.
					TestDiv.this.setHidden(true); 
					
					testsdiv.tests.remove(TestDiv.this);
					testsdiv.redraw();
				}	
			};
			remove.addClass("pull-right");
			remove.setStyle(DivRepButton.Style.IMAGE);
			
			if(rec != null) {
				id = rec.id;
				name.setValue(rec.name);
				disable.setValue(rec.disable);
				service.setValue(rec.service_id);
				type.setValue(meshTypeStringToInteger(rec.type));
				param.setValue(rec.param_id);
				group_a.setValue(rec.groupa_id);
				group_b.setValue(rec.groupb_id);
				config.setValue(rec.mesh_config_id);
			} else {
				//come up with a new ID
				Integer nextid = 0;
				for(TestDiv div : testsdiv.tests) {
					if(nextid <= div.id) {
						nextid = div.id+1;
					}
				}
				id = nextid;
			}
						
			load_keyvalues();
			showhide();
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
		
		private void showhide() {				
			param.setHidden(true);
			type.setHidden(true);
			if(service.getValue() != null) {
				param.setHidden(false);
				type.setHidden(false);
			}
			
			//hide everything by default
			group_a.setRequired(false);
			group_a.setHidden(true);
			group_b.setRequired(false);
			group_b.setHidden(true);
			
			if(service.getValue() != null && type.getValue() != null) {
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
			
			remove.render(out);
			config.render(out);
			out.write("<hr>");

			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				service.render(out);
			out.write("</div>");
			out.write("<div class=\"span7\">");
				name.render(out);
			out.write("</div>");
			out.write("</div>");

			//vo / service / params
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				param.render(out);
			out.write("</div>");
			out.write("<div class=\"span7\">");
				//empty?
			out.write("</div>");
			out.write("</div>");

			//mesh and groups
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				type.render(out);
			out.write("</div>");
			out.write("<div class=\"span7\">");
				group_a.render(out);
			out.write("</div>");
			out.write("</div>");//row-fluid
			
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				//empty
			out.write("</div>");
			out.write("<div class=\"span7\">");
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

		public void load_keyvalues() {
			LinkedHashMap<Integer, String> config_keyvalues = new LinkedHashMap();
			for(ConfigDiv config : configsdiv.configs) {
				config_keyvalues.put(config.id, config.name.getValue());
			}	
			config.setValues(config_keyvalues);	
			
			LinkedHashMap<Integer, String> groups_keyvalues = new LinkedHashMap();
			for(GroupDiv group : groupsdiv.groups) {
				Integer service_id = group.service.getValue();
				if(service_id != null && service_id.equals(service.getValue())) {
					groups_keyvalues.put(group.id, group.name.getValue());
				}
			}	
			group_a.setValues(groups_keyvalues);	
			group_b.setValues(groups_keyvalues);	
			
			LinkedHashMap<Integer, String> param_keyvalues = new LinkedHashMap();
			for(ParamDiv param : paramsdiv.params) {
				Integer service_id = param.service.getValue();
				if(service_id != null && service_id.equals(service.getValue())) {
					param_keyvalues.put(param.id, param.name.getValue());
				}
			}	
			param.setValues(param_keyvalues);	
		}

		public void save() {
			// TODO Auto-generated method stub
			
		}

		public MeshConfigTestRecord getRecord() {
			//convert type id to type string.
			String type_string = null;
			for(Integer type_id : mesh_types.keySet()) {
				if(type_id.equals(type.getValue())) {
					type_string = mesh_types.get(type_id);
				}
			}
			
			MeshConfigTestRecord rec = new MeshConfigTestRecord();
			rec.id = id;
			rec.mesh_config_id = config.getValue();
			rec.service_id = service.getValue();
			rec.name = name.getValue();
			rec.param_id = param.getValue();
			rec.type = type_string;
			rec.groupa_id = group_a.getValue();
			rec.groupb_id = group_b.getValue();
			rec.disable = disable.getValue();
			return rec;
		}		
	}
	
	class ConfigDiv extends DivRepFormElement {
		
		private HashMap<Integer, ContactEditor> contact_editors = new HashMap();

		Integer id;
		DivRepTextBox name;
		DivRepTextBox desc;
		DivRepCheckBox disable;
		DivRepButton remove;
		
		private boolean isUsed() {
			for(TestDiv test : testsdiv.tests) {
				Integer config_id = test.config.getValue();
				if(config_id != null && config_id.equals(id)) {
					return true;
				}
			}
			return false;
		}

		protected ConfigDiv(DivRep parent, MeshConfigRecord rec) {
			super(parent);
					
			name = new DivRepTextBox(this);
			name.setLabel("Name");
			name.setSampleValue("us-atlas");
			name.setRequired(true);
			name.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					testsdiv.load_keyvalues();
					testsdiv.redraw();
				}
			});
			
			desc = new DivRepTextBox(this);
			desc.setLabel("Description");
			desc.setSampleValue("USATLAS Mesh Config");
			desc.setRequired(true);
			desc.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					testsdiv.load_keyvalues();
					testsdiv.redraw();
				}
			});
			
			disable = new DivRepCheckBox(this);
			disable.setLabel("Disable");
			
			remove = new DivRepButton(this, "images/delete.png") {
				@Override
				protected void onClick(DivRepEvent e) {
					if(isUsed()) {
						alert("This configuration is currently used by 1 or more tests. Please unassociated from all test before removing.");
					} else {
						//divrep keeps track of its children, and DivrepFormElement validates all children even if I remove it here.
						//instead of overriding validate(), I am trying this new approach by hiding the element.
						ConfigDiv.this.setHidden(true); 
						
						configsdiv.configs.remove(ConfigDiv.this);
						configsdiv.redraw();
						
						testsdiv.load_keyvalues();
						testsdiv.redraw();
					}
				}	
			};
			remove.addClass("pull-right");
			remove.setStyle(DivRepButton.Style.IMAGE);
			
			if(rec != null) {
				id = rec.id;
				name.setValue(rec.name);
				disable.setValue(rec.disable);
				desc.setValue(rec.desc);
			} else {
				//come up with a new ID
				Integer nextid = 0;
				for(ConfigDiv div : configsdiv.configs) {
					if(nextid <= div.id) {
						nextid = div.id+1;
					}
				}
				id = nextid;
			}
			
			//contact editors
			try {
				Authorization auth = context.getAuthorization();
				HashMap<Integer/*contact_type_id*/, ArrayList<MeshConfigContactRecord>> voclist_grouped = null;
				if(rec != null) {
					MeshConfigContactModel vocmodel = new MeshConfigContactModel(context);
					ArrayList<MeshConfigContactRecord> contactlist = vocmodel.getByMeshConfigID(rec.id);
					voclist_grouped = vocmodel.groupByContactTypeID(contactlist);
				} else {
					//set user's contact as submitter
					voclist_grouped = new HashMap<Integer, ArrayList<MeshConfigContactRecord>>();
	
					ArrayList<MeshConfigContactRecord> submitter_list = new ArrayList<MeshConfigContactRecord>();
					MeshConfigContactRecord submitter = new MeshConfigContactRecord();
					submitter.contact_id = auth.getContact().id;
					submitter.contact_rank_id = 1;//primary
					submitter.contact_type_id = 1;//submitter
					submitter_list.add(submitter);
					voclist_grouped.put(1/*submitter*/, submitter_list);
					
					ArrayList<MeshConfigContactRecord> admin_contact_list = new ArrayList<MeshConfigContactRecord>();
					MeshConfigContactRecord primary_admin = new MeshConfigContactRecord();
					primary_admin.contact_id = auth.getContact().id;
					primary_admin.contact_rank_id = 1;//primary
					primary_admin.contact_type_id = 3;//admin
					admin_contact_list.add(primary_admin);
					voclist_grouped.put(3/*admin*/, admin_contact_list);
				}
				//set required flags
				ContactTypeModel ctmodel = new ContactTypeModel(context);
				for(ContactTypeRecord.Info contact_type : ContactTypes) {
					ContactTypeRecord trec = ctmodel.get(contact_type.id);
					ContactEditor editor = createContactEditor(voclist_grouped, trec);
					switch(contact_type.id) {
					case 1://submitter
						if(!auth.allows("admin")) {
							editor.setDisabled(true);
						}
						editor.setMinContacts(ContactRank.Primary, 1); //required
						break;
					case 3://admin
						editor.setMinContacts(ContactRank.Primary, 1); //required
						break;
					}
					contact_editors.put(contact_type.id, editor);
			
				}
			} catch (SQLException e1) {
				log.error("Failed to construct contact editor inside mesh config editor");
			}
		}
		
		private ContactEditor createContactEditor(HashMap<Integer, ArrayList<MeshConfigContactRecord>> voclist, ContactTypeRecord ctrec) throws SQLException
		{
			ContactModel pmodel = new ContactModel(context);		
			ContactEditor editor = new ContactEditor(this, pmodel, ctrec.allow_secondary, ctrec.allow_tertiary);
			
			//if provided, populate currently selected contacts
			if(voclist != null) {
				ArrayList<MeshConfigContactRecord> clist = voclist.get(ctrec.id);
				if(clist != null) {
					for(MeshConfigContactRecord rec : clist) {
						ContactRecord keyrec = new ContactRecord();
						keyrec.id = rec.contact_id;
						ContactRecord person = pmodel.get(keyrec);
						editor.addSelected(person, rec.contact_rank_id);
					}
				}
			}
			return editor;
		}
	
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well\">");

			remove.render(out);
			name.render(out);
			desc.render(out);
			
			out.write("<h2>Contact Information</h2>");
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			for(Integer type_id : contact_editors.keySet()) 
			{
				//look up contact type label
				try {
					ContactTypeRecord trec = ctmodel.get(type_id);
					out.write("<h3>"+trec.name+"</h3>");
				} catch (SQLException e) {
					log.error("failed to find contact type");
				}
				ContactEditor editor = contact_editors.get(type_id);
				editor.render(out);
			}
			disable.render(out);
			out.write("</div>");
		}
		
		private ArrayList<MeshConfigContactRecord> getContactRecordsFromEditor()
		{
			ArrayList<MeshConfigContactRecord> list = new ArrayList();
			for(Integer type_id : contact_editors.keySet()) 
			{
				ContactEditor editor = contact_editors.get(type_id);
				HashMap<ContactRecord, ContactRank> contacts = editor.getContactRecords();
				for(ContactRecord contact : contacts.keySet()) {
					MeshConfigContactRecord rec = new MeshConfigContactRecord();
					ContactRank rank = contacts.get(contact);
					rec.mesh_config_id = id;
					rec.contact_id = contact.id;
					rec.contact_type_id = type_id;
					rec.contact_rank_id = rank.id;
					list.add(rec);
				}
			}
			
			return list;
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void saveContacts() throws SQLException {
			ArrayList<MeshConfigContactRecord> recs = new ArrayList<MeshConfigContactRecord>();
			for(Integer type_id : contact_editors.keySet()) 
			{
				ContactEditor editor = contact_editors.get(type_id);
				HashMap<ContactRecord, ContactRank> contacts = editor.getContactRecords();
				for(ContactRecord contact : contacts.keySet()) {
					MeshConfigContactRecord rec = new MeshConfigContactRecord();
					ContactRank rank = contacts.get(contact);
					rec.contact_id = contact.id;
					rec.contact_type_id = type_id;
					rec.contact_rank_id = rank.id;
					rec.mesh_config_id = id;
					recs.add(rec);
				}
			}
			
			MeshConfigContactModel model = new MeshConfigContactModel(context);
			model.update(model.getByMeshConfigID(id), recs);
		}

		public MeshConfigRecord getRecord() {
			MeshConfigRecord rec = new MeshConfigRecord();
			rec.id = id;
			rec.name = name.getValue();
			rec.desc = desc.getValue();
			rec.disable = disable.getValue();
			return rec;
		}
	}
	
	class ParamDiv extends DivRepFormElement {
		
		Integer id;
		DivRepTextBox name;
		DivRepTextArea params;
		DivRepSelectBox service;
		Integer previous_service_id;
		
		DivRepButton remove;
		
		private boolean isUsed() {
			for(TestDiv test : testsdiv.tests) {
				Integer param_id = test.param.getValue();
				if(param_id != null && param_id.equals(id)) {
					return true;
				}
			}
			return false;
		}
		protected ParamDiv(DivRep parent, MeshConfigParamRecord rec) {
			super(parent);
			
			name = new DivRepTextBox(this);
			name.setLabel("Parameter Set Name");
			name.setRequired(true);
			name.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					testsdiv.load_keyvalues();
					testsdiv.redraw();
				}
			});
			
			params = new DivRepTextArea(this);
			params.setLabel("Parameters");
			params.setRequired(true);
			params.setHeight(250);
			params.addValidator(new DivRepIValidator<String>() {
				@Override
				public Boolean isValid(String value) {
					try {
						//test parameters should be a valid json object
						//TODO - JSONObject is allowing broken json like "{'hi':'there'}hoge"...
						JSONObject o = new JSONObject(value);
						params.setValue(o.toString(4));
						params.redraw();
						return true;
					} catch (ParseException e) {
						/*
						//array is ok
						try {
							new JSONArray(test);
						} catch (JSONException ex) {
							return false;
						}
						*/
						return false;
					}
				}

				@Override
				public String getErrorMessage() {
					return "Syntax error. Please enter valid JSON";
				}
				
			});
			
			service = new DivRepSelectBox(this);
			service.setLabel("Service Type");
			service.setRequired(true);
			service.setValues(service_types);
			service.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					//make sure this param is not already used by any tests
					if(isUsed()) {
						alert("This parameter set is currently used by 1 or more tests. Please unassociated this parameter set from all test before making this change.");
						service.setValue(previous_service_id);
						validate(); //need to revalidate to get rid of "this is a required field"
						service.redraw();
						return;
					}
					previous_service_id = service.getValue();

					loadTemplate();
					
					testsdiv.load_keyvalues();
					testsdiv.redraw();
					showhide();
				}	
			});
		
			remove = new DivRepButton(this, "images/delete.png") {
				@Override
				protected void onClick(DivRepEvent e) {
					if(isUsed()) {
						alert("This parameter set is currently used by 1 or more tests. Please unassociated from all test before removing.");
					} else {
						//divrep keeps track of its children, and DivrepFormElement validates all children even if I remove it here.
						//instead of overriding validate(), I am trying this new approach by hiding the element.
						ParamDiv.this.setHidden(true); 
						
						paramsdiv.params.remove(ParamDiv.this);
						paramsdiv.redraw();
						
						testsdiv.load_keyvalues();
						testsdiv.redraw();
					}
				}	
			};
			remove.addClass("pull-right");
			remove.setStyle(DivRepButton.Style.IMAGE);
			
			if(rec != null) {
				id = rec.id;
				name.setValue(rec.name);
				params.setValue(rec.params);
				service.setValue(rec.service_id);
				previous_service_id = rec.service_id;
			} else {
				//come up with a new ID (is this safe?)
				Integer nextid = 0;
				for(ParamDiv div : paramsdiv.params) {
					if(nextid <= div.id) {
						nextid = div.id+1;
					}
				}
				id = nextid;
			}
			showhide();
		}

		private void showhide() {
			params.setHidden(true);
			if(service.getValue() != null) {
				params.setHidden(false);
			}
		}
		
		private void loadTemplate() {
			ConfigModel config = new ConfigModel(context);
			String key = null;
			Integer service_id = service.getValue();
			if(service_id != null) {
				switch(service_id) {
				case 130: //net.perfSONAR.Bandwidth
					key = "meshconfig.default.params.net.perfSONAR.Bandwidth";
					break;
				case 131: //net.perfSONAR.Latency
					key = "meshconfig.default.params.net.perfSONAR.Latency";
					break;
				case 137: //net.perfSONAR.Traceroute
					key = "meshconfig.default.params.net.perfSONAR.Traceroute";
					break;
				}
			}
			String template = "";
			if(key != null) {
				Config conf = config.new Config(config, key, "{\"na\":\"update me\"}");
				template = conf.getString();
			}
			params.setValue(template);
			params.redraw();
		}
		
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
			
			//vo / service / params
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				service.render(out);
			out.write("</div>");
			out.write("<div class=\"span6\">");
				name.render(out);
			out.write("</div>");
			out.write("<div class=\"span1\">");
				remove.render(out);
			out.write("</div>");
			out.write("</div>");
			
			params.render(out);
			out.write("</div>");
		}
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		public MeshConfigParamRecord getRecord() {
			MeshConfigParamRecord rec = new MeshConfigParamRecord();
			rec.id = id;
			rec.name = name.getValue();
			rec.service_id = service.getValue();
			rec.params = params.getValue();
			return rec;
		}
	}
	
	class ParamsDiv extends DivRepFormElement {
		DivRepButton add;

		ArrayList<ParamDiv> params = new ArrayList<ParamDiv>();
		protected ParamsDiv(DivRep parent) {
			super(parent);
			
			add = new DivRepButton(this, "Add New Parameter Set") {
				protected void onClick(DivRepEvent e) {
					params.add(new ParamDiv(ParamsDiv.this, null));
					ParamsDiv.this.redraw();
				}				
			};
			add.addClass("btn");	
			
			//loading params
			MeshConfigParamModel model = new MeshConfigParamModel(context);
			try {
				for(MeshConfigParamRecord rec : model.getAll()) {
					ParamDiv div = new ParamDiv(this, rec);
					params.add(div);
				}
			} catch (SQLException e) {
				log.error("failed to load meshconfig tests", e);
			}
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span9\">");

			for(ParamDiv div: params) {
				div.render(out);
			}
			out.write("<p class=\"pull-right\">");
			add.render(out);
			out.write("</p>");
					
			out.write("</div>");
			
			out.write("<div class=\"span3\">");
			out.write("<p>");
			add.render(out);
			out.write("</p>");
			//HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
			//view.render(out);
			out.write("</div>");
			
			out.write("</div>"); //row-fluid
			
			out.write("</div>");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void save() throws SQLException {
			MeshConfigParamModel model = new MeshConfigParamModel(context);
			ArrayList<MeshConfigParamRecord> recs = new ArrayList<MeshConfigParamRecord>();
			for(ParamDiv div : params) {
				recs.add(div.getRecord());
			}
			model.update(model.getAll(), recs);
		}
	}

	class ConfigsDiv extends DivRepFormElement {
		DivRepButton add;
		ArrayList<ConfigDiv> configs = new ArrayList<ConfigDiv>();
		
		protected ConfigsDiv(DivRep parent) {
			super(parent);
			
			add = new DivRepButton(this, "Add New Configuration File") {
				protected void onClick(DivRepEvent e) {
					configs.add(new ConfigDiv(ConfigsDiv.this, null));
					ConfigsDiv.this.redraw();
				}				
			};
			add.addClass("btn");

			//loading configs
			MeshConfigModel model = new MeshConfigModel(context);
			try {
				for(MeshConfigRecord rec : model.getAll()) {
					ConfigDiv div = new ConfigDiv(this, rec);
					configs.add(div);
				}
			} catch (SQLException e) {
				log.error("failed to load meshconfig", e);
			}
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span9\">");

			for(ConfigDiv div: configs) {
				div.render(out);
			}
			out.write("<p class=\"pull-right\">");
			add.render(out);
			out.write("</p>");
			
			out.write("</div>");
			
			out.write("<div class=\"span3\">");
			out.write("<p>");
			add.render(out);
			out.write("</p>");
			//HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
			//view.render(out);
			out.write("</div>");
			
			out.write("</div>"); //row-fluid
			
			out.write("</div>");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void save() throws SQLException {
			MeshConfigModel model = new MeshConfigModel(context);
			ArrayList<MeshConfigRecord> recs = new ArrayList<MeshConfigRecord>();
			for(ConfigDiv div : configs) {
				recs.add(div.getRecord());
				div.saveContacts();
			}
			model.update(model.getAll(), recs);
		}
	}
	
	class TestsDiv extends DivRepFormElement {
		DivRepButton add;
		ArrayList<TestDiv> tests = new ArrayList<TestDiv>();
		
		protected TestsDiv(DivRep parent) {
			super(parent);
			
			add = new DivRepButton(this, "Add New Test") {
				protected void onClick(DivRepEvent e) {
					tests.add(new TestDiv(TestsDiv.this, null));
					TestsDiv.this.redraw();
				}				
			};
			add.addClass("btn");

			//loading tests
			MeshConfigTestModel tmodel = new MeshConfigTestModel(context);
			try {
				for(MeshConfigTestRecord rec : tmodel.getAll()) {
					TestDiv tdiv = new TestDiv(this, rec);
					tests.add(tdiv);
				}
			} catch (SQLException e) {
				log.error("failed to load meshconfig tests", e);
			}
		}
		
		protected void load_keyvalues() {
			for(TestDiv test : tests) {
				test.load_keyvalues();
			}
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span9\">");

			for(TestDiv div: tests) {
				div.render(out);
			}
			out.write("<p class=\"pull-right\">");
			add.render(out);
			out.write("</p>");
			
			out.write("</div>");
			
			out.write("<div class=\"span3\">");
			out.write("<p>");
			add.render(out);
			out.write("</p>");
			HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
			view.render(out);
			out.write("</div>");
			
			out.write("</div>"); //row-fluid
			
			out.write("</div>");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void save() throws SQLException {
			MeshConfigTestModel model = new MeshConfigTestModel(context);
			ArrayList<MeshConfigTestRecord> recs = new ArrayList<MeshConfigTestRecord>();
			for(TestDiv div : tests) {
				recs.add(div.getRecord());
			}
			model.update(model.getAll(), recs);
		}
	}
	
	class GroupDiv extends DivRepFormElement {
		Integer id;
		DivRepTextBox name;
		DivRepSelectBox service;
		Integer previous_service_id;
		OIMResourceServiceListEditor oim_resources;
		WLCGResourceServiceListEditor wlcg_resources;
		
		DivRepButton remove;
		
		boolean isUsed() {
			//make sure this group is not already used by any tests
			for(TestDiv test : testsdiv.tests) {
				Integer group_a = test.group_a.getValue();
				Integer group_b = test.group_b.getValue();
				if(
					(group_a != null && group_a.equals(id)) || 
					(group_b != null && group_b.equals(id))
				) {
					return true;
				}
			}	
			return false;
		}
		
		protected GroupDiv(DivRep parent, MeshConfigGroupRecord rec) {
			super(parent);
			
			name = new DivRepTextBox(this);
			name.setLabel("Group Name");
			name.setRequired(true);
			name.setSampleValue("USATLAS Latency hosts");
			name.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					testsdiv.load_keyvalues();
					testsdiv.redraw();
				}
			});
			
			service = new DivRepSelectBox(this);
			service.setLabel("Service Type");
			service.setRequired(true);
			service.setValues(service_types);
			service.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					//make sure this group is not already used by any tests
					if(isUsed()) {
						alert("This group is currently used by 1 or more tests. Please unassociated this group from all test before making this change.");
						service.setValue(previous_service_id);
						validate(); //need to revalidate to get rid of "this is a required field"
						service.redraw();
						return;
					}
					previous_service_id = service.getValue();

					oim_resources.clear();
					wlcg_resources.clear();
					
					testsdiv.load_keyvalues();
					testsdiv.redraw();
					showhide();
				}	
			});
		
			final ResourceModel rmodel = new ResourceModel(context);
			final ResourceServiceModel smodel = new ResourceServiceModel(context);
			final ResourceServiceDetailModel dmodel = new ResourceServiceDetailModel(context);
			oim_resources = new OIMResourceServiceListEditor(this) {
				protected OIMResourceServiceListEditor.ResourceInfo getDetailByResourceID(Integer id) throws SQLException {
					OIMResourceServiceListEditor.ResourceInfo info = new  OIMResourceServiceListEditor.ResourceInfo();
					info.rec = rmodel.get(id);
					if(service.getValue() != null) {
						ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), id, "endpoint");
						if(detail != null) {
							info.detail = detail.value;
						}
					}
					return info;
				}
				protected Collection<OIMResourceServiceListEditor.ResourceInfo> getAvailableResourceRecords() throws SQLException {
					ArrayList<OIMResourceServiceListEditor.ResourceInfo> recs = new ArrayList<OIMResourceServiceListEditor.ResourceInfo>();
					//find all resource/service record for currently selected service
					ArrayList<ResourceServiceRecord> rsrecs = smodel.getByServiceID(service.getValue());
					//lookup all resource record for each resource I found
					for(ResourceServiceRecord rsrec : rsrecs) {
						ResourceRecord rec = rmodel.get(rsrec.resource_id);
						if(rec.disable) continue;
						OIMResourceServiceListEditor.ResourceInfo info = new OIMResourceServiceListEditor.ResourceInfo();
						info.rec = rec;
						ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), rec.id, "endpoint");
						if(detail != null) {
							info.detail = detail.value;
						}
						recs.add(info);
					}
					return recs;
				}
			};
			oim_resources.setLabel("OIM Resources");
			//oim_resources.setRequired(true);
			
			final WLCGSiteModel wsmodel = new WLCGSiteModel(context);
			final WLCGEndpointModel wemodel = new WLCGEndpointModel(context);
			wlcg_resources = new WLCGResourceServiceListEditor(this) {
				protected WLCGResourceServiceListEditor.ResourceInfo getDetailByEndpointKey(String key) throws SQLException {
					WLCGResourceServiceListEditor.ResourceInfo info = new  WLCGResourceServiceListEditor.ResourceInfo();
					info.rec = wemodel.get(key);
					WLCGSiteRecord srec = wsmodel.get(info.rec.site_id);
					info.detail = srec.short_name;
					return info;
				}
				protected Collection<WLCGResourceServiceListEditor.ResourceInfo> getAvailableEndpoints() throws SQLException {
					ArrayList<WLCGResourceServiceListEditor.ResourceInfo> recs = new ArrayList<WLCGResourceServiceListEditor.ResourceInfo>();
					//find all resource/service record for currently selected service
					ArrayList<WLCGEndpointRecord> rsrecs = wemodel.getByServiceID(service.getValue());
					//lookup all resource record for each resource I found
					for(WLCGEndpointRecord rsrec : rsrecs) {
						WLCGResourceServiceListEditor.ResourceInfo info = new WLCGResourceServiceListEditor.ResourceInfo();
						info.rec = rsrec;
						WLCGSiteRecord srec = wsmodel.get(info.rec.site_id);
						info.detail = srec.short_name;
						recs.add(info);
					}
					return recs;
				}
			};
			wlcg_resources.setLabel("WLCG Resources");

			remove = new DivRepButton(this, "images/delete.png") {
				@Override
				protected void onClick(DivRepEvent e) {
					if(isUsed()) {
						alert("This group is currently used by 1 or more tests. Please unassociated this group from all test before removing.");
					} else {
						//divrep keeps track of its children, and DivrepFormElement validates all children even if I remove it here.
						//instead of overriding validate(), I am trying this new approach by hiding the element.
						GroupDiv.this.setHidden(true); 
						
						groupsdiv.groups.remove(GroupDiv.this);
						groupsdiv.redraw();
						
						testsdiv.load_keyvalues();
						testsdiv.redraw();
					}
				}	
			};
			remove.addClass("pull-right");
			remove.setStyle(DivRepButton.Style.IMAGE);
			
			if(rec != null) {
				id = rec.id;
				name.setValue(rec.name);
				service.setValue(rec.service_id);
				previous_service_id = rec.service_id;

				//loading directly from DB feels yicky.. maybe do this in the testdiv ctor? 
				try {
					MeshConfigOIMMemberModel model = new MeshConfigOIMMemberModel(context);
					for(MeshConfigOIMMemberRecord mrec : model.getByGroupID(rec.id)) {
						OIMResourceServiceListEditor.ResourceInfo info = oim_resources.new ResourceInfo();
						info.rec = rmodel.get(mrec.resource_id);
						ResourceServiceDetailRecord detail = dmodel.get(service.getValue(), rec.id, "endpoint");
						if(detail != null) {
							info.detail = detail.value;
						}
						oim_resources.addSelected(info);
					}
					
					MeshConfigWLCGMemberModel wmodel = new MeshConfigWLCGMemberModel(context);
					for(MeshConfigWLCGMemberRecord mrec : wmodel.getByGroupID(rec.id)) {
						WLCGResourceServiceListEditor.ResourceInfo info = wlcg_resources.new ResourceInfo();
						info.rec = wemodel.get(mrec.primary_key);
						WLCGSiteRecord srec = wsmodel.get(info.rec.site_id);
						info.detail = srec.short_name;
						wlcg_resources.addSelected(info);
					}
				} catch (SQLException e1) {
					log.error("Failed to load resource info");
				}
			} else {
				//come up with a new ID
				Integer nextid = 0;
				for(GroupDiv div : groupsdiv.groups) {
					if(nextid <= div.id) {
						nextid = div.id+1;
					}
				}
				id = nextid;
			}
			
			showhide();
		}
		
		protected void showhide() {
			oim_resources.setHidden(true);
			wlcg_resources.setHidden(true);
			if(service.getValue() != null) {
				oim_resources.setHidden(false);
				wlcg_resources.setHidden(false);
			}
		}
		
		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
			
			//vo / service / params
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span5\">");
				service.render(out);
			out.write("</div>");
			out.write("<div class=\"span6\">");
				name.render(out);
			out.write("</div>");
			out.write("<div class=\"span1\">");
				remove.render(out);
			out.write("</div>");
			out.write("</div>");
			
			oim_resources.render(out);
			wlcg_resources.render(out);
			out.write("</div>");
		}
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void saveMembers() throws SQLException {		
			Integer service_id = service.getValue();
			ArrayList<MeshConfigOIMMemberRecord> oimrecs = oim_resources.getRecords(id, service_id);
			MeshConfigOIMMemberModel oimmodel = new MeshConfigOIMMemberModel(context);
			oimmodel.update(oimmodel.getByGroupID(id), oimrecs);
			
			ArrayList<MeshConfigWLCGMemberRecord> wlcgrecs = wlcg_resources.getRecords(id, service_id);
			MeshConfigWLCGMemberModel wlcgmodel = new MeshConfigWLCGMemberModel(context);
			wlcgmodel.update(wlcgmodel.getByGroupID(id), wlcgrecs);
		}

		public MeshConfigGroupRecord getGroupRecord() {
			MeshConfigGroupRecord rec = new MeshConfigGroupRecord();
			rec.id = id;
			rec.name = name.getValue();
			rec.service_id = service.getValue();
			return rec;
		}
	}
	
	class GroupsDiv extends DivRepFormElement {
		DivRepButton add;
		ArrayList<GroupDiv> groups = new ArrayList<GroupDiv>();

		protected GroupsDiv(DivRep parent) {
			super(parent);
			
			//<i class=\"icon-plus-sign\"></i> 
			add = new DivRepButton(this, "Add New Group") {
				protected void onClick(DivRepEvent e) {
					groups.add(new GroupDiv(GroupsDiv.this, null));
					GroupsDiv.this.redraw();
				}				
			};
			add.addClass("btn");
			
			//load groups from DB
			MeshConfigGroupModel gmodel = new MeshConfigGroupModel(context);
			try {
				for(MeshConfigGroupRecord rec : gmodel.getAll()) {
					GroupDiv tdiv = new GroupDiv(this, rec);
					groups.add(tdiv);
				}
			} catch (SQLException e) {
				log.error("failed to load meshconfig tests", e);
			}
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span9\">");				
			for(GroupDiv div: groups) {
				div.render(out);
			}
			out.write("<p class=\"pull-right\">");
			add.render(out);
			out.write("</p>");
			
			out.write("</div>");
			
			out.write("<div class=\"span3\">");
			out.write("<p>");
			add.render(out);
			out.write("</p>");
			//HtmlFileView view = new HtmlFileView(getClass().getResourceAsStream("meshtype.html"));
			//view.render(out);
			out.write("</div>");
			
			out.write("</div>"); //row-fluid
			out.write("</div>");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void save() throws SQLException {
			MeshConfigGroupModel model = new MeshConfigGroupModel(context);
			ArrayList<MeshConfigGroupRecord> recs = new ArrayList<MeshConfigGroupRecord>();
			for(GroupDiv div : groups) {
				recs.add(div.getGroupRecord());
				div.saveMembers();
			}
			model.update(model.getAll(), recs);
		}
	}
	
	public MeshConfigFormDE(UserContext context, String origin_url) throws AuthorizationException, SQLException
	{	
		super(context.getPageRoot(), origin_url);
		this.context = context;
		
		//load service_types
		try {
			ServiceModel smodel = new ServiceModel(context);
			ArrayList<ServiceRecord> srecs = smodel.getByServiceGroupID(SERVICE_GROUP_PERFSONAR_MONIOTIRNG);
			Collections.sort(srecs, new Comparator<ServiceRecord> () {
				public int compare(ServiceRecord a, ServiceRecord b) {
					return a.name.compareToIgnoreCase(b.name);
				}
			});
			for(ServiceRecord srec : srecs) {
				//if(vo_rec.disable) continue;
				service_types.put(srec.id, srec.name);
			}
		} catch (SQLException e) {
			log.error("Failed to load perfsonar service records");
		}	
		
		new DivRepStaticContent(this,"<h2>Mesh Config Administrator</h2>");
		
		class TabContent extends DivRepFormElement {
			
			protected TabContent(DivRep parent) {
				super(parent);
				groupsdiv = new GroupsDiv(this);
				paramsdiv = new ParamsDiv(this);
				configsdiv = new ConfigsDiv(this);
				testsdiv = new TestsDiv(this);
			}
			
			@Override
			public void render(PrintWriter out) {
			
				BootTabView tabview = new BootTabView();
				tabview.addtab("Host Groups", renderGroupPane());
				tabview.addtab("Parameter Sets", renderParamPane());
				tabview.addtab("Configs", renderConfigPane());
				tabview.addtab("Tests", renderTestPane());
				tabview.render(out);
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}	
		};
		new TabContent(this);
	}
	
	protected IView renderTestPane() {
		return new IView(){
			public void render(PrintWriter out) {
				testsdiv.render(out);
			}
		};
	}
	protected IView renderGroupPane() {
	
		return new IView(){
			public void render(PrintWriter out) {
				groupsdiv.render(out);
			}
		};
	}
	protected IView renderParamPane() {
		return new IView(){
			public void render(PrintWriter out) {
				paramsdiv.render(out);
			}
		};
	}
	protected IView renderConfigPane() {
		return new IView(){
			public void render(PrintWriter out) {
				configsdiv.render(out);
			}
		};
	}
	
	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Boolean doSubmit() {
		try {
			Connection conn = context.getConnection();
			conn.setAutoCommit(false);
			try {
				groupsdiv.save();
				paramsdiv.save();
				configsdiv.save();
				testsdiv.save();
				conn.commit();
				return true;
			} catch (SQLException e) {
				conn.rollback();
				log.error("Failed to store mesh configs", e);
			}
		} catch (SQLException e) {
			log.error("Failed to commit or rollback");
		}
		return false;
	}
}