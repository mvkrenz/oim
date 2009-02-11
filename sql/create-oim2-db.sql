-- MySQL dump 10.11
--
-- Host: localhost    Database: oim2
-- ------------------------------------------------------
-- Server version	5.0.51a-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `oim2`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `oim2` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `oim2`;

--
-- Table structure for table `View_VoReportNamesFqans`
--

DROP TABLE IF EXISTS `View_VoReportNamesFqans`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `View_VoReportNamesFqans` (
  `vo_report_name_id` int(11) default NULL,
  `reporting_name` varchar(128) collate utf8_unicode_ci default NULL,
  `fqan_id` int(11) default NULL,
  `fqan` varchar(1024) collate utf8_unicode_ci default NULL,
  `vo_name` text collate utf8_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `authorization_type`
--

DROP TABLE IF EXISTS `authorization_type`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `authorization_type` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(512) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `certificate_authority`
--

DROP TABLE IF EXISTS `certificate_authority`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `certificate_authority` (
  `id` int(11) NOT NULL auto_increment,
  `file_id` varchar(256) collate utf8_unicode_ci NOT NULL COMMENT 'This will be the file name of the CA ... for  example 12f3f33.0',
  `name` varchar(256) collate utf8_unicode_ci default NULL COMMENT 'issuer name',
  `md5sum` varchar(512) collate utf8_unicode_ci default NULL,
  `active` tinyint(1) NOT NULL default '0',
  `disable` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `certificate_dn`
--

DROP TABLE IF EXISTS `certificate_dn`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `certificate_dn` (
  `id` int(11) NOT NULL auto_increment,
  `dn_string` varchar(1024) collate utf8_unicode_ci NOT NULL,
  `person_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `person_certificate_dn` (`person_id`),
  CONSTRAINT `person_certificate_dn` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `contact_rank`
--

DROP TABLE IF EXISTS `contact_rank`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `contact_rank` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(512) collate utf8_unicode_ci NOT NULL,
  `max_no_contacts` int(11) NOT NULL default '1' COMMENT 'There can be one primary and secondary contacts but unlimited tertiary contacts - use max_no_contacts field to enforce this.',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `contact_type`
--

DROP TABLE IF EXISTS `contact_type`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `contact_type` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(512) collate utf8_unicode_ci NOT NULL,
  `max_no_contacts` int(11) NOT NULL default '1' COMMENT 'This field can be used to specify how many contacts there can be of a particular type; for example, there can be a primary and a secondary operations contact but only one (primary) submitter contact. Default is 1.',
  `require_dn` tinyint(1) NOT NULL default '0' COMMENT 'This field can be used to specify that for a particular contact_type, a record in a corresponding xyz_contact entity will be required to provide a dn_id field. This can be useful if, say, we want to mandate that a submitter (of a resource, site, etc.) nee',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;


--
-- Table structure for table `dn_auth_type`
--

DROP TABLE IF EXISTS `dn_auth_type`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `dn_auth_type` (
  `dn_id` int(11) NOT NULL,
  `auth_type_id` int(11) NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY  (`dn_id`,`auth_type_id`),
  KEY `authorization_type_dn_auth_type` (`auth_type_id`),
  CONSTRAINT `authorization_type_dn_auth_type` FOREIGN KEY (`auth_type_id`) REFERENCES `authorization_type` (`id`),
  CONSTRAINT `certificate_dn_dn_auth_type` FOREIGN KEY (`dn_id`) REFERENCES `certificate_dn` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Will help enable various type of authorizations for a DN (i.';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `downtime_action`
--

DROP TABLE IF EXISTS `downtime_action`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `downtime_action` (
  `id` int(11) NOT NULL,
  `name` text collate utf8_unicode_ci NOT NULL COMMENT 'Initially, this will have something like CREATE, MODIFY, and CANCEL',
  `description` text collate utf8_unicode_ci,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `downtime_class`
--

DROP TABLE IF EXISTS `downtime_class`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `downtime_class` (
  `id` int(11) NOT NULL,
  `name` text collate utf8_unicode_ci NOT NULL COMMENT 'Initially, this will have something like SCHEDULED and UNSCHEDULED.',
  `description` text collate utf8_unicode_ci,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `downtime_publish_wlcg`
--

DROP TABLE IF EXISTS `downtime_publish_wlcg`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `downtime_publish_wlcg` (
  `downtime_id` int(11) NOT NULL,
  `downtime_action_id` int(11) NOT NULL,
  `publish_status` int(11) default '0' COMMENT 'This will be NULL initially unless set to some value. So for example, value 100 could be success; while 1,2,3, ... 99 could be the number of failed attempts made to publish.',
  `timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  `disable` tinyint(1) NOT NULL COMMENT 'Disable field to supersede value of publish_status. If this is set to TRUE, then record will never be published.',
  PRIMARY KEY  (`downtime_id`,`downtime_action_id`),
  KEY `downtime_action_downtime_publish_wlcg` (`downtime_action_id`),
  CONSTRAINT `downtime_action_downtime_publish_wlcg` FOREIGN KEY (`downtime_action_id`) REFERENCES `downtime_action` (`id`),
  CONSTRAINT `resource_downtime_downtime_publish_wlcg` FOREIGN KEY (`downtime_id`) REFERENCES `resource_downtime` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `downtime_severity`
--

DROP TABLE IF EXISTS `downtime_severity`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `downtime_severity` (
  `id` int(11) NOT NULL,
  `name` text collate utf8_unicode_ci NOT NULL COMMENT 'Initially, this will have something like At Risk, Moderate, Severe, Outage.',
  `wlcg_name` text collate utf8_unicode_ci NOT NULL COMMENT 'Initially, this will have something like At Risk, Moderate, Severe, Outage.',
  `description` text collate utf8_unicode_ci,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `facility`
--

DROP TABLE IF EXISTS `facility`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `facility` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci NOT NULL,
  `description` text collate utf8_unicode_ci,
  `active` tinyint(1) NOT NULL default '0',
  `disable` tinyint(1) NOT NULL default '0' COMMENT 'The disable field supersedes the active flag, and can be used to permanently inactivate a record. It has to be set by the programmatic interface, it''s set to false by default.',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10063 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `log` (
  `id` int(11) NOT NULL,
  `type` varchar(128) collate utf8_unicode_ci NOT NULL,
  `table` varchar(512) collate utf8_unicode_ci NOT NULL,
  `key` varchar(512) collate utf8_unicode_ci NOT NULL,
  `old_value` text collate utf8_unicode_ci,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `comment` text collate utf8_unicode_ci,
  `dn_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `certificate_dn_log` (`dn_id`),
  CONSTRAINT `log_ibfk_1` FOREIGN KEY (`dn_id`) REFERENCES `certificate_dn` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Log table to store all OIM-DB changes for auditing purposes.';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `metric`
--

DROP TABLE IF EXISTS `metric`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `metric` (
  `id` int(11) NOT NULL,
  `name` varchar(512) collate utf8_unicode_ci NOT NULL,
  `common_name` varchar(512) collate utf8_unicode_ci default NULL,
  `abbrev` varchar(512) collate utf8_unicode_ci default NULL,
  `description` text collate utf8_unicode_ci,
  `time_interval` int(11) NOT NULL default '720' COMMENT 'Will contain interval the probe is expected to run; this could be specified in minutes, or some sort of cron formatted text.',
  `fresh_for` int(11) default NULL,
  `help_url` text collate utf8_unicode_ci,
  `wlcg_metric_type` varchar(512) collate utf8_unicode_ci NOT NULL default 'status' COMMENT 'Silly field to hold on to status or performance values as defined in WLCG specs 0.91.',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `metric_service`
--

DROP TABLE IF EXISTS `metric_service`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `metric_service` (
  `metric_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `critical` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`metric_id`,`service_id`),
  KEY `service_metric_service` (`service_id`),
  CONSTRAINT `metric_metric_service` FOREIGN KEY (`metric_id`) REFERENCES `metric` (`id`),
  CONSTRAINT `service_metric_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `metric_status`
--

DROP TABLE IF EXISTS `metric_status`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `metric_status` (
  `id` int(11) NOT NULL,
  `description` varchar(512) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='<strong><u>Metric Status</u></strong>: Information about val';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `notification` (
  `id` int(11) NOT NULL auto_increment,
  `type` varchar(512) collate utf8_unicode_ci NOT NULL,
  `table` varchar(128) collate utf8_unicode_ci NOT NULL,
  `key` varchar(512) collate utf8_unicode_ci NOT NULL,
  `format` varchar(512) collate utf8_unicode_ci NOT NULL,
  `frequency` varchar(512) collate utf8_unicode_ci NOT NULL,
  `person_id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Notification table - who wants to be notified on what change';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `osg_grid_type`
--

DROP TABLE IF EXISTS `osg_grid_type`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `osg_grid_type` (
  `id` int(11) NOT NULL,
  `name` varchar(512) collate utf8_unicode_ci default NULL,
  `description` varchar(512) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='This table will likely be populated only by GOC staff (using';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `person` (
  `id` int(11) NOT NULL auto_increment,
  `first_name` text collate utf8_unicode_ci,
  `middle_name` text collate utf8_unicode_ci,
  `last_name` text collate utf8_unicode_ci,
  `primary_email` varchar(128) collate utf8_unicode_ci NOT NULL default 'foo@bar.com_CHANGE_THIS',
  `secondary_email` varchar(128) collate utf8_unicode_ci default NULL,
  `primary_phone` varchar(32) collate utf8_unicode_ci NOT NULL default '1-234-567-8900_CHANGE_THIS',
  `primary_phone_ext` varchar(32) collate utf8_unicode_ci default '1-234-567-8900_CHANGE_THIS',
  `secondary_phone` varchar(32) collate utf8_unicode_ci default NULL,
  `secondary_phone_ext` varchar(32) collate utf8_unicode_ci default NULL,
  `address_line_1` text collate utf8_unicode_ci,
  `address_line_2` text collate utf8_unicode_ci,
  `city` text collate utf8_unicode_ci,
  `state` text collate utf8_unicode_ci,
  `zipcode` text collate utf8_unicode_ci,
  `country` varchar(512) collate utf8_unicode_ci default NULL COMMENT 'Fill in using country table data in web code',
  `group_contact` tinyint(1) NOT NULL default '0' COMMENT 'The group_contact field indicates if a person entry represents a mailing list or a group phone number or some such, and it has to be set by the programmatic interface, it''s set to false by default. If set to true, then the programmatic interface has to en',
  `optional_submitter_dn_id` int(11) default NULL,
  `contact_preference` text collate utf8_unicode_ci COMMENT 'this field can be used to store snippets about person like if they want you to avoid calling them, etc.',
  `combine_vo_reports` tinyint(1) NOT NULL default '0' COMMENT 'User preference: Should VO reports be combined? false by default.',
  `combine_resource_reports` tinyint(1) NOT NULL default '0' COMMENT 'User preference: Should resource reports be combined? false by default.',
  `active` tinyint(1) NOT NULL default '0' COMMENT 'The active field has to be set by the programmatic interface, it''s set to false by default.',
  `disable` tinyint(1) NOT NULL default '0' COMMENT 'The disable field supersedes the active flag, and can be used to permanently inactivate a record. It has to be set by the programmatic interface, it''s set to false by default.',
  PRIMARY KEY  (`id`),
  KEY `certificate_dn_person` (`optional_submitter_dn_id`),
  CONSTRAINT `certificate_dn_person` FOREIGN KEY (`optional_submitter_dn_id`) REFERENCES `certificate_dn` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='<strong><u>Person</u></strong>: Information about any person';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci,
  `description` text collate utf8_unicode_ci,
  `fqdn` text collate utf8_unicode_ci NOT NULL,
  `url` text collate utf8_unicode_ci COMMENT 'Use this to store local resource URL, etc',
  `interop_bdii` tinyint(1) NOT NULL default '0',
  `interop_monitoring` tinyint(1) NOT NULL default '0',
  `interop_accounting` tinyint(1) NOT NULL default '0' COMMENT 'Should Gratia accounting information be forwarding to the WLCG accounting system? If TRUE, then wlcg_accounting_name will be required.',
  `wlcg_accounting_name` text collate utf8_unicode_ci COMMENT 'This field will hold the WLCG resource (site) name. For example IU_OSG would be part of virtual facility US-MWT2 which is part of USA which is part of Tier2 in the WLCG accounting system.   Note: REQUIRED field if interop_accounting field is set to TRUE i',
  `active` int(11) NOT NULL COMMENT 'Use this to also flag inactive resources?',
  `disable` int(11) NOT NULL default '0' COMMENT 'Use this to also flag inactive resources?',
  `resource_group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `resource_resource_group` (`resource_group_id`),
  CONSTRAINT `resource_resource_group` FOREIGN KEY (`resource_group_id`) REFERENCES `resource_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='This table does not have an active flag field because we hav';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_alias`
--

DROP TABLE IF EXISTS `resource_alias`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_alias` (
  `id` int(11) NOT NULL auto_increment,
  `resource_id` int(11) NOT NULL,
  `resource_alias` varchar(256) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `resource_resource_alias` (`resource_id`),
  CONSTRAINT `resource_resource_alias` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_contact`
--

DROP TABLE IF EXISTS `resource_contact`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_contact` (
  `person_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `rank_id` int(11) NOT NULL,
  PRIMARY KEY  (`person_id`,`resource_id`,`type_id`,`rank_id`),
  KEY `resource_resource_contact` (`resource_id`),
  KEY `contact_type_resource_contact` (`type_id`),
  KEY `contact_rank_resource_contact` (`rank_id`),
  CONSTRAINT `contact_rank_resource_contact` FOREIGN KEY (`rank_id`) REFERENCES `contact_rank` (`id`),
  CONSTRAINT `contact_type_resource_contact` FOREIGN KEY (`type_id`) REFERENCES `contact_type` (`id`),
  CONSTRAINT `person_resource_contact` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
  CONSTRAINT `resource_resource_contact` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_downtime`
--

DROP TABLE IF EXISTS `resource_downtime`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_downtime` (
  `id` int(11) NOT NULL auto_increment,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `start_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `end_time` timestamp NOT NULL default '0000-00-00 00:00:00',
  `downtime_summary` varchar(512) collate utf8_unicode_ci default NULL,
  `downtime_class_id` int(11) default NULL,
  `downtime_severity_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  `dn_id` int(11) NOT NULL,
  `disable` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `resource_resource_downtime` (`resource_id`),
  KEY `downtime_severity_resource_downtime` (`downtime_severity_id`),
  KEY `certificate_dn_resource_downtime` (`dn_id`),
  KEY `downtime_class_resource_downtime` (`downtime_class_id`),
  CONSTRAINT `certificate_dn_resource_downtime` FOREIGN KEY (`dn_id`) REFERENCES `certificate_dn` (`id`),
  CONSTRAINT `downtime_class_resource_downtime` FOREIGN KEY (`downtime_class_id`) REFERENCES `downtime_class` (`id`),
  CONSTRAINT `downtime_severity_resource_downtime` FOREIGN KEY (`downtime_severity_id`) REFERENCES `downtime_severity` (`id`),
  CONSTRAINT `resource_resource_downtime` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000136 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_downtime_service`
--

DROP TABLE IF EXISTS `resource_downtime_service`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_downtime_service` (
  `downtime_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  PRIMARY KEY  (`downtime_id`,`service_id`),
  KEY `service_resource_downtime_service` (`service_id`),
  CONSTRAINT `resource_downtime_resource_downtime_service` FOREIGN KEY (`downtime_id`) REFERENCES `resource_downtime` (`id`),
  CONSTRAINT `service_resource_downtime_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_group`
--

DROP TABLE IF EXISTS `resource_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci,
  `description` text collate utf8_unicode_ci,
  `site_id` int(11) default NULL,
  `osg_grid_type_id` int(11) default NULL,
  `active` int(11) NOT NULL COMMENT 'Use this to also flag inactive resources?',
  `disable` int(11) NOT NULL default '0' COMMENT 'Use this to also flag inactive resources?',
  PRIMARY KEY  (`id`),
  KEY `site_resource_group` (`site_id`),
  KEY `osg_grid_type_resource_group` (`osg_grid_type_id`),
  CONSTRAINT `osg_grid_type_resource_group` FOREIGN KEY (`osg_grid_type_id`) REFERENCES `osg_grid_type` (`id`),
  CONSTRAINT `site_resource_group` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='This table does not have an active flag field because we hav';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_service`
--

DROP TABLE IF EXISTS `resource_service`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_service` (
  `service_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  `uri` text collate utf8_unicode_ci COMMENT 'This field will store URIs like a web URL or an LDAP URI.',
  `hidden` tinyint(1) NOT NULL default '0',
  `central` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`service_id`,`resource_id`),
  KEY `resource_resource_service` (`resource_id`),
  CONSTRAINT `resource_resource_service` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`),
  CONSTRAINT `service_resource_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Each resource could run one or more service. For example, a ';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `resource_service_SE_details`
--

DROP TABLE IF EXISTS `resource_service_SE_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_service_SE_details` (
  `resource_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `read_location` text collate utf8_unicode_ci NOT NULL,
  `write_location` text collate utf8_unicode_ci NOT NULL,
  `server_list_regex` text collate utf8_unicode_ci COMMENT 'Each SE might have an endpoint that''ll redirect data to storage servers (which could be a huge number). For example, d0*.fnal.gov  might be the regex representing the list of storage servers for SE FNAL_D0, while dcache*.unl.edu might be the list''s regex ',
  PRIMARY KEY  (`resource_id`,`service_id`),
  KEY `resource_service_resource_service_SE_details` (`service_id`,`resource_id`),
  CONSTRAINT `resource_service_resource_service_SE_details` FOREIGN KEY (`service_id`, `resource_id`) REFERENCES `resource_service` (`service_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='This entity is a child of resource_service. A subset resourc';
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `sc_contact`
--

DROP TABLE IF EXISTS `sc_contact`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `sc_contact` (
  `person_id` int(11) NOT NULL,
  `sc_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `rank_id` int(11) NOT NULL,
  PRIMARY KEY  (`person_id`,`sc_id`,`type_id`,`rank_id`),
  KEY `supportcenter_sc_contact` (`sc_id`),
  KEY `contact_type_sc_contact` (`type_id`),
  KEY `contact_rank_sc_contact` (`rank_id`),
  CONSTRAINT `contact_rank_sc_contact` FOREIGN KEY (`rank_id`) REFERENCES `contact_rank` (`id`),
  CONSTRAINT `contact_type_sc_contact` FOREIGN KEY (`type_id`) REFERENCES `contact_type` (`id`),
  CONSTRAINT `person_sc_contact` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
  CONSTRAINT `supportcenter_sc_contact` FOREIGN KEY (`sc_id`) REFERENCES `supportcenter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `service` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(512) collate utf8_unicode_ci NOT NULL,
  `description` text collate utf8_unicode_ci,
  `port` varchar(128) collate utf8_unicode_ci default NULL COMMENT 'If service runs on specific port, use this field to store that info.',
  `service_group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `service_service_group` (`service_group_id`),
  CONSTRAINT `service_service_group` FOREIGN KEY (`service_group_id`) REFERENCES `service_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `service_group`
--

DROP TABLE IF EXISTS `service_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `service_group` (
  `id` int(11) NOT NULL,
  `name` varchar(256) collate utf8_unicode_ci NOT NULL,
  `description` text collate utf8_unicode_ci,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `site`
--

DROP TABLE IF EXISTS `site`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `site` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci NOT NULL,
  `long_name` text collate utf8_unicode_ci,
  `description` text collate utf8_unicode_ci,
  `address_line_1` text collate utf8_unicode_ci,
  `address_line_2` text collate utf8_unicode_ci,
  `city` varchar(256) collate utf8_unicode_ci NOT NULL,
  `state` varchar(256) collate utf8_unicode_ci NOT NULL,
  `zipcode` varchar(256) collate utf8_unicode_ci NOT NULL,
  `country` varchar(512) collate utf8_unicode_ci NOT NULL,
  `longitude` text collate utf8_unicode_ci COMMENT 'This field will be populated automagically based on city, state, zip, country entered.',
  `latitude` text collate utf8_unicode_ci COMMENT 'This field will be populated automagically based on city, state, zip, country entered.',
  `sc_id` int(11) NOT NULL,
  `facility_id` int(11) NOT NULL,
  `submitter_dn_id` int(11) default NULL COMMENT 'Just for sites, we only hold on to submitter DN .. and do not store any other type of contact information. We can always back track from support center supporting it or go back to the facility contacts at the next level.',
  `active` tinyint(1) NOT NULL default '0',
  `disable` tinyint(1) default '0' COMMENT 'The disable field supersedes the active flag, and can be used to permanently inactivate a record. It has to be set by the programmatic interface, it''s set to false by default.',
  PRIMARY KEY  (`id`),
  KEY `facility_site` (`facility_id`),
  KEY `supportcenter_site` (`sc_id`),
  KEY `certificate_dn_site` (`submitter_dn_id`),
  CONSTRAINT `certificate_dn_site` FOREIGN KEY (`submitter_dn_id`) REFERENCES `certificate_dn` (`id`),
  CONSTRAINT `facility_site` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`id`),
  CONSTRAINT `supportcenter_site` FOREIGN KEY (`sc_id`) REFERENCES `supportcenter` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10088 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `supportcenter`
--

DROP TABLE IF EXISTS `supportcenter`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `supportcenter` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci NOT NULL,
  `long_name` text collate utf8_unicode_ci,
  `description` text collate utf8_unicode_ci NOT NULL,
  `community` text collate utf8_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL default '0',
  `disable` tinyint(1) NOT NULL default '0' COMMENT 'The disable field supersedes the active flag, and can be used to permanently inactivate a record. It has to be set by the programmatic interface, it''s set to false by default.',
  `footprints_id` varchar(256) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `virtualorganization`
--

DROP TABLE IF EXISTS `virtualorganization`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `virtualorganization` (
  `id` int(11) NOT NULL auto_increment,
  `name` text collate utf8_unicode_ci NOT NULL,
  `long_name` text collate utf8_unicode_ci,
  `description` text collate utf8_unicode_ci,
  `primary_url` text collate utf8_unicode_ci,
  `aup_url` text collate utf8_unicode_ci COMMENT 'User Policy URL',
  `membership_services_url` text collate utf8_unicode_ci,
  `purpose_url` text collate utf8_unicode_ci,
  `support_url` text collate utf8_unicode_ci,
  `app_description` text collate utf8_unicode_ci,
  `community` text collate utf8_unicode_ci,
  `sc_id` int(11) NOT NULL,
  `parent_vo_id` int(11) default NULL,
  `active` tinyint(1) NOT NULL default '0',
  `disable` tinyint(1) NOT NULL default '0' COMMENT 'The disable field supersedes the active flag, and can be used to permanently inactivate a record. It has to be set by the programmatic interface, it''s set to false by default.',
  `footprints_id` varchar(256) collate utf8_unicode_ci default NULL,
  PRIMARY KEY  (`id`),
  KEY `virtualorganization_virtualorganization` (`parent_vo_id`),
  KEY `supportcenter_virtualorganization` (`sc_id`),
  CONSTRAINT `supportcenter_virtualorganization` FOREIGN KEY (`sc_id`) REFERENCES `supportcenter` (`id`),
  CONSTRAINT `virtualorganization_virtualorganization` FOREIGN KEY (`parent_vo_id`) REFERENCES `virtualorganization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_contact`
--

DROP TABLE IF EXISTS `vo_contact`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_contact` (
  `person_id` int(11) NOT NULL,
  `vo_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `rank_id` int(11) NOT NULL,
  PRIMARY KEY  (`person_id`,`vo_id`,`type_id`,`rank_id`),
  KEY `virtualorganization_vo_contact` (`vo_id`),
  KEY `contact_type_vo_contact` (`type_id`),
  KEY `contact_rank_vo_contact` (`rank_id`),
  CONSTRAINT `contact_rank_vo_contact` FOREIGN KEY (`rank_id`) REFERENCES `contact_rank` (`id`),
  CONSTRAINT `contact_type_vo_contact` FOREIGN KEY (`type_id`) REFERENCES `contact_type` (`id`),
  CONSTRAINT `person_vo_contact` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
  CONSTRAINT `virtualorganization_vo_contact` FOREIGN KEY (`vo_id`) REFERENCES `virtualorganization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_fqan`
--

DROP TABLE IF EXISTS `vo_fqan`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_fqan` (
  `id` int(11) NOT NULL auto_increment,
  `fqan` varchar(1024) collate utf8_unicode_ci NOT NULL,
  `vo_report_name_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `vo_report_name_vo_fqan` (`vo_report_name_id`),
  CONSTRAINT `vo_report_name_vo_fqan` FOREIGN KEY (`vo_report_name_id`) REFERENCES `vo_report_name` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_report_contact`
--

DROP TABLE IF EXISTS `vo_report_contact`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_report_contact` (
  `person_id` int(11) NOT NULL,
  `vo_report_name_id` int(11) NOT NULL,
  PRIMARY KEY  (`person_id`,`vo_report_name_id`),
  KEY `vo_report_name_vo_report_contact` (`vo_report_name_id`),
  CONSTRAINT `vo_report_name_vo_report_contact` FOREIGN KEY (`vo_report_name_id`) REFERENCES `vo_report_name` (`id`),
  CONSTRAINT `person_vo_report_contact` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_report_name`
--

DROP TABLE IF EXISTS `vo_report_name`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_report_name` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(128) collate utf8_unicode_ci default NULL,
  `vo_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `vo_vo_report_name` (`vo_id`),
  CONSTRAINT `vo_vo_report_name` FOREIGN KEY (`vo_id`) REFERENCES `virtualorganization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_resource_ownership`
--

DROP TABLE IF EXISTS `vo_resource_ownership`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_resource_ownership` (
  `resource_id` int(11) NOT NULL,
  `vo_id` int(11) NOT NULL,
  `percent` double default NULL,
  PRIMARY KEY  (`resource_id`,`vo_id`),
  KEY `virtualorganization_vo_resource_ownership` (`vo_id`),
  CONSTRAINT `resource_vo_resource_ownership` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`),
  CONSTRAINT `virtualorganization_vo_resource_ownership` FOREIGN KEY (`vo_id`) REFERENCES `virtualorganization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `vo_supports_ca`
--

DROP TABLE IF EXISTS `vo_supports_ca`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vo_supports_ca` (
  `ca_id` int(11) NOT NULL,
  `vo_id` int(11) NOT NULL,
  PRIMARY KEY  (`ca_id`,`vo_id`),
  KEY `virtualorganization_vo_supports_ca` (`vo_id`),
  CONSTRAINT `certificate_authoriry_vo_supports_ca` FOREIGN KEY (`ca_id`) REFERENCES `certificate_authoriry` (`id`),
  CONSTRAINT `virtualorganization_vo_supports_ca` FOREIGN KEY (`vo_id`) REFERENCES `virtualorganization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
SET character_set_client = @saved_cs_client;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-02-06 20:27:35
