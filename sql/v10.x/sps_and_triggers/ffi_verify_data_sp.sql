/*
  -------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved

  $Revision: 13675 $
  Last modified by - $Author: hcheng $
  Last modified on - $Date: 2016-10-25 11:17:13 -0400 (Tue, 25 Oct 2016) $

  Verifies the data in ffi_import_file_dadta table for further process.
  -------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
 ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `get_version_ffi_verify_data_sp` $$
CREATE FUNCTION `get_version_ffi_verify_data_sp` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 13675 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/*
 -----------------------------------------------------------------------------
  Main procedure to verify the data in the ffiImportFileData table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_data` $$
CREATE PROCEDURE `ffi_verify_data` (IN threshold INT, IN importDbName VARCHAR(25),
                                    IN ffiImportFileDataTable VARCHAR(25), OUT isValid BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    DECLARE columnNames TEXT;
    DECLARE errorCount INT DEFAULT 0;
    DECLARE importStatusId INT DEFAULT 0;
    DECLARE numRows INT DEFAULT 0;
    DECLARE query TEXT;
    DECLARE exitFlag BOOLEAN DEFAULT FALSE;

    CALL debug_log('ffi_verify_data_sp', CONCAT(get_version_ffi_verify_data_sp(), ' Starting ffi_verify_data'));

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
                       "JOIN ", ffiImportFileDataTable, " USING (import_file_id)");
    CALL exec(query);

    -- obtain total number of rows from ffiImportFileData
    SET query = CONCAT("SELECT COUNT(*) INTO @numRows FROM ", ffiImportFileDataTable);
    CALL exec(query);

    SET query = CONCAT("SELECT GROUP_CONCAT(column_name SEPARATOR ',') ",
                       "INTO @columnNames FROM information_schema.columns ",
                       "WHERE table_name = '", ffiImportFileDataTable, "' ",
                       "AND table_schema = 'adb_source'");
    CALL exec(query);

    SET isValid = TRUE;

    verify_data: BEGIN

        DECLARE EXIT HANDLER FOR SQLEXCEPTION SET isValid = FALSE;

        CALL ffi_drop_ignored_columns(@importStatusId, @columnNames, ffiImportFileDataTable);

        -- REQUIRED COLUMNS
        -- Note: No checking for input column since its max length is 255 and can be empty
        CALL ffi_verify_anon_student_id(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
           LEAVE verify_data;
        END IF;

        CALL ffi_verify_session_id(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
           LEAVE verify_data;
        END IF;

        CALL ffi_verify_time('Time',
                             'time', 'tx_time_datetime',
                             @importStatusId, @numRows, threshold, ffiImportFileDataTable,
                             TRUE,
                             exitFlag);
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_time time failure');
            LEAVE verify_data;
        END IF;

        CALL ffi_verify_level(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_level failure');
            LEAVE verify_data;
        END IF;

        CALL ffi_verify_problem_name(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_problem_name failure');
            LEAVE verify_data;
        END IF;


        -- OPTIONAL COLUMNS
        -- Note: no checking for step name column since its max length is 255 and can be empty
        -- include a comma and ws to make sure it is the 'time_zone' column
        -- and not other ignored column with prefix + 'time_zone' column
        IF FIND_IN_SET('time_zone', @columnNames) > 0 THEN
            CALL ffi_verify_time_zone(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_time_zone failure');
            LEAVE verify_data;
        END IF;

        CALL ffi_verify_selection_and_action(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_selection_and_action failure');
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('student_response_type', @columnNames) > 0 THEN
            CALL ffi_verify_student_response_type(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_student_response_type failure');
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('student_response_subtype', @columnNames) > 0 THEN
            CALL ffi_verify_student_response_subtype(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_student_response_subtype failure');
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('tutor_response_type', @columnNames) > 0 THEN
            CALL ffi_verify_tutor_response_type(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            CALL debug_log('ffi_verify_data_sp', 'ffi_verify_data : ffi_verify_tutor_response_type failure');
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('tutor_response_subtype', @columnNames) > 0 THEN
            CALL ffi_verify_tutor_response_subtype(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        -- Ignore 'Problem View' if 'Problem Start Time' is present.
        IF FIND_IN_SET('problem_view', @columnNames) > 0 AND
           FIND_IN_SET('problem_start_time', @columnNames) = 0 THEN
            CALL ffi_update_warning_for_empty_string('problem_view', @importStatusId, @numRows,
                                                   threshold, ffiImportFileDataTable, exitFlag);
            IF exitFlag = TRUE THEN
                SET isValid = FALSE;
                LEAVE verify_data;
            END IF;
            CALL ffi_verify_problem_view(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
            IF exitFlag = TRUE THEN
                SET isValid = FALSE;
                LEAVE verify_data;
            END IF;
        ELSE
            CALL debug_log('ffi_verify_data_sp', 'Problem Start Time and Problem View are both supplied. Problem View will not be recomputed.');
        END IF;

        IF FIND_IN_SET('problem_start_time', @columnNames) > 0 THEN
            CALL ffi_verify_time('Problem Start Time',
                                 'problem_start_time', 'problem_start_time_datetime',
                                 @importStatusId, @numRows, threshold, ffiImportFileDataTable,
                                 FALSE,
                                 exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            SET isValid = FALSE;
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('problem_view', @columnNames) > 0
        AND FIND_IN_SET('problem_start_time', @columnNames) > 0 THEN
            CALL ffi_save_warning_message_in_import_status(
                'Problem Start Time and Problem View are both supplied. Problem View will not be recomputed.', @importStatusId,
                '', ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            SET isValid = FALSE;
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('outcome', @columnNames) > 0 THEN
            CALL ffi_verify_outcome(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('help_level', @columnNames) > 0 THEN
            CALL ffi_verify_help_level(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('total_num_hints', @columnNames) > 0 THEN
            CALL ffi_verify_total_num_hints(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            SET isValid = FALSE;
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('school', @columnNames) > 0 THEN
            CALL ffi_verify_school(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('class', @columnNames) > 0 THEN
            CALL ffi_verify_class(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF FIND_IN_SET('feedback_text', @columnNames) > 0 THEN
            IF FIND_IN_SET('feedback_classification', @columnNames) > 0 THEN
                CALL ffi_verify_feedback_and_classification(TRUE, @importStatusId, @numRows,
                                                            threshold, ffiImportFileDataTable, exitFlag);
            ELSE
                CALL ffi_verify_feedback_and_classification(FALSE, @importStatusId, @numRows,
                                                            threshold, ffiImportFileDataTable, exitFlag);
            END IF;
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF LOCATE('condition_name_', @columnNames) > 0 THEN
            CALL ffi_verify_condition_name_and_type(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF LOCATE('kc_', @columnNames) > 0 THEN
            CALL ffi_verify_kc_and_category(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
        IF exitFlag = TRUE THEN
            LEAVE verify_data;
        END IF;

        IF LOCATE('cf_', @columnNames) > 0 THEN
            CALL ffi_verify_custom_field(@importStatusId, @numRows, threshold, ffiImportFileDataTable, exitFlag);
        END IF;
    END; -- end verify_data block

    IF exitFlag = TRUE THEN
        SET isValid = FALSE;
    END IF;

    IF isValid = TRUE THEN
        CALL debug_log('ffi_verify_data_sp', 'isValid is TRUE');
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
        CALL debug_log('ffi_verify_data_sp', 'isValid is FALSE');
    END IF;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_data');
END $$

/*
 -----------------------------------------------------------------------------
  Drop ignored columns.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_drop_ignored_columns` $$
CREATE PROCEDURE `ffi_drop_ignored_columns` (IN importStatusId INT, IN columnNames TEXT, IN ffiImportFileDataTable VARCHAR(25))
    SQL SECURITY INVOKER
BEGIN
    DECLARE ignoredColumnNames TEXT;
    DECLARE columnName TEXT;
    DECLARE startPos INT DEFAULT 0;
    DECLARE endPos INT DEFAULT 0;
    DECLARE numOfColumns INT DEFAULT 0;
    DECLARE counter INT DEFAULT 0;
    DECLARE ignoredHeadingLength INT DEFAULT 0;
    DECLARE ignoredHeadingMsgPrefix VARCHAR(50) DEFAULT 'The following heading(s) were ignored: [';
    DECLARE query TEXT;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_drop_ignored_columns');

    SET ignoredHeadingLength = LENGTH(ignoredHeadingMsgPrefix);

    SELECT warning_message
    INTO ignoredColumnNames
    FROM import_status
    WHERE import_status_id = importStatusId;

    SET startPos = LOCATE(ignoredHeadingMsgPrefix, ignoredColumnNames);

    -- get the ignored heading names from warning message
    IF  startPos > 0 THEN

        SET ignoredColumnNames = SUBSTRING(ignoredColumnNames, startPos + ignoredHeadingLength);
        -- get the list excluding ]
        SET endPos = LOCATE(']', ignoredColumnNames);
        SET ignoredColumnNames = SUBSTRING(ignoredColumnNames, 1, endPos - 1);
         -- get the number of items in the list
        SET numOfColumns = LENGTH(ignoredColumnNames) - LENGTH(REPLACE(ignoredColumnNames, ',', ''));

        -- loop through the list to drop the columns
        ignored_column_loop: LOOP
            SET counter = counter + 1;

            IF counter > numOfColumns + 1 THEN
                LEAVE ignored_column_loop;
            END IF;
            -- get the first item and convert it to lower case
            SET columnName = LCASE(SUBSTRING_INDEX(ignoredColumnNames, ', ', 1));
            -- replace some characters with underscore
            SET columnName = REPLACE(columnName, ' ', '_');
            SET columnName = REPLACE(columnName, '-', '_');
            SET columnName = REPLACE(columnName, '(', '_');
            -- remove character ')'
            SET columnName = REPLACE(columnName, ')', '');

            IF columnName <> '' THEN
                CALL debug_log('ffi_verify_data_sp', CONCAT('drop ignored column ', columnName));
                -- drop column
                SET query = CONCAT("ALTER TABLE ", ffiImportFileDataTable, " DROP COLUMN ", columnName);
                CALL exec(query);
            END IF;
            -- remove the first item and ', ' to make a new ignoredColumnNames list
            SET ignoredColumnNames = SUBSTRING(ignoredColumnNames, LOCATE(', ', ignoredColumnNames) + 2);

          END LOOP ignored_column_loop;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_drop_ignored_columns');
END $$

/*
 -----------------------------------------------------------------------------
  Verify anon_student_id column(s).
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_anon_student_id` $$
CREATE PROCEDURE `ffi_verify_anon_student_id` (IN importStatusId INT,
                                               IN numRows INT,
                                               IN threshold INT,
                                               IN ffiImportFileDataTable VARCHAR(25),
                                               OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE counter INT DEFAULT 0;
    DECLARE columnName VARCHAR(25) DEFAULT '';
    DECLARE maxLength INT DEFAULT 55;
    DECLARE concatString VARCHAR(250) DEFAULT '';
    DECLARE message TEXT;
    DECLARE fatalErrorMsg TEXT;
    DECLARE exceedMaxLengthFlag INT DEFAULT FALSE;
    DECLARE query TEXT;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_anon_student_id');

    SET exitFlag = FALSE;

    verify_student: BEGIN

        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        SET columnName = 'anon_student_id';
        -- check for empty string
        CALL ffi_update_error_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = FALSE THEN
        -- check for max length - 55
            CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);
        END IF;

        IF exitFlag = FALSE THEN
           -- check for single quotes
           CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                        threshold, ffiImportFileDataTable,
                                                        exitFlag);
        END IF;

    END; -- end verify_student block

     -- handle error if message exceeds 65535 bytes
     IF exceedMaxLengthFlag = TRUE THEN
         SET fatalErrorMsg = 'In column Anon Student Id, error message field exceeded max length';

         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_anon_student_id', fatalErrorMsg));

         CALL ffi_save_error_message_in_import_status(fatalErrorMsg, importStatusId, exitFlag);

         SET exitFlag = TRUE;
     END IF;
     CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_anon_student_id');
END $$


/*
 -----------------------------------------------------------------------------
  Verify session_id column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_session_id` $$
CREATE PROCEDURE `ffi_verify_session_id` (IN importStatusId INT,
                                          IN numRows INT,
                                          IN threshold INT,
                                          IN ffiImportFileDataTable VARCHAR(25),
                                          OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    DECLARE columnName VARCHAR(25) DEFAULT 'session_id';
    DECLARE studentIdColumnName VARCHAR(25) DEFAULT 'session_id';
    DECLARE maxLength INT DEFAULT 255;
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE counter INT DEFAULT 0;
    DECLARE header VARCHAR(75);
    DECLARE fatalErrorMsg TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE totalErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_session_id');

    SET exitFlag = FALSE;

    verify_session: BEGIN

        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        -- check for empty string
        CALL ffi_update_error_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            LEAVE verify_session;
        END IF;

    END;  -- end verify_session block

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET fatalErrorMsg = 'In column Session Id, error message field exceeded max length.';
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_session_id', fatalErrorMsg));
         CALL ffi_save_error_message_in_import_status(fatalErrorMsg, importStatusId, exitFlag);
         SET exitFlag = true;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_session_id');
END $$

/*
 -----------------------------------------------------------------------------
  Verify time column. Formats that are allowed:
  -- yyyy-MM-dd HH:mm:ss.SSS     OLI, DataShop Export
  -- yyyy-MM-dd HH:mm:ss:SSS     CL format
  -- yyyy-MM-dd HH:mm:ss         CTAT Flash, 2001-07-04 12:08:56
  -- yyyy-MM-dd HH:mm            2001-07-04 12:08
  -- MMMMM dd, yyyy hh:mm:ss a   July 04, 2001 12:08:56 AM ** WPI-Assistments format
  -- MM/dd/yyyy HH:mm:ss         2/24/2007 17:18:02
  -- MM/dd/yyyy HH:mm
  -- MM/dd/yy HH:mm:ss:SSS       07/04/01 12:08:56:322
  -- MM/dd/yy HH:mm:ss
  -- MM/dd/yy HH:mm
  -- yyyy/MM/dd HH:mm:ss.SSS     CTAT Java 2010/05/11 16:06:28.65
  -- long                        1239939193
  -- double                      01239939193.31
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_time` $$
CREATE PROCEDURE `ffi_verify_time` (IN header TEXT, IN columnName TEXT,
                                    IN columnNameDateTime TEXT,
                                    IN importStatusId INT,
                                    IN numRows INT,
                                    IN threshold INT,
                                    IN ffiImportFileDataTable VARCHAR(25),
                                    IN errorFlag BOOLEAN,
                                    OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE query TEXT;
    DECLARE message TEXT;
    DECLARE fatalErrorMessage TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE totalErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;
    DECLARE isDateTimeFormatValid BOOLEAN DEFAULT TRUE;

    -- Constants used for parsing timestamps
    DECLARE 2_DIGIT_YEAR TEXT DEFAULT '[0-9][0-9]';
    DECLARE 4_DIGIT_YEAR TEXT DEFAULT '[1-2][0-9][0-9][0-9]';
    DECLARE MONTH TEXT DEFAULT '[0-9]{1,2}';
    DECLARE DAY TEXT DEFAULT '[0-9]{1,2}';
    DECLARE HOUR TEXT DEFAULT '[0-9]{1,2}';
    DECLARE MINUTE TEXT DEFAULT '[0-9]{1,2}';
    DECLARE SECOND TEXT DEFAULT '[0-9]{1,2}';
    DECLARE MILLISEC TEXT DEFAULT '[0-9]+';
    -- yyyy-MM-dd
    DECLARE YYYY_MM_DD_DASH TEXT DEFAULT CONCAT_WS('-', 4_DIGIT_YEAR, MONTH, DAY);
    -- yyyy/MM/dd
    DECLARE YYYY_MM_DD TEXT DEFAULT CONCAT_WS('/', 4_DIGIT_YEAR, MONTH, DAY);
    -- MM/dd/yyyy
    DECLARE MM_DD_YYYY TEXT DEFAULT CONCAT_WS('/', MONTH, DAY, 4_DIGIT_YEAR);
    -- MM/dd/yy
    DECLARE MM_DD_YY TEXT DEFAULT CONCAT_WS('/', MONTH, DAY, 2_DIGIT_YEAR);
    -- HH:mm
    DECLARE HH_MM TEXT DEFAULT CONCAT_WS(':', HOUR, MINUTE);
    -- HH:mm:ss
    DECLARE HH_MM_SS TEXT DEFAULT CONCAT_WS(':', HOUR, MINUTE, SECOND);
    -- HH:mm:ss:SSS
    DECLARE HH_MM_SS_MS TEXT DEFAULT CONCAT_WS(':', HOUR, MINUTE, SECOND, MILLISEC);
    -- mm:ss
    DECLARE MM_SS TEXT DEFAULT CONCAT_WS(':', MINUTE, SECOND);
    -- Start of REGEXP statement
    DECLARE REGEXP_START TEXT DEFAULT ' REGEXP "^';
    -- End of REGEXP statement, not anchored
    DECLARE REGEXP_END TEXT DEFAULT '" ';
    -- End of REGEXP statement, anchored
    DECLARE REGEXP_END_ANCHORED TEXT DEFAULT '$" ';

    CALL debug_log('ffi_verify_data_sp',
            CONCAT('Starting ffi_verify_time for ', columnName, " and ", columnNameDateTime));

    SET exitFlag = FALSE;

    -- check for empty string
    -- if error flag is true, then mark empty string as an error for the given column, otherwise its just a warning
    IF errorFlag = TRUE THEN
        CALL ffi_update_error_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    ELSE
        CALL ffi_update_warning_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    END IF;

    verify_time: BEGIN

        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;
        -- Keep working if data format is not valid for STR_TO_DATE function. it will be caught.
        DECLARE CONTINUE HANDLER FOR SQLSTATE 'HY000' SET isDateTimeFormatValid = FALSE;

        IF exitFlag = TRUE THEN
            LEAVE verify_time;
        END IF;

        SET query = CONCAT("UPDATE IGNORE ", ffiImportFileDataTable, " SET ", columnNameDateTime, " = ( ",
          "SELECT CASE ",
            -- yyyy-MM-dd HH:mm:ss.SSS
            "WHEN ", columnName, REGEXP_START, YYYY_MM_DD_DASH, " ", HH_MM_SS, "['.']", MILLISEC, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ",'%Y-%m-%d %H:%i:%s.%f') ",
            -- yyyy-MM-dd HH:mm:ss:SSS
            "WHEN ", columnName, REGEXP_START, YYYY_MM_DD_DASH, " ", HH_MM_SS_MS, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ",'%Y-%m-%d %H:%i:%s:%f') ",
            -- yyyy-MM-dd HH:mm:ss
            "WHEN ", columnName, REGEXP_START, YYYY_MM_DD_DASH, " ", HH_MM_SS, REGEXP_END,
            "THEN CONVERT(", columnName, ", DATETIME) ",
            -- yyyy-MM-dd HH:mm
            "WHEN ", columnName, REGEXP_START, YYYY_MM_DD_DASH, " ", HH_MM, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%Y-%m-%d %H:%i') ",
            -- MMMMM dd, yyyy HH:mm:ss a
            "WHEN ", columnName, REGEXP_START, "[A-Z]+[a-z]+ ", DAY, ", ", 4_DIGIT_YEAR, " ", HH_MM_SS, " [A-Z]+", REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%M %d, %Y %r') ",
            -- MM/dd/yyyy HH:mm:ss
            "WHEN ", columnName, REGEXP_START, MM_DD_YYYY, " ", HH_MM_SS, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%m/%d/%Y %H:%i:%s') ",
            -- MM/dd/yy HH:mm:ss:SSS
            "WHEN ", columnName, REGEXP_START, MM_DD_YY, " ", HH_MM_SS_MS, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%m/%d/%y %H:%i:%s:%f') ",
            -- MM/dd/yy HH:mm:ss
            "WHEN ", columnName, REGEXP_START, MM_DD_YY, " ", HH_MM_SS, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%m/%d/%y %H:%i:%s') ",
            -- MM/dd/yy HH:mm
            "WHEN ", columnName, REGEXP_START, MM_DD_YY, " ", HH_MM, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%m/%d/%y %H:%i') ",
            -- MM/dd/yyyy HH:mm
            "WHEN ", columnName, REGEXP_START, MM_DD_YYYY, " ", HH_MM, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%m/%d/%Y %H:%i') ",
            -- yyyy/MM/dd HH:mm:ss.SSS
            "WHEN ", columnName, REGEXP_START, YYYY_MM_DD, " ", HH_MM_SS, "['.']", MILLISEC, REGEXP_END,
            "THEN STR_TO_DATE(", columnName, ", '%Y/%m/%d %H:%i:%s.%f') ",
            -- long
            "WHEN ", columnName, REGEXP_START, "[0-9]+", REGEXP_END_ANCHORED,
            "THEN FROM_UNIXTIME(", columnName, "/1000) ",
            -- double
            "WHEN ", columnName, REGEXP_START, "[0-9]+['.'][0-9]+", REGEXP_END_ANCHORED,
            "THEN FROM_UNIXTIME(", columnName, "/1000) ",
            -- empty
            "WHEN ", columnName, " = '' ",
            "THEN '' ",
          "END); ");

        CALL exec(query);

        -- Empty strings for timestamps will have been converted to "0000-00-00 00:00:00"
        -- by this point so don't include them in the error count as they were already
        -- include in the 'empty strings' error count above.
        SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
                           "FROM ", ffiImportFileDataTable,
                           " WHERE ", columnNameDateTime, " IS NULL OR ((",
                           columnNameDateTime, " = 0) AND ((",
                           columnName, " IS NOT NULL) AND (", columnName, " != ''))); ");
        CALL exec(query);

        -- All
        IF @totalErrorCount = numRows THEN
            SET message = CONCAT('In column ', header, ', all values have an invalid format.');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Most
        ELSEIF @totalErrorCount > threshold THEN
            SET query = CONCAT(
                    "SELECT CONCAT('For example in file ',"
                    "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                    "', Line ',(line_num - line_start + 2), ', ",
                    "column Time has an invalid format: ', ", columnName, ", '.' ) INTO @message ",
                "FROM import_file_info ",
                "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                "WHERE ", columnNameDateTime, " IS NULL OR ((",
                columnNameDateTime, " = 0) AND ((",
                columnName, " IS NOT NULL) AND (", columnName, " != ''))) LIMIT 1;");
            CALL exec(query);
            SET message = CONCAT('In column ', header, ', ', @totalErrorCount, ' values have an invalid format.',
                '\n(', @message, ')');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);

        -- Some
        ELSEIF @totalErrorCount > 0 THEN
            SET query = CONCAT(
                "UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                "            t1.import_file_id, ",
                "            COUNT(*) AS fileErrorCount ",
                "      FROM ( ",
                "           SELECT CONCAT('Line ', ",
                "                  (line_num - line_start + 2), ",
                "                  ', column ", header, " has an invalid format.') as message, ",
                "                  fi.import_file_id, ",
                "                  fi.error_message ",
                "           FROM ", ffiImportFileDataTable,
                "           LEFT JOIN import_file_info fi USING (import_file_id) ",
                "           WHERE ", columnNameDateTime, " IS NULL OR ((",
                            columnNameDateTime, " = 0) AND (( ",
                            columnName, " IS NOT NULL) AND (", columnName, " != ''))) ",
                "           ORDER BY line_num ",
                "           ) t1 ",
                "      GROUP BY  import_file_id ",
                "     ) t2 ",
                "USING (import_file_id) ",
                "SET status = 'error', ",
                "    error_count = error_count + t2.fileErrorCount, ",
                "    error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message);");
            CALL exec(query);
        END IF;
    END;

    CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = 'In column Time, error message field exceeded the maximum length.';
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_time', message));
         CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
         SET exitFlag = true;
    END IF;
    CALL debug_log('ffi_verify_data_sp', CONCAT('Finished ffi_verify_time for ', columnName));
END $$

/*
 -----------------------------------------------------------------------------
  Verify time_zone column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_time_zone` $$
CREATE PROCEDURE `ffi_verify_time_zone` (IN importStatusId INT,
                                         IN numRows INT,
                                         IN threshold INT,
                                         IN ffiImportFileDataTable VARCHAR(25),
                                         OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(10) DEFAULT 'time_zone';
    DECLARE maxLength INT DEFAULT 50;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_time_zone');
    -- check for max length 50
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_time_zone');
END $$

/*
 -----------------------------------------------------------------------------
  Verify level column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_level` $$
CREATE PROCEDURE `ffi_verify_level` (IN importStatusId INT,
                                     IN numRows INT,
                                     IN threshold INT,
                                     IN ffiImportFileDataTable VARCHAR(25),
                                     OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64);
    DECLARE maxLength INT DEFAULT 100;
    DECLARE numOfCols INT DEFAULT 1;
    DECLARE counter INT DEFAULT 0;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_level');

    SET exitFlag = FALSE;

    SELECT COUNT(*) INTO numOfCols
    FROM ffi_heading_column_map
    WHERE standard_name = 'Level';

    -- loop to get all the level columns
    level_loop: LOOP
        SET counter = counter + 1;

        IF counter > numOfCols THEN
            LEAVE level_loop;
        END IF;

        SELECT column_name INTO columnName
        FROM ffi_heading_column_map
        WHERE standard_name = 'Level'
        AND sequence = counter;

        -- check for empty string
        CALL ffi_update_warning_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            LEAVE level_loop;
        END IF;
        -- check for max length 100
        CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                             threshold, ffiImportFileDataTable, exitFlag);
        IF exitFlag = TRUE THEN
            LEAVE level_loop;
        END IF;

        -- check for single quotes
        CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                     threshold, ffiImportFileDataTable, exitFlag);

    END LOOP level_loop;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_level');
END $$

/*
 -----------------------------------------------------------------------------
  Verify problem_name column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_problem_name` $$
CREATE PROCEDURE `ffi_verify_problem_name` (IN importStatusId INT,
                                            IN numRows INT,
                                            IN threshold INT,
                                            IN ffiImportFileDataTable VARCHAR(25),
                                            OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'problem_name';
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_problem_name');
    -- check for empty string
    CALL ffi_update_error_for_empty_string(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    CALL ffi_update_error_for_max_length(columnName, 255, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_problem_name');
END $$

/*
 -----------------------------------------------------------------------------
  Verify student_response_type column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_student_response_type` $$
CREATE PROCEDURE `ffi_verify_student_response_type` (IN importStatusId INT,
                                                     IN numRows INT,
                                                     IN threshold INT,
                                                     IN ffiImportFileDataTable VARCHAR(25),
                                                     OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'student_response_type';
    DECLARE maxLength INT DEFAULT 30;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_student_response_type');
    -- check for max length 30
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_student_response_type');
END $$

/*
 -----------------------------------------------------------------------------
  Verify student_response_subtype column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_student_response_subtype` $$
CREATE PROCEDURE `ffi_verify_student_response_subtype` (IN importStatusId INT,
                                                        IN numRows INT,
                                                        IN threshold INT,
                                                        IN ffiImportFileDataTable VARCHAR(25),
                                                        OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'student_response_subtype';
    DECLARE maxLength INT DEFAULT 30;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_student_response_subtype');
    -- check for max length 30
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_student_response_subtype');
END $$

/*
 -----------------------------------------------------------------------------
  Verify tutor_response_type column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_tutor_response_type` $$
CREATE PROCEDURE `ffi_verify_tutor_response_type` (IN importStatusId INT,
                                                   IN numRows INT,
                                                   IN threshold INT,
                                                   IN ffiImportFileDataTable VARCHAR(25),
                                                   OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'tutor_response_type';
    DECLARE maxLength INT DEFAULT 30;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_tutor_response_type');
    -- check for max length 30
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_tutor_response_type');
END $$

/*
 -----------------------------------------------------------------------------
  Verify tutor_response_subtype column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_tutor_response_subtype` $$
CREATE PROCEDURE `ffi_verify_tutor_response_subtype` (IN importStatusId INT,
                                                      IN numRows INT,
                                                      IN threshold INT,
                                                      IN ffiImportFileDataTable VARCHAR(25),
                                                      OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'tutor_response_subtype';
    DECLARE maxLength INT DEFAULT 30;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_tutor_response_subtype');
    -- check for max length 30
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_tutor_response_subtype');
END $$

/*
 -----------------------------------------------------------------------------
  Verify outcome column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_outcome` $$
CREATE PROCEDURE `ffi_verify_outcome` (IN importStatusId INT,
                                       IN numRows INT,
                                       IN threshold INT,
                                       IN ffiImportFileDataTable VARCHAR(25),
                                       OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'outcome';
    DECLARE maxLength INT DEFAULT 30;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_outcome');
    -- check for max length 30
    CALL ffi_update_error_for_max_length(columnName, maxLength, importStatusId, numRows,
                                         threshold, ffiImportFileDataTable, exitFlag);
    CALL ffi_update_warning_for_unexpected_value(columnName, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_outcome');
END $$

/*
 -----------------------------------------------------------------------------
  Verify selection and action column(s).  The value of at least one of these columns must exist.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_selection_and_action` $$
CREATE PROCEDURE `ffi_verify_selection_and_action` (IN importStatusId INT,
                                                    IN numRows INT,
                                                    IN threshold INT,
                                                    IN ffiImportFileDataTable VARCHAR(25),
                                                    OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE selectionColNum INT DEFAULT 1;
    DECLARE actionColNum INT DEFAULT 1;
    DECLARE counter INT DEFAULT 0;
    DECLARE columnName VARCHAR(64) DEFAULT '';
    DECLARE message TEXT;
    DECLARE whereClause TEXT;
    DECLARE query TEXT;
    DECLARE selectionConcatString TEXT DEFAULT ' selection_1 = \'\'';
    DECLARE actionConcatString TEXT DEFAULT ' action_1 = \'\'';

    DECLARE header VARCHAR(75);
    DECLARE fatalErrorMsg TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    DECLARE criteria TEXT;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_selection_and_action');

    SET exitFlag = FALSE;
    -- get selection column counts
    SELECT COUNT(*) INTO selectionColNum
    FROM ffi_heading_column_map
    WHERE standard_name = 'Selection';

    -- get action column counts
    SELECT COUNT(*) INTO actionColNum
    FROM ffi_heading_column_map
    WHERE standard_name = 'Action';

    verify_selection_and_action: BEGIN

        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;
        -- loop through each column
        selection_loop: LOOP
            SET counter = counter + 1;
            IF counter > selectionColNum THEN
                LEAVE selection_loop;
            END IF;
            SET columnName = CONCAT('selection_', counter);
            -- construct string to use in the where clause
            IF counter > 1 THEN
                SET selectionConcatString = CONCAT(selectionConcatString, ' AND ', columnName, ' = \'\'');
            END IF;
        END LOOP selection_loop;

        SET counter = 0;
        -- loop through each column
        action_loop: LOOP
            SET counter = counter + 1;
            IF counter > actionColNum THEN
                LEAVE action_loop;
            END IF;
            SET columnName = CONCAT('action_', counter);
            -- construct string to use in the where clause
            IF counter > 1 THEN
                SET actionConcatString = CONCAT(actionConcatString, ' AND ', columnName, ' = \'\'');
            END IF;
        END LOOP action_loop;

        IF exitFlag = TRUE THEN
            LEAVE verify_selection_and_action;
        END IF;
        -- The value of at least one of these columns must exist.
        SET exitFlag = false;
        SET header = ffi_get_header(columnName);

        IF FIND_IN_SET('step_name', @columnNames) > 0 THEN
            SET criteria = CONCAT("step_name", ' = \'\'');
            IF selectionColNum > 0 THEN
               SET criteria = CONCAT(criteria, " AND ", selectionConcatString);
            END IF;
            IF actionColNum > 0 THEN
               SET criteria = CONCAT(criteria, " AND ", actionConcatString);
            END IF;
        ELSE
            SET criteria = CONCAT(selectionConcatString,
                                  " AND ", actionConcatString);
        END IF;
        SET whereClause = CONCAT(" WHERE ", criteria);

        SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
                           "FROM ", ffiImportFileDataTable,
                           whereClause);
        CALL exec(query);

        -- All
        IF @totalErrorCount = numRows THEN
            SET message = CONCAT('Rows without a value in one or more of the columns Step Name, Selection, and Action will not have a transaction subgoal, or step. ',
                                 'This is the case for all rows.');
            CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                    criteria, ffiImportFileDataTable, exitFlag);
        -- Some/Most
        ELSEIF @totalErrorCount > 0 THEN
            SET query = CONCAT(
                "SELECT CONCAT('For example in file ',"
                "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                "', this is true of line ',(line_num - line_start + 2), '.') INTO @message ",
                "FROM import_file_info ",
                "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                whereClause,
                " LIMIT 1;");
            CALL exec(query);
            SET message = CONCAT('Rows without a value in one or more of the columns Step Name, Selection, and Action will not have a transaction subgoal, or step. ',
                                @totalErrorCount, ' rows follow this pattern.\n(',
                                @message, ')');
            CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                    criteria, ffiImportFileDataTable, exitFlag);
        END IF;
    END;


    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET fatalErrorMsg = 'In column Step Name, Selection and Action, warning message field exceeded the maximum length.';
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_selection_and_action', fatalErrorMsg));
         CALL ffi_save_error_message_in_import_status(fatalErrorMsg, importStatusId, ffiImportFileDataTable, exitFlag);
         SET exitFlag = true;
    END IF;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_selection_and_action');
END $$

/*
 -----------------------------------------------------------------------------
  Verify feedback_text and feedback_classification columns.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_feedback_and_classification` $$
CREATE PROCEDURE `ffi_verify_feedback_and_classification` (IN classificationExist BOOLEAN,
                                                           IN importStatusId INT,
                                                           IN numRows INT,
                                                           IN threshold INT,
                                                           IN ffiImportFileDataTable VARCHAR(25),
                                                           OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE textColumnName VARCHAR(64) DEFAULT 'feedback_text';
    DECLARE classificationColumnName VARCHAR(64) DEFAULT 'feedback_classification';
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE header VARCHAR(75);
    DECLARE fatalErrorMsg TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE totalErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_feedback_and_classification');

    SET exitFlag = FALSE;

    verify_feedback: BEGIN

        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        IF classificationExist = TRUE THEN
            -- if feedback_classification has a value, then feedback_text must have a value and not equals to empty string
            SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
                               "FROM ", ffiImportFileDataTable,
                               " WHERE ((feedback_classification <> '') ",
                               "AND (feedback_classification IS NOT NULL)) ",
                               "AND ((feedback_text IS NULL) OR (feedback_text = ''))");
            CALL exec(query);

            -- All
            IF @totalErrorCount = numRows THEN
                SET message = 'In column Feedback Classification, values are given but all rows are missing Feedback Text values.';
                CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
            -- Most
            ELSEIF @totalErrorCount > threshold THEN
                SET query = CONCAT(
                    "SELECT CONCAT('For example in file ',",
                    "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), ",
                    "', Line ',(line_num - line_start + 2), ', ",
                    "Feedback Classification is given, but the row is missing a Feedback Text value.') ",
                    "INTO @message ",
                    "FROM import_file_info ",
                    "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                    "WHERE ((feedback_classification <> '') AND (feedback_classification IS NOT NULL)) ",
                           "AND ((feedback_text IS NULL) OR (feedback_text = '')) ",
                           "LIMIT 1");
                CALL exec(query);
                SET message = CONCAT('In column Feedback Classification, values are given but ',
                              @totalErrorCount,
                              ' rows are missing Feedback Text values.',
                              '\n(', @message, ')');
                CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
            -- Some
            ELSEIF @totalErrorCount > 0 THEN
                SET query = CONCAT("UPDATE import_file_info i ",
                    "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                    "            t1.import_file_id, ",
                    "            COUNT(*) AS fileErrorCount ",
                    "      FROM ( ",
                    "           SELECT CONCAT('Line ', ",
                    "                  (line_num - line_start + 2), ",
                    "                  ', Feedback Classification is given, but the row is missing a Feedback Text value.') as message, ",
                    "                  fi.import_file_id, ",
                    "                  fi.error_message ",
                    "           FROM ", ffiImportFileDataTable,
                    "           LEFT JOIN import_file_info fi ",
                    "           USING (import_file_id) ",
                    "           WHERE ((feedback_classification <> '') AND (feedback_classification IS NOT NULL)) ",
                    "                 AND ((feedback_text IS NULL) OR (feedback_text = '')) ",
                    "           ORDER BY line_num ",
                    "           ) t1 ",
                    "     GROUP BY  import_file_id ",
                    "     ) t2 ",
                    "USING (import_file_id) ",
                    "SET status = 'error', ",
                    "error_count = error_count + t2.fileErrorCount, ",
                    "error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");
                CALL exec(query);
            END IF;
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET fatalErrorMsg = 'In column Feedback Classification, error message field exceeded the maximum length.';
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_feedback_and_classification', fatalErrorMsg));
         CALL ffi_save_error_message_in_import_status(fatalErrorMsg, importStatusId, exitFlag);
         SET exitFlag = true;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_feedback_and_classification');

END $$


/*
 -----------------------------------------------------------------------------
  Verify problem_view column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_problem_view` $$
CREATE PROCEDURE `ffi_verify_problem_view` (IN importStatusId INT,
                                            IN numRows INT,
                                            IN threshold INT,
                                            IN ffiImportFileDataTable VARCHAR(25),
                                            OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'problem_view';
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_problem_view');
    CALL ffi_update_error_for_number_format(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_problem_view');
END $$

/*
 -----------------------------------------------------------------------------
  Verify help_level column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_help_level` $$
CREATE PROCEDURE `ffi_verify_help_level` (IN importStatusId INT,
                                          IN numRows INT,
                                          IN threshold INT,
                                          IN ffiImportFileDataTable VARCHAR(25),
                                          OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'help_level';
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_help_level');
    -- check for numberic format
    CALL ffi_update_error_for_number_format(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_help_level');
END $$

/*
 -----------------------------------------------------------------------------
  Verify total_num_hints column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_total_num_hints` $$
CREATE PROCEDURE `ffi_verify_total_num_hints` (IN importStatusId INT,
                                               IN numRows INT,
                                               IN threshold INT,
                                               IN ffiImportFileDataTable VARCHAR(25),
                                               OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(64) DEFAULT 'total_num_hints';
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_total_num_hints');
    -- check for numberic format
    CALL ffi_update_error_for_number_format(columnName, importStatusId, numRows, threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_total_num_hints');
END $$

/*
 -----------------------------------------------------------------------------
  Verify condition_name and condition_type column(s).
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_condition_name_and_type` $$
CREATE PROCEDURE `ffi_verify_condition_name_and_type` (IN importStatusId INT,
                                                       IN numRows INT,
                                                       IN threshold INT,
                                                       IN ffiImportFileDataTable VARCHAR(25),
                                                       OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE conditionNameColNum INT DEFAULT 1;
    DECLARE counter INT DEFAULT 0;
    DECLARE conditionConcatString TEXT DEFAULT '';
    DECLARE columnName VARCHAR(64) DEFAULT '';
    DECLARE conditionTypeColName VARCHAR(64) DEFAULT '';
    DECLARE conditionNameMaxLength INT DEFAULT 80;
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE fatalErrorMsg TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_condition_name_and_type');

    SET exitFlag = FALSE;
    -- get the total number of condition_name columns
    SELECT COUNT(*) INTO conditionNameColNum
    FROM ffi_heading_column_map
    WHERE standard_name = 'Condition Name';

    verify_condition: BEGIN

        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        -- loop through each column
        condition_name_loop: LOOP
            SET counter = counter + 1;

            IF counter > conditionNameColNum THEN
                LEAVE condition_name_loop;
            END IF;

            SELECT column_name INTO columnName
            FROM ffi_heading_column_map
            WHERE standard_name = 'Condition Name'
            AND sequence = counter;

            SELECT column_name INTO conditionTypeColName
            FROM ffi_heading_column_map
            WHERE standard_name = 'Condition Type'
            AND sequence = counter;

            -- check for max length - 80
            CALL ffi_update_error_for_max_length(columnName, conditionNameMaxLength, importStatusId, numRows,
                                                 threshold, ffiImportFileDataTable, exitFlag);

            IF exitFlag = TRUE THEN
                LEAVE condition_name_loop;
            END IF;

            -- if condition_type has a value, condition_name must have a value and not equals to empty string
            IF conditionTypeColName IS NOT NULL THEN
                SET conditionConcatString = CONCAT(" (((", conditionTypeColName, " IS NOT NULL) AND (",
                    conditionTypeColName, " <> '')) AND ((", columnName," IS NULL) OR (", columnName," = ''))) ");


                SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
                               "FROM ", ffiImportFileDataTable,
                               " WHERE ", conditionConcatString);
                CALL exec(query);

                -- All
                IF @totalErrorCount = numRows THEN
                    SET message = 'In column Condition Type, values are given but all rows are missing Condition Name values.';
                    CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
                -- Most
                ELSEIF @totalErrorCount > threshold THEN
                    SET query = CONCAT(
                        "SELECT CONCAT('For example in file ',"
                        "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                        "', Line ',(line_num - line_start + 2), ', ",
                        "Condition Type is given, but the row is missing a Condition Name value.') ",
                        "INTO @message ",
                        "FROM import_file_info ",
                        "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                        "WHERE ", conditionConcatString, " LIMIT 1;");
                    CALL exec(query);
                    SET message = CONCAT('In column Condition Type, values are given but ',
                                        @totalErrorCount, ' rows are missing Condition Name values.',
                                        '\n(', @message, ')');
                    CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
                -- Some
                ELSEIF @totalErrorCount > 0 THEN
                    SET query = CONCAT("UPDATE import_file_info i ",
                    "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                    "            t1.import_file_id, ",
                    "            COUNT(*) AS fileErrorCount ",
                    "        FROM ( ",
                    "            SELECT CONCAT('Line ', ",
                    "                (line_num - line_start + 2), ",
                    "                ', Condition Type is given, but the row is missing a Condition Name value.') as message, ",
                    "                fi.import_file_id, ",
                    "                fi.error_message ",
                    "            FROM ", ffiImportFileDataTable, " fd ",
                    "            LEFT JOIN import_file_info fi ",
                    "            USING (import_file_id) ",
                    "            WHERE ", conditionConcatString,
                    "            ORDER BY line_num ",
                    "        ) t1 ",
                    "    GROUP BY  import_file_id ",
                    "    ) t2 ",
                    "USING (import_file_id) ",
                    "SET status = 'error', ",
                    "error_count = error_count + t2.fileErrorCount, ",
                    "error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");
                    CALL exec(query);
                END IF;

                IF exitFlag = TRUE THEN
                    LEAVE condition_name_loop;
                END IF;

                SET conditionConcatString = '';
                SET conditionTypeColName = '';
                SET columnName = '';
            END IF;
        END LOOP condition_name_loop;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET fatalErrorMsg = 'In column Condition Type, error message field exceeded the maximum length.';
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_condition_name_and_type', fatalErrorMsg));
         CALL ffi_save_error_message_in_import_status(fatalErrorMsg, importStatusId, exitFlag);
         SET exitFlag = true;
    END IF;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_condition_name_and_type');
END $$

/*
 -----------------------------------------------------------------------------
  Verify kc and kc_category column(s).
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_kc_and_category` $$
CREATE PROCEDURE `ffi_verify_kc_and_category` (IN importStatusId INT,
                                               IN numRows INT,
                                               IN threshold INT,
                                               IN ffiImportFileDataTable VARCHAR(25),
                                               OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE kcColNum INT DEFAULT 1;
    DECLARE sameValueNum INT DEFAULT 0;
    DECLARE counter INT DEFAULT 0;
    DECLARE sameValueCounter INT DEFAULT 0;
    DECLARE kcColumnName VARCHAR(64);
    DECLARE tempKCColName TEXT;
    DECLARE kcColumnNameList TEXT;
    DECLARE kcCategoryColumnName VARCHAR(64);
    DECLARE kcColumnValue VARCHAR(50);
    DECLARE kcConcatString TEXT DEFAULT '';
    DECLARE kcCategoryMaxLength INT DEFAULT 50;
    DECLARE message TEXT;
    DECLARE header VARCHAR(64);
    DECLARE fatalErrorMsg TEXT;
    DECLARE query TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_kc_and_category');

    SET exitFlag = FALSE;

    -- get the total number of kc columns
    SELECT COUNT(*) INTO kcColNum
    FROM ffi_heading_column_map
    WHERE standard_name = 'KC';

    verify_kc: BEGIN
        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        -- loop through each column
        kc_loop: LOOP
            IF exceedMaxLengthFlag = TRUE THEN
               LEAVE kc_loop;
            END IF;

            SET counter = counter + 1;

            IF counter > kcColNum THEN
                LEAVE kc_loop;
            END IF;

            -- get kc column name
            SELECT column_name, column_value
            INTO kcColumnName, kcColumnValue
            FROM ffi_heading_column_map
            WHERE standard_name = 'KC'
            AND sequence = counter;
            -- get kc category column name
            SELECT column_name INTO kcCategoryColumnName
            FROM ffi_heading_column_map
            WHERE standard_name = 'KC Category'
            AND sequence = counter;

            IF (kcCategoryColumnName IS NOT NULL) AND (kcCategoryColumnName <> '') THEN
                -- check for max length - 50
                CALL ffi_update_warning_for_max_length(kcCategoryColumnName, kcCategoryMaxLength, importStatusId, numRows,
                                                       threshold, ffiImportFileDataTable, exitFlag);

                -- check if there are multiple kc columns with the same column_value
                -- group concat col name here
                SELECT COUNT(*), GROUP_CONCAT(column_name SEPARATOR ',')
                INTO sameValueNum, kcColumnNameList
                FROM ffi_heading_column_map
                WHERE standard_name = 'KC'
                AND column_value = kcColumnValue;

                IF sameValueNum > 1 THEN
                    SET sameValueCounter = 0;

                    kc_same_value_loop: LOOP

                      -- loop through kc columns that have the same value to build 'where' string
                      SET tempKCColName = SUBSTRING(kcColumnNameList, 1, LOCATE(',', kcColumnNameList) - 1);
                      IF tempKCColName = '' THEN
                          SET tempKCColName = kcColumnNameList;
                      END IF;
                      -- remove the column name that has been processed from the list
                      SET kcColumnNameList = SUBSTRING(kcColumnNameList, LOCATE(',', kcColumnNameList) + 1);

                      SET sameValueCounter = sameValueCounter + 1;

                      IF sameValueCounter > sameValueNum THEN
                          LEAVE kc_same_value_loop;
                      END IF;

                      IF sameValueCounter = 1 AND sameValueCounter = sameValueNum THEN
                          SET kcConcatString = CONCAT("(",tempKCColName," = '') ");
                      ELSEIF sameValueCounter = 1 THEN
                          SET kcConcatString = CONCAT("(",tempKCColName," = '' ");
                      ELSEIF sameValueCounter = sameValueNum THEN
                          SET kcConcatString = CONCAT(kcConcatString, " AND ", tempKCColName, " = '')");
                      ELSE
                          SET kcConcatString = CONCAT(kcConcatString, " AND ", tempKCColName, " = ''");
                      END IF;

                    END LOOP kc_same_value_loop;

                    SET kcConcatString = CONCAT(" (((", kcCategoryColumnName, " IS NOT NULL) AND (",
                    kcCategoryColumnName, " <> '')) AND ", kcConcatString, ") ");
                ELSE
                    -- construct concat string
                    SET kcConcatString = CONCAT(" (((", kcCategoryColumnName, " IS NOT NULL) AND (",
                    kcCategoryColumnName, " <> '')) AND ((", kcColumnName," IS NULL) OR (",  kcColumnName," = ''))) ");

                END IF;

                SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
                        "FROM ", ffiImportFileDataTable,
                        " WHERE ", kcConcatString);
                CALL exec(query);

                SET header = ffi_get_header(kcCategoryColumnName);
                -- All
                IF @totalErrorCount = numRows THEN
                    SET message = CONCAT('In column ', header, ', values are given but all rows are missing KC values.');
                    CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
                -- Most
                ELSEIF @totalErrorCount > threshold THEN
                    SET query = CONCAT(
                        "SELECT CONCAT('For example in file ',"
                        "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                        "', Line ',(line_num - line_start + 2), ', ",
                        header,
                        "', ' is given, but the row is missing a KC value.') ",
                        "INTO @message ",
                        "FROM import_file_info ",
                        "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                        "WHERE ", kcConcatString, " LIMIT 1;");
                    CALL exec(query);
                    SET message = CONCAT('In column ', header,', values are given but ', @totalErrorCount, ' rows are missing KC values.',
                                        '\n(', @message, ')');
                    CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
                -- Some
                ELSEIF @totalErrorCount > 0 THEN
                    SET query = CONCAT("UPDATE import_file_info i ",
                    "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                    "            t1.import_file_id, ",
                    "            COUNT(*) AS fileErrorCount ",
                    "        FROM ( ",
                    "            SELECT CONCAT('Line ', ",
                    "                (line_num - line_start + 2), ', ', ",
                    "                '", header, "', ' is given, but the row is missing a KC value.') as message, ",
                    "                fi.import_file_id, ",
                    "                fi.error_message ",
                    "            FROM ", ffiImportFileDataTable, " fd ",
                    "            LEFT JOIN import_file_info fi ",
                    "            USING (import_file_id) ",
                    "            WHERE ", kcConcatString,
                    "            ORDER BY line_num ",
                    "        ) t1 ",
                    "    GROUP BY  import_file_id ",
                    "    ) t2 ",
                    "USING (import_file_id) ",
                    "SET status = 'error', ",
                    "error_count = error_count + t2.fileErrorCount, ",
                    "error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");

                    CALL exec(query);
                END IF;
                -- reset string to next use
                SET kcConcatString = '';
                SET kcCategoryColumnName = '';
                SET kcColumnName = '';
            END IF;
        END LOOP kc_loop;
    END;
    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', error message field exceeded the maximum length.');
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_verify_kc_and_category', message));
         CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
         SET exitFlag = true;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_kc_and_category');
END $$

/*
 -----------------------------------------------------------------------------
  Verify school column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_school` $$
CREATE PROCEDURE `ffi_verify_school` (IN importStatusId INT,
                                      IN numRows INT,
                                      IN threshold INT,
                                      IN ffiImportFileDataTable VARCHAR(25),
                                      OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(6) DEFAULT 'school';
    DECLARE maxLength INT DEFAULT 100;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_school');
    -- check for max length 100, issue warning
    CALL ffi_update_warning_for_max_length(columnName, maxLength, importStatusId, numRows,
                                           threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_school');
END $$

/*
 -----------------------------------------------------------------------------
  Verify class column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_class` $$
CREATE PROCEDURE `ffi_verify_class` (IN importStatusId INT,
                                     IN numRows INT,
                                     IN threshold INT,
                                     IN ffiImportFileDataTable VARCHAR(25),
                                     OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName VARCHAR(5) DEFAULT 'class';
    DECLARE maxLength INT DEFAULT 75;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_class');
    -- check for max length 75, issue warning
    CALL ffi_update_warning_for_max_length(columnName, maxLength, importStatusId, numRows,
                                           threshold, ffiImportFileDataTable, exitFlag);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_class');
END $$

/*
 -----------------------------------------------------------------------------
  Verify custom field column(s).
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_verify_custom_field` $$
CREATE PROCEDURE `ffi_verify_custom_field` (IN importStatusId INT,
                                            IN numRows INT,
                                            IN threshold INT,
                                            IN ffiImportFileDataTable VARCHAR(25),
                                            OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE CFColNum INT DEFAULT 1;
    DECLARE counter INT DEFAULT 0;
    DECLARE columnName VARCHAR(64);
    DECLARE maxLength INT DEFAULT 65000;

    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_verify_custom_field');

    -- get cf column counts
    SELECT COUNT(*) INTO CFColNum
    FROM ffi_heading_column_map
    WHERE standard_name = 'CF';

    -- loop through each CF column
    cf_loop: LOOP
        SET counter = counter + 1;
        IF counter > CFColNum THEN
            LEAVE cf_loop;
        END IF;
        -- get the name of the column based on standard name and sequence
        SELECT column_name INTO columnName FROM ffi_heading_column_map
        WHERE standard_name = 'CF' AND sequence = counter;
        -- check for max length - 255, trim and warning if exceeds 255
        CALL ffi_update_warning_for_max_length(columnName, maxLength, importStatusId, numRows,
                                               threshold, ffiImportFileDataTable, exitFlag);
    END LOOP cf_loop;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_verify_custom_field');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the warning_count and warning_message of the import_status
  and/or import_file_info if column value is an empty string.
  If one the following is TRUE, update import_status,
  increase warning_count by 1, insert warning message:
  1.new message exceeds 65535 bytes
  2.warning count equals row count
  3.warning count exceeds threshold
  Otherwise, insert warning into import_file_info table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_warning_for_empty_string` $$
CREATE PROCEDURE `ffi_update_warning_for_empty_string` (IN columnName VARCHAR(64),
                                                        IN importStatusId INT,
                                                        IN numRows INT,
                                                        IN threshold INT,
                                                        IN ffiImportFileDataTable VARCHAR(25),
                                                        OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE message TEXT;
    DECLARE criteria TEXT;
    DECLARE query TEXT;
    DECLARE fileWarningCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    BEGIN
        -- Data too long for column 'warning_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_warning_for_empty_string');

        SET exitFlag = FALSE;
        SET header = ffi_get_header(columnName);

        CALL debug_log('ffi_verify_data_sp', CONCAT('header: ', header));
        CALL debug_log('ffi_verify_data_sp', 'before criteria');
        SET criteria = CONCAT("(", columnName, " IS NULL) OR (", columnName, " = '')");
        CALL debug_log('ffi_verify_data_sp', criteria);

        SET query = CONCAT("SELECT COUNT(*) INTO @totalWarningCount",
            " FROM ", ffiImportFileDataTable,
            " WHERE ", criteria);
        CALL exec(query);

        -- All
        IF @totalWarningCount = numRows THEN
            SET message = CONCAT('In column ', header, ', all values are empty.');
            CALL debug_log('ffi_verify_data_sp', message);
            CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                    criteria, ffiImportFileDataTable, exitFlag);
        -- Most
        ELSEIF @totalWarningCount > threshold THEN
            CALL debug_log('ffi_verify_data_sp', 'most');
            SET query = CONCAT(
                        "SELECT CONCAT('For example in file ',"
                        "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ),"
                        "', Line ',(line_num - line_start + 2), ', ",
                        "column ', ",
                        "'", header, "', ' is empty.') ",
                        "INTO @message ",
                        "FROM import_file_info ",
                        "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                        "WHERE (", columnName, " IS NULL) OR (", columnName, " = '') LIMIT 1");
            CALL exec(query);
            SET message = CONCAT('In column ', header,', ', @totalWarningCount, ' values are empty.',
                          '\n(', @message, ')');
            CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                    criteria, ffiImportFileDataTable, exitFlag);
        -- Some
        ELSEIF @totalWarningCount > 0 THEN
            CALL debug_log('ffi_verify_data_sp', 'some');
            SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                            " t1.import_file_id, ",
                            " COUNT(*) AS fileWarningCount ",
                     " FROM ( ",
                           " SELECT CONCAT('Line ', ",
                               " (line_num - line_start + 2), ",
                               " ', column ', ",
                               " '", header, "', ' is empty.') as message, ",
                               " fi.import_file_id, ",
                               " fi.warning_message ",
                           " FROM ", ffiImportFileDataTable,
                           " LEFT JOIN import_file_info fi ",
                           " USING (import_file_id) ",
                           " WHERE ", columnName, " = '' ",
                           " ORDER BY line_num ",
                          " ) t1 ",
                     " GROUP BY  import_file_id ",
                   " ) t2 ",
                "USING (import_file_id) ",
                 "SET warning_count = warning_count + t2.fileWarningCount, ",
                    " warning_message = CONCAT(IFNULL(warning_message, ''), '\n', t2.message)");
            CALL exec(query);
        END IF;
    END;

    -- handle warning if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', warning message field exceeded max length');

         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_warning_for_empty_string ', message));

         CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                    criteria, ffiImportFileDataTable, exitFlag);

         SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_warning_for_empty_string');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the error_count and error_message of the import_status
  and/or import_file_info if column value is an empty string.
  If one the following is TRUE, update import_status,
  increase error_count by 1, insert error message:
  1.new message exceeds 65535 bytes
  2.error count equals row count
  3.error count exceeds threshold
  Otherwise, insert error into import_file_info table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_error_for_empty_string` $$
CREATE PROCEDURE `ffi_update_error_for_empty_string` (IN columnName VARCHAR(64),
                                                      IN importStatusId INT,
                                                      IN numRows INT,
                                                      IN threshold INT,
                                                      IN ffiImportFileDataTable VARCHAR(25),
                                                      OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    BEGIN
        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', CONCAT('Starting ffi_update_error_for_empty_string, column: ', columnName));

        SET exitFlag = FALSE;
        SET header = ffi_get_header(columnName);

        SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
            "FROM ", ffiImportFileDataTable,
            " WHERE (", columnName, " IS NULL) OR (", columnName, " = '')");
        CALL exec(query);

        -- All
        IF @totalErrorCount = numRows THEN
            CALL debug_log('ffi_verify_data_sp', 'all are empty');
            SET message = CONCAT('In column ', header, ', all values are empty.');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Most
        ELSEIF @totalErrorCount > threshold THEN
            CALL debug_log('ffi_verify_data_sp', 'most are empty');
            SET query = CONCAT(
                        "SELECT CONCAT('For example in file ',"
                        "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ),"
                        "', Line ',(line_num - line_start + 2), ', ",
                        "column ', ",
                        "'", header, "', ' is empty.') ",
                        "INTO @message ",
                        "FROM import_file_info ",
                        "JOIN ", ffiImportFileDataTable, " USING (import_file_id) ",
                        "WHERE (", columnName, " IS NULL) OR (", columnName, " = '') LIMIT 1");
            CALL exec(query);
            SET message = CONCAT('In column ', header,', ', @totalErrorCount, ' values are empty.',
                          '\n(', @message, ')');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Some
        ELSEIF @totalErrorCount > 0 THEN
            CALL debug_log('ffi_verify_data_sp', 'some are empty');
            SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                              " t1.import_file_id, ",
                              " COUNT(*) AS fileErrorCount ",
                              " FROM ( ",
                                       " SELECT CONCAT('Line ', ",
                                       " (line_num - line_start + 2), ",
                                       " ', column ', ",
                                       " '", header, "', ' is empty.') as message, ",
                                       " fi.import_file_id, ",
                                       " fi.error_message ",
                                       " FROM ", ffiImportFileDataTable,
                                       " LEFT JOIN import_file_info fi ",
                                       " USING (import_file_id) ",
                                       " WHERE ", columnName, " = '' ",
                                       " ORDER BY line_num ",
                                   " ) t1 ",
                              " GROUP BY  import_file_id ",
                    " ) t2 ",
                " USING (import_file_id) ",
                " SET status = 'error', ",
                    " error_count = error_count + t2.fileErrorCount, ",
                    " error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");
            CALL exec(query);
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', error message field exceeded max length');

         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_error_for_empty_string ', message));

         CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);

         SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_error_for_empty_string');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the error_count and error_message of the import_status
  and/or import_file_info if column value exceeds max length.
  If one the following is TRUE, update import_status,
  increase error_count by 1, insert error message, and set exitFlag to TRUE:
  1.new message exceeds 65535 bytes
  2.error count equals row count
  3.error count exceeds threshold
  Otherwise, insert error into import_file_info table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_error_for_max_length` $$
CREATE PROCEDURE `ffi_update_error_for_max_length` (IN columnName TEXT,
                                                    IN maxLength INT,
                                                    IN importStatusId INT,
                                                    IN numRows INT,
                                                    IN threshold INT,
                                                    IN ffiImportFileDataTable VARCHAR(25),
                                                    OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE message TEXT;
    DECLARE criteria TEXT;
    DECLARE query TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    BEGIN
        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_error_for_max_length');

        SET exitFlag = FALSE;

        SET header = ffi_get_header(columnName);

        SET criteria = CONCAT("LENGTH(", columnName, ") > ", maxLength, " ");

        SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
            "FROM ", ffiImportFileDataTable,
            " WHERE ", criteria);
        CALL exec(query);
        -- All
        IF @totalErrorCount = numRows THEN
            SET message = CONCAT('In column ', header,
                                 ', all values exceed the maximum length (', maxLength, ' characters).');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Most
        ELSEIF @totalErrorCount > threshold THEN
           SET query = CONCAT(
                "SELECT CONCAT('For example in file ',"
                "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                "', Line ',(line_num - line_start + 2), ', ",
                "column ', '", header , "', ': ', ",
                " SUBSTRING(", columnName,", 1, 30), '...') ",
                " INTO @message ",
                " FROM import_file_info ",
                " JOIN ( SELECT import_file_id, line_num, ", columnName,
                " FROM ", ffiImportFileDataTable,
                " WHERE ", criteria,
                " LIMIT 1 ) t1 ",
                " USING (import_file_id)");
            CALL exec(query);
            SET message = CONCAT('In column ', header,', ', @totalErrorCount,
                                 ' values exceed the maximum length (', maxLength, ' characters).',
                                 '\n(', @message, ')');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Some
        ELSEIF @totalErrorCount > 0 THEN
            SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                "            t1.import_file_id, ",
                "            COUNT(*) AS fileErrorCount ",
                "        FROM ( ",
                "            SELECT CONCAT('Line ', ",
                "                (line_num - line_start + 2), ",
                "                ', column ', ",
                "                '", header, "', ' exceeds the maximum length (', '",
                                 maxLength, "', ' characters).') as message, ",
                "                fi.import_file_id, ",
                "                fi.error_message ",
                "            FROM ", ffiImportFileDataTable, " fd ",
                "            LEFT JOIN import_file_info fi ",
                "            USING (import_file_id) ",
                "            WHERE ", criteria,
                "            ORDER BY line_num ",
                "        ) t1 ",
                "    GROUP BY  import_file_id ",
                "    ) t2 ",
                "USING (import_file_id) ",
                "SET status = 'error', ",
                "error_count = error_count + t2.fileErrorCount, ",
                "error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");
            CALL exec(query);
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', error message field exceeded the maximum length.');
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_error_for_max_length - ', message));
         CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
         SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_error_for_max_length');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the error_count and error_message of the import_status
  and/or import_file_info if column value is not a number.
  If one the following is TRUE, update import_status,
  increase error_count by 1, insert error message, and set exitFlag to TRUE:
  1.new message exceeds 65535 bytes
  2.error count equals row count
  3.error count exceeds threshold
  Otherwise, insert error into import_file_info table.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_error_for_number_format` $$
CREATE PROCEDURE `ffi_update_error_for_number_format` (IN columnName TEXT,
                                                       IN importStatusId INT,
                                                       IN numRows INT,
                                                       IN threshold INT,
                                                       IN ffiImportFileDataTable VARCHAR(25),
                                                       OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE criteria TEXT;
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE fileErrorCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;

    BEGIN
        -- Data too long for column 'error_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_error_for_number_format');

        SET exitFlag = FALSE;
        SET header = ffi_get_header(columnName);

        SET criteria = CONCAT(columnName, " <> '' AND ", columnName, " NOT REGEXP  '^[0-9]+$' ");

        SET query = CONCAT("SELECT COUNT(*) INTO @totalErrorCount ",
            "FROM ", ffiImportFileDataTable,
            " WHERE ", criteria);
        CALL exec(query);

        SET query = CONCAT(
            "SELECT GROUP_CONCAT('For example in file ',",
            "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), ",
            "', Line ',(line_num - line_start + 2), ",
            "': ', ",
            " SUBSTRING(", columnName,", 1, 30) SEPARATOR ', \n') ",
            " INTO @topFiveValues ",
            " FROM import_file_info ",
            " JOIN ( SELECT import_file_id, line_num, ", columnName,
            " FROM ", ffiImportFileDataTable,
            " WHERE ", criteria,
            " LIMIT 5 ) t1 ",
            " USING (import_file_id)");
        CALL exec(query);
        -- All
        IF @totalErrorCount = numRows THEN
            SET message = CONCAT('In column ', header, ', none of the values are numbers:\n(',@topFiveValues,')');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Most
        ELSEIF @totalErrorCount > threshold THEN
            SET message = CONCAT('In column ', header,', ', @totalErrorCount, ' values are not numbers:\n(',@topFiveValues, ')');
            CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
        -- Some
        ELSEIF @totalErrorCount > 0 THEN
            SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                "            t1.import_file_id, ",
                "            COUNT(*) AS fileErrorCount ",
                "        FROM ( ",
                "            SELECT CONCAT('Line ', ",
                "                (line_num - line_start + 2), ",
                "                ', column ', ",
                "                '", header, "', ' is not a number (', SUBSTRING(fd.", columnName, ", 1, 30), ').') as message, ",
                "                fi.import_file_id, ",
                "                fi.error_message ",
                "            FROM ", ffiImportFileDataTable, " fd ",
                "            LEFT JOIN import_file_info fi ",
                "            USING (import_file_id) ",
                "            WHERE ", criteria,
                "            ORDER BY line_num ",
                "        ) t1 ",
                "    GROUP BY  import_file_id ",
                "    ) t2 ",
                "USING (import_file_id) ",
                "SET status = 'error', ",
                "error_count = error_count + t2.fileErrorCount, ",
                "error_message =  CONCAT(IFNULL(error_message, ''), '\n', t2.message)");
            CALL exec(query);
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', error message field exceeded the maximum length.');
         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_error_for_number_format', message));
         CALL ffi_save_error_message_in_import_status(message, importStatusId, exitFlag);
         SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_error_for_number_format');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the warning_count and warning_message of the import_status
  and/or import_file_info if column value exceeds max length.
  If one of the following is TRUE, update import_status table, and keep counting in import_file_info:
  1. new message exceeds 65535 bytes
  2. warning count equals row count
  3. warning count exceeds threshold
  Otherwise, update import_file_info
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_warning_for_unexpected_value` $$
CREATE PROCEDURE `ffi_update_warning_for_unexpected_value` (IN columnName TEXT,
                                                            IN importStatusId INT,
                                                            IN numRows INT,
                                                            IN threshold INT,
                                                            IN ffiImportFileDataTable VARCHAR(25),
                                                            OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE query2 TEXT;
    DECLARE customQuery TEXT;
    DECLARE fileWarningCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;
    DECLARE criteria TEXT;
    DECLARE postMsg VARCHAR(125) DEFAULT '';
    BEGIN
        -- Data too long for column 'warning_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_warning_for_unexpected_value');

        SET exitFlag = FALSE;
        SET header = ffi_get_header(columnName);

        -- Any valid timestamp that ends with text (AM/PM/timezone) is flagged as warning.
        -- Except for the format that starts with a named month, i.e., "July 04, 2001".
        IF columnName = 'time' THEN
            SET criteria = CONCAT("((time REGEXP ' [A-Za-z]+$') ",
                "AND !(time REGEXP '^[A-Za-z]+ .* [A-Z]+$')) ",
                "OR time REGEXP 'GMT'");

        ELSEIF columnName = 'problem_start_time' THEN
            SET criteria = CONCAT("((problem_start_time REGEXP ' [A-Za-z]+$') ",
                "AND !(problem_start_time REGEXP '^[A-Za-z]+ .* [A-Z]+$')) ",
                "OR problem_start_time REGEXP 'GMT'");

        ELSEIF columnName = 'student_response_type' THEN
            SET criteria = CONCAT(" LOWER(student_response_type) NOT IN ('', 'attempt', 'hint_request') ");

        ELSEIF columnName = 'tutor_response_type' THEN
            SET criteria = CONCAT(" LOWER(tutor_response_type) NOT IN ('', 'result', 'hint_msg') ");

        ELSEIF columnName = 'outcome' THEN
            SET criteria = CONCAT(" LOWER(outcome) NOT IN ('', 'ok', 'correct', 'error', 'bug', 'incorrect') ",
                       "AND LOWER(outcome) NOT LIKE '%help%' ",
                       "AND LOWER(outcome) NOT LIKE '%hint%' ");

        -- Single quotes in anon_student_id are flagged as warning.
        ELSEIF columnName = 'anon_student_id' THEN
            SET criteria = CONCAT(" anon_student_id REGEXP '\\'' ");

        -- Single quotes in level columns are flagged as warning.
        ELSEIF (SUBSTRING(columnName, 1, 5) = 'level') THEN
            SET criteria = CONCAT(" ", columnName, " REGEXP '\\'' ");

        END IF;

        SET query = CONCAT("SELECT COUNT(*) INTO @totalWarningCount ",
            " FROM ", ffiImportFileDataTable,
            " WHERE ", criteria);
        CALL exec(query);

        IF @totalWarningCount > 0 THEN
            SET query = CONCAT(
            "SELECT GROUP_CONCAT('For example in file ',",
            "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), ",
            "', Line ',(line_num - line_start + 2), ",
            "': ', ",
            " SUBSTRING(", columnName,", 1, 30) SEPARATOR ', \n') ",
            " INTO @topFiveValues ",
            " FROM import_file_info ",
            " JOIN ( SELECT import_file_id, line_num, ", columnName,
            " FROM ", ffiImportFileDataTable,
            " WHERE ", criteria,
            " LIMIT 5 ) t1 ",
            " USING (import_file_id)");
            CALL exec(query);
            -- All
            IF @totalWarningCount = numRows THEN

                IF columnName = 'time'
                OR columnName = 'problem_start_time' THEN
                    SET message = CONCAT('In column ', header,
                    ', all values include additional information which will be ignored, such as AM/PM or timezone \n(',
                    @topFiveValues, ').');
                ELSEIF columnName = 'student_response_type'
                    OR columnName = 'tutor_response_type'
                    OR columnName = 'outcome' THEN

                    SET message = CONCAT('In column ', header,
                    ', none of the values are those that DataShop expects \n(',
                    @topFiveValues,'). \n',
                    'If you import this data as it is, some tools in DataShop will not work properly.');
                ELSEIF columnName = 'anon_student_id'
                    OR (SUBSTRING(columnName, 1, 5) = 'level') THEN
                    SET message = CONCAT('In column ', header,
                    ', all values include single quotes \n(',
                    @topFiveValues,'). \n',
                    'If you import this data as it is, DataShop will remove the single quotes.');
                END IF;
                CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                                                               criteria, ffiImportFileDataTable, exitFlag);
            -- Most
            ELSEIF @totalWarningCount > threshold THEN

                IF columnName = 'time'
                OR columnName = 'problem_start_time' THEN
                    SET message = CONCAT('In column ', header,', ', @totalWarningCount,
                    ' values include additional information which will be ignored, such as AM/PM or timezone \n(',
                    @topFiveValues, ').');
                ELSEIF columnName = 'student_response_type'
                    OR columnName = 'tutor_response_type'
                    OR columnName = 'outcome' THEN
                    SET message = CONCAT('In column ', header,', ',  @totalWarningCount,
                     ' values are not those that DataShop expects \n(',
                    @topFiveValues, ').\n',
                    'If you import this data as it is, some tools in DataShop will not work properly.');
                ELSEIF columnName = 'anon_student_id'
                    OR (SUBSTRING(columnName, 1, 5) = 'level') THEN
                    SET message = CONCAT('In column ', header,', ',  @totalWarningCount,
                     ' values include single quotes \n(',
                    @topFiveValues, ').\n',
                    'If you import this data as it is, DataShop will remove the single quotes.');
                END IF;
                CALL ffi_save_warning_message_in_import_status(message, importStatusId,
                                                               criteria, ffiImportFileDataTable, exitFlag);
            -- Some
            ELSEIF @totalWarningCount > 0 THEN
                IF columnName = 'time' THEN
                    SET customQuery = "', ' includes additional information which will be ignored, such as AM/PM or timezone (', fd.time,').') as message, ";
                ELSEIF columnName = 'problem_start_time' THEN
                    SET customQuery = "', ' includes additional information which will be ignored, such as AM/PM or timezone (', fd.problem_start_time,').') as message, ";
                ELSEIF columnName = 'student_response_type'
                    OR columnName = 'tutor_response_type'
                    OR columnName = 'outcome' THEN

                    SET customQuery = CONCAT("', ' contains a value that DataShop does not expect (',
                        SUBSTRING(fd.", columnName,", 1, 30), ').') as message, ");
                    SET postMsg =
                        'If you import this data as it is, some tools in DataShop will not work properly.';

                ELSEIF columnName = 'anon_student_id'
                    OR (SUBSTRING(columnName, 1, 5) = 'level') THEN
                    SET customQuery = CONCAT("', ' contains a single quote (',
                        SUBSTRING(fd.", columnName,", 1, 30), ').') as message, ");
                    SET postMsg =
                        'If you import this data as it is, DataShop will remove those quotes.';

                END IF;

                SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                "            t1.import_file_id, ",
                "            COUNT(*) AS fileWarningCount ",
                "        FROM ( ",
                "            SELECT CONCAT('Line ', ",
                "                (line_num - line_start + 2), ",
                "                ', column ', ",
                "                '", header, customQuery,
                "                fi.import_file_id, ",
                "                fi.warning_message ",
                "            FROM ", ffiImportFileDataTable, " fd ",
                "            LEFT JOIN import_file_info fi ",
                "            USING (import_file_id) ",
                "            WHERE ", criteria ,
                "            ORDER BY line_num ",
                "        ) t1 ",
                "    GROUP BY  import_file_id ",
                "    ) t2 ",
                "USING (import_file_id) ",
                "SET warning_count = warning_count + t2.fileWarningCount, ",
                "warning_message =  CONCAT(IFNULL(warning_message, ''), '\n', t2.message)");
                CALL exec(query);

                -- Couldn't get the silly quotes right; writing query in two pieces.
                SET query2 = CONCAT("CONCAT(IFNULL(ifi.warning_message, ''), '\n', ",
                                    "'",
                                    postMsg,
                                    "')");

                SET query = CONCAT("UPDATE import_file_info ifi ",
                                   "JOIN import_status i USING(import_status_id) ",
                                   "SET ifi.warning_message = ",
                                   query2,
                                   " WHERE i.import_status_id = ",
                                   importStatusId);
                CALL exec(query);

            END IF;
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', warning message field exceeded max length');

         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_warning_for_unexpected_value - ', message));

         CALL ffi_save_warning_message_in_import_status(message, importStatusId, criteria, ffiImportFileDataTable, exitFlag);
    END IF;
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_warning_for_unexpected_value');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to update the waring_count and warnig_message of the import_status
  and/or import_file_info if column value exceeds max length.
  If one of the following is TRUE, update import_status table, and keep counting in import_file_info:
  1. new message exceeds 65535 bytes
  2. warning count equals row count
  3. warning count exceeds threshold
  Otherwise, update import_file_info
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_warning_for_max_length` $$
CREATE PROCEDURE `ffi_update_warning_for_max_length` (IN columnName TEXT,
                                                      IN maxLength INT,
                                                      IN importStatusId INT,
                                                      IN numRows INT,
                                                      IN threshold INT,
                                                      IN ffiImportFileDataTable VARCHAR(25),
                                                      OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(75);
    DECLARE message TEXT;
    DECLARE query TEXT;
    DECLARE fileWarningCount  INT DEFAULT 0;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;
    DECLARE criteria TEXT;
    BEGIN
        -- Data too long for column 'warning_message'
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_warning_for_max_length');

        SET exitFlag = FALSE;

        SET header = ffi_get_header(columnName);

        SET criteria = CONCAT("LENGTH(", columnName, ") > ", maxLength, " ");

        SET query = CONCAT("SELECT COUNT(*) INTO @totalWarningCount ",
            "FROM ", ffiImportFileDataTable,
            " WHERE ", criteria);
        CALL exec(query);

        -- All
        IF @totalWarningCount = numRows THEN
            SET message = CONCAT('In column ', header, ', all values exceeded the maximum length (', maxLength,' characters).');
            CALL ffi_save_warning_message_in_import_status(message, importStatusId, criteria, ffiImportFileDataTable, exitFlag);
        -- Most
        ELSEIF @totalWarningCount > threshold THEN
            SET query = CONCAT(
                "SELECT CONCAT('For example in file ',"
                "trim(LEADING 'head_' FROM substring_index(file_name, '/', -1) ), "
                "', Line ',(line_num - line_start + 2), ', ",
                "column ', '", header , "', ': ', ",
                " SUBSTRING(", columnName,", 1, 30), '...') ",
                " INTO @message ",
                " FROM import_file_info ",
                " JOIN ( SELECT import_file_id, line_num, ", columnName,
                " FROM ", ffiImportFileDataTable,
                " WHERE ", criteria,
                " LIMIT 1 ) t1 ",
                " USING (import_file_id)");
            CALL exec(query);

            SET message = CONCAT('In column ', header,', ', @totalWarningCount,
                ' values exceeded the maximum length (', maxLength,' characters):',
                '\n(', @message, ')');

            CALL ffi_save_warning_message_in_import_status(message, importStatusId, criteria, ffiImportFileDataTable, exitFlag);

            CALL ffi_trim_to_max_length(columnName, maxLength, ffiImportFileDataTable);
        -- Some
        ELSEIF @totalWarningCount > 0 THEN
            SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT GROUP_CONCAT(t1.message SEPARATOR '\n') as message, ",
                "            t1.import_file_id, ",
                "            COUNT(*) AS fileWarningCount ",
                "        FROM ( ",
                "            SELECT CONCAT('Line ', ",
                "                (line_num - line_start + 2), ",
                "                ', column ', ",
                "                '", header, "', ' exceeds the maximum length (', '",
                                 maxLength,"', ' characters).') as message, ",
                "                fi.import_file_id, ",
                "                fi.warning_message ",
                "            FROM ", ffiImportFileDataTable, " fd ",
                "            LEFT JOIN import_file_info fi ",
                "            USING (import_file_id) ",
                "            WHERE ",  criteria,
                "            ORDER BY line_num ",
                "        ) t1 ",
                "    GROUP BY  import_file_id ",
                "    ) t2 ",
                "USING (import_file_id) ",
                "SET warning_count = warning_count + t2.fileWarningCount, ",
                "warning_message =  CONCAT(IFNULL(warning_message, ''), '\n', t2.message)");
            CALL exec(query);

            CALL ffi_trim_to_max_length(columnName, maxLength, ffiImportFileDataTable);
        END IF;
    END;

    -- handle error if message exceeds 65535 bytes
    IF exceedMaxLengthFlag = TRUE THEN
         SET message = CONCAT('In column ', header, ', warning message field exceeded max length');

         CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_update_warning_for_max_length - ', message));

         CALL ffi_save_warning_message_in_import_status(message, importStatusId, criteria, ffiImportFileDataTable, exitFlag);
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_warning_for_max_length');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to trim the column value to its max length.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_trim_to_max_length` $$
CREATE PROCEDURE `ffi_trim_to_max_length` (IN columnName TEXT, IN maxLength INT, IN ffiImportFileDataTable VARCHAR(25))
    SQL SECURITY INVOKER
BEGIN
    DECLARE query TEXT;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_trim_to_max_length');
    SET query = CONCAT("UPDATE ", ffiImportFileDataTable, " SET ", columnName,
        " = SUBSTRING(", columnName,", 1, ", maxLength,") "
        "WHERE LENGTH(", columnName, ") > ", maxLength);
    CALL exec(query);
    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_trim_to_max_length');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to save the error_message and increase error_count by 1 in the import_status table.
  If the total message length exceeds 65535, truncate the existing message,
  append the new message to its end.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_save_error_message_in_import_status` $$
CREATE PROCEDURE `ffi_save_error_message_in_import_status` (IN message TEXT,
                                                            IN importStatusId INT,
                                                            OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE importErrorCount INT DEFAULT 0;
    DECLARE existingMsg TEXT;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;
    DECLARE fatalErrorMsg TEXT;

    BEGIN
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_save_error_message_in_import_status');

        SELECT IFNULL(error_message, ''), error_count
        INTO existingMsg, importErrorCount
        FROM import_status
        WHERE import_status_id = importStatusId;

        -- if error count = 100, insert message and exit the program
        IF importErrorCount = 100 THEN
            SET fatalErrorMsg = 'The number of errors exceeds 100.';

            UPDATE import_status
            SET status = 'error',
                error_count = error_count + 1,
                error_message = CONCAT(existingMsg, '\n', fatalErrorMsg)
            WHERE import_status_id = importStatusId;

            SET exitFlag = TRUE;
        ELSEIF importErrorCount < 100 THEN
            UPDATE import_status
            SET status = 'error',
                error_count = error_count + 1,
                error_message = CONCAT(existingMsg, '\n', IFNULL(message, ''))
            WHERE import_status_id = importStatusId;
        END IF;
    END;

    IF exceedMaxLengthFlag = TRUE THEN
        SET fatalErrorMsg = CONCAT('Too many errors have occurred. Error message field exceeded the maximum length.');

        CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_save_error_message_in_import_status - ', fatalErrorMsg));

        -- truncate message and this message instead
        IF LENGTH(existingMsg) > LENGTH(message) THEN
            UPDATE import_status
            SET status = 'error',
                error_count = error_count + 1,
                error_message = CONCAT(SUBSTRING(exsitingMsg, 1, CHAR_LENGTH(existingMsg) - CHAR_LENGTH(message) - 1),
                                '\n', message)
            WHERE import_status_id = importStatusId;
        ELSE
            UPDATE import_status
            SET status = 'error',
            error_count = error_count + 1,
            error_message = SUBSTRING(CONCAT(exsitingMsg, '\n', message), 1, 65535)
            WHERE import_status_id = importStatusId;
        END IF;
        SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_save_error_message_in_import_status');
END $$

/*
  -------------------------------------------------------------------------------
  Procedure to save the warning_message and increase warning_count by 1 in the import_status table.
  If the total message length exceeds 65535, truncate the existing message,
  append the new message to its end.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_update_warning_count_only` $$
CREATE PROCEDURE `ffi_update_warning_count_only` (IN criteria TEXT, IN ffiImportFileDataTable VARCHAR(25))
    SQL SECURITY INVOKER
BEGIN
    DECLARE query TEXT;
    CALL debug_log('ffi_verify_data_sp', 'Starting ffi_update_warning_count_only');

    -- keep counting the warning
    SET query = CONCAT("UPDATE import_file_info i ",
                "JOIN (SELECT t1.import_file_id, ",
                "            COUNT(*) AS fileWarningCount ",
                "        FROM ( ",
                "            SELECT fi.import_file_id, ",
                "                fi.error_message ",
                "            FROM ", ffiImportFileDataTable, " fd ",
                "            LEFT JOIN import_file_info fi ",
                "            USING (import_file_id) ",
                "            WHERE ", criteria,
                "            ORDER BY line_num ",
                "        ) t1 ",
                "    GROUP BY  import_file_id ",
                "    ) t2 ",
                "USING (import_file_id) ",
                "SET warning_count = warning_count + t2.fileWarningCount");
    CALL exec(query);

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_update_warning_count_only');
END $$



/*
  -------------------------------------------------------------------------------
  Procedure to save the warning_message and increase warning_count by 1 in the import_status table.
  If the total message length exceeds 65535, truncate the existing message,
  append the new message to its end.
  -------------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_save_warning_message_in_import_status` $$
CREATE PROCEDURE `ffi_save_warning_message_in_import_status` (IN message TEXT,
                                                              IN importStatusId INT,
                                                              IN criteria TEXT,
                                                              IN ffiImportFileDataTable VARCHAR(25),
                                                              OUT exitFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    DECLARE importWarningCount INT DEFAULT 0;
    DECLARE existingMsg TEXT;
    DECLARE exceedMaxLengthFlag BOOLEAN DEFAULT FALSE;
    DECLARE fatalWarningMsg TEXT;

        CALL debug_log('ffi_verify_data_sp', 'Starting ffi_save_warning_message_in_import_status');
    BEGIN
        DECLARE EXIT HANDLER FOR SQLSTATE '22001' SET exceedMaxLengthFlag = TRUE;

        SELECT warning_count, IFNULL(warning_message, '')
        INTO importWarningCount, existingMsg
        FROM import_status
        WHERE import_status_id = importStatusId;

        -- if warning count = 100, insert message and exit the program
        IF importWarningCount = 100 THEN
            SET fatalWarningMsg = 'The number of warnings exceeds 100.';

            UPDATE import_status
            SET warning_count = warning_count + 1,
                warning_message = CONCAT(existingMsg, '\n', fatalWarningMsg)
            WHERE import_status_id = importStatusId;

            -- keep counting the warning
            CALL ffi_update_warning_count_only(criteria, ffiImportFileDataTable);

            SET exitFlag = TRUE;
        ELSEIF importWarningCount < 100 THEN

            UPDATE import_status
            SET warning_count = warning_count + 1,
                warning_message = CONCAT(existingMsg, '\n', message)
            WHERE import_status_id = importStatusId;
        END IF;
    END;

    IF exceedMaxLengthFlag = TRUE THEN
        SET fatalWarningMsg = CONCAT('Too many warnings have occurred. Warning message field exceeded the maximum length.');

        CALL debug_log('ffi_verify_data_sp', CONCAT('ffi_save_warning_message_in_import_status - ', fatalWarningMsg));

        -- truncate message and this message instead
        IF LENGTH(existingMsg) > LENGTH(message) THEN
            UPDATE import_status
            SET warning_count = warning_count + 1,
                warning_message = CONCAT(SUBSTRING(exsitingMsg, 1, CHAR_LENGTH(existingMsg) - CHAR_LENGTH(message) - 1),
                                '\n', message)
            WHERE import_status_id = importStatusId;
        ELSE
            UPDATE import_status
            SET warning_count = warning_count + 1,
                warning_message = SUBSTRING(CONCAT(exsitingMsg, '\n', message), 1, 65535)
            WHERE import_status_id = importStatusId;
        END IF;
        SET exitFlag = TRUE;
    END IF;

    CALL debug_log('ffi_verify_data_sp', 'Finished ffi_save_warning_message_in_import_status');
END $$


/*
  -------------------------------------------------------------------------------
  Function to return the header based on its column name.
  -------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_get_header` $$
CREATE FUNCTION `ffi_get_header` (columnName VARCHAR(64))
    RETURNS VARCHAR(64)
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE header VARCHAR(64);
    IF columnName = 'problem_name' THEN
       SET header = 'Problem Name';
    ELSEIF columnName = 'problem_view' THEN
       SET header = 'Problem View';
    ELSEIF columnName = 'problem_start_time' THEN
       SET header = 'Problem Start Time';
    ELSEIF columnName = 'student_response_type' THEN
       SET header = 'Student Response Type';
    ELSEIF columnName = 'student_response_subtype' THEN
       SET header = 'Student Response Subtype';
    ELSEIF columnName = 'tutor_response_type' THEN
       SET header = 'Tutor Response Type';
    ELSEIF columnName = 'tutor_response_subtype' THEN
       SET header = 'Tutor Response Subtype';
    ELSEIF columnName = 'outcome' THEN
       SET header = 'Outcome';
    ELSEIF columnName = 'anon_student_id' THEN
       SET header = 'Anon Student Id';
    ELSEIF columnName = 'session_id' THEN
       SET header = 'Session Id';
    ELSEIF columnName = 'time' THEN
       SET header = 'Time';
    ELSEIF columnName = 'time_zone' THEN
       SET header = 'Time Zone';
    ELSEIF (LEFT(columnName, 6) = 'level_')
       OR (LEFT(columnName, 3) = 'kc_')
       OR (LEFT(columnName, 3) = 'cf_')
       OR (LEFT(columnName, 10) = 'selection_')
       OR (LEFT(columnName, 7) = 'action_')
       OR (LEFT(columnName, 10) = 'condition_') THEN

       SELECT heading INTO header
       FROM ffi_heading_column_map
       WHERE column_name = columnName;

    ELSEIF columnName = 'feedback_text' THEN
       SET header = 'Feedback Text';
    ELSEIF columnName = 'feedback_classification' THEN
       SET header = 'Feedback Classification';
    ELSEIF columnName = 'help_level' THEN
       SET header = 'Help Level';
    ELSEIF columnName = 'total_num_hints' THEN
       SET header = 'Total Num Hints';
    ELSEIF columnName = 'school' THEN
       SET header = 'School';
    ELSEIF columnName = 'class' THEN
       SET header = 'Class';
    ELSE
       SET header = 'unknown';
    END IF;
    RETURN header;
END $$




DELIMITER ;
