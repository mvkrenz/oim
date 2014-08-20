CREATE TABLE `wlcg_endpoint` (
`primary_key` varchar(255) NOT NULL,
`site_id` varchar(255) NOT NULL,
`hostname` varchar(255),
`host_ip` varchar(32),
`service_type` varchar(32),
`in_production` tinyint(1),
`roc_name` varchar(64),
`contact_email` varchar(128),
    PRIMARY KEY (`primary_key`),
    CONSTRAINT `c_site_id` FOREIGN KEY (`site_id`) REFERENCES `wlcg_site` (`primary_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


