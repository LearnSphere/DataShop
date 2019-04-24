--
-- Carnegie Mellon University, Human Computer Interaction Institute
-- Copyright 2005-2007
-- All Rights Reserved
--
-- $Revision: 12404 $
-- Last modified by - $Author: ctipper $
-- Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--
-- Fix the 'attempt_at_subgoal' column in the tutor transaction table.
--
-- PUBLIC METHODS:
--
-- Use the generate methods to create a list of datasets which have wrong aas'es.
--     generate_results_one_ds_aas(datasetId)
--     generate_results_all_ds_aas()
--
-- Use the update methods to actually update the tutor_transaction table and
-- generate the modify action in the dataset_system_log table.
--     update_tx_one_ds_aas(datasetId)
--     update_tx_all_ds_aas()
--
--
DELIMITER $$

-- --------------------------------------------------------------------------------------------------
-- Get the CVS version information.
-- --------------------------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `get_version_fix_aas_sp` $$
CREATE FUNCTION         `get_version_fix_aas_sp` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 12404 $'
        INTO version;
    RETURN version;
END $$
-- $KeyWordsOff: $

-- --------------------------------------------------------------------------------------------------
-- Drop the tables created by the 'create_temp_tables_aas' procedure.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `drop_temp_tables_aas` $$
CREATE PROCEDURE         `drop_temp_tables_aas` ()
    SQL SECURITY INVOKER
BEGIN
    -- PUT VERSION NUMBER HERE 1.2 blah blah

    DROP TABLE IF EXISTS temp_aas_txs;
    DROP TABLE IF EXISTS temp_aas_results;

END $$

-- --------------------------------------------------------------------------------------------------
-- Create the tables needed to run the other procedures.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `create_temp_tables_aas` $$
CREATE PROCEDURE         `create_temp_tables_aas` ()
    SQL SECURITY INVOKER
BEGIN

    CREATE TABLE temp_aas_txs
    (
        dataset_id          BIGINT,
        transaction_id      BIGINT,
        transaction_time    DATETIME,
        student_id          BIGINT,
        session_id          BIGINT,
        problem_id          BIGINT,
        subgoal_id          BIGINT,
        pe_start            DATETIME,
        old_aas             INT,
        new_aas             INT

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    CREATE TABLE temp_aas_results
    (
        dataset_id          BIGINT,
        count_bad_aas       INT

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

END $$

-- --------------------------------------------------------------------------------------------------
-- Load the temporary transaction table with data from all the datasets.
-- Skip transactions produced by CL's data munger, by ignoring rows with AUTOHELP, CYCLE or HINT.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `load_all_ds_aas` $$
CREATE PROCEDURE         `load_all_ds_aas` ()
    SQL SECURITY INVOKER
BEGIN

    INSERT INTO temp_aas_txs (dataset_id, transaction_id, transaction_time,
                          session_id, problem_id, subgoal_id,
                          old_aas)
        (SELECT tt.dataset_id, tt.transaction_id, tt.transaction_time,
                tt.session_id, tt.problem_id, tt.subgoal_id,
                tt.attempt_at_subgoal
         FROM tutor_transaction tt
         WHERE tt.transaction_type_tutor NOT IN ('AUTOHELP','CYCLE','HINT'));

END $$

-- --------------------------------------------------------------------------------------------------
-- Load the temporary transaction table with data from the given dataset.
-- Skip transactions produced by CL's data munger, by ignoring rows with AUTOHELP, CYCLE or HINT.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `load_one_ds_aas` $$
CREATE PROCEDURE         `load_one_ds_aas` (IN datasetId long)
    SQL SECURITY INVOKER
BEGIN

    INSERT INTO temp_aas_txs (dataset_id, transaction_id, transaction_time,
                          session_id, problem_id, subgoal_id,
                          old_aas)
        (SELECT tt.dataset_id, tt.transaction_id, tt.transaction_time,
                tt.session_id, tt.problem_id, tt.subgoal_id,
                tt.attempt_at_subgoal
         FROM tutor_transaction tt
         WHERE tt.transaction_type_tutor NOT IN ('AUTOHELP','CYCLE','HINT')
         AND tt.dataset_id = datasetId);

END $$

-- --------------------------------------------------------------------------------------------------
-- Populate the remaining columns in the temporary transaction table
-- and generate the results into the temporary results table.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `populate_temp_aas` $$
CREATE PROCEDURE         `populate_temp_aas` ()
    SQL SECURITY INVOKER
BEGIN

    UPDATE temp_aas_txs ta SET ta.student_id =
        (SELECT min(sess.student_id)
         FROM session sess
         WHERE sess.session_id = ta.session_id);

    UPDATE temp_aas_txs ta SET pe_start =
        (SELECT max(pe.start_time)
         FROM problem_event pe
         JOIN session sess ON sess.session_id = pe.session_id
         WHERE pe.start_time <= ta.transaction_time
         AND   pe.session_id = ta.session_id
         AND   pe.problem_id = ta.problem_id
         AND   pe.event_flag = 0);

    UPDATE temp_aas_txs ta SET new_aas =
        IF (ta.pe_start IS NULL,
            (IF (ta.subgoal_id IS NULL,
                 NULL,
                 (SELECT count(distinct transaction_id)
                  FROM tutor_transaction tt
                  JOIN session sess ON sess.session_id = tt.session_id
                  WHERE (tt.transaction_time < ta.transaction_time
                          OR
                        (tt.transaction_time = ta.transaction_time
                         AND tt.transaction_id < ta.transaction_id))
                  AND sess.student_id = ta.student_id
                  AND tt.dataset_id = ta.dataset_id
                  AND tt.subgoal_id = ta.subgoal_id) + 1)),
            (IF (ta.subgoal_id IS NULL,
                 NULL,
                 (SELECT count(distinct transaction_id)
                  FROM tutor_transaction tt
                  JOIN session sess ON sess.session_id = tt.session_id
                  WHERE (tt.transaction_time < ta.transaction_time
                          OR
                        (tt.transaction_time = ta.transaction_time
                         AND tt.transaction_id < ta.transaction_id))
                  AND tt.transaction_time >= ta.pe_start
                  AND tt.transaction_id < ta.transaction_id
                  AND sess.session_id = ta.session_id
                  AND tt.dataset_id = ta.dataset_id
                  AND tt.subgoal_id = ta.subgoal_id) + 1)));

    INSERT INTO temp_aas_results (dataset_id, count_bad_aas)
        (SELECT dataset_id, count(*)
         FROM temp_aas_txs ta
         WHERE (old_aas IS NULL     AND new_aas IS NOT NULL)
            OR (old_aas IS NOT NULL AND new_aas IS NULL)
            OR (new_aas - old_aas != 0)
         GROUP by dataset_id);

END $$

-- --------------------------------------------------------------------------------------------------
-- Update the tutor_transaction table with the correct attempt at subgoal
-- ONLY if it has changed.
-- This ensures that the correct number of updated rows is returned to the caller.
--
-- As per the WTF line below:  I know you're thinking why on earth is this necessary,
-- that's what I said too, without this the transaction_time is set to now() for no known reason.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `fix_tx_aas` $$
CREATE PROCEDURE         `fix_tx_aas` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('fix_aas_sp', 'Running fix_tx_aas procedure.');

    UPDATE tutor_transaction tt
        JOIN temp_aas_txs ta USING (transaction_id)
        SET tt.transaction_time = tt.transaction_time,  -- WTF
            tt.attempt_at_subgoal = ta.new_aas
        WHERE (tt.attempt_at_subgoal != ta.new_aas)
           OR (tt.attempt_at_subgoal IS NULL     AND ta.new_aas IS NOT NULL)
           OR (tt.attempt_at_subgoal IS NOT NULL AND ta.new_aas IS NULL);

END $$

-- --------------------------------------------------------------------------------------------------
-- Public: Generate the results for all datasets.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `generate_results_all_ds_aas` $$
CREATE PROCEDURE         `generate_results_all_ds_aas` ()
    SQL SECURITY INVOKER
BEGIN
    CALL drop_temp_tables_aas();
    CALL create_temp_tables_aas();
    CALL load_all_ds_aas();
    CALL populate_temp_aas();
END $$

-- --------------------------------------------------------------------------------------------------
-- Public: Generate the results for the given dataset only.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `generate_results_one_ds_aas` $$
CREATE PROCEDURE         `generate_results_one_ds_aas` (IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    CALL drop_temp_tables_aas();
    CALL create_temp_tables_aas();
    CALL load_one_ds_aas(datasetId);
    CALL populate_temp_aas();
END $$

-- --------------------------------------------------------------------------------------------------
-- Public: Generate the results for all datasets.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `update_tx_all_ds_aas` $$
CREATE PROCEDURE         `update_tx_all_ds_aas` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('fix_aas_sp', concat(get_version_fix_aas_sp(),
                                             ' Starting update_tx_all_ds_aas'));

    CALL generate_results_all_ds_aas();
    CALL fix_tx_aas();

    CALL debug_log('fix_aas_sp', 'Finished update_tx_all_ds_aas');
END $$

-- --------------------------------------------------------------------------------------------------
-- Public: Generate the results for the given dataset only.
-- --------------------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `update_tx_one_ds_aas` $$
CREATE PROCEDURE         `update_tx_one_ds_aas` (IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('fix_aas_sp', concat(get_version_fix_aas_sp(),
                                        ' Starting update_tx_one_ds_aas for dataset ',
                                        datasetId));
    CALL generate_results_one_ds_aas(datasetId);
    CALL fix_tx_aas();

    CALL debug_log('fix_aas_sp', 'Finished update_tx_one_ds_aas');
END $$

DELIMITER ;
