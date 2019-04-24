--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2015
-- All Rights Reserved
--
-- Author: Cindy Tipper
-- $Revision: 13055 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
-- $KeyWordsOff: $
--
-- SQL script to create tables in database discoursedb.
-- Based on 0.5 (final!) version of schema.
--

-- Make sure this is only run on discoursedb.
USE discoursedb;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `discourse`;
CREATE TABLE `discourse` (
  `id_discourse` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `name` text DEFAULT NULL,
  project_id INT,
  deleted_flag BOOLEAN DEFAULT FALSE,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `data_source_aggregate`;
CREATE TABLE `data_source_aggregate` (
  `id_data_sources` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  discourse_id BIGINT(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_data_sources`),
  CONSTRAINT discourse_ds_aggregate_fkey FOREIGN KEY (discourse_id) REFERENCES `discourse`(`id_discourse`) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `annotation_aggregate`;
CREATE TABLE `annotation_aggregate` (
  `id_annotation` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  discourse_id BIGINT(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_annotation`),
  CONSTRAINT discourse_an_aggregate_fkey FOREIGN KEY (discourse_id) REFERENCES `discourse`(`id_discourse`) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id_user` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `realname` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `fk_data_sources` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_user`),
  KEY `FK_xss8wv8gkqcwq7d6en5nhrhm` (`fk_data_sources`),
  CONSTRAINT `FK_xss8wv8gkqcwq7d6en5nhrhm` FOREIGN KEY (`fk_data_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `content`;
CREATE TABLE `content` (
  `id_content` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `data` longblob,
  `text` longtext,
  `title` text DEFAULT NULL,
  `fk_data_sources` bigint(20) DEFAULT NULL,
  `fk_user_id` bigint(20) DEFAULT NULL,
  `fk_next_revision` bigint(20) DEFAULT NULL,
  `fk_previous_revision` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_content`),
  KEY `FK_c6o3epjp27nob9g7vs9wutdfa` (`fk_data_sources`),
  KEY `FK_pcj8267433emqdcx83tadm5jk` (`fk_user_id`),
  KEY `FK_ddmryl7b1ri73wtaqo2i947a0` (`fk_next_revision`),
  KEY `FK_h835mqrhh3wlnqh3a71817mqv` (`fk_previous_revision`),
  CONSTRAINT `FK_c6o3epjp27nob9g7vs9wutdfa` FOREIGN KEY (`fk_data_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`),
  CONSTRAINT `FK_ddmryl7b1ri73wtaqo2i947a0` FOREIGN KEY (`fk_next_revision`) REFERENCES `content` (`id_content`),
  CONSTRAINT `FK_h835mqrhh3wlnqh3a71817mqv` FOREIGN KEY (`fk_previous_revision`) REFERENCES `content` (`id_content`),
  CONSTRAINT `FK_pcj8267433emqdcx83tadm5jk` FOREIGN KEY (`fk_user_id`) REFERENCES `user` (`id_user`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `contribution`;
CREATE TABLE `contribution` (
  `id_contribution` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `downvotes` int(11) NOT NULL,
  `upvotes` int(11) NOT NULL,
  `fk_data_sources` bigint(20) DEFAULT NULL,
  `fk_current_revision` bigint(20) DEFAULT NULL,
  `fk_first_revision` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `FK_sddu29xqypd54mm9i9mn5udhg` (`fk_data_sources`),
  KEY `FK_sl4gwv9bed7hbmdynn91p6pu4` (`fk_current_revision`),
  KEY `FK_obgv1pg2b6b0arty59h9pxdwd` (`fk_first_revision`),
  CONSTRAINT `FK_obgv1pg2b6b0arty59h9pxdwd` FOREIGN KEY (`fk_first_revision`) REFERENCES `content` (`id_content`),
  CONSTRAINT `FK_sddu29xqypd54mm9i9mn5udhg` FOREIGN KEY (`fk_data_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`),
  CONSTRAINT `FK_sl4gwv9bed7hbmdynn91p6pu4` FOREIGN KEY (`fk_current_revision`) REFERENCES `content` (`id_content`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `data_source_instance`;
CREATE TABLE `data_source_instance` (
  `id_data_source_instance` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `dataset_name` varchar(255) DEFAULT NULL,
  `entity_source_descriptor` varchar(255) DEFAULT NULL,
  `entity_source_id` varchar(255) NOT NULL,
  `source_type` varchar(255) DEFAULT NULL,
  `fk_sources` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_data_source_instance`),
  UNIQUE KEY `UK_af1bx9964hucwyjtvl3jy302l` (`entity_source_id`,`entity_source_descriptor`,`dataset_name`),
  KEY `FK_3j2xvtdxmtucbrawb2os521v5` (`fk_sources`),
  CONSTRAINT `FK_3j2xvtdxmtucbrawb2os521v5` FOREIGN KEY (`fk_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `annotation_instance`;
CREATE TABLE `annotation_instance` (
  `id_annotation_instance` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `begin_offset` int(11) DEFAULT NULL,
  `covered_text` text,
  `end_offset` int(11) DEFAULT NULL,
  `fk_data_sources` bigint(20) DEFAULT NULL,
  `fk_annotation` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_annotation_instance`),
  KEY `FK_pjhx61f9ccn8mau851p2l2shf` (`fk_data_sources`),
  KEY `FK_h4wndrnjdmb40g0p9a2ebcc09` (`fk_annotation`),
  CONSTRAINT `FK_h4wndrnjdmb40g0p9a2ebcc09` FOREIGN KEY (`fk_annotation`) REFERENCES `annotation_aggregate` (`id_annotation`),
  CONSTRAINT `FK_pjhx61f9ccn8mau851p2l2shf` FOREIGN KEY (`fk_data_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `discourse_part`;
CREATE TABLE `discourse_part` (
  `id_discourse_part` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `name` text DEFAULT NULL,
  `fk_data_sources` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse_part`),
  KEY `FK_7o0ge2prm0quxlsthk425hdi1` (`fk_data_sources`),
  CONSTRAINT `FK_7o0ge2prm0quxlsthk425hdi1` FOREIGN KEY (`fk_data_sources`) REFERENCES `data_source_aggregate` (`id_data_sources`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `discourse_part_relation`;
CREATE TABLE `discourse_part_relation` (
  `id_discourse_part_relation` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `fk_source` bigint(20) DEFAULT NULL,
  `fk_target` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse_part_relation`),
  KEY `FK_8vuu3te66eirwrybllamff79e` (`fk_source`),
  KEY `FK_1lm40u1xcf4c5mus56ua3j63e` (`fk_target`),
  CONSTRAINT `FK_1lm40u1xcf4c5mus56ua3j63e` FOREIGN KEY (`fk_target`) REFERENCES `discourse_part` (`id_discourse_part`),
  CONSTRAINT `FK_8vuu3te66eirwrybllamff79e` FOREIGN KEY (`fk_source`) REFERENCES `discourse_part` (`id_discourse_part`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `discourse_relation`;
CREATE TABLE `discourse_relation` (
  `id_discourse_relation` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `fk_source` bigint(20) DEFAULT NULL,
  `fk_target` bigint(20) DEFAULT NULL,
  source_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse_relation`),
  KEY `FK_rjj0de6qye0qnkikka2xbl2rx` (`fk_source`),
  KEY `FK_ryrsu7h5bxjxirqs4hxh4n7x2` (`fk_target`),
  CONSTRAINT `FK_rjj0de6qye0qnkikka2xbl2rx` FOREIGN KEY (`fk_source`) REFERENCES `contribution` (`id_contribution`),
  CONSTRAINT `FK_ryrsu7h5bxjxirqs4hxh4n7x2` FOREIGN KEY (`fk_target`) REFERENCES `contribution` (`id_contribution`)
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `contribution_partof_discourse_part`;
CREATE TABLE `contribution_partof_discourse_part` (
  `id_discourse_part_contribution` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `fk_contribution` bigint(20) DEFAULT NULL,
  `fk_discourse_part` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse_part_contribution`),
  UNIQUE KEY `UK_25q84mih7txxybwnj0c74wx58` (`fk_contribution`,`fk_discourse_part`),
  KEY `FK_qrh8iershjb7gs1uujkbc24a7` (`fk_discourse_part`),
  CONSTRAINT `FK_9adsfievxemacex4e8u7t9g14` FOREIGN KEY (`fk_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE CASCADE,
  CONSTRAINT `FK_qrh8iershjb7gs1uujkbc24a7` FOREIGN KEY (`fk_discourse_part`) REFERENCES `discourse_part` (`id_discourse_part`) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `discourse_has_discourse_part`;
CREATE TABLE `discourse_has_discourse_part` (
  `id_discourse_has_discourse_part` bigint(20) NOT NULL AUTO_INCREMENT,
  `entity_created` datetime DEFAULT NULL,
  `entity_modified` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `entity_version` bigint(20) DEFAULT NULL,
  `fk_discourse` bigint(20) DEFAULT NULL,
  `fk_discourse_part` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_discourse_has_discourse_part`),
  UNIQUE KEY `UK_d9fefbc7oph9pcv0ix55k3rek` (`fk_discourse`,`fk_discourse_part`),
  KEY `FK_atsffbpm9dgg89bygmxaubn8g` (`fk_discourse_part`),
  CONSTRAINT `FK_atsffbpm9dgg89bygmxaubn8g` FOREIGN KEY (`fk_discourse_part`) REFERENCES `discourse_part` (`id_discourse_part`) ON DELETE CASCADE,
  CONSTRAINT `FK_ia9s1ksk7tj2p0uuy21vxay99` FOREIGN KEY (`fk_discourse`) REFERENCES `discourse` (`id_discourse`) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

DROP TABLE IF EXISTS `user_memberof_discourse`;
CREATE TABLE `user_memberof_discourse` (
  `id_user` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_discourse` bigint(20) NOT NULL,
  PRIMARY KEY (`id_user`,`id_discourse`),
  KEY `FK_of7lgtf2l4fies0abapw01tgb` (`id_discourse`),
  CONSTRAINT `FK_of7lgtf2l4fies0abapw01tgb` FOREIGN KEY (`id_discourse`) REFERENCES `discourse` (`id_discourse`) ON DELETE CASCADE,
  CONSTRAINT `FK_pqvgq9dlouba1m2kg5jwivieh` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci;

SET FOREIGN_KEY_CHECKS=1;
