

DROP TABLE IF EXISTS moocdbs;
CREATE TABLE `moocdbs` (
	`moocdb_id` INT(11) NOT NULL AUTO_INCREMENT,
	`moocdb_name` VARCHAR(250) NOT NULL,
	`earliest_submission_timestamp` DATETIME,
	`created_by` VARCHAR(250),
	`username` VARCHAR(250),
	`password` VARCHAR(250),
	`hash_mapping_file` VARCHAR(250),
	`hash_mapping_file_md5_hash_value` char(32), 
	`general_file` VARCHAR(250),
	`general_file_md5_hash_value` char(32), 
	`forum_file` VARCHAR(250),
	`forum_file_md5_hash_value` char(32), 
	`moocdb_file` VARCHAR(250),
	`moocdb_file_md5_hash_value` char(32), 
	`current_progress` VARCHAR(50),
	`last_progress` VARCHAR(50),
	`last_progress_end_timestamp` DATETIME,
	`start_timestamp` DATETIME,
	`end_timestamp` DATETIME,
	PRIMARY KEY (`moocdb_id`),
	INDEX `moocdb_name_idx` (`moocdb_name`),
	INDEX `created_by_idx` (`created_by`),
	UNIQUE KEY (`moocdb_name`) 
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;