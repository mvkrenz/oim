CREATE TABLE `wlcg_site` (
`primary_key` varchar(255) NOT NULL,
`short_name` varchar(255),
`official_name` text,
`longitude` varchar(128),
`latitude` varchar(128),
`contact_email` varchar(128),
    PRIMARY KEY (`primary_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


