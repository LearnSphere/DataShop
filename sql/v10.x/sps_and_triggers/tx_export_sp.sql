/*
   Carnegie Mellon University, Human Computer Interaction Institute
   Copyright 2005-2007
   All Rights Reserved

   $Revision: 12404 $
   Last modified by - $Author: ctipper $
   Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
   $KeyWordsOff: $

   Run the transaction export as a stored procedure to make it faster.

   PUBLIC METHODS:

   tx_export_XXX(sampleId) where sampleId is the id of the sample for which you want
                                 to generate a cached export file

   NOTE #1:
     DS920:  (CFG: -- SQL comments cannot be used in SPs that are modified and reloaded)
     As this file is modified programmatically and then the SPs are reloaded in java code,
     we cannot use the standard -- comments to start a line.

     If you do use -- comments then the following occurs:
         HibernateException while loading customized stored procedure. could not execute native
             bulk manipulation query
         Caused by: com.mysql.jdbc.exceptions.MySQLSyntaxErrorException: You have an error in your SQL
             syntax; check the manual that corresponds to your MySQL server version for the right
             syntax to use near '' at line 1
   NOTE #2:
     This file depends on the stored procedures that are in tx_export_util_sp.sql file.
*/
DELIMITER $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_tx_export_sp_XXX` $$
CREATE FUNCTION `get_version_tx_export_sp_XXX` ()
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

/*
  ------------------------------------------------------------------------------
  Initial creation and population of tx_export_XXX table.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `createExportTables_XXX` $$
CREATE PROCEDURE `createExportTables_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('tx_export_sp', 'Starting createExportTables_XXX');

    /* enable profiling in information_schema.profiling */
    /* set profiling=1; */

    /* tx_export_XXX will contain all transaction export data */
    create temporary table tx_export_XXX(
        row_num bigint auto_increment primary key,
        transaction_id bigint,
  tx_id char(32),
        problem_id bigint,
        subgoal_attempt_id bigint,
        sample_name text,
        students text,
        session_tag text,
        transaction_time datetime,
        time_zone text,
        duration text,
        transaction_type_tool text,
        transaction_subtype_tool text,
        transaction_type_tutor text,
        transaction_subtype_tutor text,
        levels text,
        problem_name text,
        problem_view int,
        problem_start_time datetime,
        subgoal_name text,
        attempt_at_subgoal int,
        is_last_attempt boolean,
        outcome text,
        selections text,
        actions text,
        inputs text,
        feedback_text text,
        classification text,
        help_level int,
        total_num_hints int,
        conditions text,
        skills text,
        school_name text,
        class_name text,
        custom_fields longtext,
        index (transaction_id),
        index (subgoal_attempt_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    /* insert all single column values for all transactions */
    insert into tx_export_XXX
        (transaction_id, tx_id, problem_id, subgoal_attempt_id, sample_name, students, session_tag,
        transaction_time, time_zone, duration, transaction_type_tool, transaction_subtype_tool,
        transaction_type_tutor, transaction_subtype_tutor, outcome, help_level, total_num_hints,
        attempt_at_subgoal, is_last_attempt, school_name, class_name,
        problem_name, problem_view, problem_start_time,
        subgoal_name, feedback_text, classification)
    select tttsm.transaction_id, tttsm.guid, tttsm.problem_id, tttsm.subgoal_attempt_id,
        sample_name, students, session_tag, transaction_time, time_zone,
        IFNULL(TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM FORMAT(duration/1000, 3))), '.'),
        transaction_type_tool, transaction_subtype_tool,
        transaction_type_tutor, transaction_subtype_tutor,
        outcome, help_level, total_num_hints, attempt_at_subgoal, is_last_attempt,
        school_name, class_name, problem_name, problem_view, pe.start_time, subgoal_name,
        feedback_text, classification
    from
        temp_tt_tsm_XXX tttsm
        join session s on tttsm.session_id = s.session_id
        join student_export_XXX se on tttsm.session_id = se.session_id
        left join school sc on tttsm.school_id = sc.school_id
        left join class c on tttsm.class_id = c.class_id
        join sample on sample.sample_id = sampleId
        left join problem p on tttsm.problem_id = p.problem_id
        left join subgoal sub on sub.subgoal_id = tttsm.subgoal_id
        left join feedback f on tttsm.feedback_id = f.feedback_id
        left join problem_event pe on tttsm.problem_event_id = pe.problem_event_id;
    /* order by students, transaction_time, transaction_id, subgoal_attempt_id, attempt_at_subgoal; */

    /* disable profiling
    set profiling=0;

    fields cpu_user ... swaps are only available in linux mysql installation
    create table if not exists cfg_profiling(row_id BIGINT auto_increment primary key,
        query_id INT(20),
        sample_id VARCHAR(10),
        seq INT(20),
        state VARCHAR(30),
        duration DECIMAL(9,6),
        start_time datetime,
        source_function VARCHAR(30),
        source_file VARCHAR(20),
        source_line INT(20),
        cpu_user DECIMAL(9,6),
        cpu_system DECIMAL(9,6),
        context_voluntary INT(20),
        context_involuntary INT(20),
        block_ops_in INT(20),
        block_ops_out INT(20),
        messages_sent INT(20),
        messages_received INT(20),
        page_faults_major INT(20),
        page_faults_minor INT(20),
        swaps INT(20)
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    insert into cfg_profiling (query_id, sample_id, seq, state, duration, start_time, source_function, source_file, source_line, cpu_user, cpu_system, context_voluntary, context_involuntary, block_ops_in,
        block_ops_out, messages_sent, messages_received, page_faults_major, page_faults_minor, swaps)
    select QUERY_ID, sampleId, SEQ, STATE, DURATION, NOW(), SOURCE_FUNCTION, SOURCE_FILE, SOURCE_LINE, CPU_USER, CPU_SYSTEM, CONTEXT_VOLUNTARY, CONTEXT_INVOLUNTARY, BLOCK_OPS_IN,
        BLOCK_OPS_OUT, MESSAGES_SENT, MESSAGES_RECEIVED, PAGE_FAULTS_MAJOR, PAGE_FAULTS_MINOR, SWAPS
    from information_schema.profiling;
    */

    CALL debug_log('tx_export_sp', 'Finished createExportTables_XXX');
END $$
/* 47s */

/*
  ------------------------------------------------------------------------------
  Repeat hdrValue numCols times, separated by commas, and set as the value for
  columnName in tx_headers_XXX.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `setTxHeaders_XXX` $$
CREATE PROCEDURE `setTxHeaders_XXX`(columnName text, hdrValue text, numCols int)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('tx_export_sp', 'Starting setTxHeaders_XXX');
    call exec(concat("update tx_headers_XXX set ", columnName, " = '", repeatCols(hdrValue, numCols), "'"));
        CALL debug_log('tx_export_sp', 'Finished setTxHeaders_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Creation and population of tx_headers_XXX table
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `tx_headers_XXX` $$
CREATE PROCEDURE `tx_headers_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare tmp_group_concat_max_len bigint default 0;
    declare maxHeightId bigint default 0;
    declare maxHeight int default 0;
    declare maxCondition int default 0;
    declare maxCFsId bigint default 0;
    declare maxCFs int default 0;
    declare maxLevels int default 0;
    declare maxCFFieldNames text default "";
    DECLARE maxLevelTitle TEXT DEFAULT "";
    DECLARE levelTitleHdr TEXT DEFAULT "";
    DECLARE delimCount int DEFAULT 0;

    CALL debug_log('tx_export_sp', concat(get_version_tx_export_sp_XXX(), ' Starting tx_headers_XXX'));

    select @@group_concat_max_len into tmp_group_concat_max_len;
    set group_concat_max_len = 65535;

    /* tx_headers_XXX will contain a single row containing all the transaction export column headings */
    drop table if exists tx_headers_XXX;
    create table tx_headers_XXX(tx_header_id bigint,
        row_num text,
        sample_name text,
        tx_id text,
        students text,
        session_tag text,
        transaction_time text,
        time_zone text,
        duration text,
        transaction_type_tool text,
        transaction_subtype_tool text,
        transaction_type_tutor text,
        transaction_subtype_tutor text,
        levels text,
        problem_name text,
        problem_view text,
        problem_start_time text,
        subgoal_name text,
        attempt_at_subgoal text,
        is_last_attempt text,
        outcome text,
        selections text,
        actions text,
        inputs text,
        feedback_text text,
        classification text,
        help_level text,
        total_num_hints text,
        conditions text,
        skills text,
        school_name text,
        class_name text,
        custom_fields text
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    /* insert all single column headers */
    insert into tx_headers_XXX (row_num, sample_name, tx_id, session_tag, transaction_time, time_zone, duration,
        transaction_type_tool, transaction_subtype_tool, transaction_type_tutor,
        transaction_subtype_tutor, outcome, help_level, total_num_hints,
        attempt_at_subgoal, is_last_attempt, school_name, class_name,
        problem_name, problem_view, problem_start_time,
        subgoal_name, feedback_text, classification)
    values
        ("Row", "Sample Name", "Transaction Id", "Session Id", "Time", "Time Zone", "Duration (sec)",
        "Student Response Type", "Student Response Subtype", "Tutor Response Type",
        "Tutor Response Subtype", "Outcome", "Help Level", "Total Num Hints",
        "Attempt At Step", "Is Last Attempt", "School", "Class",
        "Problem Name", "Problem View", "Problem Start Time",
        "Step Name", "Feedback Text", "Feedback Classification");

    /* create temporary table to cache frequent join of tutor_transaction and transaction_sample_map */
    drop table if exists temp_tt_tsm_XXX;
    create table temp_tt_tsm_XXX(row_id bigint auto_increment primary key,
        transaction_id            BIGINT      NOT NULL,
        guid                      CHAR(32),
        sample_id                 INT         NOT NULL,
        session_id                BIGINT      NOT NULL,
        transaction_time          DATETIME    NOT NULL,
        transaction_time_ms       INT,
        time_zone                 VARCHAR(50),
        transaction_type_tutor    VARCHAR(30),
        transaction_type_tool     VARCHAR(30),
        transaction_subtype_tutor VARCHAR(30),
        transaction_subtype_tool  VARCHAR(30),
        outcome                   VARCHAR(30),
        attempt_at_subgoal        INT,
        is_last_attempt           BOOLEAN,
        dataset_id                INT         NOT NULL,
        problem_id                BIGINT      NOT NULL,
        problem_event_id          BIGINT,
        subgoal_id                BIGINT,
        subgoal_attempt_id        BIGINT      NOT NULL,
        feedback_id               BIGINT,
        class_id                  BIGINT,
        school_id                 INT,
        help_level                SMALLINT,
        total_num_hints           SMALLINT,
        duration                  INT,
        prob_solving_sequence     INT,
        index(transaction_id),
        index(subgoal_attempt_id),
        index(sample_id),
        index(session_id),
        index(school_id),
        index(class_id),
        index(problem_id),
        index(feedback_id),
        index(subgoal_id)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    select tt.transaction_id, tt.guid, tsm.sample_id, session_id, transaction_time, transaction_time_ms, time_zone,
        transaction_type_tutor, transaction_type_tool, transaction_subtype_tutor, transaction_subtype_tool,
        outcome, attempt_at_subgoal, is_last_attempt, dataset_id,
        problem_id, problem_event_id, subgoal_id, subgoal_attempt_id,
        feedback_id, class_id, school_id, help_level, total_num_hints, duration, prob_solving_sequence
    from tutor_transaction tt
        join transaction_sample_map tsm using (transaction_id)
    where tsm.sample_id = sampleId;

    /* insert the correct number of headers for each variable column */
    /* (columns that always have the same header, but there can be multiple instances of that column) */
    CALL debug_log('tx_export_sp', 'Before group of tx_headers_XXX calls');
    call setTxHeaders_XXX("students", "Anon Student Id", 1);
    call setTxHeaders_XXX("selections", "Selection", maxSelectionCols_XXX(sampleId));
    call setTxHeaders_XXX("actions", "Action", maxActionCols_XXX(sampleId));
    call setTxHeaders_XXX("inputs", "Input", maxInputCols_XXX(sampleId));
    CALL debug_log('tx_export_sp', 'After group of tx_headers_XXX calls');

    /* drop conditions column from tx_headers_XXX if there are no conditions */
    select maxConditionCols_XXX(sampleId) into maxCondition;
    if (maxCondition > 0) then
        call setTxHeaders_XXX("conditions", "Condition Name\tCondition Type", maxCondition);
    end if;

    /*
     We need to build the dataset_level_export_XXX table to generate the level_title headers.
     select the hierarchy for each dataset level that appears in the sample
     concat the level name's for tx_export_XXX, and level titles for tx_headers_XXX
    */

    CALL debug_log('tx_export_sp', 'Before dataset_level_export_XXX creation');
    select maxDatasetLevels(sampleId) into maxLevels;

    drop table if exists dataset_level_export_XXX;
    create table dataset_level_export_XXX
        (dataset_level_id bigint primary key, levels text, level_titles text)
        ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        select distinct dataset_level_id,
                (select group_concat(concat(dl2.level_name) order by dl2.lft separator '\t')
                    from dataset_level dl2
                    where dl2.lft <= dl.lft and dl2.rgt >= dl.rgt
                    and dl.dataset_id = dl2.dataset_id
                    group by dl2.dataset_id) as levels,
            (select group_concat(distinct concat(
                                 if(dl2.level_title is not null,
                                    concat('Level (', capitalize(dl2.level_title), ')'),
                                    'Level (Default)')) order by dl2.lft separator '\t')
                from dataset_level dl2
                where dl2.lft <= dl.lft and dl2.rgt >= dl.rgt
                and dl.dataset_id = dl2.dataset_id
                group by dl2.dataset_id) as level_titles
            from dataset_level dl where dataset_id = datasetIdForSample(sampleId);

    /* get the id of the dataset level with the deepest hierarchy */
    select dataset_level_id,
        (select count(distinct dl2.dataset_level_id) from
                dataset_level dl2 where dl2.lft <= dl.lft and dl2.rgt >= dl.rgt
                and dl.dataset_id = dl2.dataset_id
                group by dl2.dataset_id) as height into maxHeightId, maxHeight
        from dataset_level dl where dataset_id = datasetIdForSample(sampleId)
        order by height desc limit 1;

    /* get the titles for the dataset_level with the deepest hierarchy */
    SELECT level_titles INTO maxLevelTitle
           FROM dataset_level_export_XXX dle WHERE dataset_level_id = maxHeightId;

    /* Determine if we need to add any other titles to header. This is true when
       we have more than one level hierarchy in the dataset. */
    SELECT CONCAT(maxLevelTitle, '\t',
                  GROUP_CONCAT(DISTINCT level_titles ORDER BY dl.lft DESC SEPARATOR '\t'))
                  INTO levelTitleHdr
           FROM dataset_level_export_XXX dle
           JOIN dataset_level dl USING (dataset_level_id)
           WHERE dataset_id = datasetIdForSample(sampleId)
           AND (dl.lft + 1) = dl.rgt
           AND LOCATE(level_titles, maxLevelTitle) = 0;

    /* now set the levels header value with above info */
    IF (levelTitleHdr IS NOT NULL) THEN
        SELECT levelTitleHdr INTO maxLevelTitle;
    END IF;

    /* count delimiters in maxLevelTitle to see if maxLevels is still correct */
    SELECT substr_count('\t', maxLevelTitle) INTO delimCount;
    IF ((delimCount + 1) != maxLevels) THEN
        SELECT (delimCount + 1) INTO maxLevels;
    END IF;

    update tx_headers_XXX hdrs set hdrs.levels = maxLevelTitle;

    UPDATE dataset_level_export_XXX dle JOIN dataset_level dl USING (dataset_level_id)
           SET dle.levels = padDatasetLevel(dle.level_titles, dle.levels,
                                            maxLevelTitle, '\t', maxLevels)
           WHERE dataset_id = datasetIdForSample(sampleId);

    CALL debug_log('tx_export_sp', 'After dataset_level_export_XXX creation');

    /* We need the max_skills_export_XXX table to generate the skills headers. */
    CALL debug_log('tx_export_sp', 'Before max_skills_export_XXX creation');
    CALL debug_log('tx_export_sp', @@group_concat_max_len);
    /* create a table to cache the maximum number of skills for each skill model in the sample */
    drop table if exists max_skills_export_XXX;
    create table max_skills_export_XXX (skill_model_id bigint primary key, max_skills int, headers text)
        ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        select distinct skill_model_id from temp_tt_tsm_XXX tttsm
            join transaction_skill_map using (transaction_id)
            join skill using (skill_id);
    update max_skills_export_XXX set max_skills = maxSkillsForModel_XXX(sampleId, skill_model_id); /* 7s */
    /* create the KC headers for each skill model based on the maximum number of skills */
    update max_skills_export_XXX join skill_model using (skill_model_id)
        set headers = repeatCols(concat('KC (', skill_model_name,
                                        ')\tKC Category (', skill_model_name, ')'),
                                 max_skills); /* 0.03s */
    if (maxSkillCols_XXX(sampleId) > 0) then
        update tx_headers_XXX set skills = (select group_concat(headers order by skill_model_id separator '\t')
             from max_skills_export_XXX); /* 0.03s */
    end if;
    CALL debug_log('tx_export_sp', 'After max_skills_export_XXX creation');

    /* We need to set the custom fields header from the transaction with the most CFs */

    CALL debug_log('tx_export_sp', 'Before custom_field_name_export_XXX creation');
    drop table if exists custom_field_name_export_XXX;
    create table custom_field_name_export_XXX
        (cf_export_id bigint auto_increment primary key, custom_field_name text,
        index(custom_field_name(5)))
        ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin

    select distinct custom_field_name from custom_field cf
    where cf.dataset_id = datasetIdForSample(sampleId);

    /* Find transaction with most custom fields */
    select count(*),
        group_concat(concat('CF (', custom_field_name, ')')
                     order by custom_field_name separator '\t')
        from custom_field_name_export_XXX
        into maxCFs, maxCFFieldNames;
    if (maxCFs > 0) then
        update tx_headers_XXX hdrs set custom_fields = maxCFFieldNames;
    end if;
    CALL debug_log('tx_export_sp', 'After custom_field_name_export_XXX creation');
    set group_concat_max_len = tmp_group_concat_max_len;
    CALL debug_log('tx_export_sp', 'Finished tx_headers_XXX');
END $$
/* 6.5s */

/*
  ------------------------------------------------------------------------------
  Generate student_export_XXX table with all students and sessions in sample.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `studentExport_XXX` $$
CREATE PROCEDURE `studentExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxStudents int default 0;
    CALL debug_log('tx_export_sp', 'Starting studentExport_XXX');

        create temporary table student_export_XXX (session_id bigint primary key, students text,
            index (students(10)))
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select distinct tttsm.session_id, anon_user_id as students
                from temp_tt_tsm_XXX tttsm
                join session using (session_id)
                join student using (student_id);

    CALL debug_log('tx_export_sp', 'Finished studentExport_XXX');
END $$
/* 16s */

/*
  ------------------------------------------------------------------------------
  Generate student_export_XXX table with only sessions for student studentId.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `studentExportWithStudent_XXX` $$
CREATE PROCEDURE `studentExportWithStudent_XXX`(sampleId bigint, studentId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxStudents int default 0;
    declare anonId text default "";
    CALL debug_log('tx_export_sp', 'Starting studentExportWithStudent_XXX');

        create temporary table student_export_XXX (session_id bigint primary key, students text)
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select distinct tttsm.session_id, anon_user_id as students
                from temp_tt_tsm_XXX tttsm
                join session using (session_id)
                join student using (student_id)
                where student.student_id = studentId;

    CALL debug_log('tx_export_sp', 'Finished studentExportWithStudent_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Generate student_export_XXX table with only the sessions for the batch of
  students starting at batchStart and containing at most batchSize students.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `studentExportWithBatch_XXX` $$
CREATE PROCEDURE `studentExportWithBatch_XXX`(sampleId bigint, batchSize bigint, batchStart bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxStudents int default 0;
    declare minAnonId text default "";
    CALL debug_log('tx_export_sp', 'Starting studentExportWithBatch_XXX');

    /* create a table containing only the student id's for this batch of students */
    /* we need to process the query as a string because of a MySQL bug that does not allow */
    /* variables as the values for limit and offset parameters */
    set @createBatchQuery = '
    create temporary table student_batch_export_XXX (student_id bigint primary key)
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        select distinct sess.student_id
            from temp_tt_tsm_XXX tttsm
            join session sess using (session_id)
            join student s on sess.student_id = s.student_id
            order by anon_user_id asc
            limit ?, ?;';
    prepare stmt from @createBatchQuery;
    set @offset = batchStart;
    set @lim = batchSize;
    execute stmt using @offset, @lim;
    deallocate prepare stmt;

    /* constrain sessions in student_export_XXX by joining to student_batch_export_XXX */
        create temporary table student_export_XXX (session_id bigint primary key, students text)
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select distinct tttsm.session_id, anon_user_id as students
                from temp_tt_tsm_XXX tttsm
                join session sess using (session_id)
                join student_batch_export_XXX sbe on sess.student_id = sbe.student_id
                join student s on sess.student_id = s.student_id;

    CALL debug_log('tx_export_sp', 'Finished studentExportWithBatch_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Fill in dataset level export columns.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `datasetLevelExport_XXX` $$
CREATE PROCEDURE `datasetLevelExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('tx_export_sp', 'Starting datasetLevelExport_XXX');
    /* update the tx_export_XXX levels column by joining transactions to dataset levels through problem */
    update tx_export_XXX tx join problem p on tx.problem_id = p.problem_id
        join dataset_level_export_XXX dle on p.dataset_level_id = dle.dataset_level_id
        set tx.levels = dle.levels;
        CALL debug_log('tx_export_sp', 'Finished datasetLevelExport_XXX');
END $$
/* 6s */

/*
  ------------------------------------------------------------------------------
  Fill in selection, action and input columns.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `attemptSAIExport_XXX` $$
CREATE PROCEDURE `attemptSAIExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxSelection, maxAction, maxInput int default 0;
    declare numNullSelections, numNullActions, numNullInputs int default 0;
    declare emptyPaddedString TEXT;
    CALL debug_log('tx_export_sp', 'Starting attemptSAIExport_XXX');

    select maxSelectionCols_XXX(sampleId), maxActionCols_XXX(sampleId), maxInputCols_XXX(sampleId) into
        maxSelection, maxAction, maxInput;
    /* selection */
    create temporary table selection_export_XXX (subgoal_attempt_id bigint primary key, selections text)
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        select tx.subgoal_attempt_id,
            delimPad(group_concat(distinct SAICellValue(selection, xml_id, type)
                                  order by selection separator '\t'),
                     '\t', maxSelection) as selections
            from tx_export_XXX tx
            join attempt_selection att_sel on tx.subgoal_attempt_id = att_sel.subgoal_attempt_id
            group by tx.subgoal_attempt_id;
    /* action */
    create temporary table action_export_XXX (subgoal_attempt_id bigint primary key, actions text)
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        select tx.subgoal_attempt_id,
            delimPad(group_concat(distinct SAICellValue(action, xml_id, type)
                                  order by action separator '\t'),
                     '\t', maxAction) as actions
            from tx_export_XXX tx
            join attempt_action att_act on tx.subgoal_attempt_id = att_act.subgoal_attempt_id
            group by tx.subgoal_attempt_id;
    /* input */
    create temporary table input_export_XXX (subgoal_attempt_id bigint primary key, inputs text)
        ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
        select tx.subgoal_attempt_id,
            delimPad(group_concat(distinct SAICellValue(input, xml_id, type)
                                  order by input separator '\t'), '\t', maxInput) as inputs
            from tx_export_XXX tx
            join attempt_input att_inp on tx.subgoal_attempt_id = att_inp.subgoal_attempt_id
            group by tx.subgoal_attempt_id;

    /* copy values to tx_export_XXX joining on subgoal_attempt_id */
    update tx_export_XXX tx join selection_export_XXX se on tx.subgoal_attempt_id = se.subgoal_attempt_id
        set tx.selections = se.selections;
    update tx_export_XXX tx join action_export_XXX ae on tx.subgoal_attempt_id = ae.subgoal_attempt_id
        set tx.actions = ae.actions;
    update tx_export_XXX tx join input_export_XXX ie on tx.subgoal_attempt_id = ie.subgoal_attempt_id
        set tx.inputs = ie.inputs;

    /* If any of the SAIs are null and the number of items is greater than 1,
       then the columns get out of whack. */
    SELECT count(*) INTO numNullSelections FROM tx_export_XXX WHERE selections IS NULL;
    SELECT count(*) INTO numNullActions    FROM tx_export_XXX WHERE actions IS NULL;
    SELECT count(*) INTO numNullInputs     FROM tx_export_XXX WHERE inputs IS NULL;

    IF numNullSelections > 0 THEN
        SELECT delimPad(null, '\t', maxSelection) INTO emptyPaddedString;
        UPDATE tx_export_XXX tx
            SET tx.selections = emptyPaddedString
            WHERE tx.selections IS NULL;
    END IF;
    IF numNullActions > 0 THEN
        SELECT delimPad(null, '\t', maxAction) INTO emptyPaddedString;
        UPDATE tx_export_XXX tx
            SET tx.actions = emptyPaddedString
            WHERE tx.actions IS NULL;
    END IF;
    IF numNullInputs > 0 THEN
        SELECT delimPad(null, '\t', maxInput) INTO emptyPaddedString;
        UPDATE tx_export_XXX tx
            SET tx.inputs = emptyPaddedString
            WHERE tx.inputs IS NULL;
    END IF;

    CALL debug_log('tx_export_sp', 'Finished attemptSAIExport_XXX');
END $$
/* 45s */

/*
  ------------------------------------------------------------------------------
  Fill in condition export columns.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `conditionExport_XXX` $$
CREATE PROCEDURE `conditionExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxCondition int default 0;
    CALL debug_log('tx_export_sp', 'Starting conditionExport_XXX');

    select maxConditionCols_XXX(sampleId) into maxCondition;
    if (maxCondition > 0) then
        /* concatenate condition_name and type columns for each condition */
        /* joining conditions to transactions through transaction_condition_map */
        create temporary table condition_export_XXX (transaction_id bigint primary key, conditions text)
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select tx.transaction_id,
                delimPad(group_concat(distinct concat(ifnull(condition_name, ''),
                                      '\t', ifnull(type, ''))
                                      order by type, condition_name separator '\t'),
                         '\t', maxCondition * 2) as conditions
                from tx_export_XXX tx
                join transaction_condition_map tcm on tx.transaction_id = tcm.transaction_id
                join ds_condition c on tcm.condition_id = c.condition_id group by tx.transaction_id;
        update tx_export_XXX tx join condition_export_XXX ce on ce.transaction_id = tx.transaction_id
            set tx.conditions = ce.conditions;
        update tx_export_XXX set conditions = delimPad('', '\t', maxCondition * 2) where conditions is null;
    end if;
    CALL debug_log('tx_export_sp', 'Finished conditionExport_XXX');
END $$
/* 37s */

/*
  ------------------------------------------------------------------------------
  Fill in skill export columns.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `skillExport_XXX` $$
CREATE PROCEDURE `skillExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('tx_export_sp', 'Starting skillExport_XXX');
    if (maxSkillCols_XXX(sampleId) > 0) then
        /* concatenate skills and pad to max number of skills for each skill model */
        create temporary table skill_model_export_XXX (transaction_id bigint,
            skill_model_id bigint,
            skills text,
            index (transaction_id, skill_model_id))
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select tx.transaction_id, skm.skill_model_id,
                delimPad(group_concat(concat(ifnull(skill_name, ''), '\t', ifnull(category, ''))
                                      order by skm.skill_model_id, skill_name separator '\t'),
                         '\t', max_skills * 2) as skills
                from tx_export_XXX tx
                join transaction_skill_map tsm on tsm.transaction_id = tx.transaction_id
                join skill sk on sk.skill_id = tsm.skill_id
                join skill_model skm on sk.skill_model_id = skm.skill_model_id
                join max_skills_export_XXX mse on skm.skill_model_id = mse.skill_model_id
                group by tx.transaction_id, skm.skill_model_id; /* 14s */
        /* add rows for transaction/skill model with no assigned skills */
        insert into skill_model_export_XXX
            select transaction_id, skill_model_id, delimPad('', '\t', max_skills * 2)
                from tx_export_XXX tx, max_skills_export_XXX mse
                where not exists
                    (select transaction_id, skill_model_id from transaction_skill_map tsm
                        join skill sk using (skill_id)
                        where sk.skill_model_id = mse.skill_model_id
                        and tsm.transaction_id = tx.transaction_id); /* 22s */
        /* concatenate skills for all skill models to get skill columns for each transaction */
        create temporary table skill_export_XXX (transaction_id bigint primary key, skills text)
            ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
            select transaction_id, group_concat(skills separator '\t') as skills
                from skill_model_export_XXX
                group by transaction_id order by skill_model_id; /* 8s */
        update tx_export_XXX tx join skill_export_XXX sk using (transaction_id) set tx.skills = sk.skills;
    end if;
    CALL debug_log('tx_export_sp', 'Finished skillExport_XXX');
END $$
/* 69s total */

/*
  ------------------------------------------------------------------------------
  Check if this dataset has any custom fields.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `hasCustomFields_XXX` $$
CREATE FUNCTION `hasCustomFields_XXX`()
    RETURNS BOOLEAN READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare hasCFs BOOLEAN default FALSE;

    SELECT IF(custom_fields IS NULL, FALSE, TRUE) INTO hasCFs
        FROM tx_headers_XXX;

    return hasCFs;
END $$

/*
  ------------------------------------------------------------------------------
  Fill in custom field export columns.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `customFieldExport_XXX` $$
CREATE PROCEDURE `customFieldExport_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare maxCFs int default 0;
    declare hasCFs BOOLEAN default FALSE;
    set session group_concat_max_len = @@max_allowed_packet ;
    CALL debug_log('tx_export_sp', 'Starting customFieldExport_XXX');

    set hasCFs = hasCustomFields_XXX();
    IF (hasCFs) THEN
        /* find transaction with most custom fields */
        select count(*)
        from custom_field_name_export_XXX
        into maxCFs;

        if (maxCFs > 0) then
            /* select custom field names (for header column) and corresponding custom field values */
            create temporary table custom_field_value_export_XXX
                (transaction_id bigint, custom_field_name text, custom_field_value text,
                index(transaction_id, custom_field_name(10)))
                ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin
                select tx.transaction_id, cfne.custom_field_name, null
                    from custom_field_name_export_XXX cfne
                    join tx_export_XXX tx;
            /*
             by creating table then updating, we handle the case where one transaction has more
             than one custom field with the same custom field name
            */
      update custom_field_value_export_XXX cfv
                left join cf_tx_level cflevel using (transaction_id)
          join custom_field cf on cf.custom_field_id = cflevel.custom_field_id
                              and cf.custom_field_name = cfv.custom_field_name
                set cfv.custom_field_value = cleanCFValue(ifnull(cflevel.value, ''))
                where cflevel.big_value is NULL;

      update custom_field_value_export_XXX cfv
                left join cf_tx_level cflevel using (transaction_id)
          join custom_field cf on cf.custom_field_id = cflevel.custom_field_id
                              and cf.custom_field_name = cfv.custom_field_name
                set cfv.custom_field_value = cleanCFValue(LEFT(cflevel.big_value, 65000))
                where cflevel.big_value is not NULL;

            create temporary table custom_field_export_XXX
                (transaction_id bigint primary key, custom_fields longtext)
                ENGINE=MYISAM CHARACTER SET utf8 COLLATE utf8_bin
                select transaction_id,
                    group_concat(ifnull(uncompress(compress(custom_field_value)), '')
                        order by custom_field_name separator '\t') as custom_fields
                    from custom_field_value_export_XXX
                    group by transaction_id;

            /* update custom field values in tx_export_XXX */
            update tx_export_XXX tx join custom_field_export_XXX cfe using (transaction_id)
                set tx.custom_fields = cfe.custom_fields;
        end if;
        CALL debug_log('tx_export_sp', 'Finished customFieldExport_XXX with CFs');
    ELSE
        CALL debug_log('tx_export_sp', 'Finished customFieldExport_XXX with NO CFs');
    END IF;
END $$
/* 20s */

/*
  ------------------------------------------------------------------------------
  Drop the temporary tables populated with each new batch, to only contain the
  current chunk of transactions.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `drop_temporary_export_tables_XXX` $$
CREATE PROCEDURE `drop_temporary_export_tables_XXX`()
    SQL SECURITY INVOKER
BEGIN
    call debug_log('tx_export_sp', 'Starting drop_temporary_export_tables_XXX');

    drop temporary table if exists tx_export_XXX, student_export_XXX, session_export_XXX, student_batch_export_XXX,
       selection_export_XXX, action_export_XXX, input_export_XXX, condition_export_XXX, skill_model_export_XXX,
       skill_export_XXX, custom_field_value_export_XXX, custom_field_export_XXX;

    call debug_log('tx_export_sp', 'Finished drop_temporary_export_tables_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Private method called from both tx_export_XXX and tx_export_with_student.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `tx_export_core_XXX` $$
CREATE PROCEDURE `tx_export_core_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log('tx_export_sp', 'Starting tx_export_core_XXX');
    call createExportTables_XXX(sampleId);
    call datasetLevelExport_XXX(sampleId);
    call attemptSAIExport_XXX(sampleId);
    call conditionExport_XXX(sampleId);
    call skillExport_XXX(sampleId);
    call customFieldExport_XXX(sampleId);
    CALL debug_log('tx_export_sp', 'Finished tx_export_core_XXX');
END $$
/* 298s */

/*
  ------------------------------------------------------------------------------
  Perform the entire transaction export for this sample.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `tx_export_XXX` $$
CREATE PROCEDURE `tx_export_XXX`(sampleId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare tmp_group_concat_max_len bigint default 0;
    call debug_log('tx_export_sp', 'Starting tx_export_XXX');

    select @@group_concat_max_len into tmp_group_concat_max_len;
    set group_concat_max_len = 65535;
    call debug_log('tx_export_sp student export', @@group_concat_max_len);
    call studentExport_XXX(sampleId);
    call tx_export_core_XXX(sampleId);
    set group_concat_max_len = tmp_group_concat_max_len;
    call debug_log('tx_export_sp', 'Finished tx_export_XXX');
END $$
/* 298s */

/*
  ------------------------------------------------------------------------------
  Perform the transaction export for this sample and student
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `tx_export_with_student_XXX` $$
CREATE PROCEDURE `tx_export_with_student_XXX`(sampleId bigint, studentId bigint)
    SQL SECURITY INVOKER
BEGIN
    declare tmp_group_concat_max_len bigint default 0;
    call debug_log('tx_export_sp', 'Starting tx_export_with_student_XXX');

    select @@group_concat_max_len into tmp_group_concat_max_len;
    set group_concat_max_len = 65535;
    call debug_log('tx_export_sp student', @@group_concat_max_len);
    call studentExportWithStudent_XXX(sampleId, studentId);
    call tx_export_core_XXX(sampleId);
    set group_concat_max_len = tmp_group_concat_max_len;
    call debug_log('tx_export_sp', 'Finished tx_export_with_student_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Perform the transaction export for this sample and the batch of
  students defined by batchSize and batchStart.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `tx_export_with_student_batch_XXX` $$
CREATE PROCEDURE `tx_export_with_student_batch_XXX`(sampleId bigint, batchSize bigint, batchStart bigint)
    SQL SECURITY INVOKER
BEGIN
    declare tmp_group_concat_max_len bigint default 0;
    call debug_log('tx_export_sp', 'Starting tx_export_with_student_batch_XXX');

    select @@group_concat_max_len into tmp_group_concat_max_len;
    set group_concat_max_len = 65535;
    call drop_temporary_export_tables_XXX();
    call debug_log('tx_export_sp student batch', @@group_concat_max_len);
    call studentExportWithBatch_XXX(sampleId, batchSize, batchStart);
    call tx_export_core_XXX(sampleId);
    set group_concat_max_len = tmp_group_concat_max_len;
    call debug_log('tx_export_sp', 'Finished tx_export_with_student_batch_XXX');
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of selection export columns for sampleId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxSelectionCols_XXX` $$
CREATE FUNCTION `maxSelectionCols_XXX`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;

    select getSampleMetric(sampleId, 'Max Selections') into maxCols;
    if maxCols = 0 then
        select count(*) into maxCols from temp_tt_tsm_XXX tttsm
            join attempt_selection selection using (subgoal_attempt_id)
            group by selection.subgoal_attempt_id, tttsm.transaction_id
            order by count(*) desc limit 1;
        call updateSampleMetric(sampleId, 'Max Selections', maxCols);
    end if;
    return maxCols;
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of action export columns for sampleId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxActionCols_XXX` $$
CREATE FUNCTION `maxActionCols_XXX`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;

    select getSampleMetric(sampleId, 'Max Actions') into maxCols;
    if maxCols = 0 then
        select count(*) into maxCols from temp_tt_tsm_XXX tttsm
            join attempt_action action using (subgoal_attempt_id)
            group by action.subgoal_attempt_id, tttsm.transaction_id
            order by count(*) desc limit 1;
        call updateSampleMetric(sampleId, 'Max Actions', maxCols);
    end if;
    return maxCols;
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of input export columns for sampleId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxInputCols_XXX` $$
CREATE FUNCTION `maxInputCols_XXX`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;

    select getSampleMetric(sampleId, 'Max Inputs') into maxCols;
    if maxCols = 0 then
        select count(*) into maxCols from temp_tt_tsm_XXX tttsm
            join attempt_input input using (subgoal_attempt_id)
            group by input.subgoal_attempt_id, tttsm.transaction_id
            order by count(*) desc limit 1;
        call updateSampleMetric(sampleId, 'Max Inputs', maxCols);
    end if;
    return maxCols;
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of conditions for sampleId.
  FIXME does this need to be XXX'ed?
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxConditionCols_XXX` $$
CREATE FUNCTION `maxConditionCols_XXX`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;

    select getSampleMetric(sampleId, 'Max Conditions') into maxCols;
    if maxCols = 0 then
        select count(tcm.condition_id) as count into maxCols from temp_tt_tsm_XXX tttsm
            join transaction_condition_map tcm using (transaction_id)
            group by tcm.transaction_id order by count desc limit 1;

        call updateSampleMetric(sampleId, 'Max Conditions', maxCols);
    end if;
    return maxCols;
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of skills for sampleId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxSkillCols_XXX` $$
CREATE FUNCTION `maxSkillCols_XXX`(sampleId bigint)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;
    select count(skm.skill_id) as count into maxCols from temp_tt_tsm_XXX tttsm
        join transaction_skill_map skm using (transaction_id)
        group by skm.transaction_id order by count desc limit 1;

    return maxCols;
END $$

/*
  ------------------------------------------------------------------------------
  Fetch or calculate the maximum number of skills for sampleId and skillModelId.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `maxSkillsForModel_XXX` $$
CREATE FUNCTION `maxSkillsForModel_XXX`(sampleId bigint, skillModelId int)
    RETURNS int(11) READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    declare maxCols int default 0;

    select getSampleMetricWithSkillModel(sampleId, 'Max Skills', skillModelId) into maxCols;
    if maxCols = 0 then
        select count(skm.skill_id) as count into maxCols from temp_tt_tsm_XXX tttsm
            join transaction_skill_map skm using (transaction_id)
            join skill sk using (skill_id)
            where sk.skill_model_id = skillModelId
            group by skm.transaction_id order by count desc limit 1;
       call updateSampleMetricWithSkillModel(sampleId, skillModelId, 'Max Skills', maxCols);
    end if;
    return maxCols;
END $$



DELIMITER ;
