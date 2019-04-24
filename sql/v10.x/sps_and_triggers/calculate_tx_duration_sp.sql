-- -----------------------------------------------------------------------------------------------------
--  Carnegie Mellon University, Human Computer Interaction Institute
--  Copyright 2005-2009
--  All Rights Reserved
--
--  $Revision: 13575 $
--  Last modified by - $Author: hcheng $
--  Last modified on - $Date: 2016-09-28 13:14:48 -0400 (Wed, 28 Sep 2016) $
--  $KeyWordsOff: $
--
--  Calculate and store transaction_duration values.
--  Transaction duration = (time of current tx) - (time of previous tx OR problem start event)
--
--  HOW TO RUN:
--     CALL calculate_tx_duration(datasetId);
-- ------------------------------------------------------------------------------------------------------

DELIMITER $$

-- -----------------------------------------------------------------------------
--  Get the CVS version information.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `get_version_calculate_tx_duration` $$
CREATE FUNCTION         `get_version_calculate_tx_duration` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 13575 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */


--  ------------------------------------------------------------------------------
--  Get the time of the problem event.
--  NOTE: only interested in events where the event_flag is START (0).
--
--  @param studentId the student_id for the current tx
--  @param problemId the problem_id for the current tx
--  @param txTime the timestamp for the current tx
--  ------------------------------------------------------------------------------
DROP FUNCTION IF EXISTS `ctd_get_prob_event_time` $$
CREATE FUNCTION `ctd_get_prob_event_time`(studentId BIGINT, problemId BIGINT, txTime DATETIME)
    RETURNS DATETIME
    SQL SECURITY INVOKER
    READS SQL DATA
BEGIN
    DECLARE probEventTime         DATETIME DEFAULT NULL;

    SELECT MAX(pe.start_time)
    FROM problem_event pe
    JOIN ctd_sess_tx_dur tstd USING(session_id)
    WHERE tstd.student_id = studentId
    AND pe.problem_id = problemId
    AND pe.start_time <= txTime
    AND pe.event_flag = 0
    INTO probEventTime;

    RETURN probEventTime;
END $$

-- -------------------------------------------------------------------------------
--  Get the students/sessions that we care about and put them in a helper table
--  for use during transaction duration calculation.
--  @param datasetId the dataset we wish to process.
--  @param batchOffset the offset value to use when gathering sessions to process.
--  @param batchLimit the size of the batch to grab.
-- -------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS    `ctd_get_sessions` $$
CREATE PROCEDURE            `ctd_get_sessions`(IN datasetId long, IN batchOffset int, IN batchLimit int)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting ctd_get_sessions');

    DROP TABLE IF EXISTS ctd_sess_tx_dur;

--  We must to process the query as a string because of a MySQL bug that does not allow
--  variables as the values for limit and offset parameters.
    SET @createBatchQuery = '
    CREATE TABLE ctd_sess_tx_dur (session_id bigint, student_id bigint, PRIMARY KEY(session_id, student_id))
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        SELECT DISTINCT session_id, sess.student_id
            FROM session sess
            WHERE sess.dataset_id = ?
            ORDER BY session_id ASC
            LIMIT ?, ?;';
    PREPARE STMT FROM @createBatchQuery;
    SET @offset = batchOffset;
    SET @lim = batchLimit;
    SET @id = datasetId;
    EXECUTE STMT USING @id, @offset, @lim;
    DEALLOCATE PREPARE STMT;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_get_sessions.');
END $$

-- ----------------------------------------------------------------------------
--  Given the transactions we care about (now in ctd_tx_dur), determine the
--  prob_event_time and calculate the actual transaction duration value.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS    `ctd_get_data` $$
CREATE PROCEDURE            `ctd_get_data`()
    SQL SECURITY INVOKER
BEGIN
    DECLARE TEN_MINUTE_CUTOFF    INT DEFAULT 600;
    CALL debug_log('calculate_tx_duration', 'Starting ctd_get_data()');

    -- Calculate and set the prob_event_time for each row.
    CALL debug_log('calculate_tx_duration', 'ctd_get_data: before update ctd_tx_dur.prob_event_time');
    UPDATE ctd_tx_dur SET prob_event_time =
        ctd_get_prob_event_time(student_id, problem_id, transaction_time);

    -- Calculate and set the duration for each row.
    CALL debug_log('calculate_tx_duration', 'ctd_get_data: before update ctd_tx_dur.duration');
    UPDATE ctd_tx_dur SET duration =
        IF(prob_event_time IS NULL,
            IF(prev_tx_time IS NULL,
                null,
                IF(TIMESTAMPDIFF(SECOND, prev_tx_time, transaction_time) > TEN_MINUTE_CUTOFF,
                    null, TIMESTAMPDIFF(SECOND, prev_tx_time, transaction_time)*1000
                )
            ),
            IF(prev_tx_time IS NULL,
                IF(TIMESTAMPDIFF(SECOND, prob_event_time, transaction_time) > TEN_MINUTE_CUTOFF,
                    null, TIMESTAMPDIFF(SECOND, prob_event_time, transaction_time)*1000
                ),
                IF(TIMESTAMPDIFF(SECOND, GREATEST(prob_event_time, prev_tx_time), transaction_time) > TEN_MINUTE_CUTOFF,
                    null, TIMESTAMPDIFF(SECOND, GREATEST(prob_event_time, prev_tx_time), transaction_time)*1000
                )
            )
        );

    CALL debug_log('calculate_tx_duration', 'Finished ctd_get_data()');

END $$

-- ----------------------------------------------------------------------------
--  It could be the case that transactions for the same student have the
--  same timestamps.  We need to correct for this so that step durations are
--  accurate.  This procedure takes care of that.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS    `ctd_adjust_for_identical_timestamps` $$
CREATE PROCEDURE            `ctd_adjust_for_identical_timestamps`()
    SQL SECURITY INVOKER

BEGIN
    CALL debug_log('calculate_tx_duration', 'Started ctd_adjust_for_identical_timestamps.');

    /*
      Now correct for transactions with the same student with identical timestamps.
      Create and populate a table to hold a list of duplicates.
    */
    CALL debug_log('calculate_tx_duration', 'Creating table ctd_same_timestamps');
    DROP TABLE IF EXISTS ctd_same_timestamps;
    CREATE TABLE ctd_same_timestamps
       (
            student_id          BIGINT,
            transaction_time    DATETIME,
            problem_id		BIGINT,
            frequency           INT,
            duration            BIGINT,
            avg_duration        BIGINT
       ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
    SELECT student_id,
           transaction_time,
	   problem_id,
           COUNT(*) as frequency,
           duration,
	   null as avg_duration
        FROM ctd_tx_dur
        GROUP BY student_id, transaction_time, problem_id
        HAVING frequency > 1;

    CREATE INDEX student_id_index ON ctd_same_timestamps(student_id);

--  Now compute average duration.
    UPDATE ctd_same_timestamps
    SET avg_duration = duration/frequency;

--  Now update ctd_tx_dur with averages of the durations.
    UPDATE ctd_tx_dur tt
    JOIN ctd_same_timestamps yikes using (student_id)
    SET tt.duration = yikes.avg_duration
    WHERE yikes.student_id = tt.student_id
        AND yikes.transaction_time = tt.transaction_time
        AND yikes.problem_id = tt.problem_id;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_adjust_for_identical_timestamps.');
END $$

-- ----------------------------------------------------------------------------
--  Take the tx durations from our helper tx table and update the
--  tutor_transaction relation.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS     `ctd_update_tutor_transaction` $$
CREATE PROCEDURE            `ctd_update_tutor_transaction`(IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Started ctd_update_tutor_transaction.');
    UPDATE tutor_transaction tt
    JOIN ctd_tx_dur td ON tt.transaction_id = td.actual_tx_id
    SET tt.duration = td.duration,
        tt.transaction_time = tt.transaction_time
    WHERE tt.transaction_id = td.actual_tx_id;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_update_tutor_transaction.');
END $$

-- ----------------------------------------------------------------------------
--  Take the tx durations from our helper tx table and update the
--  sample_metric relation.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS     `ctd_update_sample_metric` $$
CREATE PROCEDURE            `ctd_update_sample_metric`(IN datasetId long, IN sampleId long)
    SQL SECURITY INVOKER
BEGIN
    DECLARE sampleMetricId INT DEFAULT 0;
    DECLARE totalStudMilliseconds BIGINT DEFAULT 0;

    CALL debug_log('calculate_tx_duration',
    	           concat('Started ctd_update_sample_metric for sample ', sampleId, ' in dataset ', datasetId));

    SELECT sample_metric_id INTO sampleMetricId
      FROM sample_metric
      WHERE sample_id = sampleId
      AND metric = 'Total Student Milliseconds';

    SET totalStudMilliseconds = get_total_stud_milliseconds_by_dataset(datasetId);

    IF (totalStudMilliseconds IS NOT null) THEN
        CALL debug_log('calculate_tx_duration',
                   concat('dataset id ', datasetId,
                          ' has ', totalStudMilliseconds, ' milliseconds'));
    END IF;

    -- Update the value if row exists, otherwise insert a row
    IF ((sampleMetricId IS NOT NULL) AND (sampleMetricId > 0)) THEN
        CALL debug_log('calculate_tx_duration', concat('Update sampleMetric ', sampleMetricId));
        IF (totalStudMilliseconds IS NOT null) THEN
            UPDATE sample_metric
             SET value = totalStudMilliseconds, calculated_time = now()
               WHERE sample_metric_id = sampleMetricId;
        ELSE
            UPDATE sample_metric
             SET value = 0, calculated_time = now()
               WHERE sample_metric_id = sampleMetricId;
        END IF;
    ELSE
        CALL debug_log('calculate_tx_duration', 'Creating new sampleMetric.');
        IF (totalStudMilliseconds IS NOT null) THEN
            INSERT INTO sample_metric (sample_id, metric, value, calculated_time)
             VALUES (sampleId, 'Total Student Milliseconds', totalStudMilliseconds, NOW());
        ELSE
            INSERT INTO sample_metric (sample_id, metric, value, calculated_time)
             VALUES (sampleId, 'Total Student Milliseconds', 0, NOW());
        END IF;
    END IF;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_update_sample_metric.');
END $$

/*
  ------------------------------------------------------------------------------
  Generate problem views on a transaction basis.  Step Rollup problem views don't
  account for problems without steps.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ctd_generate_problem_events` $$
CREATE PROCEDURE         `ctd_generate_problem_events` (IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    DECLARE numProblemEventsGenerated INT DEFAULT NULL;
    CALL debug_log('calculate_tx_duration', 'Starting ctd_generate_problem_events');

    /* "START" Problem Events exist */
    IF EXISTS (SELECT problem_event_id
               FROM ctd_problem_event
               WHERE event_flag = 0) THEN

        /* Only Problem Start Time is filled in, some/all PVs are null */
        IF EXISTS (SELECT problem_view
                   FROM ctd_problem_event
                   WHERE event_flag = 0
                   AND problem_view IS NULL) THEN

            CALL debug_log('calculate_tx_duration',
                           'ctd_generate_problem_events: PEs exist and PV is null');

            DROP TABLE IF EXISTS ctd_problem_view_sequence;
            CREATE TABLE ctd_problem_view_sequence (
                student_id         BIGINT,
                problem_id         BIGINT,
                problem_view       INT AUTO_INCREMENT,
                problem_event_id   BIGINT,
                PRIMARY KEY (student_id, problem_id, problem_view)
            ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
            SELECT DISTINCT student_id, problem_id, problem_event_id
                FROM ctd_problem_event tpe
                JOIN ctd_sess_tx_dur tssm USING (student_id)
                WHERE event_flag = 0
                AND problem_view IS NULL
                ORDER BY start_time;

            UPDATE problem_event pe
                JOIN ctd_problem_view_sequence pvs USING (problem_event_id)
                SET pe.problem_view = pvs.problem_view,
                    pe.event_type = concat(pe.event_type, '_PV_GEN');

            /* Assign Problem Events to Transactions if they aren't linked */
            IF EXISTS (SELECT tt.problem_event_id
                           FROM tutor_transaction tt
                           JOIN ctd_tx_pe_gen tmptt
                           ON tt.transaction_id = tmptt.actual_tx_id
                           WHERE tt.problem_event_id IS NULL) THEN
                CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: link tx to pe 2');

                UPDATE tutor_transaction tt
                    JOIN ctd_tx_pe_gen tmptt
                    ON tt.transaction_id = tmptt.actual_tx_id
                    SET tt.problem_event_id = tmptt.problem_event_id
                    WHERE tmptt.problem_event_id != -1;

            ELSE
                CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: link tx to pe 2 else');
            END IF;

        /* Problem Start Time (required) and Problem View fields exist */
        ELSE
            CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: PEs exist and PV is not null');

            /* Assign Problem Events to Transactions if they aren't linked */
            IF EXISTS (SELECT tt.problem_event_id
                           FROM tutor_transaction tt
                           JOIN ctd_tx_pe_gen tmptt
                           ON tt.transaction_id = tmptt.actual_tx_id
                           WHERE tt.problem_event_id IS NULL) THEN
                CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: link tx to pe 1');

                UPDATE tutor_transaction tt
                    JOIN ctd_tx_pe_gen tmptt
                    ON tt.transaction_id = tmptt.actual_tx_id
                    SET tt.problem_event_id = tmptt.problem_event_id
                    WHERE tmptt.problem_event_id != -1;

            ELSE
                CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: link tx to pe 1 else');
            END IF;

        END IF;

    /* Problem Events must be created.  Set the problem start time equal to the last transaction
     * time of the previous problem for that student.  If it is the first problem for a particular
     * student, use the earliest transaction time instead. */
    ELSE
        CALL debug_log('calculate_tx_duration', 'ctd_generate_problem_events: creating PEs');

	-- Keep session_id here so that inserts into problem_event don't require an extra JOIN. --
        DROP TABLE IF EXISTS ctd_problem_view_sequence;
        CREATE TABLE ctd_problem_view_sequence (
            session_id         BIGINT,
            student_id         BIGINT,
            problem_id         BIGINT,
            start_time         DATETIME,
            problem_view       INT AUTO_INCREMENT,
            problem_event_id   BIGINT,
            PRIMARY KEY (student_id, problem_id, problem_view)
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT distinct session_id, student_id, problem_id,
               IFNULL(IF(ABS(TIMESTAMPDIFF(SECOND, prev_prob_tx_time, transaction_time)) > 600,
                         transaction_time,
                         prev_prob_tx_time),
                      transaction_time) as start_time
            FROM ctd_tx_pe_gen tt
            WHERE tt.problem_id != tt.prev_problem_id
            ORDER BY student_id, problem_id, start_time;

        /* Count the number of PEs generated. */
        SELECT count(*) INTO numProblemEventsGenerated
           FROM ctd_problem_view_sequence;

        /* Insert into the real problem event table. */
        INSERT INTO problem_event (session_id, problem_id, start_time,
                                   event_flag, event_type, problem_view)
           SELECT session_id, problem_id, start_time,
                  0 AS event_flag, 'AGG_START_PROBLEM' AS event_type, problem_view
           FROM ctd_problem_view_sequence;

        /* Insert into the helper problem event table. */
        INSERT INTO ctd_problem_event (problem_event_id, problem_id, student_id,
                                            start_time, event_flag, problem_view)
            SELECT DISTINCT problem_event_id, problem_id, student_id,
                            start_time, event_flag, problem_view
                FROM problem_event pe
                JOIN ctd_sess_tx_dur USING (session_id)
                WHERE pe.event_flag = 0;

        /* Need to update this table to allow us to join this table when updating both tx tables. */
        UPDATE ctd_problem_view_sequence pv
            JOIN problem_event pe ON pv.session_id = pe.session_id
                                  AND pv.problem_id = pe.problem_id
                                  AND pv.problem_view = pe.problem_view
            SET pv.problem_event_id = pe.problem_event_id;

        /* Need to link the new problem events to the helper transaction table
           for the attempt at subgoal procedure. */
        UPDATE ctd_tx_pe_gen ttt
            JOIN (SELECT ttt2.transaction_id, pv.student_id, pv.problem_id, max(pv.problem_event_id) as pe_id
                  FROM ctd_problem_view_sequence pv
                  JOIN ctd_tx_pe_gen ttt2
                     ON ttt2.student_id = pv.student_id
                    AND ttt2.problem_id = pv.problem_id
                    AND ttt2.transaction_time >= pv.start_time
                  GROUP BY ttt2.transaction_id, pv.student_id, pv.problem_id) xyz
            USING (transaction_id, student_id, problem_id)
            SET ttt.problem_event_id = xyz.pe_id;

        UPDATE ctd_tx_pe_gen ttt
            JOIN ctd_problem_view_sequence pv USING (problem_event_id)
            SET ttt.start_time = pv.start_time;

        /* Need to connect the transactions to the new problem events. */
        UPDATE tutor_transaction tt
            JOIN ctd_tx_pe_gen ttt ON ttt.actual_tx_id = tt.transaction_id
            SET tt.problem_event_id = ttt.problem_event_id;

        /* Need to mark the dataset as 'pe gen' so that other samples will be aggregated. */
        INSERT INTO dataset_system_log (dataset_id, time, action, info, success_flag,
                                        datashop_version)
            VALUES(datasetId, now(), 'pe gen',
                   CONCAT('Agg: ', numProblemEventsGenerated, ' problem events were generated.'),
                   true, (SELECT version FROM datashop_version));

    END IF;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_generate_problem_events');
END $$

/*
 -----------------------------------------------------------------------------
  Trac #499 uncovered the fact that attempt_at_subgoal calculations done in
  FFI are (1) incorrect since they don't include problem_event_id and 
  (2) likely not recomputed because FFI inserts problem events so this
  method was not being called. So... don't bother calculating attempt_at_step
  at all in FFI and ensure it's called as part of calculating TX duration.

  Attempt at step could be wrong if the problem events were not linked to the
  transaction table when they were calculated.
  This table leverages a MyISAM feature that will auto_increment a column 
  (attempt_at_subgoal in our case) by group (subgoal_id, student_id, problem_event_id)
  rather than for every row. This would not work on an InnoDb table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `calculate_attempt_at_subgoal` $$
CREATE PROCEDURE         `calculate_attempt_at_subgoal` (IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting calculate_attempt_at_subgoal');

    DROP TABLE IF EXISTS ctd_attempt_at_subgoal;

    CREATE TABLE ctd_attempt_at_subgoal (
        transaction_id      BIGINT PRIMARY KEY,
        subgoal_id          BIGINT,
        student_id          BIGINT,
        problem_event_id    BIGINT,
        attempt_at_subgoal  INT AUTO_INCREMENT,
        max_attempt_at_subgoal INT,
        max_problem_event_id   INT,
        KEY (subgoal_id, student_id, problem_event_id, attempt_at_subgoal)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    /* We only want to use the first student of a transaction. */
    INSERT INTO ctd_attempt_at_subgoal
            (transaction_id, subgoal_id, student_id, problem_event_id)
        SELECT transaction_id, subgoal_id, tssm.student_id, problem_event_id
        FROM tutor_transaction tt
        JOIN ctd_sess_tx_dur tssm USING (session_id)
        WHERE tt.subgoal_id IS NOT NULL AND tt.dataset_id = datasetId
        ORDER BY transaction_time, transaction_id, tssm.student_id
            ON DUPLICATE KEY UPDATE transaction_id = tt.transaction_id;

    UPDATE ctd_attempt_at_subgoal 
        JOIN (SELECT subgoal_id, student_id, problem_event_id, MAX(attempt_at_subgoal) AS maxAAS
              FROM ctd_attempt_at_subgoal
              GROUP BY subgoal_id, student_id, problem_event_id) FOO
        USING (subgoal_id, student_id, problem_event_id)
        SET max_attempt_at_subgoal = maxAAS;

    UPDATE ctd_attempt_at_subgoal 
        JOIN (SELECT subgoal_id, student_id, MAX(problem_event_id) AS maxPE
              FROM ctd_attempt_at_subgoal
              GROUP BY subgoal_id, student_id) FOO
        USING (subgoal_id, student_id)
        SET max_problem_event_id = maxPE;

    UPDATE tutor_transaction tt
        JOIN ctd_attempt_at_subgoal aas USING (transaction_id)
        SET tt.attempt_at_subgoal = aas.attempt_at_subgoal,
            tt.is_last_attempt =
            IF ((aas.attempt_at_subgoal = aas.max_attempt_at_subgoal) AND
                (aas.problem_event_id = aas.max_problem_event_id), 1, 0);

    CALL debug_log('calculate_tx_duration', 'Finished calculate_attempt_at_subgoal');
END $$

/*
 ----------------------------------------------------------------------------
  Get the tx sample mappings we care about and put them in a helper table
  for use during transaction duration calculation.
  @param sampleId the given sample
 ----------------------------------------------------------------------------
 */
DROP PROCEDURE IF EXISTS `get_tx_list` $$
CREATE PROCEDURE         `get_tx_list`()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting get_tx_list.');

    /*
      Create the ctd_tx_list table.
    */
    DROP TABLE IF EXISTS ctd_tx_list;
    CREATE TABLE ctd_tx_list
    (
        transaction_id  BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO ctd_tx_list
        (SELECT transaction_id
        FROM tutor_transaction
        JOIN ctd_sess_tx_dur USING (session_id));

    CALL debug_log('calculate_tx_duration', 'Finished get_tx_list.');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the transaction data we need for this sample.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `get_tx_data` $$
CREATE PROCEDURE         `get_tx_data` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting get_tx_data');
    --  Create and populate the ctd_tx_pe_gen table.
    --  Keep session_id here so that inserts into problem_event (via ctd_problem_view_sequence) don't require an extra JOIN. --
    DROP TABLE IF EXISTS ctd_tx_pe_gen;
    CREATE TABLE ctd_tx_pe_gen
       (
            transaction_id      BIGINT AUTO_INCREMENT,
            actual_tx_id        BIGINT,
            session_id          BIGINT,
            problem_id          BIGINT,
            transaction_time    DATETIME,
            prev_tx_time        DATETIME,
            prob_event_time     DATETIME,
	    student_id		BIGINT,
	    problem_event_id	BIGINT,
	    subgoal_attempt_id	BIGINT,
	    start_time		DATETIME,
	    prev_problem_id	BIGINT DEFAULT -1,
	    prev_prob_tx_time	DATETIME,

            PRIMARY KEY(transaction_id)
        ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin

        SELECT transaction_id as actual_tx_id, tt.session_id, problem_id, transaction_time,
            null as prev_tx_time, null as prob_event_time, tstd.student_id,
	    IFNULL(problem_event_id, -1) as problem_event_id, tt.subgoal_attempt_id
        FROM tutor_transaction tt
        JOIN ctd_tx_list USING (transaction_id)
        JOIN ctd_sess_tx_dur tstd USING (session_id)
        ORDER BY tstd.student_id, tt.transaction_time, tt.problem_id, tt.attempt_at_subgoal;

    CALL debug_log('calculate_tx_duration', 'Creating indices ctd_tx_pe_gen');
    CREATE INDEX tx_id_index ON ctd_tx_pe_gen(transaction_id);
    CREATE INDEX actual_tx_id_index ON ctd_tx_pe_gen(actual_tx_id);
    CREATE INDEX tx_session_index ON ctd_tx_pe_gen(session_id);
    CREATE INDEX student_id_index ON ctd_tx_pe_gen(student_id);
    CREATE INDEX ttt_sess_prob_idx ON ctd_tx_pe_gen(session_id, problem_id);
    CREATE INDEX ttt_stud_prob_idx ON ctd_tx_pe_gen(student_id, problem_id);

    /* Create the ctd_prev_tx_time table to help us calculate the prev_tx_time. */
    CALL debug_log('calculate_tx_duration', 'Creating table ctd_prev_tx_time');
    DROP TABLE IF EXISTS ctd_prev_tx_time;
    CREATE TABLE ctd_prev_tx_time
    (
        transaction_id      BIGINT,
        transaction_time    DATETIME,
        student_id          BIGINT,
        problem_id          BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO ctd_prev_tx_time
        (SELECT transaction_id+1 as transaction_id,
                transaction_time,
                student_id,
                problem_id
        FROM ctd_tx_pe_gen tt);

    /* Now update the ctd_tx_pe_gen table to set prev_tx_time */
    CALL debug_log('calculate_tx_duration', 'Updating ctd_tx_pe_gen.prev_tx_time');
    UPDATE ctd_tx_pe_gen tt
       JOIN ctd_prev_tx_time previous using (transaction_id, student_id)
       SET tt.prev_tx_time = previous.transaction_time;

    /* Now update the ctd_tx_pe_gen table to set prev_problem_id,
       and this depends on the student id and NOT the session id. */
    CALL debug_log('calculate_tx_duration', 'Updating ctd_tx_pe_gen.prev_problem_id');
    UPDATE ctd_tx_pe_gen tt
       JOIN ctd_prev_tx_time previous using (transaction_id, student_id)
       SET tt.prev_problem_id = previous.problem_id,
           tt.prev_prob_tx_time = previous.transaction_time;

    CALL debug_log('calculate_tx_duration', 'Finished get_tx_data');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to get the problem event data, if there is any.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `get_pe_data` $$
CREATE PROCEDURE         `get_pe_data` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting get_pe_data');

  /*
    create and populate a table to hold only the problem_event information we
    care about.  This could be empty, based on the dataset.
  */
    CALL debug_log('calculate_tx_duration', 'Getting problem event data');
    DROP TABLE IF EXISTS ctd_problem_event;
    CREATE TABLE ctd_problem_event
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
                    start_time, event_flag, problem_view
        FROM problem_event pe
        JOIN ctd_sess_tx_dur USING (session_id)
        WHERE event_flag = 0;

    CALL debug_log('calculate_tx_duration', 'Creating indices on table ctd_problem_event');

    CREATE INDEX tpe_sess_stu_time_idx  ON ctd_problem_event(student_id, start_time);
    CREATE INDEX problem_student_index ON ctd_problem_event(problem_id, student_id);
    
    /*
	    add the problem_event information to our ctd_tx_pe_gen table
	    based on student_id (not session_id!) and problem_id;
	    but don't update the record in ctd_tx_pe_gen when problem_view exists already!
	  */
    
	CALL debug_log('calculate_tx_duration', 'Adding problem event data to tx data');
	UPDATE ctd_tx_pe_gen tt
		JOIN ctd_problem_event tpe USING (student_id, problem_id)
	        SET tt.problem_event_id = tpe.problem_event_id
	        WHERE tpe.event_flag = 0
	        AND tpe.problem_view IS NULL 
	        AND tt.transaction_time >= tpe.start_time
	        AND tt.transaction_time <
	        IFNULL((SELECT MIN(pe_start.start_time)
	                FROM ctd_problem_event pe_start
	                WHERE tpe.student_id = pe_start.student_id
	                    AND tpe.problem_id = pe_start.problem_id
	                    AND pe_start.start_time > tpe.start_time
	                    AND tpe.event_flag = 0),
	               DATE_ADD(NOW(), INTERVAL 1 DAY));
	               
	  UPDATE ctd_tx_pe_gen tt
		JOIN ctd_problem_event tpe USING (student_id, problem_id)
	        SET tt.start_time = tpe.start_time
	        WHERE tpe.event_flag = 0
	        AND tt.transaction_time >= tpe.start_time
	        AND tt.transaction_time <
	        IFNULL((SELECT MIN(pe_start.start_time)
	                FROM ctd_problem_event pe_start
	                WHERE tpe.student_id = pe_start.student_id
	                    AND tpe.problem_id = pe_start.problem_id
	                    AND pe_start.start_time > tpe.start_time
	                    AND tpe.event_flag = 0),
	               DATE_ADD(NOW(), INTERVAL 1 DAY));
	               
	
	

    CALL debug_log('calculate_tx_duration', 'Creating indices on table ctd_tx_pe_gen');
    CREATE INDEX problem_event_id_index ON ctd_tx_pe_gen(problem_event_id);
    CREATE INDEX sess_stud_prob_event_index ON ctd_tx_pe_gen(session_id, student_id, problem_event_id);

    CALL debug_log('calculate_tx_duration', 'Finished get_pe_data');
END $$

/*
 -----------------------------------------------------------------------------
  Wrapper function for the methods necessary to set-up the tables needed
  for problem event generation.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ctd_get_pe_tx_data` $$
CREATE PROCEDURE         `ctd_get_pe_tx_data` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting ctd_get_pe_tx_data');

    CALL get_tx_list();
    CALL get_tx_data();
    CALL get_pe_data();
    
    CALL debug_log('calculate_tx_duration', 'Finished ctd_get_pe_tx_data');
END $$

/*
  ------------------------------------------------------------------------------
  Procedure to reload the transaction data we need for calculating duration...
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ctd_get_tx_dur` $$
CREATE PROCEDURE         `ctd_get_tx_dur` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting ctd_get_tx_dur');

    --  Create and populate the ctd_tx_dur table.
    DROP TABLE IF EXISTS ctd_tx_dur;
    CREATE TABLE ctd_tx_dur
       (
            transaction_id      BIGINT AUTO_INCREMENT,
            actual_tx_id        BIGINT,
            student_id          BIGINT,
            problem_id          BIGINT,
            transaction_time    DATETIME,
            prev_tx_time        DATETIME,
            prob_event_time     DATETIME,
            duration            BIGINT,
	    start_time		DATETIME,

            PRIMARY KEY(transaction_id)
        ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin

        SELECT actual_tx_id, student_id, problem_id, transaction_time,
            null as prev_tx_time, null as prob_event_time, null as duration, ttpg.start_time
        FROM ctd_tx_pe_gen ttpg
        ORDER BY student_id, ttpg.transaction_time, ttpg.problem_id, ttpg.start_time, ttpg.subgoal_attempt_id;

    CALL debug_log('calculate_tx_duration', 'Creating indices ctd_tx_dur');
    CREATE INDEX tx_id_index ON ctd_tx_dur(transaction_id);
    CREATE INDEX actual_tx_id_index ON ctd_tx_dur(actual_tx_id);
    CREATE INDEX ttt_stu_prob_idx ON ctd_tx_dur(student_id, problem_id);

    /* Create the ctd_prev_tx_time table to help us calculate the prev_tx_time. */
    CALL debug_log('calculate_tx_duration', 'Creating table ctd_prev_tx_time');
    DROP TABLE IF EXISTS ctd_prev_tx_time;
    CREATE TABLE ctd_prev_tx_time
    (
        transaction_id      BIGINT,
        transaction_time    DATETIME,
        student_id          BIGINT,
        problem_id          BIGINT,
        PRIMARY KEY(transaction_id)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    INSERT INTO ctd_prev_tx_time
        (SELECT transaction_id+1 as transaction_id,
                transaction_time,
                student_id,
                problem_id
        FROM ctd_tx_dur tt);

    /* Now update the ctd_tx_dur table to set prev_tx_time */
    CALL debug_log('calculate_tx_duration', 'Updating ctd_tx_dur.prev_tx_time');
    UPDATE ctd_tx_dur tt
       JOIN ctd_prev_tx_time previous using (transaction_id, student_id)
       SET tt.prev_tx_time = previous.transaction_time;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_get_tx_dur');
END $$

-- ------------------------------------------------------------------------------
--  Controller procedure to calculate the transaction durations.
--  @param datasetId the dataset we wish to process.
--  @param batchOffset the offset at which we should start gathering student ids.
--  @param batchLimit the limit of student ids to gather for this batch.
-- ------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS     `calculate_tx_duration` $$
CREATE PROCEDURE            `calculate_tx_duration`(IN datasetId long, IN batchOffset int, IN batchLimit int)
    SQL SECURITY INVOKER
BEGIN
    DECLARE sampleId BIGINT DEFAULT 0;

    CALL debug_log('calculate_tx_duration', concat(get_version_calculate_tx_duration(),
                                                   ' Starting calculate_tx_durations on dataset ',
                                                   convert(datasetId, char(8))));

    CALL ctd_get_sessions(datasetId, batchOffset, batchLimit);

    CALL ctd_get_pe_tx_data();

    CALL ctd_generate_problem_events(datasetId);

    CALL calculate_attempt_at_subgoal(datasetId);

    -- Need a new ordering of txns for duration calculation.
    -- Ordering is different than that used for problem_event generation.
    CALL ctd_get_tx_dur();

    CALL ctd_get_data();

    CALL ctd_adjust_for_identical_timestamps();

    CALL ctd_update_tutor_transaction(datasetId);

    -- update sample metric for the all data sample if there is one
    SELECT sample_id INTO sampleId
        FROM sample WHERE dataset_id = datasetId AND sample_name = 'All Data';
    IF sampleId != 0 THEN
        CALL debug_log('calculate_tx_duration', concat('All Data [', sampleId, '] sample: updating sample_metric table.'));
        CALL ctd_update_sample_metric(datasetId, sampleId);
    ELSE
        CALL debug_log('calculate_tx_duration', 'All Data sample does not exist, not updating sample_metric table.');
    END IF;
    
    CALL ctd_drop_helper_tables();

    CALL debug_log('calculate_tx_duration', 'Finished calculate_tx_durations.');
END $$

--  ------------------------------------------------------------------------------
--  Do some clean-up.
--  ------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `ctd_drop_helper_tables` $$
CREATE PROCEDURE `ctd_drop_helper_tables` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('calculate_tx_duration', 'Starting ctd_drop_helper_tables');

    DROP TABLE IF EXISTS ctd_tx_pe_gen;
    DROP TABLE IF EXISTS ctd_tx_dur;
    DROP TABLE IF EXISTS ctd_sess_tx_dur;
    DROP TABLE IF EXISTS ctd_same_timestamps;
    DROP TABLE IF EXISTS ctd_prev_tx_time;
    DROP TABLE IF EXISTS ctd_problem_view_sequence;
    DROP TABLE IF EXISTS ctd_attempt_at_subgoal;
    DROP TABLE IF EXISTS ctd_tx_list;
    DROP TABLE IF EXISTS ctd_problem_event;

    CALL debug_log('calculate_tx_duration', 'Finished ctd_drop_helper_tables');
END $$

DELIMITER ;
