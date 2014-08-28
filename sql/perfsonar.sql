use oim;

drop table if exists perfsonar_mas;
CREATE TABLE `perfsonar_mas` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`hostname` varchar(256) COLLATE utf8_unicode_ci NOT NULL,
`ma` text,
PRIMARY KEY (`id`),
UNIQUE KEY `hostname` (`hostname`)
);

