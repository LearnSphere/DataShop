/*
   Carnegie Mellon University, Human Computer Interaction Institute
   Copyright 2005-2007
   All Rights Reserved

   $Revision: 12404 $
   Last modified by - $Author: ctipper $
   Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
   $KeyWordsOff: $

   Methods useful for the step export.

   PUBLIC METHODS:
   kc_columns(skill_model_name) - generate student-step header columns for the skill model name
*/

/*
  ------------------------------------------------------------------------------
  Get the CVS version information.
  ------------------------------------------------------------------------------
*/
DELIMITER $$

DROP FUNCTION IF EXISTS `get_version_step_export_sp_XXX` $$
CREATE FUNCTION `get_version_step_export_sp_XXX` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 12404 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/*
  ------------------------------------------------------------------------------
  Generate student-step header columns for the skill model name.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `kc_columns` $$
CREATE FUNCTION `kc_columns`(skill_model_name text) RETURNS tinytext CHARSET utf8
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE kcs TINYTEXT;
    SELECT concat('KC (', skill_model_name, ')\t', 'Opportunity (', skill_model_name, ')\t',
                  'Predicted Error Rate (', skill_model_name, ')') INTO kcs;
    RETURN kcs;
END $$

/*
  ------------------------------------------------------------------------------
  Query used by StepRollupDao.getStepRollupItems(), called during population
  of page grid for the 'Student Step' tab of the Export page.
  Had to make this a stored procedure so that we can temporarily increase
  the size of group_concat_max_len.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `get_step_rollup_items` $$
CREATE PROCEDURE `get_step_rollup_items` (IN theSelect TEXT, IN tableName VARCHAR(250))
       SQL SECURITY INVOKER
BEGIN
    DECLARE tmp_group_concat_max_len BIGINT DEFAULT 0;
    DECLARE query TEXT;

    CALL debug_log('step_export_sp', CONCAT('get_step_rollup_items starting: ', tableName));

    SELECT @@group_concat_max_len INTO tmp_group_concat_max_len;
    SET group_concat_max_len = 65535;

    -- create temporary table which will hold results
    SET query = CONCAT("DROP TABLE IF EXISTS ", tableName);
    CALL exec(query);

    SET query = CONCAT("CREATE TABLE ", tableName, " (",
                       "step_rollup_id BIGINT, ",
                       "skill_models TEXT, ",
                       "skills TEXT, ",
                       "opportunities TEXT, ",
                       "predicted_error_rates TEXT, ",
                       "PRIMARY KEY(step_rollup_id)",
                       ") ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin");
    CALL exec(query);

    -- write results of query into temporary table
    SET query = CONCAT("INSERT INTO ", tableName, " (", theSelect, ")");
    CALL exec(query);

    SET group_concat_max_len = tmp_group_concat_max_len;

    CALL debug_log('step_export_sp', 'get_step_rollup_items finished');

END $$

/*
  ------------------------------------------------------------------------------
  Query used by StepRollupDao.getStepRollupRows(), called during 'Student Step
  Export' portion of CFG.
  Had to make this a stored procedure so that we can temporarily increase
  the size of group_concat_max_len.
  ------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `get_step_rollup_rows` $$
CREATE PROCEDURE `get_step_rollup_rows` (IN theSelect TEXT, IN tableName VARCHAR(250))
       SQL SECURITY INVOKER
BEGIN
    DECLARE tmp_group_concat_max_len BIGINT DEFAULT 0;
    DECLARE query TEXT;

    CALL debug_log('step_export_sp', CONCAT('get_step_rollup_rows starting: ', tableName));

    SELECT @@group_concat_max_len INTO tmp_group_concat_max_len;
    SET group_concat_max_len = 65535;

    -- create temporary table which will hold results
    SET query = CONCAT("DROP TABLE IF EXISTS ", tableName);
    CALL exec(query);

    SET query = CONCAT("CREATE TABLE ", tableName, " (",
                       "problem_id BIGINT, ",
                       "sample_name VARCHAR(100), ",
                       "anon_user_id VARCHAR(55), ",
                       "problem_name VARCHAR(255), ",
                       "problem_view INT, ",
                       "subgoal_name TEXT, ",
                       "step_time DATETIME, ",
                       "step_start_time DATETIME, ",
                       "first_transaction_time DATETIME, ",
                       "correct_transaction_time DATETIME, ",
                       "step_end_time DATETIME, ",
                       "step_duration BIGINT, ",
                       "correct_step_duration BIGINT, ",
                       "error_step_duration BIGINT, ",
                       "first_attempt VARCHAR(5), ",
                       "total_incorrects INT, ",
                       "total_hints INT, ",
                       "total_corrects INT, ",
                       "conditions TEXT, ",
                       "skill_models TEXT, ",
                       "skills TEXT, ",
                       "opportunities TEXT, ",
                       "predicted_error_rates TEXT",
                       ") ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin");
    CALL exec(query);

    -- write results of query into temporary table
    SET query = CONCAT("INSERT INTO ", tableName, " (", theSelect, ")");
    CALL exec(query);

    SET group_concat_max_len = tmp_group_concat_max_len;

    CALL debug_log('step_export_sp', 'get_step_rollup_rows finished');

END $$

DELIMITER ;
