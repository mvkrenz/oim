
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

INSERT INTO vo_report_name_fqan (SELECT vo_report_name_id, fqan FROM oim.vo_fqan);

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
        (SELECT res.resource_id, name, description, fqdn, url, active, disable ,1 FROM oim.resource res);
UPDATE resource res SET res.resource_group_id=(SELECT resource_group_id FROM oim.resource_resource_group resResGrp WHERE resResGrp.resource_id=res.id);

INSERT INTO resource_wlcg
        (SELECT res.resource_id, resExt.interop_bdii, resExt.interop_monitoring, resExt.interop_accounting, resExt.wlcg_accounting_name, 0,0,0,0
          FROM oim.resource res
          JOIN oim.resource_ext_attributes resExt
          ON (res.resource_id=resExt.resource_id)
WHERE resExt.interop_bdii = 1 or resExt.interop_monitoring = 1 or resExt.interop_accounting = 1
         );

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

INSERT INTO authorization_type VALUES 
	(5,'Measurement and Metrics Group')
;

INSERT INTO `action` (`id`,`name`,`description`) VALUES 
 (1,'admin_authorization','Allow admin level read/write access to authorization/action matrix'),
 (2,'admin','Allow admin level read/write access to everything but authorization'),
 (5,'edit_my_vo','Allow user-level read/write access to VO for authorized users (contacts)'),
 (7, 'read_all_resource_contact','Allow read-access to all VOs contacts (for example, security group)'),
 (8, 'read_all_vo_contact', 'Allow read-access to all VO contacts (for example, security group)'),
 (9, 'read_all_contact','Allow read-access to all VO contacts (for example, security group)'),         
 (11,'edit_my_sc','Allow user-level read/write access to SCs for authorized users (contacts)'),
 (13,'edit_my_contact','Allow user-level read/write access to registered contact profiles'),
 (17,'edit_my_resource','Allow user-level read/write access to resources for authorized users (contacts)'),
 (18,'edit_measurement','Allow specific user-level read/write access to CPU Info list for authorized users (contacts)')
 ;
 
 -- CPU info
 -- resource_group
 -- vo_reporting contact?
 -- edit_my_vo_resource_ownership? probably not
 
INSERT INTO `authorization_type_action` (`authorization_type_id`,`action_id`) VALUES 
 (4,1),
 (4,2),
 (1,5),
 (4,5),
 (3,7),
 (4,7),
 (3,8),
 (4,8),
 (3,9),
 (4,9),
 (1,11),
 (4,11),
 (1,13),
 (4,13),
 (1,17),
 (4,17),
 (4,18),
 (5,18)
;
 
INSERT INTO `notification` (`notification`, `dn_id`) VALUES
("<Notification><Class>edu.iu.grid.oim.notification.VONotification</Class><VOID>1</VOID><ContactID>238</ContactID></Notification>", 123);
