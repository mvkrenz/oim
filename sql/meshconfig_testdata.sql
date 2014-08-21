use oim;

truncate mesh_config_test;
truncate mesh_config_param;
truncate mesh_config_oim_member;
truncate mesh_config_group;
truncate mesh_config_contact;
truncate mesh_config;

/*service 
    130: net.perfSONAR.Bandwidth 
    131: net.perfSONAR.Latency
*/
insert into mesh_config (name, `desc`) values('tests-us-atlas','Some description to go inside the JSON');
insert into mesh_config (name, `desc`) values('tests-us-cms','Some description to go inside the JSON');

insert into mesh_config_group (name, service_id) values('USATLAS Bandwidth instances', 130);
insert into mesh_config_group (name, service_id) values('USATLAS Latency instances', 131);
insert into mesh_config_group (name, service_id) values('USCMS Bandwidth instances', 130);
insert into mesh_config_group (name, service_id) values('USCMS Latency instances', 131);

insert into mesh_config_oim_member (group_id, resource_id, service_id) values(1, 500, 131);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(1, 502, 131);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(1, 505, 131);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(1, 507, 131);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(1, 509, 131);

insert into mesh_config_oim_member (group_id, resource_id, service_id) values(2, 499, 130);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(2, 501, 130);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(2, 504, 130);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(2, 506, 130);
insert into mesh_config_oim_member (group_id, resource_id, service_id) values(2, 508, 130);

insert into mesh_config_param (name, params, service_id) values('Default BWCTL Params', '{
"force_bidirectional" : "1",
"protocol" : "tcp",
"tool" : "bwctl/iperf",
"duration" : "30",
"type" : "perfsonarbuoy/bwctl",
"interval" : "21600"
}', 130);
insert into mesh_config_param (name, params, service_id) values('Default OWAMP Params', '{
"force_bidirectional" : "1",
"loss_threshold" : "10",
"bucket_width" : "0.001",
"packet_padding" : "0",
"sample_count" : "300",
"packet_interval" : "0.1",
"type" : "perfsonarbuoy/owamp",
"session_count" : "18000"
}', 131);


insert into mesh_config_test (name, mesh_config_id, service_id, `type`, groupa_id, groupb_id, param_id) values('US ATLAS Cloud BWCTL Mesh Test', 1, 130, 'MESH', 1, null, 1);
insert into mesh_config_test (name, mesh_config_id, service_id, `type`, groupa_id, groupb_id, param_id) values('US ATLAS Cloud Latency Mesh Test', 1, 131, 'MESH', 2, null, 2);

delete from config where `key` like 'meshconfig.default.params.%';
insert into config values ('meshconfig.default.params.net.perfSONAR.Bandwidth', '{
"force_bidirectional" : "1",
"protocol" : "tcp",
"tool" : "bwctl/iperf",
"duration" : "30",
"type" : "perfsonarbuoy/bwctl",
"interval" : "21600"
}');

insert into config values ('meshconfig.default.params.net.perfSONAR.Latency', '{
"force_bidirectional" : "1",
"loss_threshold" : "10",
"bucket_width" : "0.001",
"packet_padding" : "0",
"sample_count" : "300",
"packet_interval" : "0.1",
"type" : "perfsonarbuoy/owamp",
"session_count" : "18000"
}');

insert into mesh_config_contact (contact_id, mesh_config_id, contact_type_id, contact_rank_id) values(238, 1, 1, 1);
insert into mesh_config_contact (contact_id, mesh_config_id, contact_type_id, contact_rank_id) values(238, 1, 3, 1);
insert into mesh_config_contact (contact_id, mesh_config_id, contact_type_id, contact_rank_id) values(238, 2, 1, 1);
insert into mesh_config_contact (contact_id, mesh_config_id, contact_type_id, contact_rank_id) values(238, 2, 3, 1);

