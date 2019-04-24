/*
  -------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
  
  $Revision: 12404 $
  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $

  DataLab stored procedures.
  -------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
 ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_ai_verify_data_sp` $$
CREATE FUNCTION `get_version_ai_verify_data_sp` ()
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
 -----------------------------------------------------------------------------
  Main procedure to verify the data in the aiImportFileData table. 
  TBD: not really doing anything yet...
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_verify_data` $$
CREATE PROCEDURE `ai_verify_data` (IN threshold INT, IN importDbName VARCHAR(25),
                                   IN aiImportFileDataTable VARCHAR(25), OUT isValid BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
        
    DECLARE columnNames TEXT;
    DECLARE errorCount INT DEFAULT 0;
    DECLARE importStatusId INT DEFAULT 0;
    DECLARE numRows INT DEFAULT 0;
    DECLARE query TEXT;
    DECLARE exitFlag BOOLEAN DEFAULT FALSE;
    
    CALL debug_log('ai_verify_data_sp',
                   CONCAT(get_version_ai_verify_data_sp(), ' Starting ai_verify_data'));
    
    -- default length of group_concat is 1024, we need more than that.
    SET @@group_concat_max_len = 65535;        
    
    DROP VIEW IF EXISTS import_file_info;
    
    DROP VIEW IF EXISTS import_status;
    
    -- creating view doesn't allow a variable or parameter in the select statement
    -- and no subquery is allowed either,
    -- so we are unable to create a view for a specific status id 
    SET query = CONCAT('CREATE VIEW import_status ',
                       'AS SELECT * FROM ', importDbName, '.import_status ');
  
    CALL exec(query);
    
    SET query = CONCAT('CREATE VIEW import_file_info ',
                       'AS SELECT * FROM ', importDbName, '.import_file_info ');
   
   
    CALL exec(query);
    
    -- obtain import status id for updating status in import_status
    SET query = CONCAT("SELECT DISTINCT import_status_id ",
                       "INTO @importStatusId FROM import_file_info ",
                       "JOIN ", aiImportFileDataTable, " USING (import_file_id)");
    CALL exec(query);

    -- obtain total number of rows from aiImportFileData
    SET query = CONCAT("SELECT COUNT(*) INTO @numRows FROM ", aiImportFileDataTable);
    CALL exec(query);

    SET query = CONCAT("SELECT GROUP_CONCAT(column_name SEPARATOR ',') ",
                       "INTO @columnNames FROM information_schema.columns ",
                       "WHERE table_name = '", aiImportFileDataTable, "' ",
                       "AND table_schema = 'analysis_db'");
    CALL exec(query);

    SET isValid = TRUE;
    
    IF exitFlag = TRUE THEN
        SET isValid = FALSE;
    END IF;

    IF isValid = TRUE THEN
        CALL debug_log('ai_verify_data_sp', 'isValid is TRUE');
        SELECT COUNT(error_count) INTO errorCount FROM import_file_info
        WHERE import_status_id = @importStatusId AND error_count > 0;
        
        IF errorCount > 0 THEN
            SET isValid = FALSE;
        ELSE
            SELECT error_count 
            INTO errorCount 
            FROM import_status
            
            WHERE import_status_id = @importStatusId;
            IF errorCount > 0 THEN
                SET isValid = FALSE;
            END IF;
        END IF;
    ELSE
        CALL debug_log('ai_verify_data_sp', 'isValid is FALSE');
    END IF;
    CALL debug_log('ai_verify_data_sp', 'Finished ai_verify_data');
END $$

/*
 -----------------------------------------------------------------------------
  Main procedure to populate the database.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_populate_database` $$
CREATE PROCEDURE `ai_populate_database` (IN fileInfoTableName VARCHAR(25), IN analysisId BIGINT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ai_populate_database", "Starting ai_populate_database");

    SET foreign_key_checks = 0;

    SET group_concat_max_len = 4294967295;

    CALL ai_create_items(fileInfoTableName, analysisId);
    CALL ai_create_students(fileInfoTableName, analysisId);
    CALL ai_create_values(fileInfoTableName, analysisId);

    SET foreign_key_checks = 1;

    CALL debug_log("ai_populate_database", "Finished ai_populate_database");
END $$

/*
 -----------------------------------------------------------------------------
 Create dl_item entries for all columns _except_ the student.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_create_items` $$
CREATE PROCEDURE `ai_create_items` (IN fileInfoTableName VARCHAR(25),
                                    IN analysisId BIGINT)
    SQL SECURITY INVOKER
BEGIN

    DECLARE itemColumns TEXT;
    DECLARE selectString TEXT;
    DECLARE insertString TEXT;
    DECLARE length INT DEFAULT 0;
    DECLARE delimPos INT;
    DECLARE colName TEXT;
    DECLARE mappedColName VARCHAR(255);
    DECLARE itemCount INT DEFAULT 0;
    
    CALL debug_log("ai_populate_database", "Starting ai_create_items");

    SET selectString = CONCAT('SELECT GROUP_CONCAT(column_name) ',
                              'INTO @itemColumns FROM information_schema.columns ',
                              'WHERE table_name = "', fileInfoTableName, '" ',
                              'AND column_name != "line_num" ',
                              'AND column_name != "import_file_id" ',
                              'AND table_schema = DATABASE()');

    CALL exec(selectString);

    -- Skip the first column as it has the student name/id.
    SET delimPos = LOCATE(',', @itemColumns);
    SET @itemColumns = TRIM(SUBSTRING(@itemColumns FROM delimPos+1));

    SET length = LENGTH(@itemColumns);
    WHILE length > 0 DO
        SET delimPos = LOCATE(',', @itemColumns);
        IF delimPos != 0 THEN
           SET colName = TRIM(SUBSTRING(@itemColumns, 1, delimPos-1));
           SET @itemColumns = TRIM(SUBSTRING(@itemColumns FROM delimPos+1));
           SET length = LENGTH(@itemColumns);           

           SELECT heading INTO mappedColName FROM ai_heading_column_map
               WHERE column_name = colName;

           SET insertString = CONCAT('INSERT INTO dl_item (dl_analysis_id, item_name) ',
                                     'VALUES (', analysisId, ', "', mappedColName, '")');
           CALL exec(insertString);
        ELSE
           SELECT heading INTO mappedColName FROM ai_heading_column_map
               WHERE column_name = @itemColumns;
           SET insertString = CONCAT('INSERT INTO dl_item (dl_analysis_id, item_name, is_summary_col) ',
                                     'VALUES (', analysisId, ', "', mappedColName, '", true)');
           CALL exec(insertString);
           SET length = 0;
        END IF;

    END WHILE;

    -- Update item count for the analysis
    SELECT COUNT(dl_item_id) INTO itemCount FROM dl_item WHERE dl_analysis_id = analysisId;
    UPDATE dl_analysis SET num_items = itemCount WHERE dl_analysis_id = analysisId;

    CALL debug_log("ai_populate_database", "Finished ai_create_items");
END $$                                  

/*
 -----------------------------------------------------------------------------
 Create dl_student entries for all rows.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_create_students` $$
CREATE PROCEDURE `ai_create_students` (IN fileInfoTableName VARCHAR(25),
                                       IN analysisId BIGINT)
    SQL SECURITY INVOKER
BEGIN

    DECLARE columnNames TEXT;
    DECLARE selectString TEXT;
    DECLARE insertString TEXT;
    DECLARE delimPos INT;
    DECLARE studentColName VARCHAR(255);
    DECLARE studentCount INT DEFAULT 0;
    DECLARE theIndex INT;
    
    CALL debug_log("ai_populate_database", "Starting ai_create_students");

    SET selectString = CONCAT('SELECT GROUP_CONCAT(column_name) ',
                              'INTO @columnNames FROM information_schema.columns ',
                              'WHERE table_name = "', fileInfoTableName, '" ',
                              'AND column_name != "line_num" ',
                              'AND column_name != "import_file_id" ',
                              'AND table_schema = DATABASE()');

    CALL exec(selectString);

    -- Get the first column name only.
    SET delimPos = LOCATE(',', @columnNames);
    SET studentColName = TRIM(SUBSTRING(@columnNames, 1, delimPos-1));

    SET @theIndex := 0;
    SET insertString =
           CONCAT('INSERT INTO dl_student ',
                  '(dl_analysis_id, student_name, anon_student_id, student_index) ',
                  'SELECT ', analysisId, ', ', studentColName,
                  ', CONCAT("Stu_", MD5(', studentColName, ')), @theIndex := @theIndex + 1',
                  ' FROM ', fileInfoTableName);
    CALL exec(insertString);
    
    SELECT COUNT(dl_student_id) INTO studentCount FROM dl_student WHERE dl_analysis_id = analysisId;
    UPDATE dl_analysis SET num_students = studentCount WHERE dl_analysis_id = analysisId;

    CALL debug_log("ai_populate_database", "Finished ai_create_students");

END $$                                  

/*
 ------------------------------------------------------------------------------
  Function to test if a value is numeric.
  Allows for optional plus/minus and decimal.
 ------------------------------------------------------------------------------
*/

DROP FUNCTION IF EXISTS `is_not_numeric` $$
CREATE FUNCTION `is_not_numeric` (theValue VARCHAR(255))
    RETURNS BOOLEAN
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
RETURN theValue NOT REGEXP '^(-|\\+){0,1}([0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|[0-9]+)$';
END $$

/*
 -----------------------------------------------------------------------------
 Create dl_value entries.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_create_values` $$
CREATE PROCEDURE `ai_create_values` (IN fileInfoTableName VARCHAR(25),
                                     IN analysisId BIGINT)
    SQL SECURITY INVOKER
BEGIN

    DECLARE columnNames TEXT;
    DECLARE selectString TEXT;
    DECLARE insertString TEXT;
    DECLARE valueString TEXT;
    DECLARE fromString TEXT;
    DECLARE joinStuString TEXT;
    DECLARE joinItemString TEXT;
    DECLARE query TEXT;
    DECLARE delimPos INT;
    DECLARE studentColName VARCHAR(255);
    DECLARE itemColName TEXT;
    DECLARE mappedItemColName VARCHAR(255);
    DECLARE length INT DEFAULT 0;
    DECLARE itemAvg DOUBLE DEFAULT 0.0;
    DECLARE itemSD DOUBLE DEFAULT 0.0;
    DECLARE overallMax DOUBLE DEFAULT 0.0;
    DECLARE finalMax DOUBLE DEFAULT 0.0;
    DECLARE itemMax DOUBLE DEFAULT 0.0;
    DECLARE overallSD DOUBLE DEFAULT 0.0;
    DECLARE maxScoreComputed DOUBLE DEFAULT 0.0;
    DECLARE maxScoreGiven DOUBLE DEFAULT 0.0;

    CALL debug_log("ai_populate_database", CONCAT("Starting ai_create_values: ", analysisId));

    SET selectString = CONCAT('SELECT GROUP_CONCAT(column_name) ',
                              'INTO @columnNames FROM information_schema.columns ',
                              'WHERE table_name = "', fileInfoTableName, '" ',
                              'AND column_name != "line_num" ',
                              'AND column_name != "import_file_id" ',
                              'AND table_schema = DATABASE()');

    CALL exec(selectString);

    -- Get the first column name only.
    SET delimPos = LOCATE(',', @columnNames);
    SET studentColName = TRIM(SUBSTRING(@columnNames, 1, delimPos-1));

    SET insertString = CONCAT('INSERT INTO dl_value (dl_analysis_id, dl_student_id, dl_item_id, ',
                              'value) ',
                              'SELECT "', analysisId, '", '
                              'dls.dl_student_id, '
                              'dli.dl_item_id, ');
    SET fromString = CONCAT(' FROM ', fileInfoTableName, ' fit');
    SET joinStuString = CONCAT(' JOIN dl_student dls ON dls.student_name = fit.',
                               studentColName);

    -- Skip the first column as it has the student name/id.
    SET @columnNames = TRIM(SUBSTRING(@columnNames FROM delimPos+1));

    SET length = LENGTH(@columnNames);
    WHILE length > 0 DO
        SET delimPos = LOCATE(',', @columnNames);
        IF delimPos != 0 THEN
           SET itemColName = TRIM(SUBSTRING(@columnNames, 1, delimPos-1));
           SET @columnNames = TRIM(SUBSTRING(@columnNames FROM delimPos+1));
           SET length = LENGTH(@columnNames);           
        ELSE
           SET itemColName = @columnNames;
           SET length = 0;
        END IF;

        SELECT heading INTO mappedItemColName FROM ai_heading_column_map
            WHERE column_name = itemColName;

        SET valueString = CONCAT(' IF (is_not_numeric(fit.', itemColName, '), NULL, fit.',
                                 itemColName, ')');
        SET joinItemString = CONCAT(' JOIN dl_item dli WHERE dli.item_name = "',
                                    mappedItemColName, '"');
        -- Add student and item constraints for analysis_id.
        SET joinItemString = CONCAT(joinItemString, ' AND dli.dl_analysis_id = ', analysisId);
        SET joinItemString = CONCAT(joinItemString, ' AND dls.dl_analysis_id = ', analysisId);

        SET query = CONCAT(insertString, valueString, fromString, joinStuString, joinItemString);
        CALL exec(query);

        -- Determine max value for this item
        SELECT MAX(value) INTO itemMax FROM dl_value dlv 
               JOIN dl_item dli USING(dl_item_id) 
               WHERE dlv.dl_analysis_id = analysisId 
               AND dli.item_name = mappedItemColName;

        IF delimPos = 0 THEN
           -- This is the last column so the itemMax is the finalMax
           SET finalMax = itemMax;
        END IF;

        -- Overall Max is a sum of max values across items (computed)
        SET overallMax = overallMax + itemMax;

        -- Calculate per-item statistics
        SELECT AVG(value), STDDEV_SAMP(value) INTO itemAvg, itemSD 
               FROM dl_value dlv JOIN dl_item dli USING (dl_item_id)
               WHERE dlv.dl_analysis_id = analysisId AND dli.item_name = mappedItemColName;
        UPDATE dl_item
               SET average = itemAvg, std_deviation = itemSD, max_value = itemMax 
               WHERE dl_analysis_id = analysisId AND item_name = mappedItemColName;

    END WHILE;

    -- Update analysis with overallMax and finalMax
    UPDATE dl_analysis
           SET overall_max = overallMax, overall_max_ignore_summary = (overallMax - finalMax)
           WHERE dl_analysis_id = analysisId;

    -- Calculate per-student statistics
    CALL ai_calculate_per_student_stats(analysisId);

    -- Update analysis with overallStdDeviation
    SELECT STDDEV_SAMP(computed_overall_score) INTO overallSD 
           FROM dl_student dls WHERE dls.dl_analysis_id = analysisId;
    UPDATE dl_analysis SET overall_std_deviation = overallSD WHERE dl_analysis_id = analysisId;

    -- Update analysis with max_score_computed (as max of computed overall score)
    SELECT MAX(computed_overall_score) INTO maxScoreComputed
           FROM dl_student dls WHERE dls.dl_analysis_id = analysisId;
    UPDATE dl_analysis SET max_score_computed = maxScoreComputed WHERE dl_analysis_id = analysisId;

    -- Update analysis with max_score_given (as max value in summary column).
    SELECT MAX(dlv.value) INTO maxScoreGiven
           FROM dl_value dlv
           JOIN dl_item dli USING (dl_item_id)
           WHERE dli.is_summary_col = 1
           AND dlv.dl_analysis_id = analysisId;
    UPDATE dl_analysis SET max_score_given = maxScoreGiven WHERE dl_analysis_id = analysisId;
    
    CALL debug_log("ai_populate_database", "Finished ai_create_values");

END $$                                  

/*
 -----------------------------------------------------------------------------
 Calculate statistics for each student row.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_calculate_per_student_stats` $$
CREATE PROCEDURE `ai_calculate_per_student_stats` (IN analysisId BIGINT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ai_populate_database", "Begin ai_calculate_per_student_stats");

    -- Update student average and sum
    DROP TABLE IF EXISTS ai_temp_student_stats;
    CREATE TABLE ai_temp_student_stats (dl_student_id BIGINT, the_sum DOUBLE, the_count INT,
                                        PRIMARY KEY (dl_student_id)
                                        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dl_student_id, SUM(value) AS the_sum, COUNT(dl_item_id) AS the_count FROM dl_value
           WHERE dl_analysis_id = analysisId GROUP BY dl_student_id;

    UPDATE dl_student dls JOIN ai_temp_student_stats tmp USING (dl_student_id)
           SET dls.average = (tmp.the_sum / tmp.the_count),
               dls.computed_overall_score = tmp.the_sum;

    -- Update the final grade
    DROP TABLE IF EXISTS ai_temp_student_stats;
    CREATE TABLE ai_temp_student_stats (dl_student_id BIGINT, final_grade DOUBLE,
                                        PRIMARY KEY (dl_student_id)
                                        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT dl_student_id, value AS final_grade
           FROM dl_value dlv
           JOIN dl_student dls USING (dl_student_id)
           JOIN dl_item dli USING (dl_item_id)
           WHERE dli.is_summary_col = 1
           AND dlv.dl_analysis_id = analysisId GROUP BY dl_student_id;

    UPDATE dl_student dls JOIN ai_temp_student_stats tmp USING (dl_student_id)
           SET dls.final_grade = tmp.final_grade;

    CALL debug_log("ai_populate_database", "Finished ai_calculate_per_student_stats");
END $$

/*
 -----------------------------------------------------------------------------
 Drop the helper tables.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ai_drop_helper_tables` $$
CREATE PROCEDURE `ai_drop_helper_tables` (IN fileInfoTableName VARCHAR(25))
    SQL SECURITY INVOKER
BEGIN

    DECLARE dropString TEXT;

    SET dropString = CONCAT('DROP TABLES IF EXISTS ',
                            fileInfoTableName, ', ai_heading_column_map, ai_temp_student_stats');
    CALL exec(dropString);

END $$

DELIMITER ;
