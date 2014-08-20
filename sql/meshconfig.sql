use oim;

/*
drop table if exists mesh_config;
CREATE TABLE `mesh_config` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`description` text COLLATE utf8_unicode_ci,
`vo_id` int(11) NOT NULL,
`disable` tinyint(1) NOT NULL DEFAULT '0',
PRIMARY KEY (`id`),
UNIQUE KEY `name` (`name`),
CONSTRAINT `c_vo_id` FOREIGN KEY (`vo_id`) REFERENCES `vo` (`id`)
);
*/

drop table if exists mesh_config_group;
CREATE TABLE `mesh_config_group` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`service_id` int(11) NOT NULL comment 'service_type either 130 or 131', 
PRIMARY KEY (`id`),
UNIQUE KEY `name` (`name`),
CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
);

drop table if exists mesh_config_member;
CREATE TABLE `mesh_config_member` (
`group_id` int(11) NOT NULL,
`resource_id` int(11) NOT NULL,
`service_id` int(11) NOT NULL comment 'this should always match service_id of the group, but exists to allow cascade delete of service',
PRIMARY KEY (`group_id`, `resource_id`,`service_id`),
CONSTRAINT `c_group_id` FOREIGN KEY (`group_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_resource_id` FOREIGN KEY (`resource_id`, `service_id`) REFERENCES `resource_service` (`resource_id`, `service_id`) ON DELETE CASCADE
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
`name` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`vo_id` int(11) NOT NULL,
`disable` tinyint(1) NOT NULL DEFAULT '0',
`service_id` int(11) NOT NULL,
`type` enum('DISJOINT','MESH','STAR'),
`groupa_id` int(11) NOT NULL,
`groupb_id` int(11) DEFAULT NULL,
`param_id` int(11) NOT NULL,
PRIMARY KEY (`id`),
CONSTRAINT `c_vo_id` FOREIGN KEY (`vo_id`) REFERENCES `vo` (`id`),
CONSTRAINT `c_groupa_id` FOREIGN KEY (`groupa_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_groupb_id` FOREIGN KEY (`groupb_id`) REFERENCES `mesh_config_group` (`id`),
CONSTRAINT `c_params_id` FOREIGN KEY (`param_id`) REFERENCES `mesh_config_param` (`id`),
CONSTRAINT `c_service_id` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
);

