
USE oim2;

INSERT INTO authorization_type (SELECT auth_type_id,description FROM oim.authorization_type);

INSERT INTO contact_type (SELECT type_id,description,max_no_contacts,require_dn FROM oim.contact_type);

INSERT INTO contact_rank (SELECT rank_id, description,max_no_contacts FROM oim.contact_rank);

INSERT INTO downtime_action (SELECT downtime_action_id,downtime_action_name,downtime_action_desc FROM oim.downtime_action);

INSERT INTO downtime_class (SELECT downtime_class_id,downtime_class_name,downtime_class_desc FROM oim.downtime_class);

INSERT INTO downtime_severity (SELECT downtime_severity_id,downtime_severity_name,downtime_severity_wlcg_name,downtime_severity_desc FROM oim.downtime_severity);

INSERT INTO metric (SELECT metric_id,name,common_name,abbrev,description,time_interval,fresh_for,help_url,wlcg_metric_type FROM oim.metric);

INSERT INTO metric_status (SELECT metric_status_id,metric_status_description FROM oim.metric_status);

INSERT INTO osg_grid_type (SELECT * FROM oim.osg_grid_type);

INSERT INTO service_group (SELECT service_group_id, name, description FROM oim.service_group);

INSERT INTO service (SELECT service_id,name,description,port, 1 FROM oim.service);
UPDATE service svc SET svc.service_group_id=(SELECT service_group_id FROM oim.service_service_group ssgrp WHERE ssgrp.service_id=svc.id);

INSERT INTO metric_service (SELECT metric_id,service_id,critical FROM oim.metric_service);

INSERT INTO field_of_science (SELECT id,name FROM oim.field_of_science);

INSERT INTO facility (SELECT facility_id,name,description,active,disable FROM oim.facility);

INSERT INTO supportcenter (SELECT sc_id,short_name,long_name,description,community,active,disable,footprints_id FROM oim.supportcenter);

INSERT INTO virtualorganization 
	(SELECT vo_id,short_name,long_name,description,
	primary_url,aup_url,membership_services_url,purpose_url,
	support_url,app_description,community,sc_id,
	active,disable,footprints_id 
	FROM oim.virtualorganization);

INSERT INTO vo_vo (child_id, parent_id)
	(SELECT vo_id, parent_vo_id
	FROM oim.virtualorganization 
	WHERE parent_vo_id IS NOT NULL);


INSERT INTO vo_field_of_science (SELECT vo_id,science_id FROM oim.vo_field_of_science);

INSERT INTO vo_report_name (SELECT id,reporting_name,vo_id FROM oim.vo_report_name);

INSERT INTO vo_fqan (SELECT id,fqan,vo_report_name_id FROM oim.vo_fqan);

-- Insert person records without submiter_dn for now - will update later
INSERT INTO person 
	(id, first_name, middle_name, last_name, primary_email, secondary_email, 
	primary_phone, primary_phone_ext, secondary_phone, secondary_phone_ext, 
	address_line_1, address_line_2, city, state, zipcode, country,
	active, disable) 
	(SELECT person_id, first_name, middle_name, last_name, primary_email, secondary_email, 
	primary_phone, primary_phone_ext, secondary_phone, secondary_phone_ext, 
	address_line_1, address_line_2, city, state, zipcode, cntry.name,
	active, disable 
	FROM oim.person pers 
	LEFT JOIN oim.country cntry ON (cntry.ccode=pers.ccode)
	WHERE pers.group_contact=0);

-- Insert group_contacts into mailing_list
INSERT INTO mailing_list
	(id, name, email)
	(SELECT person_id, CONCAT(first_name, " ", last_name), primary_email
	FROM oim.person pers 
	WHERE pers.group_contact=1);

INSERT INTO certificate_dn (id,dn_string,person_id) (SELECT dn_id, dn_string, person_id FROM oim.certificate_dn);
-- UPDATE person SET optional_submitter_dn_id=(SELECT optional_submitter_dn_id FROM oim.person oldPerson WHERE oldPerson.person_id=person.id);

INSERT INTO site (SELECT site_id,site.name,long_name,description,address_line_1,address_line_2,city,state,zipcode, Ctry.name, longitude,latitude,sc_id,facility_id,submitter_dn_id,active,disable FROM oim.site JOIN oim.country Ctry ON (Ctry.ccode=site.ccode));

INSERT INTO resource_group (SELECT resource_group_id,name,description,site_id,osg_grid_type_id,active,disable FROM oim.resource_group);


INSERT INTO resource 
	(SELECT res.resource_id, name, description, fqdn, url, resExt.interop_bdii, 
		resExt.interop_monitoring, resExt.interop_accounting, 
		resExt.wlcg_accounting_name, active, disable ,1 
	FROM oim.resource res 
	LEFT JOIN oim.resource_ext_attributes resExt ON (res.resource_id=resExt.resource_id));
-- UPDATE resource res SET res.resource_group_id=(SELECT resource_group_id FROM oim.resource_resource_group resResGrp WHERE resResGrp.resource_id=res.id);

INSERT INTO vo_resource_ownership (SELECT resource_id,vo_id,percent FROM oim.vo_resource_ownership);



-- If need be
-- SELECT dn_id, COUNT(person_id) FROM oim.dn_auth_type ORDER BY COUNT(person_id);
-- DELETE FROM oim.dn_auth_type WHERE dn_id=65 AND person_id=0;
INSERT INTO dn_auth_type (SELECT dn_id, auth_type_id, active FROM oim.dn_auth_type);

INSERT INTO resource_service (SELECT service_id, resource_id, uri, FALSE, FALSE FROM oim.resource_service);
UPDATE resource_service SET central=TRUE WHERE service_id>=101 AND service_id<=106 ; -- Set central flag if service id > 100 < 107
UPDATE resource_service SET hidden=TRUE WHERE service_id=107;   -- Set hidden flag if service was hidden CE/SE before
UPDATE resource_service SET service_id=1  WHERE service_id=107; -- only hidden CEs thus far AFAIK, so setting it to CE

INSERT INTO resource_service_SE_details (SELECT resource_id,service_id,read_location,write_location,server_list_regex FROM oim.resource_service_SE_details);


INSERT INTO resource_alias (resource_id, resource_alias) (SELECT resource_id, resource_alias FROM oim.resource_alias);

INSERT INTO resource_contact (SELECT person_id,resource_id,type_id,rank_id FROM oim.resource_contact);

INSERT INTO sc_contact (SELECT person_id,sc_id,type_id,rank_id FROM oim.sc_contact);

INSERT INTO vo_contact (SELECT person_id,vo_id,type_id,rank_id FROM oim.vo_contact);

INSERT INTO vo_report_contact (SELECT person_id,vo_report_name_id FROM oim.vo_report_contact);


INSERT INTO resource_downtime (SELECT downtime_id,timestamp,start_time,end_time,downtime_summary,
	downtime_class_id,downtime_severity_id,resource_id,dn_id,disable FROM oim.resource_downtime);

INSERT INTO resource_downtime_service (SELECT downtime_id,service_id FROM oim.resource_downtime_service);

INSERT INTO downtime_publish_wlcg (SELECT downtime_id,downtime_action_id,publish_status,timestamp,disable FROM oim.downtime_publish_wlcg);
