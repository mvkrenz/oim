use oim;

drop table if exists mesh_config_group;
CREATE TABLE `mesh_config_group` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`service_id` int(11) NOT NULL comment 'service_type either 130 or 131', 
PRIMARY KEY (`id`),
UNIQUE KEY `name` (`name`),
CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
);

drop table if exists mesh_config_oim_member;
CREATE TABLE `mesh_config_oim_member` (
`group_id` int(11) NOT NULL,
`resource_id` int(11) NOT NULL,
`service_id` int(11) NOT NULL comment 'this should always match service_id of the group, but exists to allow cascade delete of service',
PRIMARY KEY (`group_id`, `resource_id`,`service_id`),
CONSTRAINT `c_group_id` FOREIGN KEY (`group_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_resource_id` FOREIGN KEY (`resource_id`, `service_id`) REFERENCES `resource_service` (`resource_id`, `service_id`) ON DELETE CASCADE
);

drop table if exists mesh_config_wlcg_member;
CREATE TABLE `mesh_config_wlcg_member` (
`group_id` int(11) NOT NULL,
`primary_key` varchar(255) NOT NULL comment 'primary key for wlcg_endpoint',
`service_id` int(11) NOT NULL comment 'this should always match service_id of the group, but exists to allow cascade delete of service',
PRIMARY KEY (`group_id`, `primary_key`,`service_id`),
CONSTRAINT `c_group_id` FOREIGN KEY (`group_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_endpoint` FOREIGN KEY (`primary_key`, `service_id`) REFERENCES `wlcg_endpoint` (`primary_key`, `service_id`) ON DELETE CASCADE
);

drop table if exists mesh_config_param;
CREATE TABLE `mesh_config_param` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`service_id` int(11) NOT NULL,
`params` TEXT NOT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
);

drop table if exists mesh_config_test;
CREATE TABLE `mesh_config_test` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`mesh_config_id` int(11) NOT NULL,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`disable` tinyint(1) NOT NULL DEFAULT '0',
`service_id` int(11) NOT NULL,
`type` enum('DISJOINT','MESH','STAR'),
`groupa_id` int(11) NOT NULL,
`groupb_id` int(11) DEFAULT NULL,
`param_id` int(11) NOT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `c_mesh_config_id` FOREIGN KEY (`mesh_config_id`) REFERENCES `mesh_config` (`id`),
CONSTRAINT `c_groupa_id` FOREIGN KEY (`groupa_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_groupb_id` FOREIGN KEY (`groupb_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_params_id` FOREIGN KEY (`param_id`) REFERENCES `mesh_config_param` (`id`),
CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
);

DROP TABLE IF EXISTS mesh_config_contact;
CREATE TABLE `mesh_config_contact` (
  `contact_id` int(11) NOT NULL,
  `mesh_config_id` int(11) NOT NULL,
  `contact_type_id` int(11) NOT NULL,
  `contact_rank_id` int(11) NOT NULL,
  PRIMARY KEY (`contact_id`,`mesh_config_id`, `contact_type_id`, `contact_rank_id`),
  CONSTRAINT `contact_mct_contact` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`id`),
  CONSTRAINT `mct_contact` FOREIGN KEY (`mesh_config_id`) REFERENCES `mesh_config` (`id`),
  CONSTRAINT `contact_type_mct_contact` FOREIGN KEY (`contact_type_id`) REFERENCES `contact_type` (`id`),
  CONSTRAINT `contact_rank_mct_contact` FOREIGN KEY (`contact_rank_id`) REFERENCES `contact_rank` (`id`)
);

DROP TABLE IF EXISTS mesh_config;
CREATE TABLE `mesh_config` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`desc` text,
`disable` tinyint(1) NOT NULL DEFAULT '0',
PRIMARY KEY (`id`)
);

DROP table IF EXISTS wlcg_endpoint;
DROP TABLE IF EXISTS wlcg_site;

CREATE TABLE `wlcg_site` (
`primary_key` varchar(255) NOT NULL,
`short_name` varchar(255),
`official_name` text,
`longitude` double,
`latitude` double,
`country` varchar(255),
`timezone` varchar(255),
`contact_email` varchar(128),
    PRIMARY KEY (`primary_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `wlcg_endpoint` (
`primary_key` varchar(255) NOT NULL,
`site_id` varchar(255) NOT NULL,
`hostname` varchar(255),
`host_ip` varchar(32),
`service_type` varchar(32),
`service_id` int(11),
`in_production` tinyint(1),
`roc_name` varchar(64),
`contact_email` varchar(128),
    PRIMARY KEY (`primary_key`),
    CONSTRAINT `c_site_id` FOREIGN KEY (`site_id`) REFERENCES `wlcg_site` (`primary_key`) ON DELETE CASCADE,
    CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


