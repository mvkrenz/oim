
USE oimnew;

INSERT INTO authorization_type (SELECT auth_type_id,description FROM oim.authorization_type);

INSERT INTO contact_type (id,name)
	(SELECT type_id,description FROM oim.contact_type);
UPDATE contact_type SET allow_secondary=1 WHERE 
	id IN (SELECT type_id FROM oim.contact_type WHERE max_no_contacts>1);
UPDATE contact_type SET allow_tertiary=1 WHERE 
	id IN (SELECT type_id FROM oim.contact_type WHERE max_no_contacts>2);

INSERT INTO contact_rank (SELECT rank_id, description FROM oim.contact_rank);

INSERT INTO downtime_action (SELECT downtime_action_id,downtime_action_name,downtime_action_desc FROM oim.downtime_action);

INSERT INTO downtime_class (SELECT downtime_class_id,downtime_class_name,downtime_class_desc FROM oim.downtime_class);

INSERT INTO downtime_severity 
	(SELECT downtime_severity_id,downtime_severity_name,downtime_severity_wlcg_name,downtime_severity_desc FROM oim.downtime_severity);

INSERT INTO metric (SELECT metric_id,name,common_name,abbrev,description,time_interval,fresh_for,help_url,wlcg_metric_type FROM oim.metric);

INSERT INTO metric_status (SELECT metric_status_id,metric_status_description FROM oim.metric_status);

INSERT INTO osg_grid_type (SELECT * FROM oim.osg_grid_type);

INSERT INTO service_group (SELECT service_group_id, name, description FROM oim.service_group);

INSERT INTO service (SELECT service_id,name,description,port, 1 FROM oim.service);
UPDATE service svc SET svc.service_group_id=(SELECT service_group_id FROM oim.service_service_group ssgrp WHERE ssgrp.service_id=svc.id);

INSERT INTO metric_service (SELECT metric_id,service_id,critical FROM oim.metric_service);

INSERT INTO field_of_science (SELECT id,name FROM oim.field_of_science);

INSERT INTO facility (SELECT facility_id,name,description,active,disable FROM oim.facility);

INSERT INTO sc (SELECT sc_id,short_name,long_name,description,community,active,disable,footprints_id FROM oim.supportcenter);

INSERT INTO vo
	(SELECT vo_id,short_name,long_name,description,
	primary_url,aup_url,membership_services_url,purpose_url,
	support_url,app_description,community,sc_id,
	active,disable,footprints_id
	FROM oim.virtualorganization);

INSERT INTO vo_vo (child_vo_id, parent_vo_id)
	(SELECT vo_id, parent_vo_id
	FROM oim.virtualorganization
	WHERE parent_vo_id IS NOT NULL);

INSERT INTO vo_field_of_science (SELECT vo_id,science_id FROM oim.vo_field_of_science);

INSERT INTO vo_report_name (SELECT id,reporting_name,vo_id FROM oim.vo_report_name);

INSERT INTO vo_fqan (SELECT id,fqan,vo_report_name_id FROM oim.vo_fqan);

-- Insert contact records without submiter_dn for now - will update later
INSERT INTO contact
	(id, name, primary_email, secondary_email,
	primary_phone, primary_phone_ext, secondary_phone, secondary_phone_ext,
	address_line_1, address_line_2, city, state, zipcode, country,
	active, disable)
	(SELECT person_id, CONCAT(first_name,' ', middle_name, ' ', last_name),
	primary_email, secondary_email,
	primary_phone, primary_phone_ext, secondary_phone, secondary_phone_ext, 
	address_line_1, address_line_2, city, state, zipcode, cntry.name,
	active, disable 
	FROM oim.person pers 
	LEFT JOIN oim.country cntry ON (cntry.ccode=pers.ccode)
);

UPDATE contact SET person=TRUE WHERE id IN 
	(SELECT person_id FROM oim.person WHERE group_contact = 0);
UPDATE contact SET person=FALSE WHERE id IN 
	(SELECT person_id FROM oim.person WHERE group_contact = 1);

-- Insert group_contacts into mailing_list
-- INSERT INTO mailing_list
--	(id, name, email)
--	(SELECT person_id, CONCAT(first_name, " ", last_name), primary_email
--	FROM oim.person pers
--	WHERE pers.group_contact=1);

INSERT INTO dn (id,dn_string,contact_id) (SELECT dn_id, dn_string, person_id FROM oim.certificate_dn);
UPDATE contact SET submitter_dn_id=(SELECT optional_submitter_dn_id FROM oim.person oldPerson WHERE oldPerson.person_id=contact.id);

INSERT INTO site 
	(SELECT site_id,site.name,long_name,description,
	address_line_1,address_line_2,city,state,zipcode, 
	Ctry.name, longitude,latitude,sc_id,facility_id,
	submitter_dn_id,active,disable 
	FROM oim.site 
	JOIN oim.country Ctry ON (Ctry.ccode=site.ccode));

INSERT INTO resource_group (SELECT resource_group_id,name,description,site_id,osg_grid_type_id,active,disable FROM oim.resource_group);

INSERT INTO resource
        (SELECT res.resource_id, name, description, fqdn, url, resExt.interop_bdii,
                resExt.interop_monitoring, resExt.interop_accounting,
                resExt.wlcg_accounting_name, active, disable ,1
        FROM oim.resource res
        LEFT JOIN oim.resource_ext_attributes resExt ON (res.resource_id=resExt.resource_id));
UPDATE resource res SET res.resource_group_id=(SELECT resource_group_id FROM oim.resource_resource_group resResGrp WHERE resResGrp.resource_id=res.id);

INSERT INTO vo_resource_ownership (SELECT resource_id,vo_id,percent FROM oim.vo_resource_ownership);

-- If need be
-- SELECT dn_id, COUNT(person_id) FROM oim.dn_auth_type GROUP BY dn_id ORDER BY COUNT(person_id);
-- DELETE FROM oim.dn_auth_type WHERE dn_id=65 AND person_id=0;
INSERT INTO dn_authorization_type (SELECT dn_id, auth_type_id FROM oim.dn_auth_type where active = 1);

INSERT INTO resource_service (service_id, resource_id, endpoint_override, hidden, central)
	(SELECT service_id, resource_id, uri, FALSE, FALSE FROM oim.resource_service);
UPDATE resource_service SET central=TRUE WHERE service_id>=101 AND service_id<=106 ; -- Set central flag if service id > 100 < 107
UPDATE resource_service SET hidden=TRUE WHERE service_id=107;   -- Set hidden flag if service was hidden CE/SE before
UPDATE resource_service SET service_id=1  WHERE service_id=107; -- only hidden CEs thus far AFAIK, so setting it to CE

UPDATE resource_service res SET server_list_regex=
	(SELECT server_list_regex FROM oim.resource_service_SE_details oimres 
	WHERE oimres.resource_id=res.resource_id AND oimres.service_id=res.service_id);

INSERT INTO resource_alias (resource_id, resource_alias) (SELECT resource_id, resource_alias FROM oim.resource_alias);

INSERT INTO resource_contact (SELECT person_id,resource_id,type_id,rank_id FROM oim.resource_contact);

INSERT INTO sc_contact (SELECT person_id,sc_id,type_id,rank_id FROM oim.sc_contact);

INSERT INTO vo_contact (SELECT person_id,vo_id,type_id,rank_id FROM oim.vo_contact);

INSERT INTO vo_report_contact (SELECT person_id,vo_report_name_id,type_id,rank_id FROM oim.vo_report_contact);

INSERT INTO resource_downtime (SELECT downtime_id,timestamp,start_time,end_time,downtime_summary,
	downtime_class_id,downtime_severity_id,resource_id,dn_id,disable FROM oim.resource_downtime);

INSERT INTO resource_downtime_service (SELECT downtime_id,service_id FROM oim.resource_downtime_service);

INSERT INTO downtime_publish_wlcg (SELECT downtime_id,downtime_action_id,publish_status,timestamp,disable FROM oim.downtime_publish_wlcg);

INSERT INTO action (id, name) values (1, "write_resource"),
(2, "admin_resource"),
(3, "write_facility"),
(4, "admin_facility"),
(5, "write_vo"),
(6, "admin_vo"),
(7, "write_sccontact"),
(8, "admin_sccontact"),
(9, "write_vocontact"),
(10, "admin_vocontact"),
(11, "write_sc"),
(12, "admin_sc"),
(13, "write_contact"),
(14, "admin_contact"),
(15, "admin_osg_grid_type"),
(16, "write_osg_grid_type");

INSERT INTO `authorization_type_action` (`authorization_type_id`,`action_id`) VALUES
 (4,1),
 (4,2),
 (4,3),
 (4,4),
 (4,5),
 (4,6),
 (4,7),
 (4,8),
 (4,9),
 (4,10),
 (4,11),
 (4,12),
 (4,13),
 (4,14),
 (4,15),
 (4,16);

