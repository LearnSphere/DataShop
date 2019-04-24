/*
  -----------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005-2010
  All Rights Reserved
  
  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $


  Check data accuracy table by table against source DB and destination DB
  ------------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information.
  ------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `get_version_dbm_check_accuracy_sp` $$
CREATE PROCEDURE         `get_version_dbm_check_accuracy_sp` ()
    SQL SECURITY INVOKER
BEGIN
    SELECT '$Revision: 12404 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $';
END $$

/*
  ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
  ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_function_dbm_check_accuracy_sp` $$
CREATE FUNCTION `get_version_function_dbm_check_accuracy_sp` ()
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
  -------------------------------------------------------------------------------
  Initiate the accuracy check
  @param sourceDatasetId dataset_id of the dataset to check in source DB
  @param sourceDatasetId dataset_id of the dataset to check in destination DB
  -------------------------------------------------------------------------------
*/

DROP PROCEDURE IF EXISTS `dbm_check_accuracy` $$
CREATE PROCEDURE         `dbm_check_accuracy` (sourceDatasetId INT, destinationDatasetId INT)
    SQL SECURITY INVOKER
BEGIN
	-- detailFlag: flag to show general or detail info. 
	-- detailFlag should always be false in this procedure since only general info will be returned.
	-- It can be set to true when calling individual sp for detail info. 
       	DECLARE detailFlag BOOLEAN;
       	SET detailFlag = FALSE;
 	CALL debug_log('dbm_check_accuracy_sp', CONCAT('Starting dbm_check_accuracy_sp.sql ', get_version_function_dbm_check_accuracy_sp()));
   	CALL debug_log('dbm_check_accuracy_sp', 'Starting dbm_check_accuracy_sp');
   	
   	-- set up table that contains general info
   	DROP TABLE IF EXISTS tmp_dbm_accuracy_info;
	CREATE TABLE tmp_dbm_accuracy_info(
		table_name VARCHAR(100) PRIMARY KEY,
	        status ENUM('pass', 'fail') NOT NULL,
	        src_dataset_id INT NOT NULL,
	        dest_dataset_id INT NOT NULL,
	        test_time DATETIME NOT NULL,
	        sp_to_call VARCHAR(100)
	) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;
	-- call individual sp for accuracy check at table level
	CALL dca_check_student(sourceDatasetId, destinationDatasetId, detailFlag);
	SELECT * FROM tmp_dbm_accuracy_info;
    	CALL debug_log('dbm_check_accuracy_sp', 'Finished dbm_check_accuracy');

END $$

/*
 -----------------------------------------------
 Set status in tmp_dbm_accuracy_info table
 @param	tableName STRING value of the name of table
 @param srcId INT value of dataset_id in source DB
 @param destId INT value of dataset_id in destination DB
 @param status STRING value of status, either 'pass' or 'fail'
 @param sp_to_call STRING value of a sql statement to call if detailed information are needed
  -------------------------------------------------- 
*/
DROP PROCEDURE IF EXISTS `dca_set_status` $$
CREATE PROCEDURE         `dca_set_status` (tableName VARCHAR(100), srcId INT, destId INT, status VARCHAR(50), sp_to_call VARCHAR(100))
    SQL SECURITY INVOKER
BEGIN
	INSERT INTO tmp_dbm_accuracy_info VALUES (tableName, status, srcId, destId, NOW(), sp_to_call);
END;
/*
 -----------------------------------------------
 check accuracy for Student table
 @param	srcId INT value of dataset_id in source DB
 @param destId INT value of dataset_id in destination DB
 @param detailFlag BOOLEAN value of flag. 
  -------------------------------------------------- 
*/
-- 
DROP PROCEDURE IF EXISTS `dca_check_student` $$
CREATE PROCEDURE         `dca_check_student` (srcId INT, destId INT, detailFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
	DECLARE counter INT DEFAULT 0;
	DECLARE stu BLOB DEFAULT '';
	DECLARE aid BLOB;
	DECLARE uid BLOB;

	IF detailFlag = TRUE THEN
		select min(student) as student, actual_user_id, anon_user_id		
		from 
		(select 'adb_source' as student, actual_user_id, anon_user_id, 0
		from adb_source.student as s
		left join adb_source.session_student_map sstm on sstm.student_id = s.student_id
		left join adb_source.session sses on sses.session_id = sstm.session_id
		where sses.dataset_id = srcId
		union all
		select 'analysis_db' as student, actual_user_id, anon_user_id, 0
		from analysis_db.student as d
		left join analysis_db.session_student_map dstm on dstm.student_id = d.student_id
		left join analysis_db.session dses on dses.session_id = dstm.session_id
		where dses.dataset_id = destId
		) as student_table 
		group by actual_user_id, anon_user_id
		having count(*) = 1;
	ELSE
		CALL debug_log('dbm_check_accuracy_sp', 'Starting dca_check_student');
		
		select min(student) as student, actual_user_id, anon_user_id
		into stu, aid, uid
		from 
		(select 'adb_source' as student, actual_user_id, anon_user_id, 0
		from adb_source.student as s
		left join adb_source.session_student_map sstm on sstm.student_id = s.student_id
		left join adb_source.session sses on sses.session_id = sstm.session_id
		where sses.dataset_id = srcId
		union all
		select 'analysis_db' as student, actual_user_id, anon_user_id, 0
		from analysis_db.student as d
		left join analysis_db.session_student_map dstm on dstm.student_id = d.student_id
		left join analysis_db.session dses on dses.session_id = dstm.session_id
		where dses.dataset_id = destId
		) as student_table 
		group by actual_user_id, anon_user_id
		having count(*) = 1
		limit 1;
		IF stu != '' THEN
			CALL dca_set_status('student',srcId, destId, 'fail', CONCAT('Call dca_check_student(', srcId,', ', destId, ', TRUE)'));
			CALL debug_log('dbm_check_accuracy_sp', CONCAT('!ERROR in student. Call dca_check_student(', srcId,', ', destId, ', TRUE)'));
		ELSE
			CALL dca_set_status('student',srcId, destId, 'pass', null);
		END IF;
		
	    	CALL debug_log('dbm_check_accuracy_sp', 'Finished dca_check_student');
    	END IF;
END $$




DELIMITER ;