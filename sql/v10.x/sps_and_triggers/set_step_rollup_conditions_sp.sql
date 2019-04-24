/*
 -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2009
  All Rights Reserved

  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
  $KeyWordsOff: $

  Helper script to avoid having to do a complete re-agg of all datasets.  This script mimics the
  functionality in the aggregator responsible for setting condition values in the step_rollup table.

------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 ----------------------------------------------------------------------------
  Get the sessions that we care about and put them in a temp table
  for use during transaction duration calculation.
  @param sampleId the sample we wish to process.
  @param batchOffset the offset value to use when gathering sessions to process.
  @param batchLimit the size of the batch to grab.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS 	`get_session_data_cond` $$
CREATE PROCEDURE			`get_session_data_cond`(IN sampleId long, IN batchOffset int, IN batchLimit int)
	SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'get_session_data_cond get_session_data');

    /*
      Create the temp_sess_cond table.
    */
    DROP TABLE IF EXISTS temp_sess_cond;

    /*
     We must to process the query as a string because of a MySQL bug that does not allow
     variables as the values for limit and offset parameters.
    */
    SET @createBatchQuery = '
    CREATE TABLE temp_sess_cond (session_id bigint PRIMARY KEY)
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT session_id
            FROM session
            JOIN tutor_transaction USING (session_id)
            JOIN transaction_sample_map map USING (transaction_id)
            WHERE map.sample_id = ?
            ORDER BY session_id ASC
            LIMIT ?, ?;';
    PREPARE STMT FROM @createBatchQuery;
    SET @offset = batchOffset;
    SET @lim = batchLimit;
    SET @id = sampleId;
    EXECUTE STMT USING @id, @offset, @lim;
    DEALLOCATE PREPARE STMT;

    CALL debug_log('cond_fix', 'Finished get_session_data.');
END $$

/*
 ----------------------------------------------------------------------------
  Get the tx sample mappings we care about and put them in a temp table
  for use during transaction duration calculation.
  @param sampleId the dataset we wish to process.
  @param batchOffset the offset value to use when gathering sessions to process.
  @param batchLimit the size of the batch to grab.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS 	`get_tx_list_cond` $$
CREATE PROCEDURE			`get_tx_list_cond`(IN sampleId long)
	SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'get_tx_list_cond get_tx_list');

    /*
      Create the temp_tx_list_cond table.
    */
    DROP TABLE IF EXISTS temp_tx_list_cond;
    CREATE TABLE temp_tx_list_cond
    (
        transaction_id  BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO temp_tx_list_cond
    	(SELECT transaction_id
		FROM transaction_sample_map map
		JOIN tutor_transaction USING (transaction_id)
		JOIN temp_sess_cond USING (session_id)
		WHERE map.sample_id = sampleId);


    CALL debug_log('cond_fix', 'Finished get_tx_list.');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the transaction data we need for this sample.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `get_tx_data_cond` $$
CREATE PROCEDURE `get_tx_data_cond` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'Starting get_tx_data_cond');

   /*
      create and populate a table to hold only the session_ids we care about.
      We'll use this to grab all of the students for the session and assign
      transactions back to them (since transactions can have multiple students).
   */

    DROP TABLE IF EXISTS temp_session_student_map_cond;
    CREATE TABLE temp_session_student_map_cond
    (
        session_id  BIGINT,
        student_id  BIGINT,

        PRIMARY KEY(session_id, student_id)
    )

    SELECT session_id, student_id
    FROM session sess
    JOIN temp_sess_cond USING (session_id);

  /*
    create and populate a  table to hold the transactions in which we
    are interested.
  */

    DROP TABLE IF EXISTS temp_tutor_transaction_cond;
    CREATE TABLE temp_tutor_transaction_cond
    (
        transaction_id		BIGINT,
        session_id          BIGINT,
        student_id          BIGINT,
        subgoal_id          BIGINT,
        PRIMARY KEY(transaction_id, session_id, student_id)

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

  /* Add in the student_id via the session */


    INSERT INTO temp_tutor_transaction_cond (transaction_id, session_id,
        student_id, subgoal_id)
    SELECT tt.transaction_id as actual_tx_id,
    	   map.session_id,
           map.student_id,
           tt.subgoal_id
    FROM tutor_transaction tt
    JOIN temp_tx_list_cond USING (transaction_id)
    JOIN temp_session_student_map_cond map USING (session_id)
    JOIN subgoal_attempt sa using(subgoal_attempt_id)
    WHERE tt.subgoal_id IS NOT NULL
    ORDER BY map.session_id, tt.transaction_time;

    CALL debug_log('cond_fix', 'Creating indeces get_tx_data_agg_XXX');
    CREATE INDEX tx_id_index ON temp_tutor_transaction_cond(transaction_id);

END $$

/*
 ----------------------------------------------------------------------------
  Get the tx condition mappings we care about and put them in a temp table
  for use.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS 	`get_tx_condition_map_cond` $$
CREATE PROCEDURE			`get_tx_condition_map_cond`()
	SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'Starting get_tx_condition_map_cond');

    /*
      Create the temp_tx_condition_map_cond table.
    */
    DROP TABLE IF EXISTS temp_tx_condition_map_cond;
    CREATE TABLE temp_tx_condition_map_cond
    (
        transaction_id  BIGINT,
        condition_id	BIGINT,
        PRIMARY KEY(transaction_id, condition_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO temp_tx_condition_map_cond
    	(SELECT transaction_id, condition_id
		FROM transaction_condition_map map
		JOIN temp_tx_list_cond USING (transaction_id));

    CALL debug_log('cond_fix', 'Finished get_tx_condition_map_cond.');
END $$

/*
 ----------------------------------------------------------------------------
  Get the conditions for this dataset.
  @param datasetId the dataset to which the sample belongs.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS 	`get_conditions_cond` $$
CREATE PROCEDURE			`get_conditions_cond`(IN sampleId LONG)
	SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'Starting get_conditions_cond');

    /*
      Create the temp_conditions_cond table.
    */
    DROP TABLE IF EXISTS temp_conditions_cond;
    CREATE TABLE temp_conditions_cond
    (
        condition_id	BIGINT,
        condition_name	TEXT,
        condition_type	TEXT,
        PRIMARY KEY(condition_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO temp_conditions_cond
    	(SELECT condition_id, condition_name, type
		FROM ds_condition
		WHERE ds_condition.dataset_id = (SELECT dataset_id
			FROM sample where sample_id = sampleId));

	/*
	  Now set up a table to hold step_id, student_id and concatenated condition names
	  for each transaction.
	*/
	DROP TABLE IF EXISTS temp_concat_conditions_cond;
    CREATE TABLE temp_concat_conditions_cond
    (
        step_id			BIGINT,
        student_id		BIGINT,
        conditions		TEXT,
        sample_id		BIGINT,

        PRIMARY KEY(sample_id, step_id, student_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO temp_concat_conditions_cond
    	(SELECT tt.subgoal_id as step_id, tt.student_id, GROUP_CONCAT(DISTINCT condition_name,
    		IF (condition_type IS NULL OR condition_type = '', '', concat(' (', condition_type, ')'))
    		ORDER BY condition_type, condition_name SEPARATOR ', ') AS conditions,
    		sampleId as sample_id
			FROM temp_conditions_cond
			JOIN temp_tx_condition_map_cond map USING (condition_id)
			JOIN temp_tutor_transaction_cond tt USING (transaction_id)
        	GROUP BY subgoal_id, student_id);

    CALL debug_log('cond_fix', 'Finished get_conditions_cond.');
END $$

/*
 ----------------------------------------------------------------------------
  Update the step_rollup table with conditions.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS 	`set_step_rollup_conditions_cond` $$
CREATE PROCEDURE			`set_step_rollup_conditions_cond`()
	SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'Starting set_step_rollup_conditions_cond');

    UPDATE step_rollup sr
    JOIN temp_concat_conditions_cond temp USING (sample_id, step_id, student_id)
    SET sr.conditions = temp.conditions,
    	sr.step_time = sr.step_time,
    	sr.first_transaction_time = sr.first_transaction_time,
    	sr.step_start_time = sr.step_start_time,
    	sr.step_end_time = sr.step_end_time,
    	sr.correct_transaction_time = sr.correct_transaction_time;


    CALL debug_log('cond_fix', 'Finished set_step_rollup_conditions_cond.');
END $$

/*
------------------------------------------------------------------------------
 Set the "conditions" column in the step rollup table for the given sample id.
 @param sampleId the sample to process
 @param batchOffset offset for batch processing
 @param batchSize size of each batch
------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `set_step_rollup_conditions` $$
CREATE PROCEDURE `set_step_rollup_conditions` (IN sampleId long, IN batchOffset int, IN batchSize int)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', concat('Starting set_step_rollup_conditions for sample id ',
    sampleId, ', offset ', batchOffset, ', limit ', batchSize));

    CALL get_session_data_cond(sampleId, batchOffset, batchSize);
    CALL get_tx_list_cond(sampleId);
    CALL get_tx_data_cond();
    CALL get_tx_condition_map_cond();
    CALL get_conditions_cond(sampleId);
    CALL set_step_rollup_conditions_cond();
    CALL drop_temp_tables_cond();

    CALL debug_log('cond_fix', 'Finished set_step_rollup_conditions');
END $$

/*
  ------------------------------------------------------------------------------
  Do some clean-up.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `drop_temp_tables_cond` $$
CREATE PROCEDURE `drop_temp_tables_cond` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('cond_fix', 'Starting drop_temp_tables_cond');

    DROP TABLE IF EXISTS temp_sess_cond;
    DROP TABLE IF EXISTS temp_tx_list_cond;
    DROP TABLE IF EXISTS temp_session_student_map_cond;
    DROP TABLE IF EXISTS temp_tutor_transaction_cond;
    DROP TABLE IF EXISTS temp_tx_condition_map_cond;
    DROP TABLE IF EXISTS temp_conditions_cond;
    DROP TABLE IF EXISTS temp_concat_conditions_cond;

    CALL debug_log('cond_fix', 'Finished drop_temp_tables_cond');
END $$

DELIMITER ;
