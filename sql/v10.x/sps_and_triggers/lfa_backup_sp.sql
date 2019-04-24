USE analysis_db;

DELIMITER $$

/**
 * Performs a backup of data that could be affected when running LFA.
 * The results are stored in the analysis_versions database.
 * @param skill_model_ids The ids of the skill models to be backed up.
*/
DROP PROCEDURE IF EXISTS `lfa_backup_skill_models` $$
CREATE PROCEDURE         `lfa_backup_skill_models` (skill_model_ids TEXT)
    SQL SECURITY INVOKER
LFA_BACKUP_SKILL_MODELS:BEGIN
    DECLARE EXIT HANDLER FOR SQLException
    BEGIN
        ROLLBACK;
        CALL raise_error('SQLException while executing lfa_backup_skill_models.');
    END;
    
    CALL debug_log('lfa_backup_skill_models_sp', 'Starting lfa_backup_skill_models.');
    
    -- Ensure the query won't be truncated when a large number of ids are used
    SET @@session.group_concat_max_len = @@global.max_allowed_packet;
    
    -- Continue only if skill model ids are present
    IF skill_model_ids IS NULL OR CHAR_LENGTH(skill_model_ids) = 0 THEN
        CALL debug_log('lfa_backup_skill_models_sp', 'No skill model ids provided.');
        LEAVE LFA_BACKUP_SKILL_MODELS;
    END IF;
    
    START TRANSACTION;

    -- Copy the latest version record
    INSERT INTO analysis_versions.datashop_version (version, version_time, backup_time)
        SELECT src_dv.version, src_dv.time, SYSDATE()
            FROM analysis_db.datashop_version src_dv;

    -- Retrieve the new version_id
    SET @version_id = LAST_INSERT_ID();

    -- Define a query to backup the skill_model table
    SELECT GROUP_CONCAT(query_body SEPARATOR '') FROM (SELECT '
        INSERT INTO analysis_versions.skill_model
            (version_id, skill_model_id, skill_model_name, aic, bic, intercept, log_likelihood,
             owner, global_flag, dataset_id, allow_lfa_flag, status, lfa_status, lfa_status_description, source,
             mapping_type, creation_time, modified_time, num_observations, cv_unstratified_rmse,
             cv_student_stratified_rmse, cv_step_stratified_rmse, cv_status, cv_status_description,
             cv_unstratified_num_observations, cv_unstratified_num_parameters, num_skills,
             src_skill_model_id)
            SELECT @version_id, src_sm.skill_model_id, src_sm.skill_model_name,
                   src_sm.aic, src_sm.bic, src_sm.intercept, src_sm.log_likelihood, src_sm.owner,
                   src_sm.global_flag, src_sm.dataset_id, src_sm.allow_lfa_flag, src_sm.status,
                   src_sm.lfa_status, src_sm.lfa_status_description, src_sm.source, src_sm.mapping_type, src_sm.creation_time,
                   src_sm.modified_time, src_sm.num_observations, src_sm.cv_unstratified_rmse,
                   src_sm.cv_student_stratified_rmse, src_sm.cv_step_stratified_rmse,
                   src_sm.cv_status, src_sm.cv_status_description, src_sm.cv_unstratified_num_observations,
                   src_sm.cv_unstratified_num_parameters, src_sm.num_skills,
                   src_sm.src_skill_model_id
                FROM analysis_db.skill_model src_sm
                WHERE src_sm.skill_model_id IN ('
        AS query_body UNION SELECT skill_model_ids UNION SELECT ')') AS full_query_text
        INTO @skill_model_stmt_text;

    -- Execute the query to backup the skill_model table
    PREPARE skill_model_stmt FROM @skill_model_stmt_text;
    EXECUTE skill_model_stmt;
    DEALLOCATE PREPARE skill_model_stmt;

    -- Define a query to backup the skill table
    SELECT GROUP_CONCAT(query_body SEPARATOR '') FROM (SELECT '
        INSERT INTO analysis_versions.skill
            (version_id, skill_id, skill_name, category, skill_model_id, beta, gamma, src_skill_id)
            SELECT @version_id, src_s.skill_id, src_s.skill_name, src_s.category,
                   src_s.skill_model_id, src_s.beta, src_s.gamma, src_s.src_skill_id
                FROM analysis_db.skill src_s
                WHERE src_s.skill_model_id IN ('
        AS query_body UNION SELECT skill_model_ids UNION SELECT ')') AS full_query_text
        INTO @skill_stmt_text;

    
    -- Execute the query to backup the skill table
    PREPARE skill_stmt FROM @skill_stmt_text;
    EXECUTE skill_stmt;
    DEALLOCATE PREPARE skill_stmt;
    
    -- Define a query to backup the alpha_score table
    SELECT GROUP_CONCAT(query_body SEPARATOR '') FROM (SELECT '
        INSERT INTO analysis_versions.alpha_score
            (version_id, student_id, skill_model_id, alpha)
            SELECT @version_id, src_as.student_id, src_as.skill_model_id, src_as.alpha
                FROM analysis_db.alpha_score src_as
                WHERE src_as.skill_model_id IN ('
        AS query_body UNION SELECT skill_model_ids UNION SELECT ')') AS full_query_text
        INTO @alpha_score_stmt_text;
    
    -- Execute the query to backup the alpha_score table
    PREPARE alpha_score_stmt FROM @alpha_score_stmt_text;
    EXECUTE alpha_score_stmt;
    DEALLOCATE PREPARE alpha_score_stmt;
    
    -- Define a query to backup the step_rollup table
    SELECT GROUP_CONCAT(query_body SEPARATOR '') FROM (SELECT '
        INSERT INTO analysis_versions.step_rollup
            (version_id, step_rollup_id, sample_id, student_id, step_id, skill_id, opportunity,
             skill_model_id, dataset_id, problem_id, problem_view, total_hints, total_incorrects,
             total_corrects, first_attempt, conditions, step_time, first_transaction_time,
             step_start_time, step_end_time, correct_transaction_time, predicted_error_rate,
             step_duration, correct_step_duration, error_step_duration, error_rate)
            SELECT @version_id, src_sr.step_rollup_id, src_sr.sample_id, src_sr.student_id,
                   src_sr.step_id, src_sr.skill_id, src_sr.opportunity, src_sr.skill_model_id,
                   src_sr.dataset_id, src_sr.problem_id, src_sr.problem_view, src_sr.total_hints,
                   src_sr.total_incorrects, src_sr.total_corrects, src_sr.first_attempt,
                   src_sr.conditions, src_sr.step_time, src_sr.first_transaction_time,
                   src_sr.step_start_time, src_sr.step_end_time, src_sr.correct_transaction_time,
                   src_sr.predicted_error_rate, src_sr.step_duration, src_sr.correct_step_duration,
                   src_sr.error_step_duration, src_sr.error_rate
                FROM analysis_db.step_rollup src_sr
                WHERE src_sr.skill_model_id IN ('
        AS query_body UNION SELECT skill_model_ids UNION SELECT ')') AS full_query_text
        INTO @step_rollup_stmt_text;
    
    -- Execute the query to backup the step_rollup table
    PREPARE step_rollup_stmt FROM @step_rollup_stmt_text;
    EXECUTE step_rollup_stmt;
    DEALLOCATE PREPARE step_rollup_stmt;
    
    COMMIT;
    CALL debug_log('lfa_backup_skill_models_sp', 'Finished lfa_backup_skill_models.');
END $$

/**
 * Performs a backup of data that could be affected when running LFA.
 * The results are stored in the analysis_versions database.
 * @param dataset_ids The ids of the datasets to be backed up.
*/
DROP PROCEDURE IF EXISTS `lfa_backup_datasets` $$
CREATE PROCEDURE         `lfa_backup_datasets` (dataset_ids TEXT)
    SQL SECURITY INVOKER
LFA_BACKUP_DATASETS:BEGIN
    DECLARE EXIT HANDLER FOR SQLException
    BEGIN
        ROLLBACK;
        CALL raise_error('SQLException while executing lfa_backup_datasets.');
    END;
    
    CALL debug_log('lfa_backup_datasets_sp', 'Starting lfa_backup_datasets.');
    
    -- Ensure the query won't be truncated when a large number of ids are used
    SET @@session.group_concat_max_len = @@global.max_allowed_packet;
    
    -- Continue only if dataset ids are present
    IF dataset_ids IS NULL OR CHAR_LENGTH(dataset_ids) = 0 THEN
        CALL debug_log('lfa_backup_datasets_sp', 'No dataset ids provided.');
        LEAVE LFA_BACKUP_DATASETS;
    END IF;
    
    START TRANSACTION;
     
    -- Define a query to get a list of skill_model_ids belonging to the specified datasets
    SELECT GROUP_CONCAT(query_text SEPARATOR '') FROM (
        SELECT '
            SELECT GROUP_CONCAT(sm.skill_model_id) INTO @skill_model_ids
                FROM analysis_db.skill_model sm
                WHERE sm.dataset_id IN (' AS query_text UNION SELECT dataset_ids UNION SELECT ')')
        AS full_query_text
        INTO @skill_model_ids_stmt_text;
    
    -- Execute the query to get a list of skill_model_ids
    PREPARE skill_model_ids_stmt FROM @skill_model_ids_stmt_text;
    EXECUTE skill_model_ids_stmt;
    DEALLOCATE PREPARE skill_model_ids_stmt;  
    
    -- Backup the LFA data associated with the skill_model_ids
    CALL lfa_backup_skill_models(@skill_model_ids);
    COMMIT;
    
    CALL debug_log('lfa_backup_datasets_sp', 'Finished lfa_backup_datasets.');
END $$

DELIMITER ;

