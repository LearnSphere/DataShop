--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2016
-- All Rights Reserved
--
-- Author: Cindy Tipper
-- $Revision: 13055 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
-- $KeyWordsOff: $
--
-- Stored procedure to delete a discourse from discoursedb.
-- Based on 0.5-SNAPSOT version of schema.
--

-- Only load on discoursedb.
USE discoursedb;

DELIMITER $$

DROP PROCEDURE IF EXISTS `delete_discourse` $$
CREATE PROCEDURE `delete_discourse`(IN `discourseId` LONG)
       SQL SECURITY INVOKER
BEGIN

    DECLARE startTime DATETIME;
    DECLARE info VARCHAR(255);
    DECLARE datashopVersion VARCHAR(255);
    DECLARE discourseName VARCHAR(255);

    SELECT now() INTO startTime;
    SELECT version FROM analysis_db.datashop_version INTO datashopVersion;
    SELECT name FROM discourse WHERE id_discourse = discourseId INTO discourseName;

    CALL analysis_db.debug_log('delete_discourse',
                               CONCAT('Deleting discourse: ',
                                      discourseName, ' (', discourseId, ')'));

    CALL do_deletes(discourseId);

    SET info = CONCAT('Deleted discourse ', discourseName, ' (', discourseId ,').');

    INSERT INTO analysis_db.dataset_system_log (dataset_id, elapsed_time, time, action,
                                                info, success_flag, datashop_version)
        VALUES (discourseId,
                TIMESTAMPDIFF(SECOND, startTime, now()),   /* elapsed time */
                now(),                                     /* time */
                "purge deleted discourse",                 /* action */
                info,
                TRUE,
                datashopVersion);

    CALL analysis_db.debug_log('delete_discourse',
                               CONCAT('Discourse delete complete: ', discourseId));

END $$

DROP PROCEDURE IF EXISTS `do_deletes` $$
CREATE PROCEDURE `do_deletes`(IN `discourseId` LONG)
       SQL SECURITY INVOKER
BEGIN

    DELETE cpdp FROM contribution_partof_discourse_part cpdp
       JOIN discourse_has_discourse_part map
         ON (map.fk_discourse_part = cpdp.fk_discourse_part)
       WHERE  map.fk_discourse = discourseId;

    DELETE dr FROM discourse_relation dr
       JOIN contribution c
         ON ((c.id_contribution = dr.fk_source) OR (c.id_contribution = dr.fk_target))
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = c.fk_data_sources)
       WHERE  ds.discourse_id = discourseId;

    DELETE c FROM contribution c
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = c.fk_data_sources)
       WHERE  ds.discourse_id = discourseId;

    DELETE dsi FROM data_source_instance dsi
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = dsi.fk_sources)
       WHERE  ds.discourse_id = discourseId;

    DELETE dpr FROM discourse_part_relation dpr
       JOIN discourse_has_discourse_part map
         ON ((dpr.fk_source = map.fk_discourse_part) OR (dpr.fk_target = map.fk_discourse_part))
       WHERE  map.fk_discourse = discourseId;

    -- Sigh. Need two versions of this... EdX has NULL content.fk_data_sources.
    -- EdX version
    DELETE c FROM content c
       JOIN user u
         ON (c.fk_user_id = u.id_user)
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = u.fk_data_sources)
       WHERE  ds.discourse_id = discourseId;

    -- Wiki version
    DELETE c FROM content c
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = c.fk_data_sources)
       WHERE  ds.discourse_id = discourseId;

    DELETE u FROM user u
       JOIN data_source_aggregate ds
         ON (ds.id_data_sources = u.fk_data_sources)
       WHERE  ds.discourse_id = discourseId;

    DELETE dp FROM discourse_part dp
       JOIN discourse_has_discourse_part map
         ON (map.fk_discourse_part = dp.id_discourse_part)
       WHERE  map.fk_discourse = discourseId;

   DELETE ai FROM annotation_instance ai
      JOIN annotation_aggregate aa ON (aa.id_annotation = ai.fk_data_sources)
      WHERE aa.discourse_id = discourseId;

    DELETE ds FROM data_source_aggregate ds
       WHERE  ds.discourse_id = discourseId;

    DELETE aa FROM annotation_aggregate aa
       WHERE  aa.discourse_id = discourseId;

    DELETE d FROM discourse d
       WHERE  d.id_discourse = discourseId; 

END $$

DELIMITER ;

