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
-- SQL script to drop tables in database discoursedb.
-- Based on 0.5-SNAPSOT version of schema.
--

-- Make sure this is only run on discoursedb.
USE discoursedb_source;
SET FOREIGN_KEY_CHECKS=0;


-- Delete map tables first.
DROP TABLE IF EXISTS `contribution_partof_discourse_part`;
DROP TABLE IF EXISTS `discourse_has_discourse_part`;
DROP TABLE IF EXISTS `user_memberof_discourse`;

DROP TABLE IF EXISTS `discourse_relation`;
DROP TABLE IF EXISTS `contribution`;
DROP TABLE IF EXISTS `data_source_instance`;
DROP TABLE IF EXISTS `annotation_instance`;
DROP TABLE IF EXISTS `discourse_part_relation`;
DROP TABLE IF EXISTS `discourse_part`;
DROP TABLE IF EXISTS `discourse`;

-- These have to come after all of the above. Order matters.
DROP TABLE IF EXISTS `content`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `data_source_aggregate`;
DROP TABLE IF EXISTS `annotation_aggregate`;


SET FOREIGN_KEY_CHECKS=1;
