/*
 -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2009
  All Rights Reserved

  $Revision: 13798 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2017-01-31 12:07:37 -0500 (Tue, 31 Jan 2017) $
  $KeyWordsOff: $

  Conduct aggregation within the database instead of in Java.  This means first gathering step data,
  then gathering the step to skill mapping.  Merge the two together and insert into the step_rollup table.
  This set of stored procedures replaces the following methods in the StepRollupDaoHibernate.java class:
    * getStepResults
    * getStepSkillData
    * populateStepRollup

------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_aggregator_sp_XXX` $$
CREATE FUNCTION         `get_version_aggregator_sp_XXX` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 13798 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/*
  ------------------------------------------------------------------------------
  Calculate the problem_view for the step.  If problemEventId is null (meaning
  the step has no problemEvent info, set it to -1.

  @param student_id the student we are working on
  @param problem_event_id the problem_event_id we care about - can be NULL
  @param problem_id the problem we care about
  @returns int value for problem_view.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `agg_get_problem_view_XXX` $$
CREATE FUNCTION         `agg_get_problem_view_XXX`(studentId BIGINT,
                                                   problemEventId BIGINT,
                                                   problemId BIGINT)
    RETURNS INT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE givenProblemEventStartTime DATETIME;
    DECLARE problemView INT default 0;
    /*
     if this is the first time accessing the tables, they will be empty, so
     use the handler below to deal with that 'state' (empty table/no rows returned).
    */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET problemView = NULL;

    IF problemEventId IS NULL THEN
        SET problemEventId = -1;
    END IF;

    /*
    check agg_problem_event_pair_view to see if we've already seen this pair
    of problem_event_id and problem_id.  if so, return the value from that table.
    */
    SELECT problem_view INTO problemView
    FROM agg_problem_event_pair_view_XXX
    WHERE student_id = studentId

      AND problem_event_id = problemEventId
        AND problem_id = problemId;

    IF problemView IS NULL THEN

        SELECT start_time INTO givenProblemEventStartTime
            FROM agg_problem_event_XXX
            WHERE problem_event_id = problemEventId
            AND event_flag = 0;

        SELECT count(*) INTO problemView
            FROM agg_problem_event_XXX
            WHERE student_id = studentId
            AND problem_id = problemId
            AND event_flag = 0
            AND start_time < givenProblemEventStartTime;

        SET problemView = problemView + 1;

        INSERT INTO agg_problem_event_pair_view_XXX
        VALUES (problemEventId, studentId, problemId, problemView);

    END IF;

    RETURN problemView;
END $$

/*
 ----------------------------------------------------------------------------
  Get the students that we care about and put them in a helper table
  for use during transaction duration calculation.
  @param studentIdClause the "IN" clause for use in batching.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `agg_get_student_data_XXX` $$
CREATE PROCEDURE         `agg_get_student_data_XXX`(IN sampleId long, IN studentIdClause text)
    SQL SECURITY INVOKER
BEGIN
    DECLARE datasetId INT DEFAULT NULL;

    CALL debug_log('aggregator_sp', CONCAT('Starting agg_get_student_data_XXX for sample ', sampleId));

    SELECT dataset_id INTO datasetId
        FROM sample
        WHERE sample_id = sampleId;

    CALL debug_log('aggregator_sp', CONCAT('agg_get_student_data_XXX dataset ', datasetId));

    /*
      Create the agg_session_XXX table.
    */
    DROP TABLE IF EXISTS agg_session_XXX;
    CREATE TABLE agg_session_XXX
    (
		session_id BIGINT,
        student_id BIGINT

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    CALL exec(CONCAT(
        "INSERT INTO agg_session_XXX
         SELECT DISTINCT session_id, student_id
         FROM session sess
         WHERE student_id IN ",
        studentIdClause,
        " AND dataset_id = ",
        datasetId));

    CALL debug_log('aggregator_sp', 'Finished agg_get_student_data_XXX.');
END $$

/*
 ----------------------------------------------------------------------------
  Get the tx sample mappings we care about and put them in a helper table
  for use during transaction duration calculation.
  @param sampleId the given sample
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `agg_get_tx_list_XXX` $$
CREATE PROCEDURE         `agg_get_tx_list_XXX`(IN sampleId long)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_tx_list_XXX');

    /*
      Create the agg_tx_list_XXX table.
    */
    DROP TABLE IF EXISTS agg_tx_list_XXX;
    CREATE TABLE agg_tx_list_XXX
    (
        transaction_id  BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_tx_list_XXX
        (SELECT transaction_id
        FROM transaction_sample_map map
        JOIN tutor_transaction USING (transaction_id)
        JOIN agg_session_XXX USING (session_id)
        WHERE map.sample_id = sampleId);


    CALL debug_log('aggregator_sp', 'Finished agg_get_tx_list_XXX.');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the transaction data we need for this sample.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `agg_get_tx_data_XXX` $$
CREATE PROCEDURE         `agg_get_tx_data_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_tx_data_XXX');

    /*
      Create and populate a table to hold the relevant transactions.
      Use agg_session_XXX which already holds the relevant students.
    */
    CALL debug_log('aggregator_sp', 'Creating table agg_tutor_transaction_XXX');
    DROP TABLE IF EXISTS agg_tutor_transaction_XXX;
    CREATE TABLE agg_tutor_transaction_XXX
    (
        transaction_id      BIGINT AUTO_INCREMENT,
        actual_tx_id        BIGINT,
        student_id          BIGINT,
        problem_id          BIGINT,
        subgoal_id          BIGINT,
        dataset_id          BIGINT,
        subgoal_attempt_id  BIGINT,
        transaction_time    DATETIME,
        duration            INT,
        correct_flag        text,
        problem_event_id    BIGINT,
        start_time          DATETIME,
        prev_step_id        BIGINT,
        prev_tx_time        DATETIME,
	high_stakes	    TINYINT,
        PRIMARY KEY(transaction_id, student_id)

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    /* Add in the student_id */
    INSERT INTO agg_tutor_transaction_XXX (actual_tx_id,
        student_id, problem_id, subgoal_id, dataset_id, subgoal_attempt_id,
        transaction_time, duration, correct_flag, problem_event_id)
    SELECT tt.transaction_id as actual_tx_id,
        sess.student_id,
        tt.problem_id,
        tt.subgoal_id,
        tt.dataset_id,
        tt.subgoal_attempt_id,
        tt.transaction_time,
        tt.duration,
        UPPER(sa.correct_flag),
        IFNULL(problem_event_id, -1) as problem_event_id
    FROM tutor_transaction tt
    JOIN agg_tx_list_XXX USING (transaction_id)
    JOIN agg_session_XXX sess USING (session_id)
    JOIN subgoal_attempt sa using(subgoal_attempt_id)

    ORDER BY sess.student_id, tt.transaction_time, tt.attempt_at_subgoal;

    CALL debug_log('aggregator_sp', 'Creating indices agg_get_tx_data_XXX');
    CREATE INDEX tx_id_index ON agg_tutor_transaction_XXX(transaction_id);
    CREATE INDEX actual_tx_id_index ON agg_tutor_transaction_XXX(actual_tx_id);
    CREATE INDEX student_id_index ON agg_tutor_transaction_XXX(student_id);
    CREATE INDEX ttt_stud_prob_idx ON agg_tutor_transaction_XXX(student_id, problem_id);

    UPDATE agg_tutor_transaction_XXX tt
        JOIN cf_tx_level ctl ON ctl.transaction_id = tt.actual_tx_id
        JOIN custom_field cf USING (custom_field_id)
        SET tt.high_stakes = IF (lower(ctl.value) = 'true', 1, IF(lower(ctl.value) = 'false', 0, NULL))
        WHERE cf.custom_field_name LIKE '%highStakes%';

    /* INCORRECT (0) and HINT (1) contribute to error rate. */

    /* Create the agg_prev_tx_time_XXX table to help us calculate the prev_tx_time. */
    CALL debug_log('aggregator_sp', 'Creating table agg_prev_tx_time_XXX');
    DROP TABLE IF EXISTS agg_prev_tx_time_XXX;
    CREATE TABLE agg_prev_tx_time_XXX
    (
        transaction_id      BIGINT,
        transaction_time    DATETIME,
        student_id          BIGINT,
        problem_id          BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_prev_tx_time_XXX
        (SELECT transaction_id+1 as transaction_id,
                transaction_time,
                student_id,
                problem_id
        FROM agg_tutor_transaction_XXX tt
        WHERE subgoal_id IS NOT NULL);

    /* Now update the agg_tutor_transaction_XXX table to set prev_tx_time,
       but this depends on the student id. */
    CALL debug_log('aggregator_sp', 'Updating agg_tutor_transaction_XXX.prev_tx_time');
    UPDATE agg_tutor_transaction_XXX tt
       JOIN agg_prev_tx_time_XXX previous using (transaction_id, student_id)
       SET tt.prev_tx_time = previous.transaction_time;

    /*
      Now correct for transactions in the same student with identical timestamps.
      Create and populate a table to hold a list of duplicates.
    */
    CALL debug_log('aggregator_sp', 'Creating table agg_same_timestamps_XXX');
    DROP TABLE IF EXISTS agg_same_timestamps_XXX;
    CREATE TABLE agg_same_timestamps_XXX
       (
            student_id          BIGINT,
            transaction_time    DATETIME,
            frequency           INT,
            prev_tx_time        DATETIME
       ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
    SELECT student_id,
           transaction_time,
           COUNT(*) as frequency,
           MIN(prev_tx_time) as prev_tx_time
        FROM agg_tutor_transaction_XXX
        WHERE subgoal_id IS NOT NULL
        GROUP BY student_id, transaction_time
        HAVING frequency > 1;

    CREATE INDEX student_id_index ON agg_same_timestamps_XXX(student_id);

    /*
       Now update agg_tutor_transaction_XXX with the correct prev_tx_time for those tx with
       identical timestamps.
    */
    CALL debug_log('aggregator_sp', 'Updating agg_tutor_transaction_XXX.prev_tx_time');
    UPDATE agg_tutor_transaction_XXX tt
    JOIN agg_same_timestamps_XXX yikes using (student_id)
    SET tt.prev_tx_time = yikes.prev_tx_time
    WHERE yikes.transaction_time = tt.transaction_time;

    CALL debug_log('aggregator_sp', 'Finished agg_get_tx_data_XXX');
END $$

/*
 ----------------------------------------------------------------------------
  Get the tx condition mappings we care about and put them in a helper table
  for use.
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `agg_get_tx_condition_map_XXX` $$
CREATE PROCEDURE         `agg_get_tx_condition_map_XXX`()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_tx_condition_map_XXX');

    /*
      Create the agg_tx_condition_map_XXX table.
    */
    DROP TABLE IF EXISTS agg_tx_condition_map_XXX;
    CREATE TABLE agg_tx_condition_map_XXX
    (
        transaction_id  BIGINT,
        condition_id    BIGINT,
        PRIMARY KEY(transaction_id, condition_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_tx_condition_map_XXX
        (SELECT transaction_id, condition_id
        FROM transaction_condition_map map
        JOIN agg_tx_list_XXX USING (transaction_id));

    CALL debug_log('aggregator_sp', 'Finished agg_get_tx_condition_map_XXX.');
END $$

/*
 ----------------------------------------------------------------------------
  Get the conditions for this sample.
  @param sampleId the given sample
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `agg_get_conditions_XXX` $$
CREATE PROCEDURE         `agg_get_conditions_XXX`(IN sampleId LONG)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_conditions_XXX');

    /*
      Create the agg_conditions_XXX table.
    */
    DROP TABLE IF EXISTS agg_conditions_XXX;
    CREATE TABLE agg_conditions_XXX
    (
        condition_id    BIGINT,
        condition_name    TEXT,
        condition_type    TEXT,
        PRIMARY KEY(condition_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_conditions_XXX
        (SELECT condition_id, condition_name, type
        FROM ds_condition
        WHERE ds_condition.dataset_id = (SELECT dataset_id
            FROM sample where sample_id = sampleId));

    /*
      Now set up a table to hold transaction_id and concatenated condition names
      for each transaction.
    */
    DROP TABLE IF EXISTS agg_concat_conditions_XXX;
    CREATE TABLE agg_concat_conditions_XXX
    (
        step_id            BIGINT,
        student_id        BIGINT,
        conditions        TEXT,

        PRIMARY KEY(step_id, student_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_concat_conditions_XXX
        (SELECT tt.subgoal_id as step_id, tt.student_id, GROUP_CONCAT(DISTINCT condition_name,
            IF (condition_type IS NULL OR condition_type = '', '', concat(' (', condition_type, ')'))
            ORDER BY condition_type, condition_name SEPARATOR ', ') AS conditions
            FROM agg_conditions_XXX
            JOIN agg_tx_condition_map_XXX map USING (condition_id)
            JOIN agg_tutor_transaction_XXX tt ON tt.actual_tx_id = map.transaction_id
            WHERE tt.subgoal_id IS NOT NULL
            GROUP BY subgoal_id, student_id);

    CALL debug_log('aggregator_sp', 'Finished agg_get_conditions_XXX.');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the problem event data, if there is any.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_get_pe_data_XXX` $$
CREATE PROCEDURE         `agg_get_pe_data_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_pe_data_XXX');

  /*
    create and populate a table to hold only the problem_event information we
    care about.  This could be empty, based on the dataset.
  */
    CALL debug_log('aggregator_sp', 'Getting problem event data XXX');
    DROP TABLE IF EXISTS agg_problem_event_XXX;
    CREATE TABLE agg_problem_event_XXX
    (
        problem_event_id        BIGINT PRIMARY KEY,
        problem_id              BIGINT,
        student_id              BIGINT,
        start_time              DATETIME,
        event_flag              INT,
        problem_view            INT
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
    SELECT DISTINCT problem_event_id, problem_id,
                    student_id,
                    pe.start_time, event_flag, problem_view
        FROM problem_event pe
		JOIN agg_session_XXX USING (session_id)
        WHERE event_flag = 0;

    CALL debug_log('aggregator_sp', 'Creating indices on table agg_problem_event_XXX');

    CREATE INDEX tpe_stu_time_idx  ON agg_problem_event_XXX(student_id, start_time);
    CREATE INDEX problem_student_index ON agg_problem_event_XXX(problem_id, student_id);

  /*
    add the problem_event information to our agg_tutor_transaction table
    based on student_id (not session_id!) and problem_id
  */
    CALL debug_log('aggregator_sp', 'Adding problem event data to tx data XXX');
    UPDATE agg_tutor_transaction_XXX tt
        JOIN agg_problem_event_XXX tpe USING (student_id, problem_id)
        SET tt.problem_event_id = tpe.problem_event_id
        WHERE tpe.event_flag = 0
        AND tpe.problem_view IS NULL 
        AND tt.transaction_time >= tpe.start_time
        AND tt.transaction_time <
        IFNULL((SELECT MIN(pe_start.start_time)
                FROM agg_problem_event_XXX pe_start
                WHERE tpe.student_id = pe_start.student_id
                    AND tpe.problem_id = pe_start.problem_id
                    AND pe_start.start_time > tpe.start_time
                    AND tpe.event_flag = 0),
               DATE_ADD(NOW(), INTERVAL 1 DAY));
               
     UPDATE agg_tutor_transaction_XXX tt
        JOIN agg_problem_event_XXX tpe USING (student_id, problem_id)
        SET tt.start_time = tpe.start_time
        WHERE tpe.event_flag = 0
        AND tt.transaction_time >= tpe.start_time
        AND tt.transaction_time <
        IFNULL((SELECT MIN(pe_start.start_time)
                FROM agg_problem_event_XXX pe_start
                WHERE tpe.student_id = pe_start.student_id
                    AND tpe.problem_id = pe_start.problem_id
                    AND pe_start.start_time > tpe.start_time
                    AND tpe.event_flag = 0),
               DATE_ADD(NOW(), INTERVAL 1 DAY));

    CALL debug_log('aggregator_sp', 'Creating indices on table agg_tutor_transaction_XXX');
    CREATE INDEX problem_event_id_index ON agg_tutor_transaction_XXX(problem_event_id);
    CREATE INDEX stu_prob_event_index ON agg_tutor_transaction_XXX(student_id, problem_event_id);

  /*
    create a table to hold the problem_event_id, problem_id and problem_view
    information for use when populating the agg_step_data table.  We will see
    multiple pairs of problem_event_id and problem_id in the agg_tutor_transaction table
  */
    CALL debug_log('aggregator_sp', 'Creating agg_problem_event_pair_view_XXX.');
    DROP TABLE IF EXISTS agg_problem_event_pair_view_XXX;
    CREATE TABLE agg_problem_event_pair_view_XXX
    (
        problem_event_id    BIGINT,
        student_id          BIGINT,
        problem_id          BIGINT,
        problem_view        INT,
        PRIMARY KEY(student_id, problem_event_id, problem_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    CALL debug_log('aggregator_sp', 'Finished agg_get_pe_data_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the step data.  Also do some calculations.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_get_step_data_XXX` $$
CREATE PROCEDURE         `agg_get_step_data_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_get_step_data_XXX');

      /* create a table to hold our step data. */

    DROP TABLE IF EXISTS agg_step_data_XXX;
    CREATE TABLE         agg_step_data_XXX
    (
        student_id              BIGINT,
        dataset_id              BIGINT,
        problem_id              BIGINT,
        problem_event_id        BIGINT,
        problem_event_time      DATETIME,
        earliest_tx_id          BIGINT,
        earliest_transaction    DATETIME,
        step_id                 BIGINT,
        first_attempt           text,
        corrects                INT,
        incorrects              INT,
        hints                   INT,
        step_time               DATETIME,
        correct_tx_time         DATETIME,
        max_tx_time             DATETIME,
        prev_tx_time            DATETIME,
        step_start_time         DATETIME,
        step_duration           BIGINT,
        correct_step_duration   BIGINT,
        error_step_duration     BIGINT,
        problem_view            INT,
        conditions              TEXT,
        error_rate              TINYINT,
        high_stakes             TINYINT,
        PRIMARY KEY(student_id, step_id, problem_event_id),
        INDEX(student_id)
        /*INDEX(problem_event_id)*/
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

  /* Now, give it some data, as it is very very lonely. */
    INSERT INTO agg_step_data_XXX
    (SELECT
        tt.student_id,
        tt.dataset_id,
        tt.problem_id,
        IFNULL(tt.problem_event_id, -1) as problem_event_id,
        min(tt.start_time) as problem_event_time,
        min(tt.transaction_id) as earliest_tx_id,
        min(tt.transaction_time) as earliest_transaction,
        tt.subgoal_id as step_id,
        null as first_attempt,
        sum(if(tt.correct_flag='CORRECT',1,0)) as corrects,
        sum(if(tt.correct_flag='INCORRECT',1,0)) as incorrects,
        sum(if(tt.correct_flag='HINT',1,0)) as hints,
        null as step_time,
        null as correct_tx_time,
        max(tt.transaction_time) as max_tx_time,
        null as prev_tx_time,
        null as step_start_time,
        sum(tt.duration) as step_duration,
        null as correct_step_duration,
        null as error_step_duration,
        pe.problem_view,
        null as conditions,
        null as error_rate,
        null as high_stakes
    FROM agg_tutor_transaction_XXX tt JOIN agg_problem_event_XXX pe ON tt.problem_event_id = pe.problem_event_id
    WHERE subgoal_id IS NOT NULL 
    GROUP BY tt.subgoal_id, tt.student_id, pe.problem_view);

    CREATE INDEX step_stu_index ON agg_step_data_XXX(step_id, student_id);
    CREATE INDEX earliest_tx_id_index ON agg_step_data_XXX(earliest_tx_id);

    /* Set the prev_tx_time */
    UPDATE agg_step_data_XXX sd
        JOIN agg_tutor_transaction_XXX tt ON sd.earliest_tx_id = tt.transaction_id
        SET sd.prev_tx_time = tt.prev_tx_time,
            sd.first_attempt = tt.correct_flag,
            sd.problem_event_id = tt.problem_event_id,
		sd.problem_event_time = tt.start_time;

    /* Set high_stakes, group by student, step and (maybe) problem. */
    UPDATE agg_step_data_XXX sd
        JOIN agg_tutor_transaction_XXX tt
	     ON sd.step_id = tt.subgoal_id AND sd.student_id = tt.student_id AND sd.problem_id = tt.problem_id
        SET sd.high_stakes = tt.high_stakes
	WHERE subgoal_id IS NOT NULL;

    /* INCORRECT (0) and HINT (1) contribute to error rate. */
    UPDATE agg_step_data_XXX sd
        SET sd.error_rate = (IF(sd.first_attempt='INCORRECT', 1, IF(sd.first_attempt='HINT', 1, 0)) * 100);

    /* Set the conditions */
    UPDATE agg_step_data_XXX sd
        JOIN agg_concat_conditions_XXX cond USING (step_id, student_id)
        SET sd.conditions = cond.conditions;

    CALL debug_log('aggregator_sp', 'Finished agg_get_step_data_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Set the step_time values in the agg_step_data table.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `agg_set_step_time_XXX` $$
CREATE PROCEDURE `agg_set_step_time_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_set_step_time_XXX');

    DROP TABLE IF EXISTS agg_step_time_XXX;

    CREATE TABLE agg_step_time_XXX
    (
        student_id          BIGINT,
        step_id             BIGINT,
        problem_view    BIGINT,
        step_time           DATETIME,

        PRIMARY KEY(student_id, step_id, problem_view)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;


    INSERT INTO agg_step_time_XXX
        (SELECT tt.student_id, subgoal_id, IFNULL(pe.problem_view, -1), MIN(transaction_time)
        FROM agg_tutor_transaction_XXX tt JOIN agg_problem_event_XXX pe ON pe.problem_event_id = tt.problem_event_id
        WHERE correct_flag = 'CORRECT' AND subgoal_id IS NOT NULL
        GROUP BY student_id, subgoal_id, problem_view);


    UPDATE agg_step_data_XXX sd
        JOIN agg_step_time_XXX st ON sd.student_id = st.student_id
				AND sd.step_id = st.step_id
				AND st.problem_view = sd.problem_view
        SET sd.step_time = IFNULL(st.step_time, sd.step_time),
            sd.correct_tx_time = IFNULL(st.step_time, sd.step_time);
            
            
    TRUNCATE TABLE agg_step_time_XXX;
    INSERT INTO agg_step_time_XXX
        (SELECT tt.student_id, subgoal_id, IFNULL(problem_view, -1), MAX(transaction_time)
        FROM agg_tutor_transaction_XXX tt JOIN agg_problem_event_XXX pe ON pe.problem_event_id = tt.problem_event_id
        WHERE subgoal_id IS NOT NULL
        GROUP BY student_id, subgoal_id, problem_view);

    /* if step_time is null, set to the max_tx_time */
    UPDATE agg_step_data_XXX sd
        JOIN agg_step_time_XXX st ON sd.student_id = st.student_id
				AND sd.step_id = st.step_id
				AND st.problem_view = sd.problem_view
        SET sd.step_time = IFNULL(sd.step_time, st.step_time);
        
         
    DROP TABLE agg_step_time_XXX;
    CREATE TABLE agg_step_time_XXX
    (
        student_id      BIGINT,
        step_id         BIGINT,
        min_tx_time     DATETIME,
        max_tx_time     DATETIME,

        PRIMARY KEY(student_id, step_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_step_time_XXX
        (SELECT student_id, subgoal_id, MIN(tt.transaction_time), null as max_tx_time
        FROM agg_tutor_transaction_XXX tt
        WHERE tt.correct_flag = 'CORRECT' AND subgoal_id IS NOT NULL
        GROUP BY tt.student_id, tt.subgoal_id);
        

    DROP TABLE IF EXISTS agg_max_tx_time_XXX;
    CREATE TABLE agg_max_tx_time_XXX
    (
        student_id      BIGINT,
        step_id         BIGINT,
        max_tx_time     DATETIME,

        PRIMARY KEY(student_id, step_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_max_tx_time_XXX
    (SELECT student_id, subgoal_id, MAX(transaction_time)
        FROM agg_tutor_transaction_XXX tt
        WHERE subgoal_id IS NOT NULL
        GROUP BY student_id, subgoal_id);
        

    UPDATE agg_step_time_XXX st
        JOIN agg_max_tx_time_XXX tx USING (student_id, step_id)
        SET st.max_tx_time = tx.max_tx_time;

    UPDATE agg_step_data_XXX sd
        JOIN agg_step_time_XXX st using (student_id, step_id)
        SET sd.step_time = IFNULL(st.min_tx_time, st.max_tx_time),
            sd.correct_tx_time = st.min_tx_time
        WHERE sd.problem_event_id = -1;
    /* problem_event_id of -1 means row does not have problem event info */

    CALL debug_log('aggregator_sp', 'Finished agg_set_step_time_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Calculate and set the step_start_time for the agg_step_data table.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_set_step_start_time_XXX` $$
CREATE PROCEDURE `agg_set_step_start_time_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_set_step_start_time_XXX');
    DROP TABLE IF EXISTS agg_step_start_time_XXX;
    CREATE TABLE agg_step_start_time_XXX
    (
        student_id          BIGINT,
        step_id             BIGINT,
        problem_view    BIGINT,
        pe_time             DATETIME,
        earliest_tx_time    DATETIME,
        prev_tx_time        DATETIME,
        step_start_time     DATETIME,
        time_difference     BIGINT,

        PRIMARY KEY(student_id, step_id, problem_view)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_step_start_time_XXX
        (SELECT student_id, step_id, problem_view, problem_event_time,
            earliest_transaction, prev_tx_time, null as step_start_time, null as time_difference
        FROM agg_step_data_XXX sd);

    UPDATE agg_step_start_time_XXX
    SET step_start_time =
    IF(pe_time IS NULL,
        prev_tx_time,
        (IF(prev_tx_time IS NOT NULL AND prev_tx_time > pe_time,
            prev_tx_time,
            IF(pe_time < earliest_tx_time,
                pe_time,
                earliest_tx_time))
            )
        );

    UPDATE agg_step_start_time_XXX
    SET time_difference = ABS((SELECT TIMESTAMPDIFF(SECOND, step_start_time, earliest_tx_time))) * 1000;

    UPDATE agg_step_start_time_XXX
    SET step_start_time = IF(step_start_time IS NOT NULL AND time_difference > 600000, null, step_start_time);

    /* Now update the agg_step_data_XXX table with start time values. */
    UPDATE agg_step_data_XXX sd
        JOIN agg_step_start_time_XXX st ON sd.student_id = st.student_id
				AND sd.step_id = st.step_id
				AND st.problem_view = sd.problem_view
        SET sd.step_start_time = st.step_start_time;

    CALL debug_log('aggregator_sp', 'Finished agg_set_step_start_time_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  We can't calculate correct_step_duration and error_step_duration on-the-fly,
  so we must go back into the agg_step_data table and do it.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_set_step_duration_XXX` $$
CREATE PROCEDURE `agg_set_step_duration_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_set_step_duration_XXX');

    UPDATE agg_step_data_XXX sd
    SET sd.step_duration = IF(sd.step_start_time IS NULL, NULL, sd.step_duration),
        sd.correct_step_duration = IF(sd.first_attempt = 'CORRECT'
            AND sd.step_duration IS NOT NULL, sd.step_duration, NULL),
        sd.error_step_duration = IF(sd.first_attempt != 'CORRECT'
            AND sd.step_duration IS NOT NULL, sd.step_duration, NULL);

    CALL debug_log('aggregator_sp', 'Finished agg_set_step_duration_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Set the problem_view values in agg_step_data_XXX.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_set_problem_view_XXX` $$
CREATE PROCEDURE `agg_set_problem_view_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_set_problem_view_XXX');

    UPDATE agg_step_data_XXX sd
    SET sd.problem_view = agg_get_problem_view_XXX(sd.student_id, sd.problem_event_id, sd.problem_id)
    WHERE sd.problem_view IS NULL;
    CALL debug_log('aggregator_sp', 'Finished agg_set_problem_view_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to populate the agg_step_skill_data table.  The agg_step_skill_data
  table is used to hold a list of student_id, step_id, skill_id, skill_model_id
  and intercept information.

  @param sample_id the same we are aggregating
  @param skill_model_clause the "IN" clause dictating which skill models to consider
    when doing the aggregation.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_gather_step_skill_data_XXX` $$
CREATE PROCEDURE `agg_gather_step_skill_data_XXX` (IN skillModelClause text)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_gather_step_skill_data_XXX');
    CALL debug_log('aggregator_sp', 'Getting skill models. XXX');
  /*
     Create a table to hold a distinct list of skill_model_ids for this
     dataset.
  */
    DROP TABLE IF EXISTS agg_skill_model_XXX;
    CREATE TABLE agg_skill_model_XXX (
        skill_model_id      BIGINT,
        PRIMARY KEY(skill_model_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    CALL exec(CONCAT(
        "INSERT INTO agg_skill_model_XXX
         SELECT distinct skill_model_id from skill_model sk
         WHERE",
        skillModelClause));


  CALL debug_log('aggregator_sp', 'Creating agg_step_skill_data_XXX');
  /* Create a table to hold a listing of student_id, step_id and
     skill_model_id.
  */
   DROP TABLE IF EXISTS agg_step_skill_data_XXX;
    CREATE TABLE agg_step_skill_data_XXX
    (
        student_id          BIGINT,
        step_id             BIGINT,
        skill_id            BIGINT,
        skill_model_id      BIGINT,

        PRIMARY KEY(student_id, step_id, skill_model_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO agg_step_skill_data_XXX (student_id, step_id, skill_model_id)
        SELECT DISTINCT sd.student_id, sd.step_id, skill_model_id
        FROM agg_step_data_XXX sd, agg_skill_model_XXX;


  CALL debug_log('aggregator_sp', 'Creating agg_step_skill_data_complete_XXX');
  /* create a table to hold the step/skill mapping data. This table holds all
     the information for steps with skill mappings.  We'll then update
     agg_step_skill_data with info from this table.
  */
    DROP TABLE IF EXISTS agg_step_skill_data_complete_XXX;
    CREATE TABLE agg_step_skill_data_complete_XXX
    (
        student_id          BIGINT,
        step_id             BIGINT,
        skill_id            BIGINT,
        skill_model_id      BIGINT,

        PRIMARY KEY(student_id, step_id, skill_id, skill_model_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    /* Now give it some data. */
    CALL exec(CONCAT(
    "INSERT INTO agg_step_skill_data_complete_XXX SELECT tt.student_id,
        tt.subgoal_id,
        tsm.skill_id,
        sk.skill_model_id
    FROM agg_tutor_transaction_XXX tt
        JOIN transaction_skill_map tsm ON tt.actual_tx_id = tsm.transaction_id
        JOIN skill sk ON tsm.skill_id = sk.skill_id
        JOIN agg_skill_model_XXX sm ON sk.skill_model_id = sm.skill_model_id
    WHERE (
        tt.correct_flag = 'CORRECT'
        OR EXISTS (
            SELECT 1
            FROM agg_tutor_transaction_XXX tt1
            JOIN subgoal_attempt sa USING (subgoal_attempt_id)
            WHERE tt1.student_id = tt.student_id
                AND tt1.subgoal_id = tt.subgoal_id
                AND tt1.student_id = tt.student_id
                AND tt1.subgoal_id IS NOT NULL
            GROUP BY tt1.subgoal_id
            HAVING SUM( if(sa.correct_flag = 'correct', 1, 0)) = 0)
    ) AND tt.subgoal_id IS NOT NULL AND ",
    skillModelClause,
    " GROUP BY tt.student_id, tt.subgoal_id, tsm.skill_id"));

  CREATE INDEX group_index on agg_step_skill_data_complete_XXX(student_id, step_id, skill_model_id);

  CALL debug_log('aggregator_sp', 'Updating agg_step_skill_data with data from complete. XXX');
  /*
     Now update agg_step_skill_data with the data in
     agg_step_skill_data_with_intercept.  This gives us a table containing
     all students, steps and skill_models, regardless of if a step does not have
     any assigned skills for the given skill_model_id.
  */
    UPDATE agg_step_skill_data_XXX t1
        JOIN agg_step_skill_data_complete_XXX t2 USING (student_id, step_id, skill_model_id)
        SET t1.skill_id = t2.skill_id;

  CALL debug_log('aggregator_sp', 'Updating agg_step_skill_data_complete with missing mappings. XXX');
  /*
    It may have been the case that a step_id had more than 1 skill mapped.  The above
    insert does not satisy this condition, so go back and take care of it.
    There may be a better way to do this.
  */
    INSERT INTO agg_step_skill_data_complete_XXX (student_id, step_id, skill_id, skill_model_id)
        SELECT student_id, step_id, -1 as skill_id, skill_model_id
        FROM agg_step_skill_data_XXX
        WHERE (student_id, step_id, skill_model_id)
        NOT IN (SELECT student_id, step_id, skill_model_id
                FROM agg_step_skill_data_complete_XXX);

    CALL debug_log('aggregator_sp', 'Finished agg_gather_step_skill_data_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Calculate and store the opportunity number for a given skill and student.

  @param student_id the student we care about.
  @param skill_id the skill we care about.
  @returns INT representing the current skill opportunity count.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `agg_get_skill_opportunity_XXX` $$
CREATE FUNCTION `agg_get_skill_opportunity_XXX`(studentId BIGINT, skillId BIGINT)
    RETURNS INT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE oppCountValue INT DEFAULT NULL;
    /*
     if this is the first time accessing the tables, they will be empty, so
     use the handler below to deal with that 'state' (empty table/no rows returned).
    */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET oppCountValue = NULL;

    /*
    check agg_skill_opp_counts to see if we've already seen this pair
    of student_id and skill_id. If so, return the value from that table.  Otherwise,
    set the opp value to 1 and store it in the table.
    */

    IF skillId IS NOT NULL THEN
        SELECT opp_count INTO oppCountValue
        FROM agg_skill_opp_counts_XXX
        WHERE student_id = studentId
            AND skill_id = skillId;
        IF oppCountValue IS NULL THEN
            SET oppCountValue = 1;
            INSERT INTO agg_skill_opp_counts_XXX
                VALUES (studentId, skillId, oppCountValue);
        ELSE
            SET oppCountValue = oppCountValue + 1;
            UPDATE agg_skill_opp_counts_XXX
            SET opp_count = oppCountValue
            WHERE student_id = studentId
                AND skill_id = skillId;
        END IF;
    END IF;

    RETURN oppCountValue;

END $$

/*
  ------------------------------------------------------------------------------
  Convert first_attempt from text to an integer

  @param first_attempt the text value of the first_attempt
  @returns an INT value (as expected by the step_rollup_table)
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `agg_convert_first_attempt_XXX` $$
CREATE FUNCTION `agg_convert_first_attempt_XXX`(firstAttempt TEXT) RETURNS TEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    /* default to 3, which is UNKNOWN */
    DECLARE firstAttemptInt TEXT DEFAULT '3';

    IF firstAttempt = 'INCORRECT' THEN
        SET firstAttemptInt = '0';
    ELSEIF firstAttempt = 'HINT' THEN
        SET firstAttemptInt = '1';
    ELSEIF firstAttempt = 'CORRECT' THEN
        SET firstAttemptInt = '2';
    END IF;
    RETURN firstAttemptInt;
END $$

/*
  ------------------------------------------------------------------------------
  Build the step_rollup table from the agg_step_data_XXX and
  agg_step_skill_data_XXX tables.  If a step has multiple skills, we need to
  create a step_rollup for each skill, so copy the step_data, but change the
  skill details.

  @param sample_id the sample we are aggregating.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_create_agg_step_rollup_XXX` $$
CREATE PROCEDURE `agg_create_agg_step_rollup_XXX` (IN sampleId long)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_create_agg_step_rollup_XXX');

  /*
   create a table to hold student_id, skill_id and opp_num so we can correctly
   calculate opportunity counts.
  */
    DROP TABLE IF EXISTS agg_skill_opp_counts_XXX;
    CREATE TABLE agg_skill_opp_counts_XXX
    (
        student_id          BIGINT,
        skill_id            BIGINT,
        opp_count           INT,
        PRIMARY KEY(student_id, skill_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    /* create a table to hold the step_rollup data. */

    DROP TABLE IF EXISTS agg_step_rollup_XXX;
    CREATE TABLE agg_step_rollup_XXX
    (
        sample_id               INT    NOT NULL,
        student_id              BIGINT NOT NULL,
        step_id                 BIGINT NOT NULL,
        skill_id                BIGINT,
        opportunity             INT,
        skill_model_id          BIGINT,

        /* additional information */
        dataset_id              INT    NOT NULL,
        problem_id              BIGINT NOT NULL,
        problem_view            INT    NOT NULL,

        /* aggregated values */
        total_hints             INT NOT NULL,
        total_incorrects        INT NOT NULL,
        total_corrects          INT NOT NULL,
        conditions                TEXT,
        first_attempt           TEXT NOT NULL, /* (incorrect, hint, correct, unknown) */

        /* times */
        step_time               DATETIME NOT NULL,
        first_transaction_time  DATETIME NOT NULL,
        step_start_time         DATETIME,
        step_end_time           DATETIME NOT NULL,
        correct_tx_time         DATETIME,

        /* Calculate values */
        predicted_error_rate    DOUBLE,
        step_duration           BIGINT,
        correct_step_duration   BIGINT,
        error_step_duration     BIGINT,
        error_rate              TINYINT,
        high_stakes             TINYINT

    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin

     /* Populate the table! */

    SELECT sampleId as sample_id,
        sd.student_id,
        sd.step_id,
        IF(ssd.skill_id = -1, NULL, ssd.skill_id) as skill_id,
        null as opportunity,
        ssd.skill_model_id,
        sd.dataset_id,
        sd.problem_id,
        sd.problem_view,
        sd.hints as total_hints,
        sd.incorrects as total_incorrects,
        sd.corrects as total_corrects,
        sd.conditions,
        sd.first_attempt,
        sd.step_time,
        sd.earliest_transaction as first_transaction_time,
        sd.step_start_time,
        sd.max_tx_time as step_end_time,
        sd.correct_tx_time,
        null as predicted_error_rate,
        sd.step_duration,
        sd.correct_step_duration,
        sd.error_step_duration,
        sd.error_rate,
        sd.high_stakes
    FROM agg_step_data_XXX sd
    JOIN agg_step_skill_data_complete_XXX ssd USING(student_id, step_id)
    WHERE step_id IS NOT NULL
    GROUP BY sd.student_id, sd.step_id, ssd.skill_id, ssd.skill_model_id, sd.problem_view
    ORDER BY sd.student_id, sd.step_time;

    UPDATE agg_step_rollup_XXX
        SET first_attempt = agg_convert_first_attempt_XXX(first_attempt);

    /* Now fill in the skill opportunity values. */
    UPDATE agg_step_rollup_XXX
        SET opportunity = agg_get_skill_opportunity_XXX(student_id, skill_id);

    CALL debug_log('aggregator_sp', 'Finished agg_create_agg_step_rollup_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Insert all of the data from agg_step_rollup into the actual step_rollup table.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_insert_into_step_rollup_XXX` $$
CREATE PROCEDURE `agg_insert_into_step_rollup_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_insert_into_step_rollup_XXX');

    INSERT INTO step_rollup (sample_id,
        student_id,
        step_id,
        skill_id,
        opportunity,
        skill_model_id,
        dataset_id,
        problem_id,
        problem_view,
        total_hints,
        total_incorrects,
        total_corrects,
        conditions,
        first_attempt,
        step_time,
        first_transaction_time,
        correct_transaction_time,
        step_start_time,
        step_end_time,
        step_duration,
        correct_step_duration,
        error_step_duration,
        error_rate)
        SELECT distinct sample_id,
            student_id,
            step_id,
            skill_id,
            opportunity,
            skill_model_id,
            tsr.dataset_id,
            problem_id,
            problem_view,
            total_hints,
            total_incorrects,
            total_corrects,
            conditions,
            first_attempt,
            step_time,
            first_transaction_time,
            correct_tx_time,
            step_start_time,
            step_end_time,
            step_duration,
            correct_step_duration,
            error_step_duration,
            error_rate
        FROM agg_step_rollup_XXX tsr
        WHERE step_id IS NOT NULL;

    CALL debug_log('aggregator_sp', 'Finished agg_insert_into_step_rollup_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Version of the above for datasets with highStakes custom field defined.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_insert_into_step_rollup_oli_XXX` $$
CREATE PROCEDURE `agg_insert_into_step_rollup_oli_XXX` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log('aggregator_sp', 'Starting agg_insert_into_step_rollup_oli_XXX');

    INSERT INTO step_rollup_oli (step_rollup_id, high_stakes)
           SELECT sr.step_rollup_id AS step_rollup_id,
                  asr.high_stakes AS high_stakes
           FROM step_rollup sr
           JOIN agg_step_rollup_XXX asr ON (asr.sample_id = sr.sample_id AND
                                            asr.student_id = sr.student_id AND
                                            asr.step_id = sr.step_id AND
                                            asr.skill_id = sr.skill_id AND
                                            asr.opportunity = sr.opportunity AND
                                            asr.skill_model_id = sr.skill_model_id)
           WHERE asr.step_id IS NOT NULL AND asr.high_stakes = 1;

    CALL debug_log('aggregator_sp', 'Finished agg_insert_into_step_rollup_oli_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Run the aggregator stored procedures.
  ** ORDER MATTERS!! **

  @param sampleID the sample we wish to aggregate.
  @param skillModelClause the skill models to consider when aggregating.
  @returns numRowsCreated the number of rows added to the step_rollup table.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `run_aggregator_XXX` $$
CREATE PROCEDURE `run_aggregator_XXX` (IN sampleId long, IN studentIdClause text, IN skillModelClause text,
        OUT numRowsCreated BIGINT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE datasetId INT DEFAULT NULL;
    DECLARE highStakesCF BIGINT DEFAULT NULL;
    DECLARE sampleName VARCHAR(100);
    SELECT sample_name INTO sampleName
        FROM sample WHERE sample_id = sampleId;
    CALL debug_log('aggregator_sp', concat(get_version_aggregator_sp_XXX(),
                                           ' Starting run_aggregator_XXX for sample ',
                                           sampleName, ' (', sampleId, ')'));

    SELECT dataset_id INTO datasetId
        FROM sample
        WHERE sample_id = sampleId;

    SELECT custom_field_id INTO highStakesCF
    	FROM custom_field
	WHERE dataset_id = datasetId AND custom_field_name LIKE '%highStakes%';

    CALL agg_get_student_data_XXX(sampleId, studentIdClause);
    CALL agg_get_tx_list_XXX(sampleId);
    CALL agg_get_tx_data_XXX();
    CALL agg_get_tx_condition_map_XXX();
    CALL agg_get_conditions_XXX(sampleId);
    CALL agg_get_pe_data_XXX();

    CALL agg_get_step_data_XXX();

    CALL agg_set_step_time_XXX();
    CALL agg_set_step_start_time_XXX();
    CALL agg_set_step_duration_XXX();
    CALL agg_set_problem_view_XXX();
    CALL agg_gather_step_skill_data_XXX(skillModelClause);
    CALL agg_create_agg_step_rollup_XXX(sampleId);

    CALL agg_insert_into_step_rollup_XXX();

    /* If highStakes is present, update step_rollup_oli. */
    IF highStakesCF IS NOT NULL THEN
       CALL agg_insert_into_step_rollup_oli_XXX();
    END IF;

    SELECT COUNT(*) INTO numRowsCreated
        FROM agg_step_rollup_XXX;

    CALL debug_log('aggregator_sp', 'Finished run_aggregator_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Do some clean-up by dropping the 21 tables created.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `agg_drop_helper_tables_XXX` $$
CREATE PROCEDURE `agg_drop_helper_tables_XXX` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('aggregator_sp', 'Starting agg_drop_helper_tables_XXX');

    DROP TABLE IF EXISTS agg_session_XXX;
    DROP TABLE IF EXISTS agg_tx_list_XXX;
    DROP TABLE IF EXISTS agg_tutor_transaction_XXX;
    DROP TABLE IF EXISTS agg_tx_condition_map_XXX;
    DROP TABLE IF EXISTS agg_conditions_XXX;
    DROP TABLE IF EXISTS agg_concat_conditions_XXX;
    DROP TABLE IF EXISTS agg_prev_tx_time_XXX;
    DROP TABLE IF EXISTS agg_problem_event_XXX;
    DROP TABLE IF EXISTS agg_same_timestamps_XXX;
    DROP TABLE IF EXISTS agg_problem_event_pair_view_XXX;
    DROP TABLE IF EXISTS agg_step_data_XXX;
    DROP TABLE IF EXISTS agg_step_time_XXX;
    DROP TABLE IF EXISTS agg_max_tx_time_XXX;
    DROP TABLE IF EXISTS agg_step_start_time_XXX;
    DROP TABLE IF EXISTS agg_skill_model_XXX;
    DROP TABLE IF EXISTS agg_step_skill_data_XXX;
    DROP TABLE IF EXISTS agg_step_skill_data_complete_XXX;
    DROP TABLE IF EXISTS agg_skill_opp_counts_XXX;
    DROP TABLE IF EXISTS agg_step_rollup_XXX;

    CALL debug_log('aggregator_sp', 'Finished agg_drop_helper_tables_XXX');
END $$

DELIMITER ;
