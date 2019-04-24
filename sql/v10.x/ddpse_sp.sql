-- -----------------------------------------------------------------------------------------------------
--  Carnegie Mellon University, Human Computer Interaction Institute
--  Copyright 2014
--  All Rights Reserved
--
--  $Revision: 12404 $
--  Last modified by - $Author: ctipper $
--  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--
-- Two stored procedures have been written, ddpse_delete_duplicate_problem_events() and
-- ddpse_delete_for_dataset(datasetId) which is used by the first SP.
-- The stored procedure iterates through all datasets having duplicate PE starts
-- and removes duplicate problem_events that have not corresponding tutor_transaction
-- rows (unwarranted duplicates). After removing the unwarranted problem_events, the stored procedure
-- then modifies the dataset_system_log table to include a modified action for the dataset so that
-- calculate tx duration, aggregation, and CFG will run during log conversion.
--  HOW TO RUN:
--    mysql -uroot -p analysis_db < CALL ddpse_delete_duplicate_problem_events();
--
-- ------------------------------------------------------------------------------------------------------

DELIMITER $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ddpse_get_version_function` $$
CREATE FUNCTION         `ddpse_get_version_function` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 12404 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */


--  ------------------------------------------------------------------------------
--  Remove duplicate problem event START events for each dataset that has them.
--  ------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `ddpse_delete_duplicate_problem_events` $$
CREATE PROCEDURE `ddpse_delete_duplicate_problem_events`()
    LANGUAGE SQL
    NOT DETERMINISTIC
    CONTAINS SQL
    SQL SECURITY INVOKER
    COMMENT ''
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE datasetId LONG;

    --  declare cursors
    DECLARE cur_dataset CURSOR FOR
        SELECT distinct sess.dataset_id
        FROM problem_event pe
        LEFT JOIN session sess ON pe.session_id = sess.session_id
        WHERE event_flag = 0
        GROUP BY sess.dataset_id, sess.student_id, pe.problem_id, pe.start_time
        HAVING COUNT(distinct problem_event_id) > 1
        ORDER BY sess.dataset_id ASC;

    --  declare handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    CALL debug_log('ddpse', CONCAT(ddpse_get_version_function(),
        ' Starting ddpse_delete_duplicate_problem_events.'));

    SET done = FALSE;
    OPEN cur_dataset;

    fix_pe_start_loop: LOOP
        FETCH cur_dataset
            INTO datasetId;

        IF done=TRUE THEN
          LEAVE fix_pe_start_loop;
        END IF;

        CALL debug_log('ddpse', concat(
            'ddpse_delete_duplicate_problem_events: Found dataset with duplicate problem event starts (', datasetId, ')'));

        CALL ddpse_delete_for_dataset(datasetId);

    END LOOP fix_pe_start_loop;

    CLOSE cur_dataset;

    DROP TABLE if exists `ddpse_problem_event`;
    CALL debug_log('ddpse', CONCAT(ddpse_get_version_function(),
        ' Finished ddpse_delete_duplicate_problem_events.'));

END $$

--  ------------------------------------------------------------------------------
--  Remove duplicate problem event START events for each dataset that has them.
--  ------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `ddpse_delete_for_dataset` $$
CREATE PROCEDURE `ddpse_delete_for_dataset`(IN `datasetId` LONG)
    LANGUAGE SQL
    NOT DETERMINISTIC
    CONTAINS SQL
    SQL SECURITY INVOKER
    COMMENT ''
BEGIN

    CALL debug_log('ddpse', CONCAT(ddpse_get_version_function(),
        ' Starting ddpse_delete_for_dataset: ', datasetId));

    -- Populate a new table with distinct problem_event rows
    DROP TABLE if exists `ddpse_problem_event`;
    CREATE TABLE `ddpse_problem_event` (
        problem_event_id BIGINT(20) NOT NULL AUTO_INCREMENT,
        session_id BIGINT(20) NOT NULL,
        student_id BIGINT(20) NOT NULL,
        problem_id BIGINT(20) NOT NULL,
        start_time DATETIME NOT NULL,
        event_flag INT(11) NOT NULL,
        event_type VARCHAR(100) COLLATE 'utf8_bin',
        problem_view INT(11) NULL DEFAULT NULL,
        transaction_id BIGINT(20) DEFAULT NULL,
        PRIMARY KEY (problem_event_id),
        INDEX session_id (session_id),
        INDEX problem_id (problem_id)
    ) CHARACTER SET utf8 COLLATE='utf8_bin' ENGINE=InnoDB

    SELECT DISTINCT MIN(transaction_id) AS transaction_id,
        MIN(problem_event_id) AS problem_event_id,
        sess.session_id, sess.student_id,
        pe.problem_id, pe.start_time, event_flag,
        event_type, problem_view
    FROM problem_event pe
    LEFT JOIN session sess USING (session_id)
    LEFT JOIN tutor_transaction tt USING (problem_event_id)
    WHERE sess.dataset_id = datasetId
        AND event_flag = 0
    GROUP BY student_id, problem_id, pe.start_time;

    CALL debug_log('ddpse', concat(
        'ddpse_delete_for_dataset: Updating problem events in tutor_transaction for dataset ', datasetId));

    -- If Tx's have PE's that are going to be deleted, then set the
    -- tt.problem_event_id to NULL
    UPDATE tutor_transaction tt
        LEFT JOIN ddpse_problem_event tped USING (problem_event_id)
        LEFT JOIN problem_event pe USING (problem_event_id)
        SET tt.problem_event_id = NULL
        WHERE tped.problem_event_id is NULL
            AND pe.event_flag = 0
            AND tt.problem_event_id is NOT NULL
            AND dataset_id = datasetId;

    CALL debug_log('ddpse', concat(
        'ddpse_delete_for_dataset: Deleting duplicate problem events from problem_event for dataset ', datasetId));
    -- Remove unwarranted problem_event duplicates
    DELETE pe FROM problem_event pe
        LEFT JOIN ddpse_problem_event ped ON pe.problem_event_id = ped.problem_event_id
        LEFT JOIN session sess ON pe.session_id = sess.session_id
        WHERE sess.dataset_id = datasetId
          AND ped.problem_event_id IS NULL
          AND pe.event_flag = 0;

    CALL debug_log('ddpse_delete_for_dataset', concat(
        'ddpse_delete_for_dataset: Setting problem views to NULL for dataset ', datasetId));
    -- Reset the problem views for the dataset
    UPDATE problem_event pe
        LEFT JOIN session sess using (session_id)
        SET problem_view = NULL, event_type = REPLACE(pe.event_type, '_PV_GEN', '')
        WHERE dataset_id = datasetId
            AND event_flag = 0;

    CALL debug_log('ddpse', concat(
        'ddpse_delete_for_dataset: Updating notes for dataset ', datasetId));

    -- Add the info to the ds_dataset 'notes' field
    UPDATE ds_dataset SET notes =
        TRIM(CONCAT_WS('', notes, ' ', DATE_FORMAT(now(), '%m/%d/%y'), ': Deleted duplicate PE Starts. (ddpse)'))
        WHERE dataset_id = datasetId;

    DROP TABLE if exists `ddpse_problem_evet`;
    CALL debug_log('ddpse', CONCAT(ddpse_get_version_function(),
        ' Finished ddpse_delete_for_dataset: ', datasetId));

END $$

DELIMITER ;


CALL ddpse_delete_duplicate_problem_events();

DROP FUNCTION IF EXISTS `ddpse_get_version_function`;
DROP PROCEDURE IF EXISTS `ddpse_delete_for_dataset`;

