/*
  -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2015
  All Rights Reserved

  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2017-03-22 10:39:23 -0400 (Wed, 22 Mar 2017) $


  Handles the merging of Carnegie Learning datasets from the analysis_db_cl database into
  ------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_db_merge` $$
CREATE FUNCTION         `get_version_db_merge` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 14019 $ '
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/*
  -------------------------------------------------------------------------------
  Initiate the db merge from adb_source into analysis_db. First test for
  existing keys before populating ds_dataset.
  @param newDatasets A comma separated TEXT string of dataset ids in adb_source
    to be merged for the first time.
  @param existingDatasets A comma separated TEXT string of dataset ids in adb_source
    that should already exist in the destination.
  @param keepDatasets A comma separated TEXT string of dataset ids in adb_source
    that are reserved in the destination.
  @param datasetDelim The delimiter of the list, default is the comma character.
  @param action Integer constants for which datasets to use { 0 - importNewAndMerge
                                                              1 - importNewOnly
                                                              2 - mergeOnly
                                                              3 - importNewKeep }
  @param mergeType Integer constants for kind of merge expected { 0 - srcAllNew
                                                                  1 - srcAppended }
  @param mappingDbName name of database that hold student ID map
  -------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `dbm_run_merge` $$
CREATE PROCEDURE         `dbm_run_merge` (newDatasets TEXT, existingDatasets TEXT, keepDatasets TEXT, datasetDelim VARCHAR(1), action INT, mergeType INT, mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN

    DECLARE keepReservedDatasets BOOL DEFAULT FALSE;

    CALL debug_log('db_merge_sp', CONCAT(get_version_db_merge(), 'Starting dbm_run_merge, action=', action, ', mergeType=', mergeType));

    CREATE TABLE IF NOT EXISTS dbm_max_table_counts (
        dataset_id INT NOT NULL,
        table_name ENUM('tutor_transaction', 'session', 'dataset_level_event','problem_event')
            NOT NULL,
        max_src_pk BIGINT NOT NULL,
        row_count  BIGINT NOT NULL DEFAULT 0,
        prev_count BIGINT NOT NULL DEFAULT 0,

        CONSTRAINT max_table_counts_pkey PRIMARY KEY (dataset_id, table_name)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    DROP TABLE IF EXISTS dbm_ds_id_map;
    CREATE TABLE dbm_ds_id_map (
        src_dataset_id INT PRIMARY KEY,
        dataset_id INT,
        merge_flag BOOL NOT NULL DEFAULT FALSE,
        merge_appended_flag BOOL NOT NULL DEFAULT FALSE,
        dsl_id INT,
        INDEX (dataset_id),
        INDEX (merge_flag),
        INDEX (merge_appended_flag),
        INDEX (dsl_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    IF action = 3 THEN
        SET keepReservedDatasets = TRUE;
        SET newDatasets = keepDatasets;
        SET action = 1;
    END IF;

    IF action = 1 THEN
        CALL dbm_insert_new_datasets(newDatasets, datasetDelim, keepReservedDatasets);
    ELSEIF action = 2 THEN
        CALL dbm_merge_existing_datasets(existingDatasets, datasetDelim, mergeType);
    ELSE
        CALL dbm_insert_new_datasets(newDatasets, datasetDelim, keepReservedDatasets);
        CALL dbm_merge_existing_datasets(existingDatasets, datasetDelim, mergeType);
        /* If the transaction count doesn't differ, see if any datasets need merging at all */
        IF mergeType = 1 THEN
            CALL dbm_reduce_action(action);
        END IF;
    END IF;

    IF EXISTS(SELECT src_dataset_id FROM dbm_ds_id_map) THEN
        SET foreign_key_checks = 0;
        CALL dbm_create_transaction_view();
        IF action = 1 THEN
            /* [12/01/23 - ysahn] order of execution of switched dbm_insert_dataset_levels()
             * and dbm_insert_students() was switched, and dbm_insert_session_student_map() was
             * factored out from  dbm_insert_students() and appended the end
             */
            CALL dbm_insert_students(mappingDbName);

            CALL dbm_insert_dataset_levels(mappingDbName);

            /* [12/01/23 - ysahn] TODO: Eventually this call should be removed */
            CALL dbm_insert_session_student_map();


            CALL dbm_insert_ds_conditions();

        ELSEIF action = 2 THEN
            /* [12/01/23 - ysahn] order of execution of switched dbm_insert_dataset_levels()
             * and dbm_insert_students() was switched, and dbm_insert_session_student_map() was
             * factored out from  dbm_insert_students() and appended the end
             */

            CALL dbm_merge_students(mappingDbName);

            CALL dbm_merge_dataset_levels(mappingDbName);

            /* [12/01/23 - ysahn] TODO: Eventually this call should be removed */
            CALL dbm_merge_session_student_map();

            CALL dbm_merge_ds_conditions();

            CALL dbm_merge_tutor_transactions();

        ELSE
            /* [12/01/23 - ysahn] order of execution of switched dbm_insert_dataset_levels()
             * and dbm_insert_students() was switched, and dbm_insert_session_student_map() was
             * factored out from  dbm_insert_students() and appended the end
             */
            CALL dbm_insert_students(mappingDbName);

            CALL dbm_insert_dataset_levels(mappingDbName);

            /* [12/01/23 - ysahn] TODO: Eventually this call should be removed */
            CALL dbm_insert_session_student_map();


            CALL dbm_insert_ds_conditions();


            /* [12/06/13 - ysahn] order of execution of switched dbm_merge_dataset_levels()
             * and dbm_merge_students() was switched, and dbm_merge_session_student_map() was
             * factored out from  dbm_merge_students() and appended the end
             */

            CALL dbm_merge_students(mappingDbName);

            CALL dbm_merge_dataset_levels(mappingDbName);

            /* [12/06/13 - ysahn] TODO: Eventually this call should be removed */
            CALL dbm_merge_session_student_map();


            CALL dbm_merge_ds_conditions();

            CALL dbm_merge_tutor_transactions();
        END IF;

        /* No longer keeping actual_user_id info in analysis_db. */
        CALL dbm_update_students();

        SET foreign_key_checks = 1;
    ELSE
        CALL debug_log('db_merge_sp','No source datasets require merging.');
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_run_merge');
END $$

/*
  -------------------------------------------------------------------------------
  Cache the transactions for source datasets and destination datasets in views so
  they retain their indexes for reading.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_create_transaction_view` $$
CREATE PROCEDURE `dbm_create_transaction_view` ()
    SQL SECURITY INVOKER
BEGIN
    CREATE OR REPLACE
    SQL SECURITY INVOKER
    VIEW dbm_dest_tt_view AS
    SELECT transaction_id, guid, session_id, transaction_time, transaction_time_ms, time_zone, transaction_type_tutor,
        transaction_type_tool, transaction_subtype_tutor, transaction_subtype_tool, outcome,
        attempt_at_subgoal, is_last_attempt, tt.dataset_id, problem_id, subgoal_id, subgoal_attempt_id, feedback_id,
        class_id, school_id, help_level, total_num_hints, duration, prob_solving_sequence, src_transaction_id
    FROM tutor_transaction tt
    JOIN dbm_ds_id_map dim ON tt.dataset_id = dim.dataset_id;

    CREATE OR REPLACE
    SQL SECURITY INVOKER
    VIEW dbm_src_tt_view AS
    SELECT transaction_id, guid, session_id, transaction_time, transaction_time_ms,
        REPLACE(REPLACE(REPLACE(time_zone, '\t', ' '), '\r', ' '), '\n', ' ') as time_zone,
        REPLACE(REPLACE(REPLACE(transaction_type_tutor, '\t', ' '), '\r', ' '), '\n', ' ') as transaction_type_tutor,
        REPLACE(REPLACE(REPLACE(transaction_type_tool, '\t', ' '), '\r', ' '), '\n', ' ') as transaction_type_tool,
        REPLACE(REPLACE(REPLACE(transaction_subtype_tutor, '\t', ' '), '\r', ' '), '\n', ' ') as transaction_subtype_tutor,
        REPLACE(REPLACE(REPLACE(transaction_subtype_tool, '\t', ' '), '\r', ' '), '\n', ' ') as transaction_subtype_tool,
        REPLACE(REPLACE(REPLACE(outcome, '\t', ' '), '\r', ' '), '\n', ' ') as outcome,
        attempt_at_subgoal, is_last_attempt, dim.dataset_id, problem_id, subgoal_id, subgoal_attempt_id, feedback_id,
        class_id, school_id, help_level, total_num_hints, duration, prob_solving_sequence, problem_event_id
    FROM adb_source.tutor_transaction tt
    JOIN dbm_ds_id_map dim ON tt.dataset_id = dim.src_dataset_id;

END $$

/*
 -------------------------------------------------------------------------------
 If the merge type is srcAppended and we are also looking to import new datasets and there are
 no new transactions then we can optimize to do an import new only and ignore merging.
 @param action modifiable action constant to change if no datasets qualify for merging.
 -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_reduce_action` $$
CREATE PROCEDURE `dbm_reduce_action` (INOUT action INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE mergeExistingCount INT DEFAULT 0;

    SELECT count(src_dataset_id) INTO mergeExistingCount FROM dbm_ds_id_map WHERE merge_flag = TRUE;

    /* If no new transactions exist */
    IF mergeExistingCount = 0 THEN
        SET action = 1;
    END IF;
END $$

/*
 -------------------------------------------------------------------------------
 Prepare datasets to be merged into an existing dataset under the same name.
 On success each dataset will have an entry in the dbm_ds_id_map table.  Also
 log a starting message to the dataset_system_log for each individual dataset to
 merge.
 @param existingDatasets Concatenated string of the source dataset IDs
 @param datasetDelim The character delimiter of the dataset string
 @param mergeType Integer constants for kind of merge expected { 0 - srcAllNew
                                                                 1 - srcAppended }
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_merge_existing_datasets` $$
CREATE PROCEDURE `dbm_merge_existing_datasets` (existingDatasets TEXT, datasetDelim VARCHAR(1), mergeType INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE srcDatasetId, existingDatasetId, txDifference BIGINT DEFAULT NULL;
    DECLARE datasetName VARCHAR(100);

    DECLARE delimPos INT DEFAULT 0;
    DECLARE datasetList TEXT DEFAULT existingDatasets;
    DECLARE idString TEXT DEFAULT '';
    DECLARE strlen int DEFAULT LENGTH(existingDatasets);

    DECLARE action VARCHAR(255) DEFAULT "merge started";
    DECLARE info TEXT DEFAULT "Started merging existing dataset(s)";

    CALL debug_log('db_merge_sp','Starting dbm_merge_existing_datasets.');

    SET delimPos = LOCATE(datasetDelim, datasetList);

    WHILE strlen > 0 DO

        /* Reached the last element in the list */
        IF delimPos = 0 THEN
            SET idString = TRIM(datasetList);
            SET datasetList = '';
            SET strlen = 0;
        END IF;

        /* Retrieve only the dataset id and trim the list from the front. */
        IF delimPos != 0 THEN
            SET idString = TRIM(SUBSTRING(datasetList, 1, delimPos-1));
            SET datasetList = TRIM(SUBSTRING(datasetList FROM delimPos+1));
            SET strlen = LENGTH(datasetList);
        END IF;

        /* If a dataset id exists, create a new dataset for it */
        IF idString != '' THEN
            SET srcDatasetId = CAST(idString AS SIGNED INTEGER);
            SET existingDatasetId = dbm_get_existing_dataset_id(srcDatasetId);
            SET datasetName = dbm_get_existing_dataset_name(srcDatasetId);

            /* Dataset being merged includes old and new data */
            IF mergeType = 1 THEN
                SET txDifference = dbm_compare_num_transaction(existingDatasetId, srcDatasetId);
                IF txDifference <= 0 THEN
                    CALL debug_log('db_merge_sp', CONCAT('Skipping dataset \'', datasetName, '\' no new transactions'));
                ELSE
                    CALL dbm_create_dataset_levels(srcDatasetId, existingDatasetId);
                    IF dbm_check_dataset_levels() THEN
                        INSERT INTO dbm_ds_id_map (src_dataset_id, dataset_id, merge_flag, merge_appended_flag)
                        VALUES (srcDatasetId, existingDatasetId, TRUE, TRUE);
                    CALL debug_log('db_merge_sp', CONCAT('Existing dataset \'', datasetName, '\' queued to merge'));
                ELSE
                    CALL debug_log('db_merge_sp', CONCAT('dataset levels vary for source dataset ', srcDatasetId));
                END IF;
                END IF;
            ELSE
                CALL dbm_create_dataset_levels(srcDatasetId, existingDatasetId);
                IF dbm_check_dataset_levels() THEN
                    INSERT INTO dbm_ds_id_map (src_dataset_id, dataset_id, merge_flag, merge_appended_flag)
                        VALUES (srcDatasetId, existingDatasetId, TRUE, FALSE);
                    CALL debug_log('db_merge_sp', CONCAT('Existing dataset \'', datasetName, '\' queued to merge'));
                ELSE
                    CALL debug_log('db_merge_sp', CONCAT('dataset levels vary for source dataset ', srcDatasetId));
                END IF;
            END IF;
        END IF;

        SET delimPos = LOCATE(datasetDelim, datasetList);

    END WHILE;

    SET info = "Started merging existing dataset(s)";

    CALL dbm_insert_dataset_system_logs();
    CALL dbm_starting_merge_status(info, TRUE);

    CALL debug_log('db_merge_sp','Finished dbm_merge_existing_datasets.');
END $$

/*
 -------------------------------------------------------------------------------
 Prepare new datasets being imported for the first time to the destination database.
 Update curriculum data if it is present in the database already.  On success
 a new dataset ID is adopted and the dataset is added to the dbm_ds_id_map and
 a starting message is logged to the dataset_system_log.
 -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_new_datasets` $$
CREATE PROCEDURE `dbm_insert_new_datasets` (newDatasets TEXT, datasetDelim VARCHAR(1), keepReservedDatasets BOOL)
    SQL SECURITY INVOKER
BEGIN
    DECLARE datasetName VARCHAR(100);
    DECLARE tutorName VARCHAR(50) DEFAULT NULL;
    DECLARE startTime DATETIME DEFAULT NULL;
    DECLARE endTime DATETIME DEFAULT NULL;
    DECLARE statusVal VARCHAR(20) DEFAULT NULL;
    DECLARE descriptionText, hypothesisText, notesText TEXT DEFAULT NULL;
    DECLARE domainId, learnLabId INT DEFAULT NULL;
    DECLARE junkFlag BOOL DEFAULT FALSE;
    DECLARE studyFlag VARCHAR(20) DEFAULT 'not specified';
    DECLARE curriculumId INT DEFAULT NULL;
    DECLARE newCurriculumId INT DEFAULT NULL;
    DECLARE curriculumName VARCHAR(60) DEFAULT NULL;
    DECLARE projectId INT DEFAULT NULL;
    DECLARE schoolName VARCHAR(255) DEFAULT NULL;
    DECLARE autoSchoolFlag, autoTimesFlag BOOL;
    DECLARE appearsAnonFlag VARCHAR(20) DEFAULT NULL;
    DECLARE irbUploaded VARCHAR(20) DEFAULT 'TBD';
    DECLARE projectSetTime DATETIME DEFAULT NULL;
    DECLARE dataLastModified DATETIME DEFAULT NULL;

    DECLARE newDatasetId, srcDatasetId, existingDatasetId BIGINT DEFAULT NULL;
    DECLARE import BOOL;
    DECLARE merge BOOL DEFAULT FALSE;
    DECLARE txDifference BIGINT DEFAULT NULL;

    DECLARE delimPos INT DEFAULT 0;
    DECLARE datasetList TEXT DEFAULT newDatasets;
    DECLARE idString TEXT DEFAULT '';
    DECLARE strlen int DEFAULT LENGTH(newDatasets);

    DECLARE info TEXT DEFAULT "Started merging new dataset(s)";

    CALL debug_log('db_merge_sp','Starting dbm_insert_new_datasets.');

    SET delimPos = LOCATE(datasetDelim, datasetList);

    WHILE strlen > 0 DO

        /* Reached the last element in the list */
        IF delimPos = 0 THEN
            SET idString = TRIM(datasetList);
            SET datasetList = '';
            SET strlen = 0;
        END IF;

        /* Retrieve only the dataset id and trim the list from the front. */
        IF delimPos != 0 THEN
            SET idString = TRIM(SUBSTRING(datasetList, 1, delimPos-1));
            SET datasetList = TRIM(SUBSTRING(datasetList FROM delimPos+1));
            SET strlen = LENGTH(datasetList);
        END IF;

        /* If a dataset id exists, create a new dataset for it */
        IF idString != '' THEN
            SET srcDatasetId = CAST(idString AS SIGNED INTEGER);

            SELECT dataset_name, tutor, start_time, end_time, status, description, hypothesis,
                domain_id, learnlab_id, study_flag, curriculum_id, notes,
                REPLACE(REPLACE(REPLACE(school, '\t', ' '), '\r', ' '), '\n', ' ') as school,
                auto_set_school_flag, auto_set_times_flag, appears_anon_flag,
                irb_uploaded, project_set_time, data_last_modified
            INTO datasetName, tutorName, startTime, endTime, statusVal, descriptionText,
                hypothesisText, domainId, learnlabId, studyFlag, curriculumId, notesText,
                schoolName, autoSchoolFlag, autoTimesFlag, appearsAnonFlag,
                irbUploaded, projectSetTime, dataLastModified
            FROM adb_source.ds_dataset
            WHERE dataset_id = srcDatasetId;

            IF curriculumId IS NOT NULL THEN
                /* Find the updated curriculum_id if the name already exists */
                IF EXISTS(SELECT c.curriculum_id  FROM curriculum c
                    JOIN adb_source.curriculum adbc
                        ON REPLACE(REPLACE(REPLACE(adbc.curriculum_name, '\t', ' '), '\r', ' '), '\n', ' ') = c.curriculum_name
                    WHERE adbc.curriculum_id = curriculumId) THEN

                    CALL debug_log('db_merge_sp', 'Curriculum Exists');
                    SELECT c.curriculum_id INTO curriculumId
                    FROM curriculum c
                    JOIN adb_source.curriculum adbc
                        ON REPLACE(REPLACE(REPLACE(adbc.curriculum_name, '\t', ' '), '\r', ' '), '\n', ' ') = c.curriculum_name
                    WHERE adbc.curriculum_id = curriculumId;
                ELSE
                    CALL debug_log('db_merge_sp', 'Insert New Curriculum');

                    INSERT INTO curriculum (curriculum_name)
                    SELECT REPLACE(REPLACE(REPLACE(curriculum_name, '\t', ' '), '\r', ' '), '\n', ' ')
                    FROM adb_source.curriculum
                    WHERE curriculum_id = curriculumId;

                    SET curriculumId = LAST_INSERT_ID();
                    CALL debug_log('db_merge_sp', CONCAT('Curriculum: ', ifnull(curriculumId, 'null')));
                END IF;
            END IF;

            /* Update domain_id even if we expect ids to be the same between databases. */
            SELECT domain_id INTO domainId
            FROM domain dom
            WHERE dom.name = (SELECT name FROM adb_source.domain WHERE domain_id = domainId);

            /* Update learnlab_id even if we expect ids to be the same between databases. */
            SELECT learnlab_id INTO learnlabId
            FROM learnlab ll
            WHERE ll.name = (SELECT name FROM adb_source.learnlab WHERE learnlab_id = learnlabId);

            SELECT project_id INTO projectId FROM project WHERE project_name = 'Unclassified';

            IF projectId IS NULL THEN
                INSERT INTO project (project_name) VALUES ('Unclassified');
                SET projectId = LAST_INSERT_ID();
            END IF;

            IF keepReservedDatasets = TRUE THEN

               SET newDatasetId = srcDatasetId;

               UPDATE ds_dataset 
                   SET dataset_name = datasetName, tutor = tutorName, start_time = startTime,
                   end_time = endTime, status = statusVal, description = descriptionText,
                   hypothesis = hypothesisText, domain_id = domainId, learnlab_id = learnlabId,
                   study_flag = studyFlag, curriculum_id = curriculumId, notes = notesText,
                   project_id = projectId, school = schoolName,
                   auto_set_school_flag = autoSchoolFlag, auto_set_times_flag = autoTimesFlag,
                   appears_anon_flag = appearsAnonFlag, irb_uploaded = irbUploaded,
                   project_set_time = projectSetTime, data_last_modified = dataLastModified
                   WHERE dataset_id = newDatasetId;

            ELSE
               INSERT INTO ds_dataset (dataset_name, tutor, start_time, end_time, status,
                   description, hypothesis, domain_id, learnlab_id, study_flag, curriculum_id,
                   notes, project_id, school, auto_set_school_flag, auto_set_times_flag,
                   appears_anon_flag, irb_uploaded, project_set_time, data_last_modified)
               VALUES (datasetName, tutorName, startTime, endTime, statusVal,
                   descriptionText, hypothesisText, domainId, learnlabId, studyFlag, curriculumId,
                   notesText, projectId, schoolName, autoSchoolFlag, autoTimesFlag, appearsAnonFlag,
                   irbUploaded, projectSetTime, dataLastModified);

               SET newDatasetId = LAST_INSERT_ID();

            END IF;

            INSERT INTO dbm_ds_id_map (src_dataset_id, dataset_id, merge_flag, merge_appended_flag)
            VALUES (srcDatasetId, newDatasetId, FALSE, FALSE);

            CALL debug_log('db_merge_sp', CONCAT('New dataset \'', datasetName, '\' queued to merge'));
        END IF;

        SET delimPos = LOCATE(datasetDelim, datasetList);

    END WHILE;

    -- Log starting db merge message in dataset system log
    CALL dbm_insert_dataset_system_logs();
    CALL dbm_starting_merge_status(info, FALSE);
    CALL dbm_create_merge_status();

    CALL debug_log('db_merge_sp','Finished dbm_insert_new_datasets.');
END $$

/*
 -------------------------------------------------------------------------------
 Update the start end times fields in the dataset table for each dataset.
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_update_dataset_start_end_times` $$
CREATE PROCEDURE `dbm_update_dataset_start_end_times` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_update_dataset_start_end_times.');

    UPDATE ds_dataset ds
        JOIN (
            SELECT
                dataset_id,
                min(tt.transaction_time) as theMin,
                max(tt.transaction_time) as theMax
            FROM tutor_transaction tt
            JOIN dbm_ds_id_map dimInner using (dataset_id)
            GROUP BY tt.dataset_id) a ON a.dataset_id = ds.dataset_id
        JOIN dbm_ds_id_map dim ON dim.dataset_id = ds.dataset_id
        SET ds.start_time = a.theMin,
            ds.end_time = a.theMax;

    CALL debug_log('db_merge_sp', 'Finished dbm_update_dataset_start_end_times.');
END $$

/*
 -------------------------------------------------------------------------------
 Copy over munger create and modify logs from the source dataset_system_log so at
 the very least we know the version of the munger.  Prefixed with dbm_ to
 differentiate between 'create' and 'modify' actions we'll insert.
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_insert_dataset_system_logs` $$
CREATE PROCEDURE `dbm_insert_dataset_system_logs` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_insert_dataset_system_logs.');

    INSERT INTO dataset_system_log (dataset_id, time, action, info, success_flag, value,
        elapsed_time, datashop_version)
    SELECT dim.dataset_id, time, CONCAT("dbm ", action), info, success_flag, value,
        elapsed_time, IFNULL(datashop_version,'unknown')
    FROM adb_source.dataset_system_log dsl
    JOIN dbm_ds_id_map dim ON dim.src_dataset_id = dsl.dataset_id;

    CALL debug_log('db_merge_sp', 'Finished dbm_insert_dataset_system_logs.');
END $$

/*
 -------------------------------------------------------------------------------
 Called at the start of a batch of merge new or merge existing, concatenates the
 dataset IDs and this row is used to later calculate the elapsed time
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_starting_merge_status` $$
CREATE PROCEDURE `dbm_starting_merge_status` (info TEXT, merge BOOL)
    SQL SECURITY INVOKER
BEGIN
    DECLARE dslId INT;

    CALL debug_log('db_merge_sp', 'Starting dbm_starting_merge_status');

    INSERT INTO dataset_system_log(time, action, info, datashop_version)
    SELECT NOW(), "merge started", CONCAT("DB Merge: ", info, " ",
        GROUP_CONCAT(dataset_id ORDER BY dataset_id SEPARATOR ',')), IFNULL(version,'unknown')
    FROM dbm_ds_id_map
    LEFT OUTER JOIN datashop_version ON dataset_id
    LIMIT 1;

    SET dslId = LAST_INSERT_ID();

    UPDATE dbm_ds_id_map SET dsl_id = dslId WHERE merge_flag = merge;
    CALL debug_log('db_merge_sp', 'Finished dbm_starting_merge_status');
END $$

/*
 -------------------------------------------------------------------------------
 Called at the conclusion of the database merge to calculate the elapsed time
 and the sum of all the transactions of the datasets involved in the merge.
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_completed_merge_status` $$
CREATE PROCEDURE `dbm_completed_merge_status` (success BOOL)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_completed_merge_status');

    INSERT INTO dataset_system_log(time, action, info, success_flag, value,
        elapsed_time, datashop_version)
    SELECT NOW(), "merge complete", CONCAT("DB Merge: Finished merging dataset(s) '",
        GROUP_CONCAT(dim.dataset_id ORDER BY dim.dataset_id SEPARATOR ','), "'"), success,
        SUM(row_count - prev_count), (TIMESTAMPDIFF(SECOND,dsl.time,NOW()))*1000,
        IFNULL(version,'unknown')
    FROM dbm_ds_id_map dim
    LEFT OUTER JOIN datashop_version ON dataset_id
    JOIN ds_dataset ds ON dim.dataset_id = ds.dataset_id
    JOIN dataset_system_log dsl ON dsl.dataset_system_log_id = dim.dsl_id
    LEFT JOIN (SELECT row_count, prev_count, dataset_id FROM dbm_max_table_counts
               WHERE table_name = "tutor_transaction") table_counts
        ON dim.dataset_id = table_counts.dataset_id
    LIMIT 1;

CALL dbm_update_dataset_start_end_times();

    CALL debug_log('db_merge_sp', 'Finished dbm_completed_merge_status');
END $$

/*
 -------------------------------------------------------------------------------
 Called by every dataset on its initial merge into the analysis database.
 Looked for by the aggregator and cached file generator.  Existing datasets
 don't require a new create statement, only a modify.
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_create_merge_status` $$
CREATE PROCEDURE `dbm_create_merge_status` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_create_merge_status');

    INSERT INTO dataset_system_log(dataset_id, time, action, info, datashop_version)
    SELECT dataset_id, SYSDATE(), "create", CONCAT("DB Merge: New Dataset '", dataset_name, "' (",
        dataset_id, ")"), IFNULL(version,'unknown')
    FROM ds_dataset
    LEFT OUTER JOIN datashop_version ON dataset_id
    JOIN dbm_ds_id_map USING (dataset_id)
    WHERE merge_flag = FALSE;

    UPDATE ds_dataset adbds
        JOIN dbm_ds_id_map dim ON dim.dataset_id = adbds.dataset_id
      SET adbds.data_last_modified = now()
      WHERE adbds.data_last_modified IS NULL;

    CALL debug_log('db_merge_sp', 'Finished dbm_create_merge_status');
END $$

/*
 -------------------------------------------------------------------------------
 Insert into the dataset_system_log at the conclusion of every merge, new or
 existing, and write the total number of transactions for that dataset.
 -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_modify_merge_status` $$
CREATE PROCEDURE `dbm_modify_merge_status` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_modify_merge_status');

    INSERT INTO dataset_system_log(dataset_id, time, action, info, value, datashop_version)
    SELECT dim.dataset_id, NOW(), "modify", CONCAT("DB Merge: Finished populating data for ",
            "dataset(s) '", dataset_name, "' (", dim.dataset_id, ")"),
            (ifnull(row_count, 0) - ifnull(prev_count, 0)), IFNULL(version,'unknown')
    FROM dbm_ds_id_map dim
    LEFT OUTER JOIN datashop_version ON dataset_id
    LEFT JOIN adb_source.ds_dataset ds ON dim.src_dataset_id = ds.dataset_id
    LEFT JOIN dbm_max_table_counts max_count ON dim.dataset_id = max_count.dataset_id
    WHERE table_name = 'tutor_transaction';

    UPDATE ds_dataset adbds
        JOIN dbm_ds_id_map dim ON dim.dataset_id = adbds.dataset_id
      SET adbds.data_last_modified = now()
      WHERE adbds.data_last_modified IS NULL;

    CALL debug_log('db_merge_sp', 'Finished dbm_modify_merge_status');
END $$

/*
 -------------------------------------------------------------------------------
 Given a source dataset ID find the corresponding destination id by dataset_name.
 @param srcDatasetId the source dataset ID
 @return the matching dataset ID if there is one.
 -------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `dbm_get_existing_dataset_id` $$
CREATE FUNCTION `dbm_get_existing_dataset_id` (srcDatasetId BIGINT)
    RETURNS BIGINT READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE datasetId BIGINT;

    SELECT ds.dataset_id INTO datasetId
    FROM ds_dataset ds
    JOIN adb_source.ds_dataset srcds using (dataset_name)
    WHERE srcds.dataset_id = srcDatasetId;

    RETURN datasetId;
END $$
/*
 -------------------------------------------------------------------------------
 Given a source dataset ID find the corresponding destination dataset_name.
 Maybe we should just use the source dataset_name, it should match anyway.
 @param srcDatasetId the source dataset ID
 @return the matching dataset_name
 -------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `dbm_get_existing_dataset_name` $$
CREATE FUNCTION `dbm_get_existing_dataset_name` (srcDatasetId BIGINT)
    RETURNS VARCHAR(100) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE datasetName VARCHAR(100);

    SELECT ds.dataset_name INTO datasetName
    FROM ds_dataset ds
    JOIN adb_source.ds_dataset srcds using (dataset_name)
    WHERE srcds.dataset_id = srcDatasetId;

    RETURN datasetName;
END $$

/*
 -------------------------------------------------------------------------------
 Find the difference of transaction rows (src - dest) of two supposedly same datasets.
 Used as a check for when using srcAppended to make sure we really have more data.
 @param existingDatasetId dataset ID in the destination database
 @param srcDatasetId dataset ID in the source database
 @return the difference in transaction count.
 -------------------------------------------------------------------------------
 */
DROP FUNCTION IF EXISTS `dbm_compare_num_transaction` $$
CREATE FUNCTION `dbm_compare_num_transaction` (existingDatasetId BIGINT, srcDatasetId BIGINT)
    RETURNS BIGINT READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE destinationCount BIGINT;
    DECLARE sourceCount BIGINT;

    SELECT count(transaction_id)
    INTO destinationCount
    FROM tutor_transaction
    WHERE dataset_id = existingDatasetId;

    SELECT count(transaction_id)
    INTO sourceCount
    FROM adb_source.tutor_transaction
    WHERE dataset_id = srcDatasetId;

    RETURN (sourceCount - destinationCount);
END $$

/*
  -------------------------------------------------------------------------------
  Copy the dataset's levels into the dataset_level table.  The foreign key must be
  dropped because it is self referential.  The parent and child processing is already
  done for us in the adb_source table, we'll save time by not recomputing it.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_dataset_levels` $$
CREATE PROCEDURE `dbm_insert_dataset_levels` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_dataset_levels.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    /* Constraint name may vary, run 'SHOW CREATE TABLE dataset_level' to confirm its name. */
    ALTER IGNORE TABLE dataset_level DROP FOREIGN KEY `dataset_level_fkey_parent`;
    ALTER IGNORE TABLE dataset_level DROP INDEX `dataset_level_fkey_parent`;

    INSERT IGNORE INTO dataset_level (level_name, level_title, parent_id, dataset_id,
        lft, rgt, src_dataset_level_id, description)
    SELECT REPLACE(REPLACE(REPLACE(level_name, '\t', ' '), '\r', ' '), '\n', ' ') as level_name,
        REPLACE(REPLACE(REPLACE(level_title, '\t', ' '), '\r', ' '), '\n', ' ') as level_title,
        parent_id, dim.dataset_id, lft, rgt, dataset_level_id, description
    FROM adb_source.dataset_level dl
    JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.src_dataset_id
    WHERE dim.merge_flag = FALSE;

    /* Update the parent ids to match the new dataset_level_ids after insertion. */
    UPDATE dataset_level d1 SET d1.parent_id = (SELECT x.dataset_level_id
        FROM (SELECT d2.dataset_level_id, d2.src_dataset_level_id FROM dataset_level d2 ) AS x
        WHERE x.src_dataset_level_id = d1.parent_id)
    WHERE d1.dataset_id IN (SELECT dataset_id FROM dbm_ds_id_map WHERE merge_flag = FALSE)
        AND d1.parent_id IS NOT NULL;

    /* Database will give warning about 'NAME_CONST() issues' breaking the bin-log
       if a local variable is used in a 'create table ... select' statement.
       That is why they are broken into separate queries below. */
    DROP TABLE IF EXISTS dbm_ds_level_id_map;
    CREATE TABLE dbm_ds_level_id_map (src_dataset_level_id INT NOT NULL,
        dataset_level_id INT NOT NULL,
        PRIMARY KEY(src_dataset_level_id, dataset_level_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO dbm_ds_level_id_map (src_dataset_level_id, dataset_level_id)
    SELECT src_dataset_level_id, dataset_level_id
    FROM dataset_level dl
    JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.dataset_id
    WHERE src_dataset_level_id IS NOT NULL AND dim.merge_flag = FALSE;

    UPDATE dataset_level dl
    JOIN dbm_ds_level_id_map dlim ON dl.dataset_level_id = dlim.dataset_level_id
    SET dl.src_dataset_level_id = NULL;

    CALL dbm_insert_dataset_level_sequence(mappingDbName);

    CALL dbm_insert_problems();

    ALTER TABLE dataset_level ADD CONSTRAINT `dataset_level_fkey_parent` FOREIGN KEY (parent_id)
        REFERENCES dataset_level(dataset_level_id) ON DELETE CASCADE ON UPDATE CASCADE;

    CALL debug_log('db_merge_sp','Finished dbm_insert_dataset_levels.');
    END;

    IF status = 1 THEN
        CALL dbm_error_dataset_level();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge new dataset levels.  lft and rgt values pickup where the dataset
  left off in the analysis database.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_dataset_levels` $$
CREATE PROCEDURE `dbm_merge_dataset_levels` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_dataset_levels.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_ds_level_id_map;
        CREATE TABLE dbm_ds_level_id_map (src_dataset_level_id INT NOT NULL,
            dataset_level_id INT NOT NULL,
            PRIMARY KEY (src_dataset_level_id, dataset_level_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src.dataset_level_id AS src_dataset_level_id, dest.dataset_level_id
        FROM (SELECT dl.dataset_level_id, dl.level_name, IFNULL(dl.level_title,'') AS level_title,
                  IFNULL(dl2.level_name,'') AS parent_level_name, dl.dataset_id, dl.lft, dl.rgt,
                  IFNULL(dl.description,'') AS description
              FROM dataset_level dl
              LEFT JOIN dataset_level dl2 ON dl.parent_id = dl2.dataset_level_id
              JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.dataset_id
              WHERE merge_flag = TRUE) dest
        JOIN (SELECT dl.dataset_level_id, REPLACE(REPLACE(REPLACE(dl.level_name, '\t', ' '), '\r', ' '), '\n', ' ') as level_name,
                  IFNULL(REPLACE(REPLACE(REPLACE(dl.level_title, '\t', ' '), '\r', ' '), '\n', ' '), '') AS level_title,
                  IFNULL(REPLACE(REPLACE(REPLACE(dl2.level_name, '\t', ' '), '\r', ' '), '\n', ' '),'') AS parent_level_name,
                  dim.dataset_id, dl.lft, dl.rgt,
                  IFNULL(dl.description,'') AS description
              FROM adb_source.dataset_level dl
              LEFT JOIN adb_source.dataset_level dl2 ON dl.parent_id = dl2.dataset_level_id
              JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.src_dataset_id
              WHERE merge_flag = TRUE) src ON dest.level_name = src.level_name
            AND dest.level_title = src.level_title AND dest.parent_level_name = src.parent_level_name
            AND dest.dataset_id = src.dataset_id AND dest.lft = src.lft AND dest.rgt = src.rgt
            AND dest.description = src.description;

        CALL dbm_merge_dataset_level_sequence(mappingDbName);

        CALL dbm_merge_problems();


        CALL debug_log('db_merge_sp','Finished dbm_merge_dataset_levels.');
    END;

    IF status = 1 THEN
        CALL dbm_error_dataset_level();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the dataset_level_sequence table and calls inserts into
  school/instructor/class/class_dataset_map then session, which requires class_id
  and a dataset_level_seq_id.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_dataset_level_sequence` $$
CREATE PROCEDURE `dbm_insert_dataset_level_sequence` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_dataset_level_sequence.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO dataset_level_sequence (name, dataset_id,
            src_dataset_level_seq_id)
        SELECT name, dim.dataset_id, dataset_level_sequence_id
        FROM adb_source.dataset_level_sequence dls
        JOIN dbm_ds_id_map dim ON dls.dataset_id = dim.src_dataset_id
        WHERE dim.merge_flag = FALSE;

        CALL dbm_insert_dataset_level_sequence_map();
        CALL dbm_insert_schools();
        CALL dbm_insert_sessions(mappingDbName);

        UPDATE dataset_level_sequence SET src_dataset_level_seq_id = NULL WHERE src_dataset_level_seq_id IS NOT NULL;

        CALL debug_log('db_merge_sp','Finished dbm_insert_dataset_level_sequence.');
    END;

    IF status = 1 THEN
        CALL dbm_error_dataset_level_sequence();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge new dataset_level_sequences.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_dataset_level_sequence` $$
CREATE PROCEDURE `dbm_merge_dataset_level_sequence` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_dataset_level_sequence.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS `dbm_duplicate_dataset_lvl_seq`;
        CREATE TABLE `dbm_duplicate_dataset_lvl_seq` (dataset_level_sequence_id INT NOT NULL,
            src_dataset_level_seq_id INT NOT NULL,
            dataset_id INT NOT NULL,
            name VARCHAR(255) NOT NULL,
            PRIMARY KEY (src_dataset_level_seq_id, dataset_level_sequence_id),
            INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT edls.dataset_level_sequence_id,
            ndls.dataset_level_sequence_id AS src_dataset_level_seq_id, edls.dataset_id,
            REPLACE(REPLACE(REPLACE(edls.name, '\t', ' '), '\r', ' '), '\n', ' ') as name
        FROM adb_source.dataset_level_sequence ndls
        JOIN dbm_ds_id_map dim ON dim.src_dataset_id = ndls.dataset_id
        LEFT JOIN dataset_level_sequence edls ON edls.dataset_id = dim.dataset_id
            AND REPLACE(REPLACE(REPLACE(edls.name, '\t', ' '), '\r', ' '), '\n', ' ') = ndls.name
        WHERE merge_flag = TRUE AND edls.dataset_id IS NOT NULL
            AND REPLACE(REPLACE(REPLACE(edls.name, '\t', ' '), '\r', ' '), '\n', ' ') IS NOT NULL;

        UPDATE dataset_level_sequence dls
        JOIN dbm_duplicate_dataset_lvl_seq ddls USING (dataset_level_sequence_id)
        SET dls.src_dataset_level_seq_id = ddls.src_dataset_level_seq_id;

        INSERT INTO dataset_level_sequence (name, dataset_id,
            src_dataset_level_seq_id)
        SELECT REPLACE(REPLACE(REPLACE(dls.name, '\t', ' '), '\r', ' '), '\n', ' ') as name, dim.dataset_id,
            dls.dataset_level_sequence_id AS src_dataset_level_seq_id
        FROM adb_source.dataset_level_sequence dls
        JOIN dbm_ds_id_map dim ON dls.dataset_id = dim.src_dataset_id
        LEFT JOIN dbm_duplicate_dataset_lvl_seq ddls
            ON dls.dataset_level_sequence_id = ddls.src_dataset_level_seq_id
        WHERE dim.merge_flag = TRUE AND ddls.src_dataset_level_seq_id IS NULL;

        DROP TABLE IF EXISTS dbm_ds_level_seq_id_map;
        CREATE TABLE dbm_ds_level_seq_id_map (
            dataset_level_sequence_id INT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT dataset_level_sequence_id
        FROM dataset_level_sequence
        WHERE src_dataset_level_seq_id IS NOT NULL;

        CALL dbm_merge_dataset_level_sequence_map();

        CALL dbm_merge_schools();

        CALL dbm_merge_sessions(mappingDbName);

        UPDATE dataset_level_sequence SET src_dataset_level_seq_id = NULL
        WHERE src_dataset_level_seq_id IS NOT NULL;
    END;

    IF status = 1 THEN
        CALL dbm_error_dataset_level_sequence();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_dataset_level_sequence.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy school data into the school table and check for an existing school because
  school name, while not the primary key, must be unique.  If we find a matching
  school name we'll set its school_id to the carnegie learning school_id.  Current
  query is slow because we must scan the every transaction in the dataset because
  the adb_source might have schools belonging to multiple datasets.  Calls
  insertInstructor.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_schools` $$
CREATE PROCEDURE `dbm_insert_schools` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_schools.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO school (school_name, src_school_id)
        SELECT DISTINCT REPLACE(REPLACE(REPLACE(school_name, '\t', ' '), '\r', ' '), '\n', ' '), sch.school_id
        FROM adb_source.school sch
        JOIN dbm_src_tt_view tt ON sch.school_id = tt.school_id
            ON DUPLICATE KEY UPDATE src_school_id = sch.school_id;

        DROP TABLE IF EXISTS dbm_school_id_map;
        CREATE TABLE dbm_school_id_map (src_school_id INT NOT NULL,
            school_id INT NOT NULL,
            PRIMARY KEY (src_school_id, school_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_school_id, school_id
        FROM school
        WHERE src_school_id IS NOT NULL;

        UPDATE school sch JOIN dbm_school_id_map sim ON sch.school_id = sim.school_id
        SET sch.src_school_id = NULL;

        CALL dbm_insert_instructors();

        CALL debug_log('db_merge_sp','Finished dbm_insert_schools.');
    END;

    IF status = 1 THEN
        CALL dbm_error_school();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge new schools.  School names have a unique constraint across all
  datasets so we can leverage ON DUPLICATE KEY UPDATE to find duplicates.  Search
  all places a school may be: tutor_transaction, session, class tables for all
  distinct school names because school_id isn't a foreign key in any one table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_schools` $$
CREATE PROCEDURE `dbm_merge_schools` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_schools.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_schools;
        CREATE TABLE dbm_new_schools (
            school_id INT NOT NULL PRIMARY KEY,
            school_name VARCHAR(100),
            INDEX (school_name)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sch.school_id,
            REPLACE(REPLACE(REPLACE(sch.school_name, '\t', ' '), '\r', ' '), '\n', ' ') as school_name
        FROM adb_source.school sch
        JOIN (SELECT DISTINCT school_id
              FROM dbm_src_tt_view
              UNION
              SELECT DISTINCT school_id FROM adb_source.session sess
              JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id
              WHERE merge_flag = TRUE
              UNION
              SELECT DISTINCT school_id FROM adb_source.class cl
              JOIN adb_source.class_dataset_map cdm ON cl.class_id = cdm.class_id
              JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.src_dataset_id
              WHERE merge_flag = TRUE) allsch ON sch.school_id = allsch.school_id;

        DROP TABLE IF EXISTS dbm_duplicate_schools;
        CREATE TABLE dbm_duplicate_schools (
            school_id INT NOT NULL,
            src_school_id INT NOT NULL,
            PRIMARY KEY (school_id, src_school_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sch.school_id, nsch.school_id AS src_school_id
        FROM dbm_new_schools nsch
        JOIN school sch ON nsch.school_name = sch.school_name;

        UPDATE school sch JOIN dbm_duplicate_schools dsch ON sch.school_id = dsch.school_id
        SET sch.src_school_id = dsch.src_school_id;

        INSERT INTO school (school_name, src_school_id)
        SELECT nsch.school_name, nsch.school_id
        FROM dbm_new_schools nsch
        LEFT JOIN dbm_duplicate_schools dsch ON nsch.school_id = dsch.src_school_id
        WHERE dsch.src_school_id IS NULL;

        DROP TABLE IF EXISTS dbm_school_id_map;
        CREATE TABLE dbm_school_id_map (src_school_id INT NOT NULL,
            school_id INT NOT NULL,
            PRIMARY KEY (src_school_id, school_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_school_id, school_id
        FROM school
        WHERE src_school_id IS NOT NULL;

        UPDATE school sch JOIN dbm_school_id_map sim ON sch.school_id = sim.school_id
        SET sch.src_school_id = NULL;

        CALL dbm_merge_instructors();
    END;

    IF status = 1 THEN
        CALL dbm_error_school();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_schools.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the instructor table with a left join on school_id because
  analysis_db permits a null school_id.  Calls insertClass.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_instructors` $$
CREATE PROCEDURE `dbm_insert_instructors` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_instructors.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_instructors;
        CREATE TABLE dbm_new_instructors (instructor_id BIGINT PRIMARY KEY,
            instructor_name VARCHAR(55) NOT NULL,
            school_id INT,
            INDEX(instructor_name, school_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT instructor_id, instructor_name, sim.school_id
        FROM (SELECT instructor_id,
                  REPLACE(REPLACE(REPLACE(instructor_name, '\t', ' '), '\r', ' '), '\n', ' ') as instructor_name, i.school_id
              FROM adb_source.instructor i
              JOIN dbm_school_id_map sim ON sim.src_school_id = i.school_id
              UNION
              SELECT instructor_id,
                  REPLACE(REPLACE(REPLACE(instructor_name, '\t', ' '), '\r', ' '), '\n', ' ') as instructor_name, i.school_id
              FROM adb_source.instructor i
              JOIN adb_source.class USING (instructor_id)
              JOIN adb_source.class_dataset_map cdm USING (class_id)
              JOIN dbm_ds_id_map dim ON dim.src_dataset_id = cdm.dataset_id
              WHERE merge_flag = FALSE) src
        LEFT JOIN dbm_school_id_map sim ON sim.src_school_id = src.school_id;

    DROP TABLE IF EXISTS dbm_duplicate_instructors;
    CREATE TABLE dbm_duplicate_instructors (instructor_id BIGINT PRIMARY KEY,
        src_instructor_id BIGINT
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT destinstr.instructor_id, srcinstr.instructor_id AS src_instructor_id
    FROM (SELECT instructor_id, instructor_name, IFNULL(school_id,0) AS school_id
          FROM dbm_new_instructors) srcinstr
    JOIN (SELECT instructor_id, instructor_name, IFNULL(school_id,0) AS school_id
          FROM instructor) destinstr USING (instructor_name, school_id);

        UPDATE instructor i JOIN dbm_duplicate_instructors di ON i.instructor_id = di.instructor_id
        SET i.src_instructor_id = di.src_instructor_id;

    INSERT INTO instructor (instructor_name, school_id, src_instructor_id)
        SELECT newinstr.instructor_name, school_id, newinstr.instructor_id
        FROM dbm_new_instructors newinstr
        WHERE instructor_id NOT IN (SELECT src_instructor_id FROM dbm_duplicate_instructors);

        DROP TABLE IF EXISTS dbm_instructor_id_map;
        CREATE TABLE dbm_instructor_id_map (
            instructor_id BIGINT NOT NULL,
            src_instructor_id BIGINT NOT NULL,
            PRIMARY KEY (instructor_id, src_instructor_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT instructor_id, src_instructor_id
        FROM instructor
        WHERE src_instructor_id IS NOT NULL;

        CALL dbm_insert_classes();

        /**
         * [12/06/04 - ysahn] Brought from 3128, SP: dbm_insert_students
         * Since dbm_insert_students was moved to be called before db_insert_dataset_levels() in run_merge,
         * this caused dependency break: dbm_insert_student calls dbm_insert_rosters which reads
         * dbm_class_id_map but the table is not created until the  dbm_insert_classes() is called by
         * db_insert_dataset_levels.
         *
         */
        CALL dbm_insert_rosters();

        UPDATE instructor SET src_instructor_id = NULL WHERE src_instructor_id IS NOT NULL;

        CALL debug_log('db_merge_sp','Finished dbm_insert_instructors.');
    END;

    IF status = 1 THEN
        CALL dbm_error_instructor();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge new instructors.  Calls dbm_merge_class.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_instructors` $$
CREATE PROCEDURE `dbm_merge_instructors` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_instructors.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_instructors;
        CREATE TABLE dbm_new_instructors (instructor_id BIGINT PRIMARY KEY,
            instructor_name VARCHAR(55) NOT NULL,
            school_id INT,
            INDEX (instructor_name, school_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT instructor_id, instructor_name, IFNULL(sim.school_id,0) AS school_id
        FROM (SELECT DISTINCT instructor_id,
              REPLACE(REPLACE(REPLACE(instructor_name, '\t', ' '), '\r', ' '), '\n', ' ') as instructor_name, i.school_id
              FROM adb_source.instructor i
              JOIN (SELECT DISTINCT school_id
                    FROM adb_source.session sess
                    JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id
                    WHERE merge_flag = TRUE) uniqsess USING (school_id)
              UNION
              SELECT DISTINCT instructor_id,
              REPLACE(REPLACE(REPLACE(instructor_name, '\t', ' '), '\r', ' '), '\n', ' ') as instructor_name, i.school_id
              FROM adb_source.instructor i
              JOIN adb_source.class USING (instructor_id)
              JOIN adb_source.class_dataset_map cdm USING (class_id)
              JOIN dbm_ds_id_map dim ON dim.src_dataset_id = cdm.dataset_id
              WHERE merge_flag = TRUE) src
        LEFT JOIN dbm_school_id_map sim ON sim.src_school_id = src.school_id;

        DROP TABLE IF EXISTS dbm_duplicate_instructors;
        CREATE TABLE dbm_duplicate_instructors (instructor_id BIGINT NOT NULL,
            src_instructor_id BIGINT NOT NULL,
            PRIMARY KEY (src_instructor_id, instructor_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT destinstr.instructor_id, srcinstr.instructor_id AS src_instructor_id
        FROM dbm_new_instructors srcinstr
        JOIN (SELECT instructor_id, instructor_name, IFNULL(school_id,0) AS school_id
              FROM instructor) destinstr USING (instructor_name, school_id);

        UPDATE instructor i JOIN dbm_duplicate_instructors di ON i.instructor_id = di.instructor_id
        SET i.src_instructor_id = di.src_instructor_id;

        INSERT INTO instructor (instructor_name, school_id, src_instructor_id)
        SELECT newinstr.instructor_name, sim.school_id, newinstr.instructor_id
        FROM dbm_new_instructors newinstr
        JOIN adb_source.instructor i USING (instructor_id)
        LEFT JOIN dbm_school_id_map sim ON sim.src_school_id = i.school_id
        LEFT JOIN dbm_duplicate_instructors dinstr ON newinstr.instructor_id = dinstr.src_instructor_id
        WHERE dinstr.src_instructor_id IS NULL;

        DROP TABLE IF EXISTS dbm_instructor_id_map;
        CREATE TABLE dbm_instructor_id_map (
            instructor_id BIGINT NOT NULL,
            src_instructor_id BIGINT NOT NULL,
            PRIMARY KEY (instructor_id, src_instructor_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT instructor_id, src_instructor_id
        FROM instructor
        WHERE src_instructor_id IS NOT NULL;

        CALL dbm_merge_classes();

        /**
         * [12/06/13 - ysahn] Brought from 3128, SP: dbm_merge_students
         * Since dbm_merge_students was moved to be called before db_merge_dataset_levels() in run_merge,
         * this caused dependency break: dbm_insert_student calls dbm_merge_rosters which reads
         * dbm_class_id_map but the table is not created until the dbm_merge_classes() is called by
         * db_merge_dataset_levels.
         *
         */
        CALL dbm_merge_rosters();

        UPDATE instructor SET src_instructor_id = NULL WHERE src_instructor_id IS NOT NULL;
    END;

    IF status = 1 THEN
        CALL dbm_error_instructor();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_instructors.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the class table with left joins on school and instructor because
  null values are permitted.  Join with class_dataset_map to ensure limitted to
  only the dataset we want.  Datasets should share a class if all class fields
  are the same and not insert new.  Calls dbm_insert_class_dataset_map.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_classes` $$
CREATE PROCEDURE `dbm_insert_classes` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_classes.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS `dbm_duplicate_classes`;
        CREATE TABLE `dbm_duplicate_classes` (class_id BIGINT PRIMARY KEY,
            src_class_id BIGINT
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cl.class_id, srccl.class_id as src_class_id
        FROM (SELECT class_id, class_name, IFNULL(period,'') AS period, IFNULL(description,'') AS description,
                  IFNULL(school_id,0) AS school_id, IFNULL(instructor_id,0) AS instructor_id
              FROM class) cl
        JOIN (SELECT DISTINCT adbcl.class_id,
                  REPLACE(REPLACE(REPLACE(class_name, '\t', ' '), '\r', ' '), '\n', ' ') AS class_name,
                  REPLACE(REPLACE(REPLACE(IFNULL(period,''), '\t', ' '), '\r', ' '), '\n', ' ') AS period,
                  IFNULL(description,'') AS description, IFNULL(sim.school_id,0) AS school_id,
                  IFNULL(i.instructor_id,0) AS instructor_id
              FROM adb_source.class adbcl
              JOIN adb_source.class_dataset_map cdm ON adbcl.class_id = cdm.class_id
              JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.src_dataset_id
              LEFT JOIN dbm_school_id_map sim ON adbcl.school_id = sim.src_school_id
              LEFT JOIN instructor i ON adbcl.instructor_id = i.src_instructor_id
              WHERE merge_flag = FALSE) srccl
            ON cl.class_name = REPLACE(REPLACE(REPLACE(srccl.class_name, '\t', ' '), '\r', ' '), '\n', ' ') AND
                cl.period = REPLACE(REPLACE(REPLACE(srccl.period, '\t', ' '), '\r', ' '), '\n', ' ')
                AND cl.description = srccl.description AND cl.school_id = srccl.school_id
                AND cl.instructor_id = srccl.instructor_id;

        UPDATE class cl JOIN dbm_duplicate_classes dcl USING (class_id)
        SET cl.src_class_id = dcl.src_class_id;

        INSERT INTO class (class_name, period, description, school_id, instructor_id, src_class_id)
        SELECT DISTINCT
            REPLACE(REPLACE(REPLACE(class_name, '\t', ' '), '\r', ' '), '\n', ' ') as class_name,
            REPLACE(REPLACE(REPLACE(period, '\t', ' '), '\r', ' '), '\n', ' ') as period,
            description, sim.school_id, i.instructor_id, c.class_id
        FROM adb_source.class c
        LEFT JOIN dbm_school_id_map sim ON sim.src_school_id = c.school_id
        LEFT JOIN instructor i ON i.src_instructor_id = c.instructor_id
        JOIN adb_source.class_dataset_map cdm ON cdm.class_id = c.class_id
        JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.src_dataset_id
        WHERE merge_flag = FALSE AND c.class_id NOT IN (SELECT src_class_id
            FROM dbm_duplicate_classes);

        DROP TABLE IF EXISTS dbm_class_id_map;
        CREATE TABLE dbm_class_id_map (src_class_id BIGINT NOT NULL,
            class_id BIGINT NOT NULL,
            PRIMARY KEY (src_class_id, class_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_class_id, class_id
        FROM class
        WHERE src_class_id IS NOT NULL;

        UPDATE class cl JOIN dbm_class_id_map cim ON cl.class_id = cim.class_id
        SET cl.src_class_id = NULL;

        CALL dbm_insert_class_dataset_map();

        CALL debug_log('db_merge_sp','Finished dbm_insert_classes.');
    END;

    IF status = 1 THEN
        CALL dbm_error_class();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge new classes.  Unique classes aren't enforced in the database
  however we shouldn't insert a new class if all class data exists already in
  another dataset.  Every existing class is searched, not just of the dataset
  being merged with.  Calls dbm_merge_class_dataset_map.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_classes` $$
CREATE PROCEDURE `dbm_merge_classes` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_classes.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_classes;
        CREATE TABLE dbm_new_classes (class_id BIGINT PRIMARY KEY,
            dataset_id INT,
            INDEX(dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT class_id
        FROM dbm_src_tt_view
        WHERE class_id IS NOT NULL;

        DROP TABLE IF EXISTS `dbm_duplicate_classes`;
        CREATE TABLE `dbm_duplicate_classes` (class_id BIGINT NOT NULL,
            src_class_id BIGINT NOT NULL,
            PRIMARY KEY (src_class_id, class_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cl.class_id, srccl.class_id as src_class_id
        FROM (SELECT class_id, class_name, IFNULL(period,'') AS period,
                  IFNULL(description,'') AS description, IFNULL(school_id,0) AS school_id,
                  IFNULL(instructor_id,0) AS instructor_id
              FROM class) cl
        JOIN (SELECT adbcl.class_id, REPLACE(REPLACE(REPLACE(class_name, '\t', ' '), '\r', ' '), '\n', ' ') as class_name,
                  REPLACE(REPLACE(REPLACE(IFNULL(period,''), '\t', ' '), '\r', ' '), '\n', ' ') AS period,
                  IFNULL(description,'') AS description, IFNULL(sim.school_id,0) AS school_id,
                  IFNULL(i.instructor_id,0) AS instructor_id
              FROM dbm_new_classes newcl
              JOIN adb_source.class adbcl ON newcl.class_id = adbcl.class_id
              LEFT JOIN dbm_school_id_map sim ON adbcl.school_id = sim.src_school_id
              LEFT JOIN instructor i ON adbcl.instructor_id = i.src_instructor_id) srccl
            ON cl.class_name = srccl.class_name
                AND cl.period = srccl.period
                AND cl.description = srccl.description AND cl.school_id = srccl.school_id
                AND cl.instructor_id = srccl.instructor_id;

        UPDATE class c JOIN dbm_duplicate_classes ddc ON c.class_id = ddc.class_id
        SET c.src_class_id = ddc.src_class_id;

        INSERT INTO class (class_name, period, description, instructor_id, school_id, src_class_id)
        SELECT REPLACE(REPLACE(REPLACE(class_name, '\t', ' '), '\r', ' '), '\n', ' ') as class_name,
            REPLACE(REPLACE(REPLACE(period, '\t', ' '), '\r', ' '), '\n', ' ') as period,
            description, i.instructor_id, sim.school_id, dnc.class_id
        FROM dbm_new_classes dnc
        JOIN adb_source.class c USING (class_id)
        LEFT JOIN instructor i ON i.src_instructor_id = c.instructor_id
        LEFT JOIN dbm_school_id_map sim ON sim.src_school_id = c.school_id
        LEFT JOIN dbm_duplicate_classes dcl ON dnc.class_id = dcl.src_class_id
        WHERE dcl.src_class_id IS NULL;

        DROP TABLE IF EXISTS dbm_class_id_map;
        CREATE TABLE dbm_class_id_map (src_class_id BIGINT NOT NULL,
            class_id BIGINT NOT NULL,
            PRIMARY KEY (src_class_id, class_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_class_id, class_id
        FROM class
        WHERE src_class_id IS NOT NULL;

        UPDATE class cl JOIN dbm_class_id_map cim ON cl.class_id = cim.class_id
        SET cl.src_class_id = NULL;

        CALL dbm_merge_class_dataset_map();
    END;

    IF status = 1 THEN
        CALL dbm_error_class();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_classes.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the class dataset map.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_class_dataset_map` $$
CREATE PROCEDURE `dbm_insert_class_dataset_map` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp','Starting dbm_insert_class_dataset_map.');

    INSERT INTO class_dataset_map (class_id, dataset_id)
    SELECT cim.class_id, dim.dataset_id
    FROM adb_source.class_dataset_map cdm
    JOIN dbm_class_id_map cim ON cim.src_class_id = cdm.class_id
    JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.src_dataset_id
    WHERE dim.merge_flag = FALSE;

    CALL debug_log('db_merge_sp','Finished dbm_insert_class_dataset_map.');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge data into the class_dataset_map.  If class and dataset pair
  already exist use ON DUPLICATE KEY UPDATE to essentially perform no operation.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_class_dataset_map` $$
CREATE PROCEDURE `dbm_merge_class_dataset_map` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp','Starting dbm_merge_class_dataset_map.');

    DROP TABLE IF EXISTS dbm_existing_class_dataset_map;
    CREATE TABLE dbm_existing_class_dataset_map (class_id BIGINT NOT NULL,
        dataset_id INT NOT NULL,
        PRIMARY KEY (class_id, dataset_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT cdm.class_id, cdm.dataset_id
    FROM class_dataset_map cdm
    JOIN dbm_class_id_map cim ON cdm.class_id = cim.class_id
    JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.dataset_id;

    INSERT INTO class_dataset_map (class_id, dataset_id)
    SELECT cim.class_id, dim.dataset_id
    FROM adb_source.class_dataset_map srccdm
    JOIN dbm_class_id_map cim ON srccdm.class_id = cim.src_class_id
    JOIN dbm_ds_id_map dim ON dim.src_dataset_id = srccdm.dataset_id
    WHERE merge_flag = TRUE
        ON DUPLICATE KEY UPDATE dataset_id = dim.dataset_id;

    CALL debug_log('db_merge_sp','Finished dbm_merge_class_dataset_map.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the session table with left joins on class, school, and dataset_level
  ids that may be null.  Calls dbm_insert_dataset_level_events.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_sessions` $$
CREATE PROCEDURE `dbm_insert_sessions` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    DECLARE sqlStr TEXT;
    CALL debug_log('db_merge_sp','Starting dbm_insert_sessions.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    SET sqlStr = CONCAT('INSERT INTO session (session_tag, start_time, start_time_ms, end_time, end_time_ms, ',
                        'completion_code, dataset_id, class_id, school_id, dataset_level_sequence_id, src_session_id, student_id) ',
                        'SELECT session_tag, start_time, start_time_ms, end_time, end_time_ms, completion_code, dim.dataset_id, ',
                        'cim.class_id, scim.school_id, dls.dataset_level_sequence_id, sess.session_id, stu.orig_student_id ',
                        'FROM adb_source.session sess ',
                        'LEFT JOIN dbm_class_id_map cim ON cim.src_class_id = sess.class_id ',
                        'LEFT JOIN dbm_school_id_map scim ON scim.src_school_id = sess.school_id ',
                        'LEFT JOIN dataset_level_sequence dls ON dls.src_dataset_level_seq_id = sess.dataset_level_sequence_id ',
                        'JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id ',
                        'JOIN adb_source.student sstu ON sess.student_id = sstu.student_id ',
                        'JOIN ', mappingDbName, '.mapped_student stu ON sstu.actual_user_id = stu.actual_user_id ',
                        'WHERE dim.merge_flag = FALSE');
        CALL exec(sqlStr);

        DROP TABLE IF EXISTS dbm_session_id_map;
        CREATE TABLE dbm_session_id_map(src_session_id BIGINT NOT NULL,
           session_id BIGINT NOT NULL,
           dataset_id INT NOT NULL,
           PRIMARY KEY (src_session_id, session_id),
           INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

        INSERT INTO dbm_session_id_map (src_session_id, session_id, dataset_id)
        SELECT src_session_id, session_id, sess.dataset_id
        FROM session sess
        JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.dataset_id
        WHERE src_session_id IS NOT NULL AND dim.merge_flag = FALSE;

        UPDATE session sess JOIN dbm_session_id_map sim ON sim.session_id = sess.session_id
        SET sess.src_session_id = NULL;

        CALL dbm_insert_dataset_level_events();

        CALL debug_log('db_merge_sp','Finished dbm_insert_sessions.');
    END;

    IF status = 1 THEN
        CALL dbm_error_session();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge session data.  Calls dbm_merge_dataset_level_events.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_sessions` $$
CREATE PROCEDURE `dbm_merge_sessions` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    DECLARE sqlStr TEXT;
    CALL debug_log('db_merge_sp','Starting dbm_merge_sessions.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_duplicate_sessions;
        CREATE TABLE dbm_duplicate_sessions(session_id BIGINT NOT NULL,
            src_session_id BIGINT NOT NULL,
            dataset_id INT,
            PRIMARY KEY (src_session_id, session_id),
            INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT destsess.session_id, srcsess.session_id AS src_session_id
        FROM (SELECT session_id, session_tag, dim.dataset_id
              FROM adb_source.session sess
              JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id
              WHERE merge_flag = TRUE) srcsess
        JOIN (SELECT session_id, session_tag, dataset_id
              FROM session
              JOIN dbm_ds_id_map USING (dataset_id)
              WHERE merge_flag = TRUE) destsess USING (session_tag, dataset_id);

        UPDATE session sess
        JOIN dbm_duplicate_sessions dsess ON sess.session_id = dsess.session_id
        JOIN adb_source.session srcsess ON dsess.src_session_id = srcsess.session_id
        SET sess.src_session_id = dsess.src_session_id,
            sess.end_time = IF(TIMESTAMPDIFF(SECOND,IFNULL(srcsess.end_time,'1900-01-01 00:00:00'),
                IFNULL(sess.end_time,'1900-01-01 00:00:00')) < 0,srcsess.end_time, sess.end_time),
            sess.end_time_ms = IF(IFNULL(sess.end_time_ms,0) < IFNULL(srcsess.end_time_ms,0),
                srcsess.end_time_ms, sess.end_time_ms);

        /**
         * [12/04/23 - ysahn] Joining with student to obtain the student_id
         */

        SET sqlStr = CONCAT('INSERT INTO session (session_tag, start_time, start_time_ms, end_time, end_time_ms, ',
                            'completion_code, dataset_id, class_id, school_id, dataset_level_sequence_id, src_session_id, student_id) ',
                            'SELECT session_tag, start_time, start_time_ms, end_time, end_time_ms, completion_code, dim.dataset_id, ',
                            'cim.class_id, scim.school_id, dls.dataset_level_sequence_id, sess.session_id, stu.orig_student_id ',
                            'FROM adb_source.session sess ',
                            'LEFT JOIN dbm_class_id_map cim ON cim.src_class_id = sess.class_id ',
                            'LEFT JOIN dbm_school_id_map scim ON scim.src_school_id = sess.school_id ',
                            'LEFT JOIN dataset_level_sequence dls ON dls.src_dataset_level_seq_id = sess.dataset_level_sequence_id ',
                            'LEFT JOIN dbm_duplicate_sessions dsess ON sess.session_id = dsess.src_session_id ',
                            'JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id ',
                            'JOIN adb_source.student sstu ON sess.student_id = sstu.student_id ',
                            'JOIN ', mappingDbName, '.mapped_student stu ON sstu.actual_user_id = stu.actual_user_id ',
                            'WHERE dim.merge_flag = TRUE AND dsess.src_session_id IS NULL');
        CALL exec(sqlStr);

        DROP TABLE IF EXISTS dbm_session_id_map;
        CREATE TABLE dbm_session_id_map(src_session_id BIGINT NOT NULL,
            session_id BIGINT NOT NULL,
            dataset_id INT NOT NULL,
            PRIMARY KEY (src_session_id, session_id),
            INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_session_id, session_id, sess.dataset_id
        FROM session sess
        JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.dataset_id
        WHERE src_session_id IS NOT NULL AND merge_flag = TRUE;

        UPDATE session sess JOIN dbm_session_id_map sim ON sim.session_id = sess.session_id
        SET sess.src_session_id = NULL;

        CALL dbm_merge_dataset_level_events();
    END;

    IF status = 1 THEN
        CALL dbm_error_session();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_sessions.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the dataset_level_event table.  No id mapping is necessary because
  the primary key isn't referenced by any other table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_dataset_level_events` $$
CREATE PROCEDURE `dbm_insert_dataset_level_events` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp','Starting dbm_insert_dataset_level_events.');

    INSERT INTO dataset_level_event (dataset_level_id, session_id, start_time, start_time_ms,
        event_flag, event_type, level_view)
    SELECT dlim.dataset_level_id, sim.session_id, dle.start_time, dle.start_time_ms, event_flag,
        event_type, dle.level_view
    FROM adb_source.dataset_level_event dle
    JOIN dbm_ds_level_id_map dlim ON dlim.src_dataset_level_id = dle.dataset_level_id
    JOIN dbm_session_id_map sim ON sim.src_session_id = dle.session_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_dataset_level_events.');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge data to the dataset_level_event table.  No id mapping is necessary
  because the primary key isn't referenced by any other table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_dataset_level_events` $$
CREATE PROCEDURE `dbm_merge_dataset_level_events` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE maxDsLvlEvt BIGINT;
    DECLARE minDsLvlEvt BIGINT;

    CALL debug_log('db_merge_sp','Starting dbm_merge_dataset_level_events.');

    DROP TABLE IF EXISTS dbm_new_dataset_level_events;
    CREATE TABLE dbm_new_dataset_level_events (dataset_level_event_id BIGINT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT srcdle.dataset_level_event_id
    FROM (SELECT dataset_level_event_id, dlim.dataset_level_id, seim.session_id, dle.start_time,
              IFNULL(dle.start_time_ms,-1) AS start_time_ms, IFNULL(event_type,0) AS event_type,
              event_flag, IFNULL(level_view,0) AS level_view
          FROM adb_source.dataset_level_event dle
          JOIN dbm_ds_level_id_map dlim ON dlim.src_dataset_level_id = dle.dataset_level_id
          JOIN dbm_session_id_map seim ON seim.src_session_id = dle.session_id) srcdle
    LEFT JOIN (SELECT dataset_level_event_id, dataset_level_id, session_id, dle.start_time,
                   IFNULL(dle.start_time_ms,-1) AS start_time_ms,
                   IFNULL(event_type,0) AS event_type, event_flag,
                   IFNULL(level_view,0) AS level_view
               FROM dataset_level_event dle
               JOIN dataset_level dl USING (dataset_level_id)
               JOIN session USING (session_id)
               JOIN dbm_ds_id_map dim ON dim.dataset_id =  dl.dataset_id
               WHERE merge_flag = TRUE) destdle ON srcdle.dataset_level_id = destdle.dataset_level_id
        AND srcdle.session_id = destdle.session_id AND srcdle.start_time = destdle.start_time
        AND srcdle.start_time_ms = destdle.start_time_ms AND srcdle.event_flag = destdle.event_flag
        AND srcdle.event_type = destdle.event_type AND srcdle.level_view = destdle.level_view
    WHERE destdle.dataset_level_id IS NULL AND destdle.session_id IS NULL
        AND destdle.start_time IS NULL AND destdle.start_time_ms IS NULL
        AND destdle.event_flag IS NULL AND destdle.event_type IS NULL
        AND destdle.level_view IS NULL;

    SELECT MAX(dataset_level_event_id) INTO minDsLvlEvt FROM dataset_level_event;

    INSERT INTO dataset_level_event (session_id, dataset_level_id, start_time, event_type,
        event_flag, start_time_ms, level_view)
    SELECT session_id, dataset_level_id, start_time, event_type, event_flag,
        start_time_ms, level_view
    FROM dbm_new_dataset_level_events ndle
    JOIN adb_source.dataset_level_event USING (dataset_level_event_id);

    SELECT MAX(dataset_level_event_id) INTO maxDsLvlEvt FROM dataset_level_event;

    DROP TABLE IF EXISTS dbm_ds_level_event_range;
    CREATE TABLE dbm_ds_level_event_range (
        dataset_level_event_id BIGINT NOT NULL,
        session_id BIGINT NOT NULL,
        dataset_level_id INT NOT NULL,
        PRIMARY KEY (dataset_level_event_id, session_id, dataset_level_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dataset_level_event_id, session_id, dataset_level_id
    FROM dataset_level_event
    WHERE dataset_level_event_id > minDsLvlEvt AND dataset_level_event_id <= maxDsLvlEvt;

    CALL debug_log('db_merge_sp','Finished dbm_merge_dataset_level_events.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the dataset_level_sequence_map table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_dataset_level_sequence_map` $$
CREATE PROCEDURE `dbm_insert_dataset_level_sequence_map` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp','Starting dbm_insert_dataset_level_sequence_map.');

    INSERT INTO dataset_level_sequence_map (dataset_level_sequence_id, dataset_level_id, sequence)
    SELECT dls.dataset_level_sequence_id, dlim.dataset_level_id, dlsm.sequence
    FROM adb_source.dataset_level_sequence_map dlsm
    JOIN dataset_level_sequence dls ON dlsm.dataset_level_sequence_id = dls.src_dataset_level_seq_id
    JOIN dbm_ds_level_id_map dlim ON dlsm.dataset_level_id = dlim.src_dataset_level_id
    JOIN dbm_ds_id_map dim ON dls.dataset_id = dim.dataset_id
    WHERE dim.merge_flag = FALSE;

    CALL debug_log('db_merge_sp','Finished dbm_insert_dataset_level_sequence_map.');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge data to the dataset_level_sequence_map table.  If we insert
  a duplicate primary key update the sequence column by chance it may have an
  updated value.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_dataset_level_sequence_map` $$
CREATE PROCEDURE `dbm_merge_dataset_level_sequence_map` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp','Starting dbm_merge_dataset_level_sequence_map.');

    INSERT INTO dataset_level_sequence_map (dataset_level_sequence_id, dataset_level_id, sequence)
    SELECT dls.dataset_level_sequence_id, dlim.dataset_level_id, dlsm.sequence
    FROM adb_source.dataset_level_sequence_map dlsm
    JOIN dataset_level_sequence dls ON dlsm.dataset_level_sequence_id = dls.src_dataset_level_seq_id
    JOIN dbm_ds_level_id_map dlim ON dlsm.dataset_level_id = dlim.src_dataset_level_id
    JOIN dbm_ds_id_map dim ON dls.dataset_id = dim.dataset_id
    WHERE dim.merge_flag = TRUE
        ON DUPLICATE KEY UPDATE sequence = dlsm.sequence;

    CALL debug_log('db_merge_sp','Finished dbm_merge_dataset_level_sequence_map.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the problem table.  Calls dbm_insert_subgoals and
  dbm_insert_problem_events.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_problems` $$
CREATE PROCEDURE `dbm_insert_problems` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_problems.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO problem (problem_name, problem_description, dataset_level_id,
                tutor_flag, tutor_other, src_problem_id)
            SELECT REPLACE(REPLACE(REPLACE(problem_name, '\t', ' '), '\r', ' '), '\n', ' ') as problem_name, problem_description, dlim.dataset_level_id,
                tutor_flag, tutor_other, problem_id
            FROM adb_source.problem p
            JOIN dbm_ds_level_id_map dlim ON dlim.src_dataset_level_id = p.dataset_level_id;

        DROP TABLE IF EXISTS dbm_problem_id_map;
        CREATE TABLE dbm_problem_id_map (
                src_problem_id BIGINT NOT NULL,
                problem_id BIGINT NOT NULL,
                PRIMARY KEY (src_problem_id, problem_id)
            ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
            SELECT src_problem_id, problem_id
            FROM problem
            WHERE src_problem_id IS NOT NULL;

        UPDATE problem p JOIN dbm_problem_id_map pim ON p.problem_id = pim.problem_id
            SET p.src_problem_id = NULL;

        CALL dbm_insert_subgoals();
        CALL dbm_insert_problem_events();

        CALL debug_log('db_merge_sp','Finished dbm_insert_problems.');
    END;

    IF status = 1 THEN
        CALL dbm_error_problem();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge problem data.  Calls dbm_merge_subgoals and
  dbm_merge_problem_events.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_problems` $$
CREATE PROCEDURE `dbm_merge_problems` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_problems.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_problems;
        CREATE TABLE dbm_new_problems (problem_id BIGINT PRIMARY KEY,
            problem_name VARCHAR(255) NOT NULL,
            problem_description TEXT,
            dataset_level_id INT NOT NULL,
            tutor_flag ENUM('tutor', 'test', 'pre-test', 'post-test', 'other'),
            tutor_other VARCHAR(50),
            INDEX (problem_name(25), dataset_level_id, tutor_flag, tutor_other)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT problem_id, REPLACE(REPLACE(REPLACE(problem_name, '\t', ' '), '\r', ' '), '\n', ' ') as problem_name,
            problem_description, dlim.dataset_level_id, tutor_flag, tutor_other
        FROM adb_source.problem p
        JOIN dbm_ds_level_id_map dlim ON p.dataset_level_id = dlim.src_dataset_level_id;

        /* tutor_flag is an ENUM so we can't test for duplicates if it is null because it
         can't be set to the dummy empty string, which is the reason for the union. */
        DROP TABLE IF EXISTS dbm_duplicate_problems;
        CREATE TABLE dbm_duplicate_problems (problem_id BIGINT NOT NULL,
            src_problem_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_id, problem_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT destprob.problem_id, srcprob.problem_id AS src_problem_id
        FROM (SELECT problem_id, problem_name,
                  IFNULL(problem_description,'') AS problem_description, dataset_level_id,
                  tutor_flag, IFNULL(tutor_other,'') AS tutor_other
              FROM dbm_new_problems) srcprob
        JOIN (SELECT problem_id, problem_name,
                  IFNULL(problem_description,'') AS problem_description, p.dataset_level_id,
                  tutor_flag, IFNULL(tutor_other,'') AS tutor_other
              FROM problem p
              JOIN dataset_level dl ON dl.dataset_level_id = p.dataset_level_id
              JOIN dbm_ds_id_map dim ON dim.dataset_id = dl.dataset_id
              WHERE merge_flag = TRUE) destprob USING (problem_name, dataset_level_id, tutor_flag,
                  tutor_other, problem_description)
        UNION
        SELECT destprob.problem_id, srcprob.problem_id AS src_problem_id
        FROM (SELECT problem_id, problem_name,
                  IFNULL(problem_description,'') AS problem_description, dataset_level_id,
                  IFNULL(tutor_other,'') AS tutor_other
              FROM dbm_new_problems
              WHERE tutor_flag IS NULL) srcprob
        JOIN (SELECT problem_id, problem_name,
                  IFNULL(problem_description,'') AS problem_description, p.dataset_level_id,
                  IFNULL(tutor_other,'') AS tutor_other
              FROM problem p
              JOIN dataset_level dl ON dl.dataset_level_id = p.dataset_level_id
              JOIN dbm_ds_id_map dim ON dim.dataset_id = dl.dataset_id
              WHERE merge_flag = TRUE AND tutor_flag IS NULL) destprob USING (problem_name,
                  dataset_level_id, tutor_other, problem_description);

        UPDATE problem p JOIN dbm_duplicate_problems dp ON p.problem_id = dp.problem_id
        SET p.src_problem_id = dp.src_problem_id;

        INSERT INTO problem (problem_name, problem_description, dataset_level_id, tutor_flag,
            tutor_other, src_problem_id)
        SELECT problem_name, problem_description, dataset_level_id, tutor_flag,
            tutor_other, npr.problem_id
        FROM dbm_new_problems npr
        LEFT JOIN dbm_duplicate_problems dpr ON npr.problem_id = dpr.src_problem_id
        WHERE dpr.src_problem_id IS NULL;

        DROP TABLE IF EXISTS dbm_problem_id_map;
        CREATE TABLE dbm_problem_id_map (src_problem_id BIGINT NOT NULL,
            problem_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_id, problem_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_problem_id, problem_id
        FROM problem
        WHERE src_problem_id IS NOT NULL;

        UPDATE problem p JOIN dbm_problem_id_map pim ON p.problem_id = pim.problem_id
        SET p.src_problem_id = NULL;

        CALL dbm_merge_subgoals();
        CALL dbm_merge_problem_events();
    END;

    IF status = 1 THEN
        CALL dbm_error_problem();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_problems.');
END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the subgoal table.  No longer using the interpretation/cognitive
  tables so ignore interpretation_id.  Calls dbm_insert_SAIs, dbm_insert_subgoal_attempts,
  and dbm_insert_skill_models.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_subgoals` $$
CREATE PROCEDURE `dbm_insert_subgoals` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_subgoals.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    INSERT INTO subgoal (guid, subgoal_name, input_cell_type, problem_id, src_subgoal_id)
        SELECT guid, REPLACE(REPLACE(REPLACE(subgoal_name, '\t', ' '), '\r', ' '), '\n', ' ') as subgoal_name, input_cell_type, pim.problem_id, s.subgoal_id
        FROM adb_source.subgoal s
        JOIN dbm_problem_id_map pim ON pim.src_problem_id = s.problem_id;

        DROP TABLE IF EXISTS dbm_subgoal_id_map;
        CREATE TABLE dbm_subgoal_id_map (src_subgoal_id BIGINT NOT NULL,
            subgoal_id BIGINT NOT NULL,
            PRIMARY KEY (src_subgoal_id, subgoal_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_subgoal_id, subgoal_id
        FROM subgoal
        WHERE src_subgoal_id IS NOT NULL
        ORDER BY src_subgoal_id, subgoal_id;

        UPDATE subgoal sub JOIN dbm_subgoal_id_map sim ON sub.subgoal_id = sim.subgoal_id
        SET sub.src_subgoal_id = NULL;

        CALL dbm_insert_SAIs();
        CALL dbm_insert_subgoal_attempts();
        CALL dbm_insert_skill_models();

        CALL debug_log('db_merge_sp','Finished dbm_insert_subgoals.');
    END;

    IF status = 1 THEN
        CALL dbm_error_subgoal();
    END IF;
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to merge data to the subgoal table.  No longer using the
  interpretation/cognitive tables so ignore interpretation_id. Calls dbm_merge_SAIs,
  dbm_merge_subgoal_attempts, and dbm_merge_skill_models.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_subgoals` $$
CREATE PROCEDURE `dbm_merge_subgoals` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_subgoals.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_subgoals;
        CREATE TABLE dbm_new_subgoals (
            subgoal_id BIGINT PRIMARY KEY,
            guid CHAR(32) NOT NULL,
            subgoal_name TEXT NOT NULL,
            input_cell_type VARCHAR(50),
            problem_id BIGINT NOT NULL,
            INDEX (subgoal_id, guid, input_cell_type, problem_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT subgoal_id, guid, REPLACE(REPLACE(REPLACE(subgoal_name, '\t', ' '), '\r', ' '), '\n', ' ') as subgoal_name,
            REPLACE(REPLACE(REPLACE(input_cell_type, '\t', ' '), '\r', ' '), '\n', ' ') as input_cell_type, pim.problem_id
        FROM adb_source.subgoal sub
        JOIN dbm_problem_id_map pim ON pim.src_problem_id = sub.problem_id;

        DROP TABLE IF EXISTS dbm_duplicate_subgoals;
        CREATE TABLE dbm_duplicate_subgoals (subgoal_id BIGINT PRIMARY KEY,
            src_subgoal_id BIGINT,
            INDEX (src_subgoal_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT destsub.subgoal_id, srcsub.subgoal_id AS src_subgoal_id
        FROM (SELECT subgoal_id, guid, subgoal_name, IFNULL(input_cell_type,'') AS input_cell_type,
                  problem_id
              FROM dbm_new_subgoals) srcsub
        JOIN (SELECT subgoal_id, guid, subgoal_name, IFNULL(input_cell_type,'') AS input_cell_type,
                  sub.problem_id
              FROM subgoal sub
              JOIN problem pr ON pr.problem_id = sub.problem_id
              JOIN dataset_level dl USING (dataset_level_id)
              JOIN dbm_ds_id_map dim USING (dataset_id)
              WHERE merge_flag = TRUE) destsub USING (guid, subgoal_name, input_cell_type, problem_id);

        UPDATE subgoal sub JOIN dbm_duplicate_subgoals dsub USING (subgoal_id)
        SET sub.src_subgoal_id = dsub.src_subgoal_id;

        INSERT INTO subgoal (guid, subgoal_name, input_cell_type, problem_id, src_subgoal_id)
        SELECT guid, subgoal_name, input_cell_type, problem_id, nsub.subgoal_id AS src_subgoal_id
        FROM dbm_new_subgoals nsub
        LEFT JOIN dbm_duplicate_subgoals dsub ON nsub.subgoal_id = dsub.src_subgoal_id
        WHERE dsub.src_subgoal_id IS NULL;

        DROP TABLE IF EXISTS dbm_subgoal_id_map;
        CREATE TABLE dbm_subgoal_id_map (src_subgoal_id BIGINT NOT NULL,
            subgoal_id BIGINT NOT NULL,
            PRIMARY KEY (src_subgoal_id, subgoal_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_subgoal_id, subgoal_id
        FROM subgoal
        WHERE src_subgoal_id IS NOT NULL;

        UPDATE subgoal sub JOIN dbm_subgoal_id_map sim ON sub.subgoal_id = sim.subgoal_id
        SET sub.src_subgoal_id = NULL;

        CALL dbm_merge_SAIs();
        CALL dbm_merge_subgoal_attempts();
        CALL dbm_merge_skill_models();
    END;

    IF status = 1 THEN
        CALL dbm_error_subgoal();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_subgoals.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the selection, action, and input tables.  Joining to the subgoal
  id map ensures we're using the correct source datasets.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_SAIs` $$
CREATE PROCEDURE `dbm_insert_SAIs` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_SAIs.');

    INSERT INTO selection (selection, type, xml_id, subgoal_id)
    SELECT selection, type, xml_id, sim.subgoal_id
    FROM adb_source.selection sel
    JOIN dbm_subgoal_id_map sim ON sim.src_subgoal_id = sel.subgoal_id;

    INSERT INTO action (action, subgoal_id, type, xml_id)
    SELECT action, sim.subgoal_id, type, xml_id
    FROM adb_source.action act
    JOIN dbm_subgoal_id_map sim ON sim.src_subgoal_id = act.subgoal_id;

    INSERT INTO input (input, subgoal_id)
    SELECT input, sim.subgoal_id
    FROM adb_source.input i
    JOIN dbm_subgoal_id_map sim ON sim.src_subgoal_id = i.subgoal_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_SAIs.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the selection, action, and input tables.  Joining to the subgoal
  id map ensures we're using the correct source datasets.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_SAIs` $$
CREATE PROCEDURE `dbm_merge_SAIs` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE minSAI BIGINT;
    DECLARE maxSAI BIGINT;

    CALL debug_log('db_merge_sp','Starting dbm_merge_SAIs.');

    SELECT MAX(selection_id) INTO minSAI FROM selection;

    INSERT INTO selection (selection, type, xml_id, subgoal_id)
    SELECT srcsel.selection, srcsel.type, srcsel.xml_id, srcsel.subgoal_id
    FROM (SELECT REPLACE(REPLACE(REPLACE(selection, '\t', ' '), '\r', ' '), '\n', ' ') as selection,
              IFNULL(type,'') AS type_not_null, type,
              IFNULL(xml_id,'') AS xml_id_not_null, xml_id, sim.subgoal_id
          FROM adb_source.selection sel
          JOIN dbm_subgoal_id_map sim ON sim.src_subgoal_id = sel.subgoal_id) srcsel
    LEFT JOIN (SELECT selection, IFNULL(type,'') AS type_not_null, type,
                   IFNULL(xml_id,'') AS xml_id_not_null, xml_id, subgoal_id
               FROM selection) destsel USING (selection, type_not_null, xml_id_not_null,
                   subgoal_id)
    WHERE destsel.subgoal_id IS NULL;

    SET maxSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_selection_range;
    CREATE TABLE dbm_selection_range (
        selection_id BIGINT NOT NULL PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT selection_id
    FROM selection
    WHERE selection_id > minSAI AND selection_id <= maxSAI;

    SELECT MAX(action_id) INTO minSAI FROM action;

    INSERT INTO action (action, type, xml_id, subgoal_id)
    SELECT srcact.action, srcact.type, srcact.xml_id, srcact.subgoal_id
    FROM (SELECT REPLACE(REPLACE(REPLACE(action, '\t', ' '), '\r', ' '), '\n', ' ') as action, IFNULL(type,'') AS type_not_null, type,
              IFNULL(xml_id,'') AS xml_id_not_null, xml_id, sim.subgoal_id
          FROM adb_source.action act
          JOIN dbm_subgoal_id_map sim ON act.subgoal_id = sim.src_subgoal_id) srcact
    LEFT JOIN (SELECT action, IFNULL(type,'') AS type_not_null, type,
                   IFNULL(xml_id,'') AS xml_id_not_null, xml_id, subgoal_id
               FROM action) destact USING (action, type_not_null, xml_id_not_null, subgoal_id)
    WHERE destact.subgoal_id IS NULL;

    SET maxSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_action_range;
    CREATE TABLE dbm_action_range (
        action_id BIGINT NOT NULL PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT action_id
    FROM action
    WHERE action_id > minSAI AND action_id <= maxSAI;

    SELECT MAX(input_id) INTO minSAI FROM input;

    INSERT INTO input (input, subgoal_id)
    SELECT srcinp.input, srcinp.subgoal_id
    FROM (SELECT REPLACE(REPLACE(REPLACE(input, '\t', ' '), '\r', ' '), '\n', ' ') as input, sim.subgoal_id
          FROM adb_source.input inp
          JOIN dbm_subgoal_id_map sim ON inp.subgoal_id = sim.src_subgoal_id) srcinp
    LEFT JOIN (SELECT input, subgoal_id
               FROM input) destinp USING (input, subgoal_id)
    WHERE destinp.subgoal_id IS NULL;

    SET maxSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_input_range;
    CREATE TABLE dbm_input_range (
        input_id BIGINT NOT NULL PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT input_id
    FROM input
    WHERE input_id > minSAI AND input_id <= maxSAI;

    CALL debug_log('db_merge_sp','Finished dbm_merge_SAIs.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the subgoal_attempt table.  Calls dbm_insert_feedback and
  dbm_insert_attempt_SAIs.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_subgoal_attempts` $$
CREATE PROCEDURE `dbm_insert_subgoal_attempts` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_subgoal_attempts.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    DROP TABLE IF EXISTS dbm_new_subgoal_attempts;
    CREATE TABLE dbm_new_subgoal_attempts (subgoal_attempt_id BIGINT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT subgoal_attempt_id
    FROM dbm_src_tt_view
        UNION
        SELECT subgoal_attempt_id FROM adb_source.subgoal_attempt sa
        JOIN dbm_subgoal_id_map sgim ON sa.subgoal_id = sgim.src_subgoal_id;

    /* Insert only subgoal attempts from datasets being imported, also copy subgoal
       attempts that don't appear in any transactions but have a subgoal in the datasets
       being imported. */
    INSERT INTO subgoal_attempt (correct_flag, subgoal_id, src_subgoal_att_id)
        SELECT correct_flag, sim.subgoal_id, sa.subgoal_attempt_id
        FROM adb_source.subgoal_attempt sa
        JOIN dbm_new_subgoal_attempts nsa ON sa.subgoal_attempt_id = nsa.subgoal_attempt_id
        LEFT JOIN dbm_subgoal_id_map sim ON sa.subgoal_id = sim.src_subgoal_id;

        DROP TABLE IF EXISTS dbm_subgoal_att_id_map;
        CREATE TABLE dbm_subgoal_att_id_map (
            src_subgoal_att_id BIGINT NOT NULL,
            subgoal_attempt_id BIGINT NOT NULL,
            PRIMARY KEY(src_subgoal_att_id, subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_subgoal_att_id, subgoal_attempt_id
        FROM subgoal_attempt
        WHERE src_subgoal_att_id IS NOT NULL
        ORDER BY src_subgoal_att_id, subgoal_attempt_id;

        UPDATE subgoal_attempt satt JOIN dbm_subgoal_att_id_map sam ON satt.subgoal_attempt_id = sam.subgoal_attempt_id
        SET satt.src_subgoal_att_id = NULL;

        CALL dbm_insert_feedback();
        CALL dbm_insert_attempt_SAIs();

        CALL debug_log('db_merge_sp','Finished dbm_insert_subgoal_attempts.');
    END;

    IF status = 1 THEN
        CALL dbm_error_subgoal_attempt();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Procedure merging data to the subgoal_attempt table.  Calls dbm_merge_feedback and
  dbm_merge_attempt_SAIs.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_subgoal_attempts` $$
CREATE PROCEDURE `dbm_merge_subgoal_attempts` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_subgoal_attempts.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_subgoal_attempts;
        CREATE TABLE dbm_new_subgoal_attempts (subgoal_attempt_id BIGINT,
            correct_flag ENUM( 'correct', 'incorrect', 'hint', 'unknown', 'untutored' ) NOT NULL,
            subgoal_id BIGINT,
            selection VARCHAR(255),
            sel_type VARCHAR(50),
            sel_xml_id VARCHAR(50),
            action VARCHAR(255),
            act_type VARCHAR(50),
            act_xml_id VARCHAR(50),
            input TEXT,
            inp_type VARCHAR(50),
            inp_xml_id VARCHAR(50),
            corrected_input VARCHAR(255),
            INDEX(subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT subgoal_attempt_id, correct_flag, sgim.subgoal_id,
            REPLACE(REPLACE(REPLACE(selection, '\t', ' '), '\r', ' '), '\n', ' ') as selection, attsel.type AS sel_type,
            attsel.xml_id AS sel_xml_id, REPLACE(REPLACE(REPLACE(action, '\t', ' '), '\r', ' '), '\n', ' ') as action,
            attact.type AS act_type, attact.xml_id AS act_xml_id,
            REPLACE(REPLACE(REPLACE(input, '\t', ' '), '\r', ' '), '\n', ' ') as input, attinp.type AS inp_type,
            attinp.xml_id AS inp_xml_id, REPLACE(REPLACE(REPLACE(corrected_input, '\t', ' '), '\r', ' '), '\n', ' ') as corrected_input
        FROM adb_source.subgoal_attempt sa
        JOIN (SELECT DISTINCT subgoal_attempt_id
              FROM dbm_src_tt_view
              UNION
              SELECT subgoal_attempt_id
              FROM adb_source.subgoal_attempt sa
              JOIN adb_source.subgoal sub USING (subgoal_id)
              JOIN adb_source.problem p USING (problem_id)
              JOIN adb_source.dataset_level dl USING (dataset_level_id)
              JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.src_dataset_id) uniqsa
            USING (subgoal_attempt_id)
        LEFT JOIN adb_source.attempt_selection attsel USING (subgoal_attempt_id)
        LEFT JOIN adb_source.attempt_action attact USING (subgoal_attempt_id)
        LEFT JOIN adb_source.attempt_input attinp USING (subgoal_attempt_id)
        LEFT JOIN dbm_subgoal_id_map sgim ON sa.subgoal_id = sgim.src_subgoal_id;

        DROP TABLE IF EXISTS dbm_new_null_subgoal_attempts;
        CREATE TABLE dbm_new_null_subgoal_attempts (subgoal_attempt_id BIGINT,
            correct_flag ENUM( 'correct', 'incorrect', 'hint', 'unknown', 'untutored' ) NOT NULL,
            subgoal_id BIGINT,
            selection VARCHAR(255),
            sel_type VARCHAR(50),
            sel_xml_id VARCHAR(50),
            action VARCHAR(255),
            act_type VARCHAR(50),
            act_xml_id VARCHAR(50),
            input TEXT,
            inp_type VARCHAR(50),
            inp_xml_id VARCHAR(50),
            corrected_input VARCHAR(255),
            INDEX(correct_flag, subgoal_id, selection(10), action(10), input(10))
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT subgoal_attempt_id, correct_flag, IFNULL(subgoal_id,0) AS subgoal_id,
            REPLACE(REPLACE(REPLACE(IFNULL(selection,''), '\t', ' '), '\r', ' '), '\n', ' ') as selection, IFNULL(sel_type,'') AS sel_type,
            IFNULL(sel_xml_id,'') AS sel_xml_id,
            REPLACE(REPLACE(REPLACE(IFNULL(action,''), '\t', ' '), '\r', ' '), '\n', ' ') AS action,
            IFNULL(act_type,'') AS act_type, IFNULL(act_xml_id,'') AS act_xml_id,
            REPLACE(REPLACE(REPLACE(IFNULL(input,''), '\t', ' '), '\r', ' '), '\n', ' ') AS input, IFNULL(inp_type,'') AS inp_type,
            IFNULL(inp_xml_id,'') AS inp_xml_id, REPLACE(REPLACE(REPLACE(IFNULL(corrected_input,''), '\t', ' '), '\r', ' '), '\n', ' ') AS corrected_input
        FROM dbm_new_subgoal_attempts;

        DROP TABLE IF EXISTS dbm_dest_null_subgoal_attempts;
        CREATE TABLE dbm_dest_null_subgoal_attempts (subgoal_attempt_id BIGINT,
            correct_flag ENUM('correct', 'incorrect', 'hint', 'unknown', 'untutored') NOT NULL,
            subgoal_id BIGINT,
            selection VARCHAR(255),
            sel_type VARCHAR(50),
            sel_xml_id VARCHAR(50),
            action VARCHAR(255),
            act_type VARCHAR(50),
            act_xml_id VARCHAR(50),
            input TEXT,
            inp_type VARCHAR(50),
            inp_xml_id VARCHAR(50),
            corrected_input VARCHAR(255),
            INDEX(correct_flag, subgoal_id, selection(10), action(10), input(10)),
            INDEX(subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sa.subgoal_attempt_id, correct_flag,
            IFNULL(sa.subgoal_id,0) AS subgoal_id, IFNULL(selection,'') AS selection,
            IFNULL(attsel.type,'') AS sel_type, IFNULL(attsel.xml_id,'') AS sel_xml_id,
            IFNULL(action,'') AS action, IFNULL(attact.type,'') AS act_type,
            IFNULL(attact.xml_id,'') AS act_xml_id, IFNULL(input,'') AS input,
            IFNULL(attinp.type,'') AS inp_type, IFNULL(attinp.xml_id,'') AS inp_xml_id,
            IFNULL(corrected_input,'') AS corrected_input
        FROM subgoal_attempt sa
        JOIN (SELECT DISTINCT subgoal_attempt_id
                  FROM dbm_dest_tt_view
                  UNION
                  SELECT subgoal_attempt_id FROM subgoal_attempt sa
                  JOIN subgoal USING (subgoal_id)
                  JOIN problem USING (problem_id)
                  JOIN dataset_level USING (dataset_level_id)
                  JOIN dbm_ds_id_map dim USING (dataset_id)
                  WHERE merge_flag = TRUE) uniqsubatt USING (subgoal_attempt_id)
        LEFT JOIN attempt_selection attsel USING (subgoal_attempt_id)
        LEFT JOIN attempt_action attact USING (subgoal_attempt_id)
        LEFT JOIN attempt_input attinp USING (subgoal_attempt_id);

        CALL debug_log('db_merge_sp','Creating duplicate subgoal attempts table');
        DROP TABLE IF EXISTS dbm_duplicate_subgoal_attempts;
        CREATE TABLE dbm_duplicate_subgoal_attempts (subgoal_attempt_id BIGINT NOT NULL,
            src_subgoal_attempt_id BIGINT NOT NULL,
            PRIMARY KEY (src_subgoal_attempt_id, subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT destsa.subgoal_attempt_id, srcsa.subgoal_attempt_id AS src_subgoal_attempt_id
        FROM dbm_new_null_subgoal_attempts srcsa
        JOIN dbm_dest_null_subgoal_attempts destsa
            USING (correct_flag, subgoal_id, selection, action, input, sel_type, sel_xml_id,
                act_type, act_xml_id, inp_type, inp_xml_id, corrected_input);

        CALL debug_log('db_merge_sp','Updating duplicate subgoal attempt src ids');
        UPDATE subgoal_attempt sa JOIN dbm_duplicate_subgoal_attempts dsa USING (subgoal_attempt_id)
        SET src_subgoal_att_id = dsa.src_subgoal_attempt_id;

        CALL debug_log('db_merge_sp','Inserting new subgoal attempts');
        INSERT INTO subgoal_attempt (correct_flag, subgoal_id, src_subgoal_att_id)
        SELECT DISTINCT correct_flag, subgoal_id, nsgatt.subgoal_attempt_id
        FROM dbm_new_subgoal_attempts nsgatt
        LEFT JOIN dbm_duplicate_subgoal_attempts dsgatt
            ON nsgatt.subgoal_attempt_id = dsgatt.src_subgoal_attempt_id
        WHERE dsgatt.src_subgoal_attempt_id IS NULL;

        CALL debug_log('db_merge_sp','Creating dbm_subgoal_att_id_map');
        DROP TABLE IF EXISTS dbm_subgoal_att_id_map;
        CREATE TABLE dbm_subgoal_att_id_map (src_subgoal_att_id BIGINT NOT NULL,
            subgoal_attempt_id BIGINT NOT NULL,
            PRIMARY KEY (src_subgoal_att_id, subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_subgoal_att_id, subgoal_attempt_id
        FROM subgoal_attempt
        WHERE src_subgoal_att_id IS NOT NULL;

        CALL debug_log('db_merge_sp','Nulling out src subgoal attempt ids');
        UPDATE subgoal_attempt satt JOIN dbm_subgoal_att_id_map sam ON satt.subgoal_attempt_id = sam.subgoal_attempt_id
        SET satt.src_subgoal_att_id = NULL;

        CALL dbm_merge_feedback();
        CALL dbm_merge_attempt_SAIs();
    END;

    IF status = 1 THEN
        CALL dbm_error_subgoal_attempt();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_insert_subgoal_attempts.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the feedback table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_feedback` $$
CREATE PROCEDURE `dbm_insert_feedback` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_feedback.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;
        INSERT INTO feedback (feedback_text, classification, template_tag, subgoal_attempt_id,
            src_feedback_id)
        SELECT REPLACE(REPLACE(REPLACE(feedback_text, '\t', ' '), '\r', ' '), '\n', ' ') as feedback_text,
        REPLACE(REPLACE(REPLACE(classification, '\t', ' '), '\r', ' '), '\n', ' ') as classification, template_tag, saim.subgoal_attempt_id, f.feedback_id
        FROM adb_source.feedback f
        JOIN dbm_subgoal_att_id_map saim on saim.src_subgoal_att_id = f.subgoal_attempt_id;

        DROP TABLE IF EXISTS dbm_feedback_id_map;
        CREATE TABLE dbm_feedback_id_map (src_feedback_id BIGINT NOT NULL,
            feedback_id BIGINT NOT NULL,
            PRIMARY KEY (src_feedback_id, feedback_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_feedback_id, feedback_id
        FROM feedback
        WHERE src_feedback_id IS NOT NULL;

        UPDATE feedback f JOIN dbm_feedback_id_map fim ON f.feedback_id = fim.feedback_id
        SET f.src_feedback_id = NULL;

        CALL debug_log('db_merge_sp','Finished dbm_insert_feedback.');
    END;

    IF status = 1 THEN
        CALL dbm_error_feedback();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the feedback table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_feedback` $$
CREATE PROCEDURE `dbm_merge_feedback` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_feedback.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_duplicate_feedback;
        CREATE TABLE dbm_duplicate_feedback (feedback_id BIGINT NOT NULL,
            src_feedback_id BIGINT NOT NULL,
            PRIMARY KEY (src_feedback_id, feedback_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT destfee.feedback_id, srcfee.feedback_id AS src_feedback_id
        FROM (SELECT feedback_id,
                  REPLACE(REPLACE(REPLACE(feedback_text, '\t', ' '), '\r', ' '), '\n', ' ') as feedback_text,
                  REPLACE(REPLACE(REPLACE(IFNULL(classification,''), '\t', ' '), '\r', ' '), '\n', ' ') AS classification,
                  IFNULL(template_tag,'') AS template_tag, saim.subgoal_attempt_id
              FROM adb_source.feedback fee
              JOIN dbm_subgoal_att_id_map saim
                  ON fee.subgoal_attempt_id = saim.src_subgoal_att_id) srcfee
        JOIN (SELECT feedback_id, feedback_text, IFNULL(classification,'') AS classification,
                  IFNULL(template_tag,'') AS template_tag, fee.subgoal_attempt_id
              FROM feedback fee
              JOIN (SELECT DISTINCT feedback_id, dataset_id FROM dbm_dest_tt_view) tt
                  USING (feedback_id)) destfee
            USING (feedback_text, classification, template_tag, subgoal_attempt_id);

        UPDATE feedback fee JOIN dbm_duplicate_feedback dfee USING (feedback_id)
        SET fee.src_feedback_id = dfee.src_feedback_id;

        INSERT INTO feedback (feedback_text, classification, template_tag, subgoal_attempt_id,
            src_feedback_id)
        SELECT REPLACE(REPLACE(REPLACE(feedback_text, '\t', ' '), '\r', ' '), '\n', ' ') as feedback_text,
            REPLACE(REPLACE(REPLACE(classification, '\t', ' '), '\r', ' '), '\n', ' ') as classification,
            template_tag, saim.subgoal_attempt_id, fee.feedback_id
        FROM adb_source.feedback fee
        JOIN dbm_subgoal_att_id_map saim ON fee.subgoal_attempt_id = saim.src_subgoal_att_id
        LEFT JOIN dbm_duplicate_feedback dfee ON fee.feedback_id = dfee.src_feedback_id
        WHERE dfee.src_feedback_id IS NULL;

        DROP TABLE IF EXISTS dbm_feedback_id_map;
        CREATE TABLE dbm_feedback_id_map (src_feedback_id BIGINT NOT NULL,
            feedback_id BIGINT NOT NULL,
            PRIMARY KEY (src_feedback_id, feedback_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_feedback_id, feedback_id
        FROM feedback
        WHERE src_feedback_id IS NOT NULL;

        UPDATE feedback f JOIN dbm_feedback_id_map fim ON f.feedback_id = fim.feedback_id
        SET f.src_feedback_id = NULL;
    END;

    IF status = 1 THEN
        CALL dbm_error_feedback();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_feedback.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the attempt_selection, attempt_action, and attempt_input tables.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_attempt_SAIs` $$
CREATE PROCEDURE `dbm_insert_attempt_SAIs` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_attempt_SAIs.');

    INSERT INTO attempt_selection (selection, subgoal_attempt_id, type, xml_id)
    SELECT REPLACE(REPLACE(REPLACE(selection, '\t', ' '), '\r', ' '), '\n', ' ') as selection, saim.subgoal_attempt_id, type, xml_id
    FROM adb_source.attempt_selection att
    JOIN dbm_subgoal_att_id_map saim ON att.subgoal_attempt_id = saim.src_subgoal_att_id;

    INSERT INTO attempt_action (action, subgoal_attempt_id, type, xml_id)
    SELECT REPLACE(REPLACE(REPLACE(action, '\t', ' '), '\r', ' '), '\n', ' ') as action, saim.subgoal_attempt_id, type, xml_id
    FROM adb_source.attempt_action aa
    JOIN dbm_subgoal_att_id_map saim ON aa.subgoal_attempt_id = saim.src_subgoal_att_id;

    INSERT INTO attempt_input (input, subgoal_attempt_id, corrected_input, type, xml_id)
    SELECT REPLACE(REPLACE(REPLACE(input, '\t', ' '), '\r', ' '), '\n', ' ') as input, saim.subgoal_attempt_id, corrected_input, type, xml_id
    FROM adb_source.attempt_input ai
    JOIN dbm_subgoal_att_id_map saim ON ai.subgoal_attempt_id = saim.src_subgoal_att_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_attempt_SAIs.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the attempt_selection, attempt_action, and attempt_input tables.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_attempt_SAIs` $$
CREATE PROCEDURE `dbm_merge_attempt_SAIs` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE minAttSAI BIGINT;
    DECLARE maxAttSAI BIGINT;

    CALL debug_log('db_merge_sp','Starting dbm_merge_attempt_SAIs.');

    /* attempt_selection */
    DROP TABLE IF EXISTS dbm_src_attempt_selection;
    CREATE TABLE dbm_src_attempt_selection ( attempt_selection_id BIGINT PRIMARY KEY,
        selection            VARCHAR(255) NOT NULL,
        subgoal_attempt_id   BIGINT       NOT NULL,
        type                 VARCHAR(50),
        xml_id               VARCHAR(50)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_selection_id, REPLACE(REPLACE(REPLACE(selection, '\t', ' '), '\r', ' '), '\n', ' ') as selection,
        saim.subgoal_attempt_id, type, xml_id
    FROM adb_source.attempt_selection attsel
    JOIN dbm_subgoal_att_id_map saim ON attsel.subgoal_attempt_id = saim.src_subgoal_att_id;

    DROP TABLE IF EXISTS dbm_src_null_attempt_selection;
    CREATE TABLE dbm_src_null_attempt_selection ( attempt_selection_id BIGINT,
        selection            VARCHAR(255) NOT NULL,
        subgoal_attempt_id   BIGINT       NOT NULL,
        type                 VARCHAR(50),
        xml_id               VARCHAR(50),
        INDEX(selection(10), subgoal_attempt_id, type(10), xml_id(10))
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_selection_id, REPLACE(REPLACE(REPLACE(selection, '\t', ' '), '\r', ' '), '\n', ' ') as selection,
        subgoal_attempt_id, IFNULL(type,'') AS type, IFNULL(xml_id,'') AS xml_id
    FROM dbm_src_attempt_selection;

    /* Use destination subgoal_attempts which are already linked to the dataset IDs being merged.
     * Null values have already been converted into comparison friendly values. */
    DROP TABLE IF EXISTS dbm_dest_attempt_selection;
    CREATE TABLE dbm_dest_attempt_selection ( attempt_selection_id BIGINT,
        selection            VARCHAR(255) NOT NULL,
        subgoal_attempt_id   BIGINT       NOT NULL,
        type                 VARCHAR(50),
        xml_id               VARCHAR(50),
        INDEX(selection(10), subgoal_attempt_id, type(10), xml_id(10))
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attsel.attempt_selection_id, dsgatt.selection, dsgatt.subgoal_attempt_id,
        sel_type AS type, sel_xml_id AS xml_id
    FROM dbm_dest_null_subgoal_attempts dsgatt
    JOIN attempt_selection attsel USING (subgoal_attempt_id, selection)
    GROUP BY subgoal_attempt_id, selection;

    DROP TABLE IF EXISTS dbm_duplicate_attempt_selection;
    CREATE TABLE dbm_duplicate_attempt_selection (attempt_selection_id BIGINT
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT srcattsel.attempt_selection_id
    FROM dbm_dest_attempt_selection destattsel
    JOIN dbm_src_null_attempt_selection srcattsel
        USING (selection, subgoal_attempt_id, type, xml_id);

    SELECT MAX(attempt_selection_id) INTO minAttSAI FROM attempt_selection;

    INSERT INTO attempt_selection (selection, subgoal_attempt_id, type, xml_id)
    SELECT srcattsel.selection, srcattsel.subgoal_attempt_id, srcattsel.type, srcattsel.xml_id
    FROM dbm_src_attempt_selection srcattsel
    LEFT JOIN dbm_duplicate_attempt_selection dupattsel
        ON srcattsel.attempt_selection_id = dupattsel.attempt_selection_id
    WHERE dupattsel.attempt_selection_id IS NULL;

    SET maxAttSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_attempt_selection_range;
    CREATE TABLE dbm_attempt_selection_range (
        attempt_selection_id BIGINT NOT NULL,
        subgoal_attempt_id BIGINT NOT NULL,
        PRIMARY KEY (attempt_selection_id, subgoal_attempt_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_selection_id, subgoal_attempt_id
    FROM attempt_selection
    WHERE attempt_selection_id > minAttSAI AND attempt_selection_id <= maxAttSAI;

    /* attempt_action */
    DROP TABLE IF EXISTS dbm_src_attempt_action;
    CREATE TABLE dbm_src_attempt_action (attempt_action_id BIGINT PRIMARY KEY,
        action             VARCHAR(255) NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        type               VARCHAR(50),
        xml_id             VARCHAR(50)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_action_id, REPLACE(REPLACE(REPLACE(action, '\t', ' '), '\r', ' '), '\n', ' ') as action,
        sgaim.subgoal_attempt_id, type, xml_id
    FROM adb_source.attempt_action attact
    JOIN dbm_subgoal_att_id_map sgaim ON attact.subgoal_attempt_id = sgaim.src_subgoal_att_id;

    DROP TABLE IF EXISTS dbm_src_null_attempt_action;
    CREATE TABLE dbm_src_null_attempt_action (attempt_action_id BIGINT PRIMARY KEY,
        action             VARCHAR(255) NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        type               VARCHAR(50),
        xml_id             VARCHAR(50),
        INDEX(action(10), subgoal_attempt_id, type, xml_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_action_id, action, subgoal_attempt_id, IFNULL(type,'') AS type,
        IFNULL(xml_id,'') AS xml_id
    FROM dbm_src_attempt_action;

    DROP TABLE IF EXISTS dbm_dest_attempt_action;
    CREATE TABLE dbm_dest_attempt_action (attempt_action_id BIGINT PRIMARY KEY,
        action             VARCHAR(255) NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        type               VARCHAR(50),
        xml_id             VARCHAR(50),
        INDEX(action(10), subgoal_attempt_id, type, xml_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attact.attempt_action_id, attact.action, subgoal_attempt_id,
        IFNULL(attact.type,'') AS type, IFNULL(attact.xml_id,'') AS xml_id
    FROM dbm_dest_null_subgoal_attempts dsgatt
    JOIN attempt_action attact USING (subgoal_attempt_id, action)
    GROUP BY subgoal_attempt_id, action;

    DROP TABLE IF EXISTS dbm_duplicate_attempt_action;
    CREATE TABLE dbm_duplicate_attempt_action (attempt_action_id BIGINT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT src.attempt_action_id
    FROM dbm_src_null_attempt_action src
    JOIN dbm_dest_attempt_action dest ON src.action = dest.action
        AND src.subgoal_attempt_id = dest.subgoal_attempt_id
        AND src.type = dest.type AND src.xml_id = dest.xml_id;

    SELECT MAX(attempt_action_id) INTO minAttSAI FROM attempt_action;

    INSERT INTO attempt_action (action, subgoal_attempt_id, type, xml_id)
    SELECT srcattact.action, srcattact.subgoal_attempt_id, srcattact.type, srcattact.xml_id
    FROM dbm_src_attempt_action srcattact
    LEFT JOIN dbm_duplicate_attempt_action dupattact
        ON srcattact.attempt_action_id = dupattact.attempt_action_id
    WHERE dupattact.attempt_action_id IS NULL;

    SET maxAttSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_attempt_action_range;
    CREATE TABLE dbm_attempt_action_range (
        attempt_action_id BIGINT NOT NULL,
        subgoal_attempt_id BIGINT NOT NULL,
        PRIMARY KEY (attempt_action_id, subgoal_attempt_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_action_id, subgoal_attempt_id
    FROM attempt_action
    WHERE attempt_action_id > minAttSAI AND attempt_action_id <= maxAttSAI;

    /* attempt_input */
    DROP TABLE IF EXISTS dbm_src_attempt_input;
    CREATE TABLE dbm_src_attempt_input (attempt_input_id BIGINT PRIMARY KEY,
        input              TEXT         NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        corrected_input    VARCHAR(255),
        type               VARCHAR(50),
        xml_id             VARCHAR(50)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_input_id, REPLACE(REPLACE(REPLACE(input, '\t', ' '), '\r', ' '), '\n', ' ') as input,
    sgaim.subgoal_attempt_id, REPLACE(REPLACE(REPLACE(corrected_input, '\t', ' '), '\r', ' '), '\n', ' ') as corrected_input, type, xml_id
    FROM adb_source.attempt_input atti
    JOIN dbm_subgoal_att_id_map sgaim ON atti.subgoal_attempt_id = sgaim.src_subgoal_att_id;

    DROP TABLE IF EXISTS dbm_src_null_attempt_input;
    CREATE TABLE dbm_src_null_attempt_input (attempt_input_id BIGINT PRIMARY KEY,
        input              TEXT         NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        corrected_input    VARCHAR(255),
        type               VARCHAR(50),
        xml_id             VARCHAR(50),
        INDEX (input(10), subgoal_attempt_id, corrected_input(10), type(10), xml_id(10))
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_input_id, input, subgoal_attempt_id,
        IFNULL(corrected_input,'') AS corrected_input, IFNULL(type,'') AS type,
        IFNULL(xml_id,'') AS xml_id
    FROM dbm_src_attempt_input;

    DROP TABLE IF EXISTS dbm_dest_attempt_input;
    CREATE TABLE dbm_dest_attempt_input (attempt_input_id BIGINT PRIMARY KEY,
        input              TEXT         NOT NULL,
        subgoal_attempt_id BIGINT       NOT NULL,
        corrected_input    VARCHAR(255),
        type               VARCHAR(50),
        xml_id             VARCHAR(50),
        INDEX (input(10), subgoal_attempt_id, corrected_input(10), type(10), xml_id(10))
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_input_id, attinp.input, attinp.subgoal_attempt_id,
        IFNULL(attinp.corrected_input,'') AS corrected_input, IFNULL(attinp.type,'') AS type,
        IFNULL(attinp.xml_id,'') AS xml_id
    FROM attempt_input attinp
    JOIN dbm_dest_null_subgoal_attempts USING (subgoal_attempt_id, input)
    GROUP BY subgoal_attempt_id, input;

    DROP TABLE IF EXISTS dbm_duplicate_attempt_input;
    CREATE TABLE dbm_duplicate_attempt_input (attempt_input_id BIGINT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT src.attempt_input_id
    FROM dbm_src_null_attempt_input src
    JOIN dbm_dest_attempt_input USING (input, subgoal_attempt_id, corrected_input, type, xml_id);

    SELECT MAX(attempt_input_id) INTO minAttSAI FROM attempt_input;

    INSERT INTO attempt_input (input, subgoal_attempt_id, corrected_input, type, xml_id)
    SELECT srcattinp.input, srcattinp.subgoal_attempt_id, srcattinp.corrected_input,
        srcattinp.type, srcattinp.xml_id
    FROM dbm_src_attempt_input srcattinp
    LEFT JOIN dbm_duplicate_attempt_input dest
        ON srcattinp.attempt_input_id = dest.attempt_input_id
    WHERE dest.attempt_input_id IS NULL;

    SET maxAttSAI = LAST_INSERT_ID();

    DROP TABLE IF EXISTS dbm_attempt_input_range;
    CREATE TABLE dbm_attempt_input_range (
        attempt_input_id BIGINT NOT NULL,
        subgoal_attempt_id BIGINT NOT NULL,
        PRIMARY KEY (attempt_input_id, subgoal_attempt_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT attempt_input_id, subgoal_attempt_id
    FROM attempt_input
    WHERE attempt_input_id > minAttSAI AND attempt_input_id <= maxAttSAI;

    CALL debug_log('db_merge_sp','Finished dbm_merge_attempt_SAIs.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the skill_model table.  Owner field is not copied because user
  table is not populated either.  Calls dbm_insert_skills.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_skill_models` $$
CREATE PROCEDURE `dbm_insert_skill_models` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE errorStatus INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_skill_models.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET errorStatus = 1;

    INSERT INTO skill_model (skill_model_name, aic, bic, intercept, log_likelihood, global_flag,
        dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source, mapping_type, creation_time,
        modified_time, num_observations, cv_unstratified_rmse, cv_student_stratified_rmse, cv_step_stratified_rmse,
            cv_unstratified_num_observations, cv_unstratified_num_parameters,
        cv_status, cv_status_description, src_skill_model_id)
        SELECT REPLACE(REPLACE(REPLACE(skill_model_name, '\t', ' '), '\r', ' '), '\n', ' ') as skill_model_name,
            aic, bic, intercept, log_likelihood, global_flag,
            dim.dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source, mapping_type, creation_time,
            modified_time, num_observations, cv_unstratified_rmse, cv_student_stratified_rmse, cv_step_stratified_rmse,
            cv_unstratified_num_observations, cv_unstratified_num_parameters,
            cv_status, cv_status_description, skill_model_id
        FROM adb_source.skill_model skm
        JOIN dbm_ds_id_map dim ON skm.dataset_id = dim.src_dataset_id
        WHERE dim.merge_flag = FALSE;

        DROP TABLE IF EXISTS dbm_skill_model_id_map;
        CREATE TABLE dbm_skill_model_id_map (src_skill_model_id BIGINT NOT NULL,
            skill_model_id BIGINT NOT NULL,
            PRIMARY KEY (src_skill_model_id, skill_model_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

        INSERT INTO dbm_skill_model_id_map (src_skill_model_id, skill_model_id)
        SELECT src_skill_model_id, skill_model_id
        FROM skill_model skm
        JOIN dbm_ds_id_map dim ON skm.dataset_id = dim.dataset_id
        WHERE src_skill_model_id IS NOT NULL AND dim.merge_flag = FALSE;

        UPDATE skill_model sm JOIN dbm_skill_model_id_map smim ON sm.skill_model_id = smim.skill_model_id
        SET sm.src_skill_model_id = NULL;

        CALL dbm_insert_skills();

        CALL debug_log('db_merge_sp','Finished dbm_insert_skill_models.');
    END;

    IF errorStatus = 1 THEN
        CALL dbm_error_skill_model();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the skill_model table.  Owner field is not copied because user
  table is not populated either.  Calls dbm_merge_skills.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_skill_models` $$
CREATE PROCEDURE `dbm_merge_skill_models` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE errorStatus INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_skill_models.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET errorStatus = 1;

        DROP TABLE IF EXISTS dbm_new_skill_models;
        CREATE TABLE dbm_new_skill_models (skill_model_id BIGINT PRIMARY KEY,
            skill_model_name VARCHAR(50) NOT NULL,
            aic DOUBLE,
            bic DOUBLE,
            intercept DOUBLE,
            log_likelihood DOUBLE,
            global_flag BOOL NOT NULL,
            dataset_id INT NOT NULL,
            allow_lfa_flag BOOL NOT NULL,
            status VARCHAR(100),
            lfa_status VARCHAR(100),
		 lfa_status_description VARCHAR(250),
            source VARCHAR(100),
            mapping_type VARCHAR(100),
            creation_time DATETIME,
            modified_time DATETIME,
            num_observations INT,
            cv_unstratified_rmse DOUBLE,
            cv_student_stratified_rmse DOUBLE,
            cv_step_stratified_rmse DOUBLE,
            cv_unstratified_num_observations INT,
            cv_unstratified_num_parameters INT,
            cv_status VARCHAR(100),
		 cv_status_description VARCHAR(250),
            INDEX(skill_model_name, global_flag, dataset_id, source, mapping_type)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT skill_model_id, REPLACE(REPLACE(REPLACE(skill_model_name, '\t', ' '), '\r', ' '), '\n', ' ') as skill_model_name,
            aic, bic, intercept, log_likelihood, global_flag,
            dim.dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source, mapping_type, creation_time,
            modified_time, num_observations, cv_unstratified_rmse, cv_student_stratified_rmse, cv_step_stratified_rmse,
            cv_unstratified_num_observations, cv_unstratified_num_parameters, cv_status, cv_status_description
        FROM adb_source.skill_model skm
        JOIN dbm_ds_id_map dim ON dim.src_dataset_id = skm.dataset_id
        WHERE merge_flag = TRUE;

        DROP TABLE IF EXISTS dbm_duplicate_skill_models;
        CREATE TABLE dbm_duplicate_skill_models (skill_model_id BIGINT NOT NULL,
            src_skill_model_id BIGINT NOT NULL,
            PRIMARY KEY (src_skill_model_id, skill_model_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT dest_skmod.skill_model_id, src_skmod.skill_model_id AS src_skill_model_id
        FROM (SELECT skill_model_id, skill_model_name, global_flag, dataset_id,
                  IFNULL(mapping_type,'') AS mapping_type
              FROM dbm_new_skill_models) src_skmod
        JOIN (SELECT skill_model_id, skill_model_name, global_flag, dataset_id,
                  IFNULL(mapping_type,'') AS mapping_type
              FROM skill_model
              JOIN dbm_ds_id_map USING (dataset_id)
              WHERE merge_flag = TRUE) dest_skmod USING (skill_model_name, global_flag, dataset_id,
                  mapping_type);

        UPDATE skill_model skm
        JOIN dbm_duplicate_skill_models dskm ON skm.skill_model_id = dskm.skill_model_id
        SET skm.src_skill_model_id = dskm.src_skill_model_id;

        INSERT INTO skill_model (skill_model_name, aic, bic, intercept, log_likelihood, global_flag,
            dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source, mapping_type, creation_time,
            modified_time, num_observations, cv_unstratified_rmse, cv_student_stratified_rmse, cv_step_stratified_rmse,
            cv_unstratified_num_observations, cv_unstratified_num_parameters,
            cv_status, cv_status_description, src_skill_model_id)
        SELECT skill_model_name, aic, bic, intercept, log_likelihood, global_flag,
            dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source, mapping_type, creation_time,
            modified_time, num_observations, cv_unstratified_rmse, cv_student_stratified_rmse, cv_step_stratified_rmse,
            cv_unstratified_num_observations, cv_unstratified_num_parameters,
            cv_status, cv_status_description, nskm.skill_model_id
        FROM dbm_new_skill_models nskm
        LEFT JOIN dbm_duplicate_skill_models dskm ON nskm.skill_model_id = dskm.src_skill_model_id
        WHERE dskm.src_skill_model_id IS NULL;

        DROP TABLE IF EXISTS dbm_skill_model_id_map;
        CREATE TABLE dbm_skill_model_id_map (src_skill_model_id BIGINT NOT NULL,
            skill_model_id BIGINT NOT NULL,
            PRIMARY KEY (src_skill_model_id, skill_model_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_skill_model_id, skill_model_id
        FROM skill_model skm
        WHERE src_skill_model_id IS NOT NULL;

        UPDATE skill_model sm
        JOIN dbm_skill_model_id_map smim ON sm.skill_model_id = smim.skill_model_id
        SET sm.src_skill_model_id = NULL;

        CALL dbm_merge_skills();
    END;

    IF errorStatus = 1 THEN
        CALL dbm_error_skill_model();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_skill_models.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the skill table.  Skill Model is an implied required foreign key.
  Calls dbm_insert_subgoal_skill_map.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_skills` $$
CREATE PROCEDURE `dbm_insert_skills` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_skills.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    INSERT INTO skill (skill_name, category, skill_model_id, beta, gamma, src_skill_id)
        SELECT REPLACE(REPLACE(REPLACE(skill_name, '\t', ' '), '\r', ' '), '\n', ' ') as skill_name,
            REPLACE(REPLACE(REPLACE(category, '\t', ' '), '\r', ' '), '\n', ' ') as category, smim.skill_model_id, beta, gamma, skill_id
        FROM adb_source.skill s
        JOIN dbm_skill_model_id_map smim ON smim.src_skill_model_id = s.skill_model_id;

        DROP TABLE IF EXISTS dbm_skill_id_map;
        CREATE TABLE dbm_skill_id_map(src_skill_id BIGINT NOT NULL,
            skill_id bigint NOT NULL,
            PRIMARY KEY (src_skill_id, skill_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_skill_id, skill_id
        FROM skill
        WHERE src_skill_id IS NOT NULL;

        UPDATE skill sk JOIN dbm_skill_id_map sim ON sk.skill_id = sim.skill_id
        SET sk.src_skill_id = NULL;

        CALL dbm_insert_subgoal_skill_map();

        CALL debug_log('db_merge_sp','Finished dbm_insert_skills.');
    END;

    IF status = 1 THEN
        CALL dbm_error_skill();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the skill table.  Skill Model is an implied foreign key and shouldn't
  be null. Calls dbm_merge_subgoal_skill_map.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_skills` $$
CREATE PROCEDURE `dbm_merge_skills` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_skills.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_skills;
        CREATE TABLE dbm_new_skills (skill_id BIGINT PRIMARY KEY,
            skill_name TEXT NOT NULL,
            category VARCHAR(50),
            skill_model_id BIGINT,
            beta DOUBLE,
            gamma DOUBLE,
            INDEX(category, skill_model_id, beta, gamma)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT skill_id, REPLACE(REPLACE(REPLACE(skill_name, '\t', ' '), '\r', ' '), '\n', ' ') as skill_name,
            REPLACE(REPLACE(REPLACE(category, '\t', ' '), '\r', ' '), '\n', ' ') as category, smim.skill_model_id, beta, gamma
        FROM adb_source.skill sk
        JOIN dbm_skill_model_id_map smim ON smim.src_skill_model_id = sk.skill_model_id;

        DROP TABLE IF EXISTS dbm_duplicate_skills;
        CREATE TABLE dbm_duplicate_skills (skill_id BIGINT NOT NULL,
            src_skill_id BIGINT NOT NULL,
            PRIMARY KEY (src_skill_id, skill_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT dest_skl.skill_id, src_skl.skill_id AS src_skill_id
        FROM (SELECT skill_id, skill_name, IFNULL(category,'') AS category,
                  skill_model_id, IFNULL(beta,0.0) AS beta,
                  IFNULL(gamma,0.0) AS gamma
              FROM dbm_new_skills) src_skl
        JOIN (SELECT skill_id, skill_name, IFNULL(category,'') AS category,
                  IFNULL(skl.skill_model_id,0) AS skill_model_id,
                  IFNULL(beta,0.0) AS beta, IFNULL(gamma,0.0) AS gamma
              FROM skill skl
              JOIN skill_model skm ON skl.skill_model_id = skm.skill_model_id
              JOIN dbm_ds_id_map dim ON skm.dataset_id = dim.dataset_id
              WHERE merge_flag = TRUE) dest_skl USING (skill_name, category, skill_model_id);

        UPDATE skill sk JOIN dbm_duplicate_skills dsk USING (skill_id)
        SET sk.src_skill_id = dsk.src_skill_id;

        INSERT INTO skill (skill_name, category, skill_model_id, beta, gamma, src_skill_id)
        SELECT skill_name, category, skill_model_id, beta, gamma, nsk.skill_id
        FROM dbm_new_skills nsk
        LEFT JOIN dbm_duplicate_skills dsk ON nsk.skill_id = dsk.src_skill_id
        WHERE dsk.src_skill_id IS NULL;

        DROP TABLE IF EXISTS dbm_skill_id_map;
        CREATE TABLE dbm_skill_id_map(src_skill_id BIGINT NOT NULL,
            skill_id bigint NOT NULL,
            PRIMARY KEY (src_skill_id, skill_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_skill_id, skill_id
        FROM skill
        WHERE src_skill_id IS NOT NULL;

        UPDATE skill sk JOIN dbm_skill_id_map skim ON skim.skill_id = sk.skill_id
        SET sk.src_skill_id = NULL;

        CALL dbm_merge_subgoal_skill_map();
    END;

    IF status = 1 THEN
        CALL dbm_error_skill();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_skills.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to subgoal_skill_map table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_subgoal_skill_map` $$
CREATE PROCEDURE `dbm_insert_subgoal_skill_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_subgoal_skill_map.');

    INSERT INTO subgoal_skill_map (subgoal_id, skill_id)
    SELECT sgm.subgoal_id, skm.skill_id
    FROM adb_source.subgoal_skill_map ssm
    JOIN dbm_subgoal_id_map sgm ON sgm.src_subgoal_id = ssm.subgoal_id
    JOIN dbm_skill_id_map skm ON skm.src_skill_id = ssm.skill_id
    ORDER BY sgm.subgoal_id, skm.skill_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_subgoal_skill_map.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to subgoal_skill_map table.  Skip duplicates on the composite primary
  keys subgoal_id and skill_id.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_subgoal_skill_map` $$
CREATE PROCEDURE `dbm_merge_subgoal_skill_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_merge_subgoal_skill_map.');

    INSERT INTO subgoal_skill_map (subgoal_id, skill_id)
    SELECT sgm.subgoal_id, skm.skill_id
    FROM adb_source.subgoal_skill_map ssm
    JOIN dbm_subgoal_id_map sgm ON sgm.src_subgoal_id = ssm.subgoal_id
    JOIN dbm_skill_id_map skm ON skm.src_skill_id = ssm.skill_id
        ON DUPLICATE KEY UPDATE subgoal_id = sgm.subgoal_id;

    CALL debug_log('db_merge_sp','Finished dbm_merge_subgoal_skill_map.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the problem_event table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_problem_events` $$
CREATE PROCEDURE `dbm_insert_problem_events` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_problem_events.');

    INSERT INTO problem_event (
            session_id, problem_id, start_time, start_time_ms,
            event_flag, event_type, problem_view, src_problem_event_id)
    SELECT sim.session_id, pim.problem_id, start_time, start_time_ms,
            event_flag, event_type, problem_view, problem_event_id
        FROM adb_source.problem_event pe
        JOIN dbm_problem_id_map pim ON pim.src_problem_id = pe.problem_id
        JOIN dbm_session_id_map sim ON sim.src_session_id = pe.session_id;

    DROP TABLE IF EXISTS dbm_problem_event_id_map;
    CREATE TABLE dbm_problem_event_id_map (
            src_problem_event_id BIGINT NOT NULL,
            problem_event_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_event_id, problem_event_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_problem_event_id, problem_event_id
        FROM problem_event
        WHERE src_problem_event_id IS NOT NULL;

    /* If there are no problem events, then create this table so that the db_merge_load_data.sql doesn't fail. */
    CREATE TABLE IF NOT EXISTS dbm_problem_event_id_map (
            src_problem_event_id BIGINT NOT NULL,
            problem_event_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_event_id, problem_event_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    UPDATE problem_event pe
        JOIN dbm_problem_event_id_map peim ON pe.problem_event_id = peim.problem_event_id
        SET pe.src_problem_event_id = NULL;

    CALL debug_log('db_merge_sp','Finished dbm_insert_problem_events.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge data to the problem_event table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_problem_events` $$
CREATE PROCEDURE `dbm_merge_problem_events` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE minProbEvtId BIGINT;
    DECLARE maxProbEvtId BIGINT;

    CALL debug_log('db_merge_sp','Starting dbm_merge_problem_events.');

    /* This is list of new PEs, not the PEs that appear in new data. */
    DROP TABLE IF EXISTS dbm_new_problem_events;
    CREATE TABLE dbm_new_problem_events (
        problem_event_id BIGINT PRIMARY KEY,
        session_id BIGINT NOT NULL,
        problem_id BIGINT NOT NULL
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT srcpe.problem_event_id, srcpe.session_id, srcpe.problem_id
    FROM (SELECT problem_event_id, seim.session_id, pim.problem_id, start_time,
              IFNULL(start_time_ms,-1) AS start_time_ms, event_flag, event_type,
              IFNULL(problem_view,0) AS problem_view
          FROM adb_source.problem_event pe
          JOIN dbm_session_id_map seim ON seim.src_session_id = pe.session_id
          JOIN dbm_problem_id_map pim ON pim.src_problem_id = pe.problem_id
          ) srcpe
    LEFT JOIN (SELECT pe.session_id, pe.problem_id, pe.start_time,
                   IFNULL(pe.start_time_ms,-1) AS start_time_ms, event_flag, event_type,
                   IFNULL(problem_view,0) AS problem_view
               FROM problem_event pe
               JOIN session USING (session_id)
               JOIN problem USING (problem_id)
               JOIN dbm_ds_id_map dim USING (dataset_id)
               WHERE merge_flag = TRUE) destpe ON srcpe.session_id = destpe.session_id
        AND srcpe.problem_id = destpe.problem_id AND srcpe.start_time = destpe.start_time
        AND srcpe.start_time_ms = destpe.start_time_ms AND srcpe.event_flag = destpe.event_flag
        AND srcpe.event_type = destpe.event_type AND srcpe.problem_view = destpe.problem_view
    WHERE destpe.session_id IS NULL AND destpe.problem_id IS NULL AND destpe.start_time IS NULL
        AND destpe.start_time_ms IS NULL AND destpe.event_flag IS NULL
        AND destpe.event_type IS NULL AND destpe.problem_view IS NULL;

    SELECT MAX(problem_event_id) INTO minProbEvtId FROM problem_event;

    INSERT INTO problem_event (
            session_id, problem_id, start_time, start_time_ms,
            event_flag, event_type, problem_view, src_problem_event_id)
    SELECT npe.session_id, npe.problem_id, start_time, start_time_ms,
            event_flag, event_type, problem_view, problem_event_id
    FROM adb_source.problem_event pe
    JOIN dbm_new_problem_events npe USING (problem_event_id);

    SELECT MAX(problem_event_id) INTO maxProbEvtId FROM problem_event;

    DROP TABLE IF EXISTS dbm_problem_event_range;
    CREATE TABLE dbm_problem_event_range (
       problem_event_id BIGINT NOT NULL,
       problem_id BIGINT NOT NULL,
       session_id BIGINT NOT NULL,
       PRIMARY KEY (problem_event_id, problem_id, session_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT problem_event_id, problem_id, session_id
    FROM problem_event
    WHERE problem_event_id > minProbEvtId AND problem_event_id <= maxProbEvtId;

    DROP TABLE IF EXISTS dbm_problem_event_id_map;
    CREATE TABLE dbm_problem_event_id_map (
            src_problem_event_id BIGINT NOT NULL,
            problem_event_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_event_id, problem_event_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_problem_event_id, problem_event_id
        FROM problem_event
        WHERE src_problem_event_id IS NOT NULL;

    /* If there are no problem events, then create this table so that the db_merge_load_data.sql doesn't fail. */
    CREATE TABLE IF NOT EXISTS dbm_problem_event_id_map (
            src_problem_event_id BIGINT NOT NULL,
            problem_event_id BIGINT NOT NULL,
            PRIMARY KEY (src_problem_event_id, problem_event_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    UPDATE problem_event pe
        JOIN dbm_problem_event_id_map peim ON pe.problem_event_id = peim.problem_event_id
        SET pe.src_problem_event_id = NULL;

    CALL debug_log('db_merge_sp','Finished dbm_merge_problem_events.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the student table joining to the session table to find only students
  for the current dataset being merged.  If the student's actual_user_id already
  exists in the database, update the cl_student_id column to mark as belonging so
  it is included in the student_id_map.  If a student doesn't belong to any sessions
  should we still include that student?  If we want to include the student, we'll have
  to check the class & roster table if used for a dataset.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_students` $$
CREATE PROCEDURE `dbm_insert_students` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    DECLARE sqlStr TEXT;

    CALL debug_log('db_merge_sp', CONCAT('Starting dbm_insert_students: ', mappingDbName));

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    -- Insert into mapping_db first to check for duplicates.
    SET sqlStr = CONCAT('INSERT INTO ', mappingDbName,
                        '.mapped_student (actual_user_id, anon_user_id, src_student_id) ',
                        'SELECT DISTINCT actual_user_id, anon_user_id, s.student_id ',
                        'FROM adb_source.student s '
                        'JOIN adb_source.session sess ON s.student_id = sess.student_id ',
                        'JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id ',
                        'WHERE dim.merge_flag = FALSE ',
                        'ON DUPLICATE KEY UPDATE src_student_id = s.student_id');
    CALL exec(sqlStr);

    -- Now, anything with src_student_id NOT NULL in mappingDbName must be
    -- added to analysis_db.student.
    -- (1) If orig_student_id IS NULL, insert new student
    -- (2) If orig_student_id NOT NULL, update student.src_student_id
    -- (1)
    SET sqlStr = CONCAT('INSERT INTO student (actual_user_id, anon_user_id, src_student_id) ',
                        'SELECT m.actual_user_id, m.anon_user_id, m.src_student_id ',
                        'FROM ', mappingDbName, '.mapped_student m ',
                        'WHERE m.src_student_id IS NOT NULL AND ',
                        'm.orig_student_id IS NULL ');
    CALL exec(sqlStr);

    -- Update mapped_student.orig_student_id for new students.
    SET sqlStr = CONCAT('UPDATE ', mappingDbName, '.mapped_student m ',
                         'JOIN student s USING (src_student_id) ',
                         'SET m.orig_student_id = s.student_id ',
                         'WHERE m.orig_student_id IS NULL ',
                         'AND s.src_student_id IS NOT NULL');
    CALL exec(sqlStr);

    -- (2)
    SET sqlStr = CONCAT('UPDATE student s ',
                        'JOIN ', mappingDbName, '.mapped_student m ',
                        'ON (m.orig_student_id = s.student_id) ',
                        'SET s.src_student_id = m.src_student_id ',
                        'WHERE m.orig_student_id IS NOT NULL');
    CALL exec(sqlStr);

    DROP TABLE IF EXISTS dbm_student_id_map;
    CREATE TABLE dbm_student_id_map (src_student_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (src_student_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_student_id, student_id
        FROM student
        WHERE src_student_id IS NOT NULL;

    UPDATE student stu JOIN dbm_student_id_map sim ON stu.student_id = sim.student_id
        SET stu.src_student_id = NULL;

    -- And clear src_student_id for mappingDbName
    SET sqlStr = CONCAT('UPDATE ', mappingDbName, '.mapped_student m ',
                        'JOIN dbm_student_id_map sim ON (m.orig_student_id = sim.student_id) ',
                        'SET m.src_student_id = NULL');
    CALL exec(sqlStr);

        /* [12/04/23 - ysahn] Factored out, now it in, line 107 and 122 in SP dbm_run_merge
        CALL dbm_insert_session_student_map();
         */

        /* [12/06/04 - ysahn] Factored out, now it in, line 1060 in SP dbm_insert_instructors
         * Because dbm_class_id_map is populated there
        CALL dbm_insert_rosters();
         */

    CALL debug_log('db_merge_sp','Finished dbm_insert_students.');

    END;

    IF status = 1 THEN
        CALL dbm_error_student();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Merge students belonging to a session into the destination if they don't exist
  already.
  -------------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `dbm_merge_students` $$
CREATE PROCEDURE `dbm_merge_students` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', CONCAT('Starting dbm_merge_students: ', mappingDbName));

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_new_students;
        CREATE TABLE dbm_new_students (
            student_id BIGINT NOT NULL,
            actual_user_id VARCHAR(55) NOT NULL,
            anon_user_id VARCHAR(55) NOT NULL,
            PRIMARY KEY (student_id),
            INDEX (actual_user_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT s.student_id, s.actual_user_id, s.anon_user_id
        FROM adb_source.student s
        /* [2012/6/13 - ysahn]
         * The join to the session_student_map is deprecated, instead using the
         * new column studet_id from session table.
        JOIN adb_source.session_student_map ssm ON ssm.student_id = s.student_id
        JOIN adb_source.session sess ON ssm.session_id = sess.session_id */
        JOIN adb_source.session sess ON s.student_id = sess.student_id
        JOIN dbm_ds_id_map dim ON sess.dataset_id = dim.src_dataset_id
        WHERE merge_flag = TRUE;

        DROP TABLE IF EXISTS dbm_duplicate_students;
        CREATE TABLE dbm_duplicate_students (
            student_id BIGINT NOT NULL,
            src_student_id BIGINT NOT NULL,
            PRIMARY KEY (src_student_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;
        CALL exec(CONCAT("INSERT INTO dbm_duplicate_students ",
            "SELECT stu.orig_student_id AS student_id, nstu.student_id AS src_student_id ",
            "FROM dbm_new_students nstu ",
            "JOIN ", mappingDbName, ".mapped_student stu ",
            "ON nstu.actual_user_id = stu.actual_user_id"));

        UPDATE student stu JOIN dbm_duplicate_students dstu ON stu.student_id = dstu.student_id
        SET stu.src_student_id = dstu.src_student_id;

        INSERT INTO student (actual_user_id, anon_user_id, src_student_id)
        SELECT nstu.actual_user_id, nstu.anon_user_id, nstu.student_id
        FROM dbm_new_students nstu
        LEFT JOIN dbm_duplicate_students dstu ON nstu.student_id = dstu.src_student_id
        WHERE dstu.student_id IS NULL;

        DROP TABLE IF EXISTS dbm_student_id_map;
        CREATE TABLE dbm_student_id_map (src_student_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (src_student_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_student_id, student_id
        FROM student
        WHERE src_student_id IS NOT NULL;

        UPDATE student stu JOIN dbm_student_id_map stim ON stu.student_id = stim.student_id
        SET stu.src_student_id = NULL;

        CALL exec(CONCAT("INSERT IGNORE INTO ",
              mappingDbName,
              ".mapped_student (actual_user_id, anon_user_id, orig_student_id) ",
              "SELECT DISTINCT s.actual_user_id, s.anon_user_id, s.student_id ",
              "FROM student s ",
              "JOIN dbm_student_id_map dsim ON s.student_id = dsim.student_id"));

        /* [12/06/13 - ysahn] Factored out, now it in, in SP dbm_run_merge
        CALL dbm_merge_session_student_map();
        */

        /* [12/06/13 - ysahn] Factored out, now it in, line 1060 in SP dbm_insert_instructors
         * Because dbm_class_id_map is populated there
        CALL dbm_merge_rosters();
        */
    END;

    IF status = 1 THEN
        CALL dbm_error_student();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_students.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the session_student_map table.
  -------------------------------------------------------------------------------
  [12/01/23 - ysahn] TODO: Eventually this procedure should be deprecated
*/
DROP PROCEDURE IF EXISTS `dbm_insert_session_student_map` $$
CREATE PROCEDURE `dbm_insert_session_student_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_session_student_map.');

    INSERT INTO session_student_map (session_id, student_id)
    SELECT seim.session_id, sim.student_id
    FROM adb_source.session_student_map ssm
    JOIN dbm_session_id_map seim ON ssm.session_id = seim.src_session_id
    JOIN dbm_student_id_map sim ON ssm.student_id = sim.src_student_id
    ORDER BY seim.session_id, sim.student_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_session_student_map.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge into the session student map, logically do nothing on duplicates.
  -------------------------------------------------------------------------------
  [12/01/23 - ysahn] TODO: Eventually this procedure should be removed
*/
DROP PROCEDURE IF EXISTS `dbm_merge_session_student_map` $$
CREATE PROCEDURE `dbm_merge_session_student_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_merge_session_student_map.');

    DROP TABLE IF EXISTS dbm_existing_session_student_maps;
    CREATE TABLE dbm_existing_session_student_maps (session_id BIGINT NOT NULL,
        student_id BIGINT NOT NULL,
        PRIMARY KEY (session_id, student_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT ssm.session_id, ssm.student_id
    FROM session_student_map ssm
    JOIN dbm_session_id_map seim ON ssm.session_id = seim.session_id
    JOIN dbm_student_id_map stim ON ssm.student_id = stim.student_id;

    INSERT INTO session_student_map (session_id, student_id)
    SELECT seim.session_id, sim.student_id
    FROM adb_source.session_student_map ssm
    JOIN dbm_session_id_map seim ON ssm.session_id = seim.src_session_id
    JOIN dbm_student_id_map sim ON ssm.student_id = sim.src_student_id
        ON DUPLICATE KEY UPDATE session_id = seim.session_id;

    CALL debug_log('db_merge_sp','Finished dbm_merge_session_student_map.');

END $$
/*
  -------------------------------------------------------------------------------
  Copy data to the roster table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS dbm_insert_rosters $$
CREATE PROCEDURE `dbm_insert_rosters` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_insert_rosters.');

    DROP TABLE IF EXISTS dbm_existing_rosters;
    CREATE TABLE dbm_existing_rosters (class_id BIGINT NOT NULL,
        student_id BIGINT NOT NULL,
        PRIMARY KEY (class_id, student_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT class_id, student_id
    FROM roster r
    JOIN dbm_class_id_map USING (class_id)
    JOIN dbm_student_id_map USING (student_id);

    INSERT INTO roster (class_id, student_id)
    SELECT DISTINCT cim.class_id, sim.student_id
    FROM adb_source.roster r
    JOIN dbm_student_id_map sim ON r.student_id = sim.src_student_id
    JOIN adb_source.class_dataset_map cdm ON r.class_id = cdm.class_id
    JOIN dbm_ds_id_map dim ON dim.src_dataset_id = cdm.dataset_id
    JOIN dbm_class_id_map cim ON r.class_id = cim.src_class_id
    WHERE dim.merge_flag = FALSE
    ORDER BY cim.class_id, sim.student_id
        ON DUPLICATE KEY UPDATE class_id = cim.class_id;

    CALL debug_log('db_merge_sp','Finished dbm_insert_rosters.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge into rosters, do nothing on duplicate key.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS dbm_merge_rosters $$
CREATE PROCEDURE `dbm_merge_rosters` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_merge_rosters.');

    DROP TABLE IF EXISTS dbm_existing_rosters;
    CREATE TABLE dbm_existing_rosters (class_id BIGINT NOT NULL,
        student_id BIGINT NOT NULL,
        PRIMARY KEY (class_id, student_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT class_id, student_id
    FROM roster r
    JOIN dbm_class_id_map USING (class_id)
    JOIN dbm_student_id_map USING (student_id);

    INSERT INTO roster (class_id, student_id)
    SELECT DISTINCT cim.class_id, sim.student_id
    FROM adb_source.roster r
    JOIN dbm_student_id_map sim ON r.student_id = sim.src_student_id
    JOIN adb_source.class_dataset_map cdm ON r.class_id = cdm.class_id
    JOIN dbm_ds_id_map dim ON dim.src_dataset_id = cdm.dataset_id
    JOIN dbm_class_id_map cim ON r.class_id = cim.src_class_id
    WHERE dim.merge_flag = TRUE
        ON DUPLICATE KEY UPDATE class_id = cim.class_id;

    CALL debug_log('db_merge_sp','Finished dbm_merge_rosters.');

END $$

/*
  -------------------------------------------------------------------------------
  Copy data to the ds_condition table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_insert_ds_conditions` $$
CREATE PROCEDURE `dbm_insert_ds_conditions` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_insert_ds_conditions.');

    BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

    INSERT INTO ds_condition (condition_name, type, description, dataset_id, src_condition_id)
        SELECT REPLACE(REPLACE(REPLACE(condition_name, '\t', ' '), '\r', ' '), '\n', ' ') as condition_name,
            REPLACE(REPLACE(REPLACE(type, '\t', ' '), '\r', ' '), '\n', ' ') as type, description, dim.dataset_id, condition_id
        FROM adb_source.ds_condition dsc
        JOIN dbm_ds_id_map dim ON dsc.dataset_id = dim.src_dataset_id
        WHERE dim.merge_flag = FALSE;

        DROP TABLE IF EXISTS dbm_ds_condition_id_map;
        CREATE TABLE dbm_ds_condition_id_map (src_condition_id BIGINT NOT NULL,
            condition_id BIGINT NOT NULL,
            PRIMARY KEY (src_condition_id, condition_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_condition_id, condition_id
        FROM ds_condition
        WHERE src_condition_id IS NOT NULL;

        UPDATE ds_condition dscon JOIN dbm_ds_condition_id_map dscim ON dscon.condition_id = dscim.condition_id
        SET dscon.src_condition_id = NULL;

        CALL debug_log('db_merge_sp','Finished dbm_insert_ds_conditions.');
    END;

    IF status = 1 THEN
        CALL dbm_error_ds_condition();
    END IF;

END $$

/*
  -------------------------------------------------------------------------------
  Merge into the ds_condition table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_ds_conditions` $$
CREATE PROCEDURE `dbm_merge_ds_conditions` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp','Starting dbm_merge_ds_conditions.');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_existing_conditions;
        CREATE TABLE dbm_existing_conditions (condition_id BIGINT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT condition_id
        FROM ds_condition
        JOIN dbm_ds_id_map USING (dataset_id);

        INSERT INTO ds_condition (condition_name, type, description, dataset_id, src_condition_id)
        SELECT REPLACE(REPLACE(REPLACE(condition_name, '\t', ' '), '\r', ' '), '\n', ' ') as condition_name,
            REPLACE(REPLACE(REPLACE(type, '\t', ' '), '\r', ' '), '\n', ' ') as type, description, dim.dataset_id, condition_id
        FROM adb_source.ds_condition dsc
        JOIN dbm_ds_id_map dim ON dsc.dataset_id = dim.src_dataset_id
        WHERE dim.merge_flag = TRUE
            ON DUPLICATE KEY UPDATE src_condition_id = dsc.condition_id;

        DROP TABLE IF EXISTS dbm_ds_condition_id_map;
        CREATE TABLE dbm_ds_condition_id_map (src_condition_id BIGINT NOT NULL,
            condition_id BIGINT NOT NULL,
            PRIMARY KEY (src_condition_id, condition_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_condition_id, condition_id
        FROM ds_condition
        WHERE src_condition_id IS NOT NULL;

        UPDATE ds_condition dscon
        JOIN dbm_ds_condition_id_map dsim ON dscon.condition_id = dsim.condition_id
        SET dscon.src_condition_id = NULL;
    END;

    IF status = 1 THEN
        CALL dbm_error_ds_condition();
    END IF;

    CALL debug_log('db_merge_sp','Finished dbm_merge_ds_conditions.');

END $$

/*
  -------------------------------------------------------------------------------
  Merge new transactions and update the src_transaction_id of existing ones, in
  case the mappings reference existing transactions.  Update the dbm_max_table_counts
  value for this dataset.  An entry should already exist, as the dataset was imported
  new initially with the DB Merge tool.  Calls dbm_merge_transaction_condition_map().
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `dbm_merge_tutor_transactions` $$
CREATE PROCEDURE `dbm_merge_tutor_transactions` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', 'Starting dbm_merge_tutor_transactions');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_src_tutor_transactions;
        CREATE TABLE dbm_src_tutor_transactions (transaction_id BIGINT PRIMARY KEY,
        guid CHAR(32),
            session_id BIGINT NOT NULL,
            transaction_time DATETIME NOT NULL,
            transaction_time_ms INT,
            time_zone VARCHAR(50),
            transaction_type_tutor VARCHAR(30),
            transaction_type_tool VARCHAR(30),
            transaction_subtype_tutor VARCHAR(30),
            transaction_subtype_tool VARCHAR(30),
            outcome VARCHAR(30),
            attempt_at_subgoal INT,
            is_last_attempt BOOLEAN,
            dataset_id INT NOT NULL,
            problem_id BIGINT NOT NULL,
            subgoal_id BIGINT,
            subgoal_attempt_id BIGINT NOT NULL,
            feedback_id BIGINT,
            class_id BIGINT,
            school_id INT,
            help_level SMALLINT,
            total_num_hints SMALLINT,
            duration INT,
            prob_solving_sequence INT,
            problem_event_id BIGINT
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT transaction_id, guid, seim.session_id, transaction_time, transaction_time_ms, time_zone,
            transaction_type_tutor, transaction_type_tool, transaction_subtype_tutor,
            transaction_subtype_tool, outcome, attempt_at_subgoal, is_last_attempt, tt.dataset_id, pim.problem_id,
            sgim.subgoal_id, sgaim.subgoal_attempt_id, fim.feedback_id, cim.class_id, schim.school_id,
            help_level, total_num_hints, duration, prob_solving_sequence, peim.problem_event_id
        FROM dbm_src_tt_view tt
        JOIN dbm_session_id_map seim ON tt.session_id = seim.src_session_id
        JOIN dbm_problem_id_map pim ON tt.problem_id = pim.src_problem_id
        LEFT JOIN dbm_problem_event_id_map peim ON tt.problem_event_id = peim.src_problem_event_id
        LEFT JOIN dbm_subgoal_id_map sgim ON tt.subgoal_id = sgim.src_subgoal_id
        JOIN dbm_subgoal_att_id_map sgaim ON tt.subgoal_attempt_id = sgaim.src_subgoal_att_id
        LEFT JOIN dbm_feedback_id_map fim ON tt.feedback_id = fim.src_feedback_id
        LEFT JOIN dbm_class_id_map cim ON tt.class_id = cim.src_class_id
        LEFT JOIN dbm_school_id_map schim ON tt.school_id = schim.src_school_id;

        DROP TABLE IF EXISTS dbm_src_null_tutor_transactions;
        CREATE TABLE dbm_src_null_tutor_transactions (transaction_id BIGINT PRIMARY KEY,
        guid CHAR(32),
            session_id BIGINT NOT NULL,
            transaction_time DATETIME NOT NULL,
            transaction_time_ms INT,
            time_zone VARCHAR(50),
            transaction_type_tutor VARCHAR(30),
            transaction_type_tool VARCHAR(30),
            transaction_subtype_tutor VARCHAR(30),
            transaction_subtype_tool VARCHAR(30),
            outcome VARCHAR(30),
            attempt_at_subgoal INT,
            is_last_attempt BOOLEAN,
            dataset_id INT NOT NULL,
            problem_id BIGINT NOT NULL,
            subgoal_id BIGINT,
            subgoal_attempt_id BIGINT NOT NULL,
            feedback_id BIGINT,
            class_id BIGINT,
            school_id INT,
            help_level SMALLINT,
            total_num_hints SMALLINT,
            duration INT,
            prob_solving_sequence INT,
            INDEX(session_id, transaction_time, dataset_id, problem_id, subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT transaction_id, guid, session_id, transaction_time,
            IFNULL(transaction_time_ms,-1) AS transaction_time_ms,
            IFNULL(time_zone,'') AS time_zone,
            IFNULL(transaction_type_tutor,'') AS transaction_type_tutor,
            IFNULL(transaction_type_tool,'') AS transaction_type_tool,
            IFNULL(transaction_subtype_tutor,'') AS transaction_subtype_tutor,
            IFNULL(transaction_subtype_tool,'') AS transaction_subtype_tool,
            IFNULL(outcome,'') AS outcome, IFNULL(attempt_at_subgoal,0) AS attempt_at_subgoal,
            is_last_attempt,
            dataset_id, problem_id, IFNULL(subgoal_id,0) AS subgoal_id,
            subgoal_attempt_id, IFNULL(feedback_id,0) AS feedback_id,
            IFNULL(class_id,0) AS class_id, IFNULL(school_id,0) AS school_id,
            IFNULL(help_level,-1) AS help_level, IFNULL(total_num_hints,-1) AS total_num_hints,
            IFNULL(duration,-1) AS duration,
            IFNULL(prob_solving_sequence,-1) AS prob_solving_sequence
        FROM dbm_src_tutor_transactions;

        DROP TABLE IF EXISTS dbm_dest_null_tutor_transactions;
        CREATE TABLE dbm_dest_null_tutor_transactions (transaction_id BIGINT PRIMARY KEY,
        guid CHAR(32),
            session_id BIGINT NOT NULL,
            transaction_time DATETIME NOT NULL,
            transaction_time_ms INT,
            time_zone VARCHAR(50),
            transaction_type_tutor VARCHAR(30),
            transaction_type_tool VARCHAR(30),
            transaction_subtype_tutor VARCHAR(30),
            transaction_subtype_tool VARCHAR(30),
            outcome VARCHAR(30),
            attempt_at_subgoal INT,
            is_last_attempt BOOLEAN,
            dataset_id INT NOT NULL,
            problem_id BIGINT NOT NULL,
            subgoal_id BIGINT,
            subgoal_attempt_id BIGINT NOT NULL,
            feedback_id BIGINT,
            class_id BIGINT,
            school_id INT,
            help_level SMALLINT,
            total_num_hints SMALLINT,
            duration INT,
            prob_solving_sequence INT,
            INDEX(session_id, transaction_time, dataset_id, problem_id, subgoal_attempt_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT transaction_id, guid, session_id, transaction_time,
            IFNULL(transaction_time_ms,-1) AS transaction_time_ms,
            IFNULL(time_zone,'') AS time_zone,
            IFNULL(transaction_type_tutor,'') AS transaction_type_tutor,
            IFNULL(transaction_type_tool,'') AS transaction_type_tool,
            IFNULL(transaction_subtype_tutor,'') AS transaction_subtype_tutor,
            IFNULL(transaction_subtype_tool,'') AS transaction_subtype_tool,
            IFNULL(outcome,'') AS outcome, IFNULL(attempt_at_subgoal,0) AS attempt_at_subgoal,
            is_last_attempt,
            dataset_id, problem_id, IFNULL(subgoal_id,0) AS subgoal_id, subgoal_attempt_id,
            IFNULL(feedback_id,0) AS feedback_id, IFNULL(class_id,0) AS class_id,
            IFNULL(school_id,0) AS school_id, IFNULL(help_level,-1) AS help_level,
            IFNULL(total_num_hints,-1) AS total_num_hints, IFNULL(duration,-1) AS duration,
            IFNULL(prob_solving_sequence,-1) AS prob_solving_sequence
        FROM dbm_dest_tt_view;

        DROP TABLE IF EXISTS dbm_duplicate_transactions;
        CREATE TABLE dbm_duplicate_transactions (transaction_id BIGINT NOT NULL,
            src_transaction_id BIGINT NOT NULL,
            PRIMARY KEY (src_transaction_id, transaction_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT desttt.transaction_id, srctt.transaction_id AS src_transaction_id
        FROM dbm_src_null_tutor_transactions srctt
        JOIN dbm_dest_null_tutor_transactions desttt
            USING (session_id, transaction_time, dataset_id, problem_id, subgoal_attempt_id,
                transaction_time_ms, time_zone, transaction_type_tutor, transaction_type_tool,
                transaction_subtype_tutor, transaction_subtype_tool, outcome, attempt_at_subgoal,
                is_last_attempt,
                subgoal_id, feedback_id, class_id, school_id, help_level, total_num_hints,
                prob_solving_sequence);

        UPDATE tutor_transaction tt
        JOIN dbm_duplicate_transactions dtt ON tt.transaction_id = dtt.transaction_id
        SET tt.src_transaction_id = dtt.src_transaction_id;

        INSERT INTO tutor_transaction (guid, session_id, transaction_time, transaction_time_ms, time_zone,
            transaction_type_tutor, transaction_type_tool, transaction_subtype_tutor,
            transaction_subtype_tool, outcome, attempt_at_subgoal, is_last_attempt,
            dataset_id, problem_id,
            subgoal_id, subgoal_attempt_id, feedback_id, class_id, school_id, help_level,
            total_num_hints, duration, prob_solving_sequence, src_transaction_id, problem_event_id)
        SELECT guid, session_id, transaction_time, transaction_time_ms, time_zone,
            transaction_type_tutor, transaction_type_tool, transaction_subtype_tutor,
            transaction_subtype_tool, outcome, attempt_at_subgoal, is_last_attempt,
            dataset_id, problem_id,
            subgoal_id, subgoal_attempt_id, feedback_id, class_id, school_id,
            help_level, total_num_hints, duration, prob_solving_sequence, srctt.transaction_id, problem_event_id
        FROM dbm_src_tutor_transactions srctt
        LEFT JOIN dbm_duplicate_transactions dtx ON srctt.transaction_id = dtx.src_transaction_id
        WHERE dtx.src_transaction_id IS NULL;

        DROP TABLE IF EXISTS dbm_transaction_id_map;
        CREATE TABLE dbm_transaction_id_map (src_transaction_id BIGINT NOT NULL,
            transaction_id bigint NOT NULL,
            dataset_id BIGINT NOT NULL,
            PRIMARY KEY (src_transaction_id, transaction_id),
            INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT src_transaction_id, transaction_id, tt.dataset_id
        FROM tutor_transaction tt
        JOIN dbm_ds_id_map dim ON dim.dataset_id = tt.dataset_id
        WHERE src_transaction_id IS NOT NULL AND merge_flag = TRUE;

        UPDATE tutor_transaction tt
        JOIN dbm_transaction_id_map tim ON tt.transaction_id = tim.transaction_id
        SET tt.src_transaction_id = NULL;

        UPDATE dbm_max_table_counts maxcnts JOIN (SELECT count(transaction_id) txcnt, dataset_id
            FROM tutor_transaction JOIN dbm_ds_id_map USING (dataset_id) WHERE merge_flag = TRUE
            GROUP BY dataset_id) cnt USING (dataset_id)
        SET prev_count = row_count, row_count = cnt.txcnt;

        CALL dbm_merge_transaction_condition_map();

    END;

    IF status = 1 THEN
        CALL dbm_error_tutor_transaction();
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_merge_tutor_transactions');
END $$

DROP PROCEDURE IF EXISTS `dbm_merge_transaction_condition_map` $$
CREATE PROCEDURE `dbm_merge_transaction_condition_map` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', 'Starting dbm_merge_transaction_condition_map');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO transaction_condition_map (transaction_id, condition_id)
        SELECT tim.transaction_id, dscim.condition_id
        FROM adb_source.transaction_condition_map tcm
        JOIN dbm_transaction_id_map tim ON tcm.transaction_id = tim.src_transaction_id
        JOIN dbm_ds_condition_id_map dscim ON tcm.condition_id = dscim.src_condition_id
            ON DUPLICATE KEY UPDATE transaction_id = tim.transaction_id;

        CALL dbm_merge_transaction_skill_map();

    END;

    IF status = 1 THEN
        CALL raise_error('SQLException encountered while inserting into the transaction_condition_map table');
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_merge_transaction_condition_map');

END $$

DROP PROCEDURE IF EXISTS `dbm_merge_transaction_skill_map` $$
CREATE PROCEDURE `dbm_merge_transaction_skill_map` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', 'Starting dbm_merge_transaction_skill_map');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO transaction_skill_map (transaction_id, skill_id)
        SELECT tim.transaction_id, skim.skill_id
        FROM adb_source.transaction_skill_map tskm
        JOIN dbm_transaction_id_map tim ON tskm.transaction_id = tim.src_transaction_id
        JOIN dbm_skill_id_map skim ON tskm.skill_id = skim.src_skill_id
            ON DUPLICATE KEY UPDATE transaction_id = tim.transaction_id;

        CALL dbm_merge_transaction_skill_event();

    END;

    IF status = 1 THEN
        CALL raise_error('SQLException encountered while inserting into the transaction_skill_map table');
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_merge_transaction_skill_map');

END $$

DROP PROCEDURE IF EXISTS `dbm_merge_transaction_skill_event` $$
CREATE PROCEDURE `dbm_merge_transaction_skill_event` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', 'Starting dbm_merge_transaction_skill_event');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        INSERT INTO transaction_skill_event (transaction_id, skill_id, resulting_p_known,
            initial_p_known)
        SELECT tim.transaction_id, skim.skill_id, resulting_p_known, initial_p_known
        FROM adb_source.transaction_skill_event tske
        JOIN dbm_transaction_id_map tim ON tske.transaction_id = tim.src_transaction_id
        JOIN dbm_skill_id_map skim ON tske.skill_id = skim.src_skill_id
            ON DUPLICATE KEY UPDATE resulting_p_known = tske.resulting_p_known,
                initial_p_known = tske.initial_p_known;

        CALL dbm_merge_custom_fields();

    END;

    IF status = 1 THEN
        CALL raise_error('SQLException encountered while inserting into the transaction_skill_event table');
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_merge_transaction_skill_event');

END $$

DROP PROCEDURE IF EXISTS `dbm_merge_custom_fields` $$
CREATE PROCEDURE `dbm_merge_custom_fields` ()
    SQL SECURITY INVOKER
BEGIN

    DECLARE status INT DEFAULT 0;
    CALL debug_log('db_merge_sp', 'Starting dbm_merge_custom_fields');

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET status = 1;

        DROP TABLE IF EXISTS dbm_src_custom_fields;
        CREATE TABLE dbm_src_custom_fields (custom_field_id BIGINT PRIMARY KEY,
            custom_field_name  VARCHAR(255)  NOT NULL,
            description     VARCHAR(255) ,
            dataset_id         INT           NOT NULL,
            level        VARCHAR(255),
            owner        VARCHAR(255),
            INDEX (dataset_id, custom_field_name(5))
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT custom_field_id, REPLACE(REPLACE(REPLACE(custom_field_name, '\t', ' '), '\r', ' '), '\n', ' ') as custom_field_name,
            description, dim.dataset_id,
            level, IFNULL(owner, 'system')
        FROM adb_source.custom_field cf
        JOIN dbm_ds_id_map dim ON cf.dataset_id = dim.src_dataset_id;

        DROP TABLE IF EXISTS dbm_dest_custom_fields;
        CREATE TABLE dbm_dest_custom_fields (custom_field_id BIGINT PRIMARY KEY,
            custom_field_name  VARCHAR(255)  NOT NULL,
            description     VARCHAR(255)  ,
            dataset_id         INT           NOT NULL,
            level        VARCHAR(255),
            owner        VARCHAR(255),
            INDEX (dataset_id, custom_field_name(5))
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT custom_field_id, custom_field_name, description, dataset_id, level, IFNULL(owner, 'system')
        FROM custom_field
        JOIN dbm_ds_id_map dim USING (dataset_id);


        INSERT INTO custom_field (custom_field_name, description, dataset_id, level, owner)
        SELECT srccf.custom_field_name, srccf.description, srccf.dataset_id,
            srccf.level, srccf.owner
        FROM dbm_src_custom_fields srccf
        LEFT JOIN dbm_dest_custom_fields destcf
            ON srccf.dataset_id = destcf.dataset_id
                AND srccf.custom_field_name = destcf.custom_field_name
        WHERE destcf.dataset_id IS NULL;

        DROP TABLE IF EXISTS dbm_src_cf_tx_level;
        CREATE TABLE dbm_src_cf_tx_level (custom_field_id BIGINT,
            transaction_id  BIGINT,
            value     VARCHAR(255) ,
            big_value     MEDIUMTEXT ,
            type VARCHAR(255),
            logging_flag BOOL,
            INDEX (custom_field_id, transaction_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cf.custom_field_id, tim.transaction_id,
            REPLACE(REPLACE(REPLACE(value, '\t', ' '), '\r', ' '), '\n', ' ') as value,
            REPLACE(REPLACE(REPLACE(big_value, '\t', ' '), '\r', ' '), '\n', ' ') as big_value, type, logging_flag
        FROM adb_source.cf_tx_level srccflevel
        JOIN adb_source.custom_field srccf ON srccf.custom_field_id = srccflevel.custom_field_id
        JOIN dbm_transaction_id_map tim ON srccflevel.transaction_id = tim.src_transaction_id
        JOIN dbm_ds_id_map dim ON srccf.dataset_id = dim.src_dataset_id
        JOIN custom_field cf ON cf.custom_field_name = REPLACE(REPLACE(REPLACE(srccf.custom_field_name, '\t', ' '), '\r', ' '), '\n', ' ')
            and dim.dataset_id = cf.dataset_id;

        DROP TABLE IF EXISTS dbm_dest_cf_tx_level;
        CREATE TABLE dbm_dest_cf_tx_level (custom_field_id BIGINT,
            transaction_id  BIGINT,
            value     VARCHAR(255) ,
            big_value     MEDIUMTEXT ,
            type VARCHAR(255),
            logging_flag BOOL,
            INDEX (custom_field_id, transaction_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cflevel.custom_field_id, transaction_id, value, big_value, type, logging_flag
        FROM cf_tx_level cflevel
        JOIN custom_field cf ON cf.custom_field_id = cflevel.custom_field_id
        JOIN dbm_ds_id_map dim ON cf.dataset_id = dim.dataset_id;

        INSERT INTO cf_tx_level (custom_field_id, transaction_id, value, big_value, type, logging_flag)
        SELECT srccf.custom_field_id, srccf.transaction_id, srccf.value, srccf.big_value, srccf.type, srccf.logging_flag
        FROM dbm_src_cf_tx_level srccf
        LEFT JOIN dbm_dest_cf_tx_level destcf
            ON srccf.custom_field_id = destcf.custom_field_id
                AND srccf.transaction_id = destcf.transaction_id
        WHERE destcf.transaction_id IS NULL;

    END;

    IF status = 1 THEN
        CALL raise_error('SQLException encountered while inserting into the custom_field table');
    END IF;

    CALL debug_log('db_merge_sp', 'Finished dbm_merge_custom_fields');

END $$

DROP PROCEDURE IF EXISTS `dbm_drop_mapping_tables` $$
CREATE PROCEDURE `dbm_drop_mapping_tables` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp','Starting dbm_drop_mapping_tables.');

    DROP TABLE IF EXISTS dbm_class_id_map, dbm_ds_level_id_map, dbm_ds_level_seq_id_map,
        dbm_ds_condition_id_map, dbm_ds_id_map, dbm_feedback_id_map, dbm_instructor_id_map,
        dbm_problem_id_map, dbm_problem_event_id_map,
        dbm_school_id_map, dbm_session_id_map, dbm_skill_id_map,
        dbm_skill_model_id_map, dbm_student_id_map, dbm_subgoal_att_id_map, dbm_subgoal_id_map,
        dbm_transaction_id_map;

    CALL debug_log('db_merge_sp','Finished dbm_drop_mapping_tables.');

END $$

DROP PROCEDURE IF EXISTS `dbm_drop_helper_tables` $$
CREATE PROCEDURE `dbm_drop_helper_tables` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp', 'Starting dbm_drop_helper_tables.');

    DROP VIEW IF EXISTS dbm_src_tt_view, dbm_dest_tt_view;

    CALL debug_log('db_merge_sp', 'Finished dbm_drop_helper_tables.');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_dataset_level` $$
CREATE PROCEDURE `dbm_error_dataset_level` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp', 'Error in dataset_level');
    IF NOT EXISTS(SELECT CONSTRAINT_NAME FROM information_schema.referential_constraints
        WHERE table_name = 'dataset_level' AND constraint_schema = 'analysis_db'
    AND constraint_name = 'dataset_level_fkey_parent') THEN

    DROP TABLE IF EXISTS incorrect_parents;
    CREATE TABLE incorrect_parents (dataset_level_id INT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dl.dataset_level_id
    FROM dataset_level dl
    LEFT JOIN dataset_level dl2 ON dl.parent_id = dl2.dataset_level_id
    JOIN dbm_ds_id_map dim ON dl.dataset_id = dim.dataset_id AND dl2.dataset_id = dim.dataset_id
    WHERE dl.parent_id IS NOT NULL AND dl2.dataset_level_id IS NULL;

    DELETE dl FROM dataset_level dl JOIN incorrect_parents ip ON dl.dataset_level_id = ip.dataset_level_id;

    ALTER TABLE dataset_level ADD CONSTRAINT `dataset_level_fkey_parent` FOREIGN KEY (parent_id)
           REFERENCES dataset_level(dataset_level_id) ON DELETE CASCADE ON UPDATE CASCADE;
    END IF;

    UPDATE dataset_level SET src_dataset_level_id = NULL WHERE src_dataset_level_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the dataset_level table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_dataset_level_sequence` $$
CREATE PROCEDURE `dbm_error_dataset_level_sequence` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in dataset_level_sequence');
    UPDATE dataset_level_sequence SET src_dataset_level_seq_id = NULL
    WHERE src_dataset_level_seq_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the dataset_level_sequence table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_school` $$
CREATE PROCEDURE `dbm_error_school` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in school');
    UPDATE school SET src_school_id = NULL WHERE src_school_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the school table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_instructor` $$
CREATE PROCEDURE `dbm_error_instructor` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in instructor');
    UPDATE instructor SET src_instructor_id = NULL WHERE src_instructor_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the instructor table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_class` $$
CREATE PROCEDURE `dbm_error_class` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in class');
    UPDATE class SET src_class_id = NULL WHERE src_class_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the class table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_session` $$
CREATE PROCEDURE `dbm_error_session` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in session');
    UPDATE session SET src_session_id = NULL WHERE src_session_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the session table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_problem` $$
CREATE PROCEDURE `dbm_error_problem` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in problem');
    UPDATE problem SET src_problem_id = NULL WHERE src_problem_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the problem table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_subgoal` $$
CREATE PROCEDURE `dbm_error_subgoal` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in subgoal');
    UPDATE subgoal SET src_subgoal_id = NULL WHERE src_subgoal_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the subgoal table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_subgoal_attempt` $$
CREATE PROCEDURE `dbm_error_subgoal_attempt` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in subgoal_attempt');
    UPDATE subgoal_attempt SET src_subgoal_att_id = NULL WHERE src_subgoal_att_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the subgoal_attempt table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_feedback` $$
CREATE PROCEDURE `dbm_error_feedback` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in feedback');
    UPDATE feedback SET src_feedback_id = NULL WHERE src_feedback_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the feedback table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_skill_model` $$
CREATE PROCEDURE `dbm_error_skill_model` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in skill_model');
    UPDATE skill_model SET src_skill_model_id = NULL WHERE src_skill_model_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the skill_model table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_skill` $$
CREATE PROCEDURE `dbm_error_skill` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in skill');
    UPDATE skill SET src_skill_id = NULL WHERE src_skill_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the skill table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_student` $$
CREATE PROCEDURE `dbm_error_student` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in student');
    UPDATE student SET src_student_id = NULL WHERE src_student_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the student table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_ds_condition` $$
CREATE PROCEDURE `dbm_error_ds_condition` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in ds_condition');
    UPDATE ds_condition SET src_condition_id = NULL WHERE src_condition_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the ds_condition table');
END $$

DROP PROCEDURE IF EXISTS `dbm_error_tutor_transaction` $$
CREATE PROCEDURE `dbm_error_tutor_transaction` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Error in tutor_transaction');
    UPDATE tutor_transaction SET src_transaction_id = NULL WHERE src_transaction_id IS NOT NULL;

    CALL raise_error('SQLException encountered while inserting into the tutor_transaction table');
END $$

DROP PROCEDURE IF EXISTS `raise_error` $$
CREATE PROCEDURE `raise_error` (error VARCHAR(255))
BEGIN
    /* Rollback deletes won't cascade if foreign key checks aren't reenabled. */
    SET foreign_key_checks = 1;
    SET @sql = CONCAT(" Update '", error, "' SET x=1");
    PREPARE raise_error FROM @sql;
    EXECUTE raise_error;
    DEALLOCATE PREPARE raise_error;
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_transactions` $$
CREATE PROCEDURE `dbm_rollback_transactions` ()
    SQL SECURITY INVOKER
BEGIN
    SET foreign_key_checks = 1;
    DELETE tt FROM tutor_transaction tt JOIN dbm_ds_id_map dim ON tt.dataset_id = dim.dataset_id;
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_dataset` $$
CREATE PROCEDURE `dbm_rollback_dataset` ()
    SQL SECURITY INVOKER
BEGIN
    /* Find unshared curriculum */
    DROP TABLE IF EXISTS dbm_curriculum_to_delete;
    CREATE TABLE dbm_curriculum_to_delete (curriculum_id INT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT curriculum_id
    FROM curriculum
    JOIN (SELECT curriculum_id
        FROM curriculum
        JOIN ds_dataset using (curriculum_id)
        JOIN dbm_ds_id_map using (dataset_id)) newCurr USING (curriculum_id)
    JOIN ds_dataset using (curriculum_id)
    GROUP BY curriculum_name
    HAVING count(curriculum_id) = 1;

    DELETE ds FROM ds_dataset ds JOIN dbm_ds_id_map dim ON ds.dataset_id = dim.dataset_id;

    DELETE curr FROM curriculum curr JOIN dbm_curriculum_to_delete USING (curriculum_id);

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_subgoal_att_id_map") THEN

        DELETE sga FROM subgoal_attempt sga JOIN dbm_subgoal_att_id_map sgaim ON sga.subgoal_attempt_id = sgaim.subgoal_attempt_id;
    END IF;
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_subject_data` $$
CREATE PROCEDURE `dbm_rollback_subject_data` (mappingDbName VARCHAR(20))
    SQL SECURITY INVOKER
BEGIN

    /* Differentiate between those that were inserted and those that were updated */
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_instructor_id_map") THEN

        DROP TABLE IF EXISTS dbm_instructors_to_delete;
        CREATE TABLE dbm_instructors_to_delete (instructor_id BIGINT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT ii.instructor_id
        FROM dbm_instructor_id_map ii
        LEFT JOIN dbm_duplicate_instructors di ON ii.instructor_id = di.instructor_id
        WHERE di.instructor_id IS NULL;
    END IF;

    /* Only delete classes belonging to datasets being merged */
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_class_id_map") THEN

        DROP TABLE IF EXISTS dbm_classes_to_delete;
        CREATE TABLE dbm_classes_to_delete (class_id BIGINT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cim.class_id
        FROM dbm_class_id_map cim
        LEFT JOIN dbm_duplicate_classes dc ON cim.class_id = dc.class_id
        WHERE dc.class_id IS NULL;

        DELETE cl FROM class cl JOIN dbm_classes_to_delete ctd ON cl.class_id = ctd.class_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_instructor_id_map") THEN

        DELETE instr FROM instructor instr
        JOIN dbm_instructors_to_delete itd ON instr.instructor_id = itd.instructor_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_student_id_map") THEN

        DROP TABLE IF EXISTS dbm_students_with_datasets;
        CREATE TABLE dbm_students_with_datasets (student_id BIGINT,
            INDEX(student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT student_id
        FROM (SELECT DISTINCT student.student_id, dataset_id
              FROM student
              JOIN dbm_student_id_map USING (student_id)
              /* [2012/06/13 - ysahn] Removed use of session_student_map table
              JOIN session_student_map USING (student_id)
              JOIN session USING (session_id)*/
              JOIN session ON session.student_id = student.student_id
              ) stu
        WHERE stu.dataset_id NOT IN (SELECT dataset_id FROM dbm_ds_id_map);

        DROP TABLE IF EXISTS dbm_students_to_delete;
        CREATE TABLE dbm_students_to_delete (student_id BIGINT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sim.student_id
        FROM dbm_student_id_map sim
        LEFT JOIN dbm_students_with_datasets swd ON sim.student_id = swd.student_id
        WHERE swd.student_id IS NULL;

        DELETE stu FROM student stu JOIN dbm_students_to_delete std ON stu.student_id = std.student_id;
        -- Also need to clean-up the mapping_db.mapped_student table
        CALL exec(CONCAT("DELETE stu FROM ",
             mappingDbName,
             ".mapped_student stu ",
             "JOIN dbm_students_to_delete std ",
             "ON stu.orig_student_id = std.student_id"));

        IF EXISTS(SELECT table_name FROM information_schema.tables
            WHERE table_name = "dbm_class_id_map") THEN

                DROP TABLE IF EXISTS dbm_src_rosters;
                CREATE TABLE dbm_src_rosters (class_id BIGINT NOT NULL,
                    student_id BIGINT NOT NULL,
                    PRIMARY KEY (class_id, student_id)
                ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
                SELECT cim.class_id, sim.student_id
                FROM adb_source.roster r
                JOIN dbm_class_id_map cim ON cim.src_class_id = r.class_id
                JOIN dbm_student_id_map sim ON sim.src_student_id = r.student_id;

                DROP TABLE IF EXISTS dbm_rosters_to_delete;
                CREATE TABLE dbm_rosters_to_delete (class_id BIGINT NOT NULL,
                    student_id BIGINT NOT NULL,
                    PRIMARY KEY (class_id, student_id)
                ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
                SELECT srcrost.class_id, srcrost.student_id
                FROM dbm_src_rosters srcrost
                LEFT JOIN dbm_existing_rosters exrost ON srcrost.class_id = exrost.class_id
                    AND srcrost.student_id = exrost.student_id
                WHERE exrost.class_id IS NULL;

                DELETE ros FROM roster ros JOIN dbm_rosters_to_delete rtd ON ros.class_id = rtd.class_id
                    AND ros.student_id = rtd.student_id;
        END IF;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_school_id_map") THEN

        DROP TABLE IF EXISTS dbm_schools_with_datasets;
        CREATE TABLE dbm_schools_with_datasets (school_id INT,
            dataset_id INT,
            INDEX (school_id),
            INDEX (dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT school_id
        FROM (SELECT school_id, dataset_id
              FROM (SELECT DISTINCT school_id, dataset_id FROM dbm_dest_tt_view) tt
              JOIN dbm_school_id_map USING (school_id)
              UNION
              SELECT school_id, dataset_id
              FROM (SELECT DISTINCT school_id, dataset_id FROM session) sess
              JOIN dbm_school_id_map USING (school_id)
              UNION
              SELECT school_id, dataset_id
              FROM (SELECT DISTINCT school_id, dataset_id FROM class
                    JOIN class_dataset_map USING (class_id)) cl
                    JOIN dbm_school_id_map USING (school_id)) sch
        WHERE sch.dataset_id NOT IN (SELECT dataset_id FROM dbm_ds_id_map);

        DROP TABLE IF EXISTS dbm_schools_to_delete;
        CREATE TABLE dbm_schools_to_delete (school_id INT PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sim.school_id
        FROM dbm_school_id_map sim
        LEFT JOIN dbm_schools_with_datasets swd ON sim.school_id = swd.school_id
        WHERE swd.school_id IS NULL;

        /* 1 in 3 */
        DELETE sch FROM school sch JOIN dbm_schools_to_delete std ON sch.school_id = std.school_id;
    END IF;
END $$

DROP FUNCTION IF EXISTS `dbm_check_dataset_levels` $$
CREATE FUNCTION `dbm_check_dataset_levels` ()
    RETURNS BOOL
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN

    DECLARE srcCount INT DEFAULT 0;
    DECLARE destCount INT DEFAULT 0;

    SELECT count(dataset_level_id) INTO srcCount
    FROM dbm_src_dataset_levels;

    SELECT count(dataset_level_id) INTO destCount
    FROM dbm_dest_dataset_levels ;

    IF srcCount != destCount THEN
        return FALSE;
    END IF;

    IF EXISTS(SELECT *
        FROM dbm_dest_dataset_levels dest
        LEFT JOIN dbm_src_dataset_levels src ON dest.level_name = src.level_name
            AND dest.level_title = src.level_title
            AND dest.description = src.description
            AND dest.parent_level_name = src.parent_level_name
            AND dest.parent_level_title = src.parent_level_title
            AND dest.parent_description = src.parent_description
        WHERE src.level_name IS NULL AND src.parent_level_name IS NULL AND src.level_title IS NULL
            AND src.dataset_id IS NULL
            AND src.description IS NULL AND src.parent_level_title IS NULL
            AND src.parent_dataset_id IS NULL
            AND src.parent_description IS NULL) THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END $$

DROP PROCEDURE IF EXISTS `dbm_create_dataset_levels` $$
CREATE PROCEDURE `dbm_create_dataset_levels` (srcDatasetId INT, destDatasetId INT)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('db_merge_sp', 'Starting dbm_create_dataset_levels');

    DROP TABLE IF EXISTS dbm_dest_dataset_levels;
    CREATE TABLE dbm_dest_dataset_levels (
        dataset_level_id      INT,
        level_name            VARCHAR(100),
        parent_level_name     VARCHAR(100),
        level_title           VARCHAR(100),
        parent_level_title    VARCHAR(100),
        dataset_id            INT,
        parent_dataset_id     INT,
        description           VARCHAR(100),
        parent_description    VARCHAR(100),
        INDEX(level_name, level_title, dataset_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dl.dataset_level_id, dl.level_name, IFNULL(dl2.level_name,'') AS parent_level_name,
        IFNULL(dl2.level_title,'') AS parent_level_title,
        IFNULL(dl2.description,'') AS parent_description,
        IFNULL(dl2.dataset_id,0) AS parent_dataset_id,
        IFNULL(dl.level_title,'') AS level_title, dl.dataset_id,
        IFNULL(dl.description,'') AS description
    FROM dataset_level dl
    LEFT JOIN dataset_level dl2 ON dl.parent_id = dl2.dataset_level_id
    WHERE dl.dataset_id = destDatasetId;

    DROP TABLE IF EXISTS dbm_src_dataset_levels;
    CREATE TABLE dbm_src_dataset_levels (
        dataset_level_id      INT,
        level_name            VARCHAR(100),
        parent_level_name     VARCHAR(100),
        level_title           VARCHAR(100),
        parent_level_title    VARCHAR(100),
        dataset_id            INT,
        parent_dataset_id     INT,
        description           VARCHAR(100),
        parent_description    VARCHAR(100),
        INDEX(level_name, level_title, dataset_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dl.dataset_level_id, REPLACE(REPLACE(REPLACE(dl.level_name, '\t', ' '), '\r', ' '), '\n', ' ') as level_name,
        IFNULL(REPLACE(REPLACE(REPLACE(dl2.level_name, '\t', ' '), '\r', ' '), '\n', ' '), '') AS parent_level_name,
        IFNULL(REPLACE(REPLACE(REPLACE(dl2.level_title, '\t', ' '), '\r', ' '), '\n', ' '), '') AS parent_level_title,
        IFNULL(dl2.description,'') AS parent_description,
        IFNULL(dl2.dataset_id,0) AS parent_dataset_id,
        IFNULL(REPLACE(REPLACE(REPLACE(dl.level_title, '\t', ' '), '\r', ' '), '\n', ' '), '') AS level_title, dl.dataset_id,
        IFNULL(dl.description,'') AS description
    FROM adb_source.dataset_level dl
    LEFT JOIN adb_source.dataset_level dl2 ON dl.parent_id = dl2.dataset_level_id
    WHERE dl.dataset_id = srcDatasetId;

    CALL debug_log('db_merge_sp', 'Finished dbm_create_dataset_levels');
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_dataset_level_sequence` $$
CREATE PROCEDURE `dbm_rollback_merge_dataset_level_sequence` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_ds_level_seq_id_map") THEN

    DROP TABLE IF EXISTS dbm_dls_to_delete;
    CREATE TABLE dbm_dls_to_delete(
        dataset_level_sequence_id INT PRIMARY KEY
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dlsim.dataset_level_sequence_id
    FROM dbm_ds_level_seq_id_map dlsim
    LEFT JOIN dbm_duplicate_dataset_lvl_seq ddls
        ON dlsim.dataset_level_sequence_id = ddls.dataset_level_sequence_id
    WHERE ddls.dataset_level_sequence_id IS NULL;

    DELETE dls FROM dataset_level_sequence dls JOIN dbm_dls_to_delete dlstd
        ON dls.dataset_level_sequence_id = dlstd.dataset_level_sequence_id;
    END IF;

    CALL dbm_rollback_merge_problems();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_classes` $$
CREATE PROCEDURE `dbm_rollback_merge_classes` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_class_id_map") THEN

        CALL debug_log('db_merge_sp', 'classes to delete');
        DROP TABLE IF EXISTS dbm_classes_to_delete;
        CREATE TABLE dbm_classes_to_delete(
            class_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cim.class_id
        FROM dbm_class_id_map cim
        LEFT JOIN dbm_duplicate_classes dcl ON cim.class_id = dcl.class_id
        WHERE dcl.class_id IS NULL;

        DELETE cl FROM class cl JOIN dbm_classes_to_delete ctd ON cl.class_id = ctd.class_id;
    END IF;

    CALL dbm_rollback_merge_instructors();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_instructors` $$
CREATE PROCEDURE `dbm_rollback_merge_instructors` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_instructor_id_map") THEN

        CALL debug_log('db_merge_sp', 'instructors to delete');
        DROP TABLE IF EXISTS dbm_instructors_to_delete;
        CREATE TABLE dbm_instructors_to_delete(
            instructor_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT iim.instructor_id
        FROM dbm_instructor_id_map iim
        LEFT JOIN dbm_duplicate_instructors distr ON iim.instructor_id = distr.instructor_id
        WHERE distr.instructor_id IS NULL;

        DELETE i FROM instructor i JOIN dbm_instructors_to_delete itd ON i.instructor_id = itd.instructor_id;
    END IF;

    CALL dbm_rollback_merge_schools();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_schools` $$
CREATE PROCEDURE `dbm_rollback_merge_schools` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_school_id_map") THEN

        DROP TABLE IF EXISTS dbm_schools_to_delete;
        CREATE TABLE dbm_schools_to_delete(
            school_id INT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sim.school_id
        FROM dbm_school_id_map sim
        LEFT JOIN dbm_duplicate_schools dsch ON sim.school_id = dsch.school_id
        WHERE dsch.school_id IS NULL;

        DELETE sch FROM school sch JOIN dbm_schools_to_delete std ON sch.school_id = std.school_id;
    END IF;

    CALL dbm_rollback_merge_students();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_sessions` $$
CREATE PROCEDURE `dbm_rollback_merge_sessions` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_session_id_map") THEN

        DROP TABLE IF EXISTS dbm_sessions_to_delete;
        CREATE TABLE dbm_sessions_to_delete (
            session_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT seim.session_id
        FROM dbm_session_id_map seim
        LEFT JOIN dbm_duplicate_sessions dsess ON seim.session_id = dsess.session_id
        WHERE dsess.session_id IS NULL;

        DELETE sess FROM session sess JOIN dbm_sessions_to_delete setd ON sess.session_id = setd.session_id;
    END IF;

    CALL dbm_rollback_merge_rosters();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_rosters` $$
CREATE PROCEDURE `dbm_rollback_merge_rosters` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_existing_rosters") THEN

        DROP TABLE IF EXISTS dbm_src_rosters;
        CREATE TABLE dbm_src_rosters (class_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (class_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cim.class_id, sim.student_id
        FROM adb_source.roster r
        JOIN dbm_class_id_map cim ON cim.src_class_id = r.class_id
        JOIN dbm_student_id_map sim ON sim.src_student_id = r.student_id;

        DROP TABLE IF EXISTS dbm_rosters_to_delete;
        CREATE TABLE dbm_rosters_to_delete (class_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (class_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT srcrost.class_id, srcrost.student_id
        FROM dbm_src_rosters srcrost
        LEFT JOIN dbm_existing_rosters exrost ON srcrost.class_id = exrost.class_id
            AND srcrost.student_id = exrost.student_id
        WHERE exrost.class_id IS NULL;

        DELETE ros FROM roster ros JOIN dbm_rosters_to_delete rtd ON ros.class_id = rtd.class_id
            AND ros.student_id = rtd.student_id;
    END IF;

    /* [2012/06/13 - ysahn] TODO: Eventually remove */
    CALL dbm_rollback_merge_session_student_map();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_session_student_map` $$
CREATE PROCEDURE `dbm_rollback_merge_session_student_map` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_existing_session_student_maps") THEN

        DROP TABLE IF EXISTS dbm_src_session_student_maps;
        CREATE TABLE dbm_src_session_student_maps(session_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (session_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT seim.session_id, stim.student_id
        FROM adb_source.session_student_map ssm
        JOIN dbm_session_id_map seim ON ssm.session_id = seim.src_session_id
        JOIN dbm_student_id_map stim ON ssm.student_id = stim.src_student_id;

        DROP TABLE IF EXISTS dbm_session_student_maps_to_delete;
        CREATE TABLE dbm_session_student_maps_to_delete (session_id BIGINT NOT NULL,
            student_id BIGINT NOT NULL,
            PRIMARY KEY (session_id, student_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT srcssm.session_id, srcssm.student_id
        FROM dbm_src_session_student_maps srcssm
        LEFT JOIN dbm_existing_session_student_maps exssm ON srcssm.session_id = exssm.session_id
            AND srcssm.student_id = exssm.student_id
        WHERE exssm.session_id IS NULL;

        DELETE ssm FROM session_student_map ssm JOIN dbm_session_student_maps_to_delete ssmtd
            ON ssm.session_id = ssmtd.session_id AND ssm.student_id = ssmtd.student_id;
    END IF;

    CALL dbm_rollback_merge_class_dataset_map();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_class_dataset_map` $$
CREATE PROCEDURE `dbm_rollback_merge_class_dataset_map` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_existing_class_dataset_map") THEN

        DROP TABLE IF EXISTS dbm_src_class_dataset_map;
        CREATE TABLE dbm_src_class_dataset_map(class_id BIGINT NOT NULL,
            dataset_id INT NOT NULL,
            PRIMARY KEY (class_id, dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT cim.class_id, dim.dataset_id
        FROM adb_source.class_dataset_map cdm
        JOIN dbm_class_id_map cim ON cdm.class_id = cim.src_class_id
        JOIN dbm_ds_id_map dim ON cdm.dataset_id = dim.src_dataset_id;

        DROP TABLE IF EXISTS dbm_class_dataset_map_to_delete;
        CREATE TABLE dbm_class_dataset_map_to_delete (class_id BIGINT NOT NULL,
            dataset_id BIGINT NOT NULL,
            PRIMARY KEY (class_id, dataset_id)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT srccdm.class_id, srccdm.dataset_id
        FROM dbm_src_class_dataset_map srccdm
        LEFT JOIN dbm_existing_class_dataset_map excdm ON srccdm.class_id = excdm.class_id
            AND srccdm.dataset_id = excdm.dataset_id
        WHERE excdm.class_id IS NULL;

        DELETE cdm FROM class_dataset_map cdm JOIN dbm_class_dataset_map_to_delete cdmtd
            ON cdm.class_id = cdmtd.class_id AND cdm.dataset_id = cdmtd.dataset_id;
    END IF;

END $$


DROP PROCEDURE IF EXISTS `dbm_rollback_merge_problems` $$
CREATE PROCEDURE `dbm_rollback_merge_problems` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_problem_id_map") THEN

        DROP TABLE IF EXISTS dbm_problems_to_delete;
        CREATE TABLE dbm_problems_to_delete (
            problem_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT pim.problem_id
        FROM dbm_problem_id_map pim
        LEFT JOIN dbm_duplicate_problems dprob ON pim.problem_id = dprob.problem_id
        WHERE dprob.problem_id IS NULL;

        DELETE prob FROM problem prob
        JOIN dbm_problems_to_delete ptd ON prob.problem_id = ptd.problem_id;
    END IF;

    CALL dbm_rollback_merge_subgoals();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_subgoals` $$
CREATE PROCEDURE `dbm_rollback_merge_subgoals` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_subgoal_id_map") THEN

        DROP TABLE IF EXISTS dbm_subgoals_to_delete;
        CREATE TABLE dbm_subgoals_to_delete (
            subgoal_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT sgim.subgoal_id
        FROM dbm_subgoal_id_map sgim
        LEFT JOIN dbm_duplicate_subgoals dsub ON sgim.subgoal_id = dsub.subgoal_id
        WHERE dsub.subgoal_id IS NULL;

        DELETE sub FROM subgoal sub
        JOIN dbm_subgoals_to_delete sgtd ON sub.subgoal_id = sgtd.subgoal_id;
    END IF;

    CALL dbm_rollback_merge_SAI();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_SAI` $$
CREATE PROCEDURE `dbm_rollback_merge_SAI` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_selection_range") THEN

        DELETE sel FROM selection sel
        JOIN dbm_selection_range selrng ON sel.selection_id = selrng.selection_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_action_range") THEN

        DELETE act FROM action act
        JOIN dbm_action_range actrng ON act.action_id = actrng.action_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_input_range") THEN

        DELETE inp FROM input inp
        JOIN dbm_input_range inprng ON inp.input_id = inprng.input_id;
    END IF;

    CALL dbm_rollback_merge_subgoal_attempts();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_subgoal_attempts` $$
CREATE PROCEDURE `dbm_rollback_merge_subgoal_attempts` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_subgoal_att_id_map") THEN

        DROP TABLE IF EXISTS dbm_subgoal_att_to_delete;
        CREATE TABLE dbm_subgoal_att_to_delete (
            subgoal_attempt_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT saim.subgoal_attempt_id
        FROM dbm_subgoal_att_id_map saim
        LEFT JOIN dbm_duplicate_subgoal_attempts dsatt ON saim.subgoal_attempt_id = dsatt.subgoal_attempt_id
        WHERE dsatt.subgoal_attempt_id IS NULL;

        DELETE satt FROM subgoal_attempt satt
        JOIN dbm_subgoal_att_to_delete satttd ON satt.subgoal_attempt_id = satttd.subgoal_attempt_id;
    END IF;

    CALL dbm_rollback_merge_attempt_SAI();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_attempt_SAI` $$
CREATE PROCEDURE `dbm_rollback_merge_attempt_SAI` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_attempt_selection_range") THEN

        DELETE attsel FROM attempt_selection attsel
        JOIN dbm_attempt_selection_range asr ON attsel.attempt_selection_id = asr.attempt_selection_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_attempt_action_range") THEN

        DELETE attact FROM attempt_action attact
        JOIN dbm_attempt_action_range aar ON attact.attempt_action_id = aar.attempt_action_id;
    END IF;

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_attempt_input_range") THEN

        DELETE attinp FROM attempt_input attinp
        JOIN dbm_attempt_input_range air ON attinp.attempt_input_id = air.attempt_input_id;
    END IF;

    CALL dbm_rollback_merge_feedback();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_feedback` $$
CREATE PROCEDURE `dbm_rollback_merge_feedback` ()
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_feedback_id_map") THEN

        DROP TABLE IF EXISTS dbm_feedback_to_delete;
        CREATE TABLE dbm_feedback_to_delete (
            feedback_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT fim.feedback_id
        FROM dbm_feedback_id_map fim
        LEFT JOIN dbm_duplicate_feedback dfee ON fim.feedback_id = dfee.feedback_id
        WHERE dfee.feedback_id IS NULL;

        DELETE fee FROM feedback fee
        JOIN dbm_feedback_to_delete ftd ON fee.feedback_id = ftd.feedback_id;
    END IF;

    CALL dbm_rollback_merge_skill_models();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_skill_models` $$
CREATE PROCEDURE `dbm_rollback_merge_skill_models` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_skill_model_id_map") THEN

        DROP TABLE IF EXISTS dbm_skill_models_to_delete;
        CREATE TABLE dbm_skill_models_to_delete (
           skill_model_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT smim.skill_model_id
        FROM dbm_skill_model_id_map smim
        LEFT JOIN dbm_duplicate_skill_models dskm ON smim.skill_model_id = dskm.skill_model_id
        WHERE dskm.skill_model_id IS NULL;

        DELETE skm FROM skill_model skm
        JOIN dbm_skill_models_to_delete smtd ON skm.skill_model_id = smtd.skill_model_id;
    END IF;

    CALL dbm_rollback_merge_skills();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_skills` $$
CREATE PROCEDURE `dbm_rollback_merge_skills` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_skill_id_map") THEN

        DROP TABLE IF EXISTS dbm_skills_to_delete;
        CREATE TABLE dbm_skills_to_delete (
            skill_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT skim.skill_id
        FROM dbm_skill_id_map skim
        LEFT JOIN dbm_duplicate_skills dski ON skim.skill_id = dski.skill_id
        WHERE dski.skill_id IS NULL;

        DELETE sk FROM skill sk JOIN dbm_skills_to_delete sktd ON sk.skill_id = sktd.skill_id;
    END IF;

    CALL dbm_rollback_merge_dataset_level_events();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_dataset_level_events` $$
CREATE PROCEDURE `dbm_rollback_merge_dataset_level_events` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_ds_level_event_range") THEN

        /* Join back to maps in case there were inserts while the range was calculated earlier. */
        DROP TABLE IF EXISTS dbm_ds_level_events_to_delete;
        CREATE TABLE dbm_ds_level_events_to_delete (
            dataset_level_event_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT dataset_level_event_id
        FROM dbm_ds_level_event_range dler
        JOIN dbm_ds_level_id_map dlim ON dler.dataset_level_id = dlim.dataset_level_id
        JOIN dbm_session_id_map seim ON dler.session_id = seim.session_id;

        DELETE dle FROM dataset_level_event dle
        JOIN dbm_ds_level_events_to_delete dletd
            ON dle.dataset_level_event_id = dletd.dataset_level_event_id;
    END IF;

    CALL dbm_rollback_merge_problem_events();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_problem_events` $$
CREATE PROCEDURE `dbm_rollback_merge_problem_events` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_problem_event_range") THEN

        /* Join back to maps in case there were inserts while the range was calculated earlier. */
        DROP TABLE IF EXISTS dbm_problem_events_to_delete;
        CREATE TABLE dbm_problem_events_to_delete (
            problem_event_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT problem_event_id
        FROM dbm_problem_event_range per
        JOIN dbm_problem_id_map pim ON per.problem_id = pim.problem_id
        JOIN dbm_session_id_map seim ON per.session_id = seim.session_id;

        DELETE pe FROM problem_event pe
        JOIN dbm_problem_events_to_delete petd
            ON pe.problem_event_id = petd.problem_event_id;
    END IF;
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_students` $$
CREATE PROCEDURE `dbm_rollback_merge_students` ()
    SQL SECURITY INVOKER
BEGIN

    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_student_id_map") THEN

        DROP TABLE IF EXISTS dbm_students_to_delete;
        CREATE TABLE dbm_students_to_delete (
            student_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT stim.student_id
        FROM dbm_student_id_map stim
        LEFT JOIN dbm_duplicate_students dstu ON stim.student_id = dstu.student_id
        WHERE dstu.student_id IS NULL;

        DELETE stu FROM student stu JOIN dbm_students_to_delete std ON stu.student_id = std.student_id;
    END IF;

    CALL dbm_rollback_merge_sessions();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_transactions` $$
CREATE PROCEDURE `dbm_rollback_merge_transactions` ()
    SQL SECURITY INVOKER
BEGIN

    SET foreign_key_checks = 1;
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_transaction_id_map") THEN

        DROP TABLE IF EXISTS dbm_transactions_to_delete;
        CREATE TABLE dbm_transactions_to_delete(
           transaction_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT tim.transaction_id
        FROM dbm_transaction_id_map tim
        LEFT JOIN dbm_duplicate_transactions dtx ON tim.transaction_id = dtx.transaction_id
        WHERE dtx.transaction_id IS NULL;

        DELETE tt.* FROM tutor_transaction tt
        JOIN dbm_transactions_to_delete txtd ON tt.transaction_id = txtd.transaction_id;
   END IF;

   CALL dbm_rollback_merge_ds_conditions();
END $$

DROP PROCEDURE IF EXISTS `dbm_rollback_merge_ds_conditions` $$
CREATE PROCEDURE `dbm_rollback_merge_ds_conditions` ()
    SQL SECURITY INVOKER
BEGIN

    SET foreign_key_checks = 1;
    IF EXISTS(SELECT table_name FROM information_schema.tables
        WHERE table_name = "dbm_ds_condition_id_map") THEN

        DROP TABLE IF EXISTS dbm_conditions_to_delete;
        CREATE TABLE dbm_conditions_to_delete(
           condition_id BIGINT NOT NULL PRIMARY KEY
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT dscim.condition_id
        FROM dbm_ds_condition_id_map dscim
        LEFT JOIN dbm_existing_conditions exc ON dscim.condition_id = exc.condition_id
        WHERE exc.condition_id IS NULL;

        DELETE dsc.* FROM ds_condition dsc
        JOIN dbm_conditions_to_delete ctd ON dsc.condition_id = ctd.condition_id;
   END IF;
END $$

DROP PROCEDURE IF EXISTS `dbm_update_students` $$
CREATE PROCEDURE `dbm_update_students` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('db_merge_sp', 'Starting dbm_update_students');

    UPDATE student stu JOIN dbm_student_id_map stim ON stu.student_id = stim.student_id
      SET stu.actual_user_id = NULL;

    CALL debug_log('db_merge_sp', 'Finished dbm_update_students');

END $$

DELIMITER ;
