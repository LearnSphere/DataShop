USE analysis_versions;

DROP TABLE IF EXISTS datashop_version;
CREATE TABLE `datashop_version` (
    `version_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` VARCHAR(20) NOT NULL COLLATE 'utf8_bin',
    `version_time` DATETIME NOT NULL,
    `backup_time` DATETIME NOT NULL,
    PRIMARY KEY (`version_id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
;

DROP TABLE IF EXISTS skill_model;
CREATE TABLE `skill_model` (
    `version_id` BIGINT(20) NOT NULL,
    `skill_model_id` BIGINT(20) NOT NULL,
    `skill_model_name` VARCHAR(50) NOT NULL COLLATE 'utf8_bin',
    `aic` DOUBLE NULL DEFAULT NULL,
    `bic` DOUBLE NULL DEFAULT NULL,
    `intercept` DOUBLE NULL DEFAULT NULL,
    `log_likelihood` DOUBLE NULL DEFAULT NULL,
    `owner` VARCHAR(32) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `global_flag` TINYINT(1) NOT NULL,
    `dataset_id` INT(11) NOT NULL,
    `allow_lfa_flag` TINYINT(1) NOT NULL,
    `status` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `lfa_status` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `lfa_status_description` VARCHAR(250) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `source` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `mapping_type` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `creation_time` DATETIME NULL DEFAULT NULL,
    `modified_time` DATETIME NULL DEFAULT NULL,
    `num_observations` INT(11) NULL DEFAULT NULL,
    `cv_unstratified_rmse` DOUBLE NULL DEFAULT NULL,
    `cv_student_stratified_rmse` DOUBLE NULL DEFAULT NULL,
    `cv_step_stratified_rmse` DOUBLE NULL DEFAULT NULL,
    `cv_status` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `cv_status_description` VARCHAR(250) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `cv_unstratified_num_observations` INT(11) NULL DEFAULT NULL,
    `cv_unstratified_num_parameters` INT(11) NULL DEFAULT NULL,
    `num_skills` INT(11) NULL DEFAULT NULL,
    `src_skill_model_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`version_id`, `skill_model_id`),
    INDEX `owner` (`owner`),
    INDEX `dataset_id` (`dataset_id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
;

DROP TABLE IF EXISTS skill;
CREATE TABLE `skill` (
    `version_id` BIGINT(20) NOT NULL,
    `skill_id` BIGINT(20) NOT NULL,
    `skill_name` TEXT NOT NULL COLLATE 'utf8_bin',
    `category` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8_bin',
    `skill_model_id` BIGINT(20) NULL DEFAULT NULL,
    `beta` DOUBLE NULL DEFAULT NULL,
    `gamma` DOUBLE NULL DEFAULT NULL,
    `src_skill_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`version_id`, `skill_id`),
    INDEX `skill_model_id` (`skill_model_id`),
    INDEX `skill_name` (`skill_name`(255))
)
COLLATE='utf8_bin'
ENGINE=InnoDB
;

DROP TABLE IF EXISTS alpha_score;
CREATE TABLE `alpha_score` (
    `version_id` BIGINT(20) NOT NULL,
    `student_id` BIGINT(20) NOT NULL,
    `skill_model_id` BIGINT(20) NOT NULL,
    `alpha` DOUBLE NULL DEFAULT NULL,
    PRIMARY KEY (`version_id`, `student_id`, `skill_model_id`),
    INDEX `skill_model_id` (`skill_model_id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
;

DROP TABLE IF EXISTS step_rollup;
CREATE TABLE `step_rollup` (
    `version_id` BIGINT(20) NOT NULL,
	`step_rollup_id` BIGINT(20) NOT NULL,
	`sample_id` INT(11) NOT NULL,
	`student_id` BIGINT(20) NOT NULL,
	`step_id` BIGINT(20) NOT NULL,
	`skill_id` BIGINT(20) NULL DEFAULT NULL,
	`opportunity` INT(11) NULL DEFAULT NULL,
	`skill_model_id` BIGINT(20) NULL DEFAULT NULL,
	`dataset_id` INT(11) NOT NULL,
	`problem_id` BIGINT(20) NOT NULL,
	`problem_view` INT(11) NOT NULL,
	`total_hints` INT(11) NOT NULL,
	`total_incorrects` INT(11) NOT NULL,
	`total_corrects` INT(11) NOT NULL,
	`first_attempt` ENUM('0','1','2','3') NOT NULL COLLATE 'utf8_bin',
	`conditions` TEXT NULL COLLATE 'utf8_bin',
	`step_time` DATETIME NOT NULL,
	`first_transaction_time` DATETIME NOT NULL,
	`step_start_time` DATETIME NULL DEFAULT NULL,
	`step_end_time` DATETIME NOT NULL,
	`correct_transaction_time` DATETIME NULL DEFAULT NULL,
	`predicted_error_rate` DOUBLE NULL DEFAULT NULL,
	`step_duration` BIGINT(20) NULL DEFAULT NULL,
	`correct_step_duration` BIGINT(20) NULL DEFAULT NULL,
	`error_step_duration` BIGINT(20) NULL DEFAULT NULL,
	`error_rate` TINYINT(4) NULL DEFAULT NULL,
	PRIMARY KEY (`version_id`, `step_rollup_id`),
	INDEX `SamStuSkil_index` (`sample_id`, `student_id`, `skill_id`) USING BTREE,
	INDEX `SamStepStu_index` (`sample_id`, `step_id`, `student_id`) USING BTREE,
	INDEX `sample_id` (`sample_id`),
	INDEX `student_id` (`student_id`),
	INDEX `skill_id` (`skill_id`),
	INDEX `opportunity` (`opportunity`),
	INDEX `step_id` (`step_id`),
	INDEX `problem_id` (`problem_id`),
	INDEX `skill_model_id` (`skill_model_id`),
	INDEX `step_rollup_fkey_dataset` (`dataset_id`)
)
COLLATE='utf8_bin'
ENGINE=InnoDB
;

