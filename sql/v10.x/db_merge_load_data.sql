/*
  -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2013
  All Rights Reserved

  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $


  Merges the transaction tables during the db merge process from adb_source to
  The 'Load Data' from file cannot legally be executed from a stored procedure so we must invoke
  this .sql file separately.
  ------------------------------------------------------------------------------------------------------
*/

SET foreign_key_checks = 0;
SET unique_checks = 0;

/*
   Write updated tutor transaction data to file, file cannot exist and is stored in mysql data/database directory.
   The transaction_id auto_increment is written out as NULL, which mysql assigns on insert.
*/
SELECT sess.session_id, tt.guid, tt.transaction_time, tt.transaction_time_ms, tt.time_zone,
    tt.transaction_type_tutor, tt.transaction_type_tool, tt.transaction_subtype_tutor,
    tt.transaction_subtype_tool, tt.outcome, tt.attempt_at_subgoal, tt.is_last_attempt,
    tt.dataset_id,
    pr.problem_id, sub.subgoal_id, subatt.subgoal_attempt_id, fee.feedback_id, cl.class_id,
    sch.school_id, tt.help_level, tt.total_num_hints, tt.duration, tt.prob_solving_sequence,
    tt.transaction_id, dpeim.problem_event_id
FROM dbm_src_tt_view tt
JOIN dbm_session_id_map sess ON tt.session_id = sess.src_session_id
JOIN dbm_subgoal_att_id_map subatt ON tt.subgoal_attempt_id = subatt.src_subgoal_att_id
JOIN dbm_problem_id_map pr ON tt.problem_id = pr.src_problem_id
LEFT JOIN dbm_problem_event_id_map dpeim ON tt.problem_event_id = dpeim.src_problem_event_id
LEFT JOIN dbm_subgoal_id_map sub ON tt.subgoal_id = sub.src_subgoal_id
LEFT JOIN dbm_feedback_id_map fee ON tt.feedback_id = fee.src_feedback_id
LEFT JOIN dbm_class_id_map cl ON tt.class_id = cl.src_class_id
LEFT JOIN dbm_school_id_map sch ON tt.school_id = sch.src_school_id
INTO OUTFILE './merge_files/tt_merge.txt'
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

LOAD DATA INFILE './merge_files/tt_merge.txt'
INTO TABLE tutor_transaction
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n'
    (session_id, guid, transaction_time, transaction_time_ms, time_zone, transaction_type_tutor,
    transaction_type_tool, transaction_subtype_tutor, transaction_subtype_tool, outcome,
    attempt_at_subgoal, is_last_attempt, dataset_id, problem_id, subgoal_id, subgoal_attempt_id,
    feedback_id,
    class_id, school_id, help_level, total_num_hints, duration, prob_solving_sequence,
    src_transaction_id, problem_event_id);

/*
   Create a mapping of inserted transaction_ids with their old carnegie learning ids, rather than index
   src_transaction_id column in the tutor_transaction table.
*/
DROP TABLE IF EXISTS dbm_transaction_id_map;
CREATE TABLE dbm_transaction_id_map (src_transaction_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    dataset_id INT NOT NULL,
    PRIMARY KEY (src_transaction_id, transaction_id),
    INDEX (dataset_id)
) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
SELECT src_transaction_id, transaction_id, tt.dataset_id
FROM tutor_transaction tt
JOIN dbm_ds_id_map dim ON dim.dataset_id = tt.dataset_id
WHERE src_transaction_id IS NOT NULL;

REPLACE INTO dbm_max_table_counts (dataset_id, table_name, max_src_pk, row_count, prev_count)
SELECT dim.dataset_id, 'tutor_transaction', ifnull(max(src_transaction_id), 0),
    count(src_transaction_id), 0
FROM dbm_ds_id_map dim
LEFT JOIN dbm_transaction_id_map tim USING (dataset_id)
WHERE dim.merge_flag = FALSE
GROUP BY dataset_id;

UPDATE tutor_transaction tt JOIN dbm_transaction_id_map tim ON tt.transaction_id = tim.transaction_id
SET tt.src_transaction_id = NULL WHERE tt.src_transaction_id IS NOT NULL;

/* Write transaction_condition_map data to an outfile that must not exist. */
SELECT tim.transaction_id, dsc.condition_id
FROM adb_source.transaction_condition_map tcm
JOIN dbm_transaction_id_map tim ON tcm.transaction_id = tim.src_transaction_id
JOIN dbm_ds_condition_id_map dsc ON tcm.condition_id = dsc.src_condition_id
ORDER BY tim.transaction_id, dsc.condition_id
INTO OUTFILE './merge_files/tcm_merge.txt'
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

LOAD DATA INFILE './merge_files/tcm_merge.txt'
INTO TABLE transaction_condition_map
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

/* Write transaction_skill_map data to an outfile that must not exist. */
SELECT tim.transaction_id, skim.skill_id
FROM adb_source.transaction_skill_map tsm
JOIN dbm_transaction_id_map tim ON tsm.transaction_id = tim.src_transaction_id
JOIN dbm_skill_id_map skim ON tsm.skill_id = skim.src_skill_id
ORDER BY tim.transaction_id, skim.skill_id
INTO OUTFILE './merge_files/tskm_merge.txt'
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

LOAD DATA INFILE './merge_files/tskm_merge.txt'
INTO TABLE transaction_skill_map
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

/* Write transaction_skill_event data to an outfile that must not exist. */
SELECT tim.transaction_id, skim.skill_id, initial_p_known, resulting_p_known
FROM adb_source.transaction_skill_event tse
JOIN dbm_transaction_id_map tim ON tim.src_transaction_id = tse.transaction_id
JOIN dbm_skill_id_map skim ON skim.src_skill_id = tse.skill_id
ORDER BY tim.transaction_id, skim.skill_id
INTO OUTFILE './merge_files/tse_merge.txt'
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

LOAD DATA INFILE './merge_files/tse_merge.txt'
INTO TABLE transaction_skill_event
FIELDS TERMINATED BY '~~!'
LINES TERMINATED BY '\n';

/* Copy into custom field table now that new transactions are present */
INSERT INTO custom_field (custom_field_name, description, dataset_id, level, owner)
    SELECT REPLACE(REPLACE(REPLACE(custom_field_name, '\t', ' '), '\r', ' '), '\n', ' ') as custom_field_name,
    description, dim.dataset_id, level, IFNULL(owner, 'system')
FROM adb_source.custom_field cf
JOIN dbm_ds_id_map dim ON cf.dataset_id = dim.src_dataset_id
WHERE dim.merge_flag = FALSE;

/* Copy into cf_tx_level table now that new transactions are present */
INSERT INTO cf_tx_level (custom_field_id, transaction_id, type, value, big_value, logging_flag)
SELECT cf.custom_field_id, tim.transaction_id, srccftxlevel.type,
    REPLACE(REPLACE(REPLACE(srccftxlevel.value, '\t', ' '), '\r', ' '), '\n', ' ') as value,
    REPLACE(REPLACE(REPLACE(srccftxlevel.big_value, '\t', ' '), '\r', ' '), '\n', ' ') as big_value,
    srccftxlevel.logging_flag
FROM custom_field cf
JOIN dbm_ds_id_map dim ON cf.dataset_id = dim.dataset_id
Join adb_source.custom_field srccf on cf.custom_field_name = srccf.custom_field_name and srccf.dataset_id = dim.src_dataset_id
join adb_source.cf_tx_level srccftxlevel on srccftxlevel.custom_field_id = srccf.custom_field_id
JOIN dbm_transaction_id_map tim ON srccftxlevel.transaction_id = tim.src_transaction_id
WHERE dim.merge_flag = FALSE;

SET unique_checks = 1;
SET foreign_key_checks = 1;
