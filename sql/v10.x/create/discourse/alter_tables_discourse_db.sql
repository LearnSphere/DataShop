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
-- SQL script to alter DiscourseDb schema to include source_id in tables,
-- discourse_id reference to data_source_aggregate table and deleted_flag to discourse.
--

-- Make sure this is only run on discoursedb_source.
USE discoursedb_source;

ALTER TABLE `data_source_aggregate` ADD COLUMN discourse_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `data_source_aggregate`
      ADD CONSTRAINT discourse_ds_fkey FOREIGN KEY (discourse_id)
      REFERENCES discourse(id_discourse) ON DELETE CASCADE;

ALTER TABLE `annotation_aggregate` ADD COLUMN discourse_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `annotation_aggregate`
      ADD CONSTRAINT discourse_aa_fkey FOREIGN KEY (discourse_id)
      REFERENCES discourse(id_discourse) ON DELETE CASCADE;

ALTER TABLE `discourse` ADD COLUMN deleted_flag BOOLEAN DEFAULT FALSE;
ALTER TABLE `discourse` ADD COLUMN project_id INT;

ALTER TABLE `data_source_aggregate` ADD COLUMN source_id BIGINT(20) DEFAULT NULL AFTER discourse_id;
ALTER TABLE `annotation_aggregate` ADD COLUMN source_id BIGINT(20) DEFAULT NULL AFTER discourse_id;
ALTER TABLE `user` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `content` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `discourse` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `contribution` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `data_source_instance` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `annotation_instance` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `discourse_part` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `discourse_part_relation` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;
ALTER TABLE `discourse_relation` ADD COLUMN source_id BIGINT(20) DEFAULT NULL;


