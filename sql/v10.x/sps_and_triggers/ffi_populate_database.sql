/*
  -------------------------------------------------------------------------------------------------
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved

  Last modified by - $Author: ctipper $
  Last modified on - $Date: 2017-07-17 10:56:10 -0400 (Mon, 17 Jul 2017) $

  Takes the hand-off from the flat file importer and populates the normalized source database for
  the DB Merge tool.
  -------------------------------------------------------------------------------------------------
*/

DELIMITER $$

/*
 -----------------------------------------------------------------------------
  Get the CVS version information.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_get_version` $$
CREATE PROCEDURE         `ffi_get_version` ()
    SQL SECURITY INVOKER
BEGIN
    SELECT  '$Revision: 14223 $ Last modified by - $Author: ctipper $ Last modified on - $Date: 2017-07-17 10:56:10 -0400 (Mon, 17 Jul 2017) $';
END $$

/*
 ------------------------------------------------------------------------------
  Get the CVS version information as a return value.
 ------------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_get_version_function` $$
CREATE FUNCTION `ffi_get_version_function` ()
    RETURNS TINYTEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE version TINYTEXT;
    SELECT '$Revision: 14223 $'
        INTO version;
    RETURN version;
END $$
/* $KeyWordsOff: $ */

/*
 -----------------------------------------------------------------------------
  Main procedure to populate the database.  Test to see if optional columns
  exist here rather than doing multiple lookups later.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_populate_database` $$
CREATE PROCEDURE `ffi_populate_database` (datasetId INT, anonFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    DECLARE hasStepNameHeading BOOLEAN DEFAULT FALSE;
    DECLARE hasOutcomeHeading BOOLEAN DEFAULT FALSE;
    DECLARE hasFeedbackHeading BOOLEAN DEFAULT FALSE;
    DECLARE hasFeedbackClassHeading BOOLEAN DEFAULT FALSE;
    DECLARE hasClassHeading BOOLEAN DEFAULT FALSE;
    DECLARE hasSchoolHeading BOOLEAN DEFAULT FALSE;
    DECLARE selectionColumns TEXT DEFAULT "";
    DECLARE actionColumns TEXT DEFAULT "";
    DECLARE inputColumns TEXT DEFAULT "";

    CALL debug_log("ffi_populate_database", "Starting ffi_populate_database");

    SET foreign_key_checks = 0;
    /* Set to size of max_allowed_packet since it constrains group_concat_max_len. */
    SET group_concat_max_len = 8388608;

    CALL ffi_set_column_headings(hasStepNameHeading, hasOutcomeHeading, hasFeedbackHeading,
        hasFeedbackClassHeading, hasClassHeading, hasSchoolHeading);

    SELECT GROUP_CONCAT(column_name) INTO selectionColumns
        FROM ffi_heading_column_map
        WHERE heading = 'Selection';

    SELECT GROUP_CONCAT(column_name) INTO actionColumns
        FROM ffi_heading_column_map
        WHERE heading = 'Action';

    SELECT GROUP_CONCAT(column_name) INTO inputColumns
        FROM ffi_heading_column_map
        WHERE heading = 'Input';

    CALL ffi_create_dataset_levels(datasetId);
    CALL ffi_create_problems();
    CALL ffi_create_skill_models(datasetId);
    CALL ffi_create_skills(datasetId);
    CALL ffi_create_subgoals(hasStepNameHeading);
    CALL ffi_create_subject_data(datasetId, hasClassHeading, hasSchoolHeading, anonFlag);
    CALL ffi_create_subgoal_skill_map();

    CALL ffi_create_subgoal_attempts(hasStepNameHeading, hasOutcomeHeading, hasFeedbackHeading,
        hasFeedbackClassHeading, selectionColumns, actionColumns, inputColumns);

    CALL ffi_create_transactions(datasetId, hasFeedbackHeading, hasFeedbackClassHeading,
        hasStepNameHeading, hasOutcomeHeading, hasClassHeading, hasSchoolHeading);

    CALL ffi_create_problem_events();

    CALL ffi_create_tx_skill_map();
    CALL ffi_create_conditions(datasetId);
    CALL ffi_create_custom_fields(datasetId);

    SET foreign_key_checks = 1;

    CALL debug_log("ffi_populate_database", "Finished ffi_populate_database");
END $$

/*
 -----------------------------------------------------------------------------
 Pass by reference procedure to determine which optional columns are declared
 in ffi_import_file_data.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_set_column_headings` $$
CREATE PROCEDURE `ffi_set_column_headings` (INOUT hasStepNameHeading BOOLEAN,
    INOUT hasOutcomeHeading BOOLEAN, INOUT hasFeedbackHeading BOOLEAN,
    INOUT hasFeedbackClassHeading BOOLEAN, INOUT hasClassHeading BOOLEAN,
    INOUT hasSchoolHeading BOOLEAN)
    SQL SECURITY INVOKER
BEGIN
    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = 'ffi_import_file_data' AND column_name = 'step_name'
        AND table_schema = DATABASE()) THEN
        SET hasStepNameHeading = TRUE;
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = 'ffi_import_file_data' AND column_name = 'outcome'
        AND table_schema = DATABASE()) THEN
        SET hasOutcomeHeading = TRUE;
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = 'ffi_import_file_data' AND column_name = 'feedback_text') THEN
        IF EXISTS(SELECT column_name FROM information_schema.columns
            WHERE table_name = 'ffi_import_file_data' AND column_name = 'feedback_classification'
            AND table_schema = DATABASE()) THEN
            SET hasFeedbackClassHeading = TRUE;
        END IF;
        SET hasFeedbackHeading = TRUE;
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = "ffi_import_file_data" AND column_name = 'class'
        AND table_schema = DATABASE()) THEN
        SET hasClassHeading = TRUE;
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = "ffi_import_file_data"
        AND column_name = 'school' AND table_schema = DATABASE()) THEN
        SET hasSchoolHeading = TRUE;
    END IF;

END $$

/*
 -----------------------------------------------------------------------------
  Create the dataset levels hierarchy.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_dataset_levels` $$
CREATE PROCEDURE `ffi_create_dataset_levels` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN

    DECLARE levelColumnNames, levelName, columnName, columnTitle, distinctQuery, createQuery TEXT;
    DECLARE datasetLevelId INT;
    DECLARE lft, rgt INT DEFAULT 0;
    DECLARE length, delimPos, maxDepth INT;
    DECLARE depth INT DEFAULT 1;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_dataset_levels");

    SET max_sp_recursion_depth = 15;

    /* From column mapping table retrieve level title */
    DROP TABLE IF EXISTS ffi_level_mappings;
    CREATE TABLE ffi_level_mappings (level_column VARCHAR(64),
        level_title VARCHAR(100),
        ordering INT,
        PRIMARY KEY (level_column, level_title)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT column_name AS level_column, column_value AS level_title, sequence AS ordering
    FROM ffi_heading_column_map
    WHERE column_name LIKE 'level_%';

    /* Hashes of dataset level tree branches */
    DROP TABLE IF EXISTS ffi_level_hashes;
    CREATE TABLE ffi_level_hashes (dataset_level_id INT PRIMARY KEY,
        hash CHAR(32),
        INDEX(hash(5))
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    /* Create list of level_% column names */
    SELECT GROUP_CONCAT(CONCAT("REPLACE(", level_column, ", \"'\", '')") ORDER BY ordering) INTO levelColumnNames FROM ffi_level_mappings;

    DROP TABLE IF EXISTS ffi_distinct_level_pairs;

    /* Create ffi_distinct_level_pairs table */
    CALL ffi_build_dataset_level_create_table(levelColumnNames);

    /* Insert distinct pairs into the ffi_distinct_level_pairs table */
    CALL exec(ffi_build_distinct_level_pairs_query(levelColumnNames));

    /* Find the depth of dataset_level tree */
    SELECT count(*) INTO maxDepth FROM ffi_level_mappings;

    SELECT level_column, level_title INTO columnName, columnTitle FROM ffi_level_mappings WHERE ordering = depth;

    /* Group concat all parent levels into a list */
    SET @distinctLevelQuery = ffi_build_distinct_level_query(columnName);
    PREPARE stmt FROM @distinctLevelQuery;
    EXECUTE stmt;

    /* Check for trailing XXX and strip if present. */
    IF @distinctLevels LIKE '%XXX' THEN
       SET length = LENGTH(@distinctLevels);
       SET @distinctLevels = SUBSTRING(@distinctLevels FROM 1 FOR (length - 3));
    END IF;

    /* Insert dataset level tree */
    SET length = LENGTH(@distinctLevels);
    WHILE length > 0 DO
        SET delimPos = LOCATE('XXX', @distinctLevels);
        SET lft = rgt + 1;
        SET rgt = lft + 1;
        IF delimPos != 0 THEN
            SET levelName = TRIM(SUBSTRING(@distinctLevels, 1, delimPos-1));
            SET @distinctLevels = TRIM(SUBSTRING(@distinctLevels FROM delimPos+3));
            SET length = LENGTH(@distinctLevels);

            INSERT IGNORE INTO dataset_level (level_name, level_title, dataset_id, lft, rgt)
                VALUES (levelName, columnTitle, datasetId, lft, rgt);
            SET datasetLevelId = LAST_INSERT_ID();

            INSERT INTO ffi_level_hashes (dataset_level_id, hash) VALUES (datasetLevelId, MD5(levelName));

            IF maxDepth != 1 THEN
                CALL ffi_insert_dataset_level_children(depth, maxDepth, levelName, datasetLevelId,
                    datasetId, lft, levelName);
                /* set the start lft value (rgt + 1) for the next parent level */
            END IF;
            SELECT dl.rgt INTO rgt FROM dataset_level dl WHERE dataset_level_id = datasetLevelId;
        ELSE

            INSERT IGNORE INTO dataset_level (level_name, level_title, dataset_id, lft, rgt)
                VALUES (@distinctLevels, columnTitle, datasetId, lft, rgt);
            SET datasetLevelId = LAST_INSERT_ID();

            INSERT INTO ffi_level_hashes (dataset_level_id, hash) VALUES (datasetLevelId, MD5(@distinctLevels));

            IF maxDepth != 1 THEN
                CALL ffi_insert_dataset_level_children(depth, maxDepth, @distinctLevels, datasetLevelId,
                    datasetId, lft, @distinctLevels);
            END IF;

            SET length = 0;
        END IF;
    END WHILE;

    /* Create helpful mapping between dataset_level_ids and distinct sequences of levels in
     ffi_import_file_data */
    UPDATE ffi_distinct_level_pairs dlp JOIN ffi_level_hashes hash USING (hash)
    SET dlp.dataset_level_id = hash.dataset_level_id;

    CREATE INDEX dataset_level_id ON ffi_distinct_level_pairs (dataset_level_id);

    /* Add new dataset_level_id column to ffi_import_file_data and fill it by matching hashes */
    CALL ffi_write_back_dataset_level_ids(levelColumnNames);

    /* Create guid_hierarchy for subgoal generation */
    CALL ffi_generate_guid_hierarchy();

    CALL debug_log("ffi_populate_database", "Finished ffi_create_dataset_levels");
END $$

/*
 -----------------------------------------------------------------------------
 Copied from the SubgoalDaoHibernate.java to generate a sequence of dataset level
 names and their titles (e.g. (Unit) Level 1, (Section) Child of Level 1) that
 added to the subgoal name will created a subgoal guid.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_generate_guid_hierarchy` $$
CREATE PROCEDURE `ffi_generate_guid_hierarchy` ()
    SQL SECURITY INVOKER
BEGIN

    UPDATE ffi_distinct_level_pairs dlp JOIN (SELECT (select group_concat(DISTINCT CONCAT(
        IF(dl2.level_title IS NOT NULL, CONCAT('(', dl2.level_title, ') '), ''),
        dl2.level_name) ORDER BY dl2.lft SEPARATOR 'XXX ')
        FROM dataset_level dl2
        WHERE dl2.lft <= dl.lft AND dl2.rgt >= dl.rgt
        AND dl.dataset_id = dl2.dataset_id
        GROUP BY dl2.dataset_id) as probHierarchy, dl.dataset_level_id
        FROM ffi_distinct_level_pairs dlp
        JOIN dataset_level dl on dl.dataset_level_id = dlp.dataset_level_id) probHier
            ON probHier.dataset_level_id = dlp.dataset_level_id
    SET guid_hierarchy = probHier.probHierarchy;

END $$

/*
 -----------------------------------------------------------------------------
 Now that dataset_levels have been created update the ffi_import_file_data table
 with dataset_ids as a prerequisite of inserting problems.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_write_back_dataset_level_ids` $$
CREATE PROCEDURE `ffi_write_back_dataset_level_ids` (levelColumnNames TEXT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_write_back_dataset_level_ids");

    CALL exec(CONCAT("UPDATE ffi_import_file_data ifd
        JOIN (SELECT line_num, lvlhash.dataset_level_id
              FROM ffi_distinct_level_pairs lvlhash
              JOIN (SELECT line_num, MD5(CONCAT(", levelColumnNames, ")) AS hash
                    FROM ffi_import_file_data) data USING (hash)) ifd2 USING (line_num)
        SET ifd.dataset_level_id = ifd2.dataset_level_id"));

    CALL debug_log("ffi_populate_database", "Finished ffi_write_back_dataset_level_ids");
END $$
/*
 -----------------------------------------------------------------------------
 Recursive procedure called if dataset_levels contain child levels.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_insert_dataset_level_children` $$
CREATE PROCEDURE `ffi_insert_dataset_level_children` (depth INT, maxDepth INT, levelName TEXT,
    parentId INT, datasetId INT, lft INT, hash TEXT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE columnName, childName, localChildren, levelTitle, hashText TEXT;
    DECLARE datasetLevelId, delimPos, length, rgt INT;

    CALL debug_log("ffi_populate_database","Starting ffi_insert_dataset_level_children");

    SET depth = depth + 1;
    SELECT level_column, level_title INTO columnName, levelTitle FROM ffi_level_mappings
        WHERE ordering = depth;
    SET @childrenQuery = ffi_build_get_children_query(columnName, levelName, parentId, depth);

    PREPARE childStmt FROM @childrenQuery;
    EXECUTE childStmt;

    /* Check for trailing XXX and strip if present. */
    IF @children LIKE '%XXX' THEN
       SET length = LENGTH(@children);
       SET @children = SUBSTRING(@children FROM 1 FOR (length - 3));
    END IF;

    SET localChildren = @children;
    SET length = LENGTH(localChildren);
    WHILE length > 0 DO
        SET delimPos = LOCATE('XXX', localChildren);
        SET lft = lft + 1;
        SET rgt = lft + 1;
        IF delimPos != 0 THEN
            SET childName = TRIM(SUBSTRING(localChildren, 1, delimPos-1));
            SET localChildren = TRIM(SUBSTRING(localChildren FROM delimPos+3));
            SET length = LENGTH(localChildren);

            INSERT IGNORE INTO dataset_level (level_name, level_title, parent_id, dataset_id, lft, rgt)
                VALUES (childName, levelTitle, parentId, datasetId, lft, rgt);
            SET datasetLevelId = LAST_INSERT_ID();

            SET hashText = CONCAT(hash,childName);
            INSERT INTO ffi_level_hashes (dataset_level_id, hash) VALUES (datasetLevelId, MD5(hashText));

            IF depth < maxDepth THEN
                CALL ffi_insert_dataset_level_children(depth, maxDepth, childName, datasetLevelId,
                    datasetId, lft, hashText);
            END IF;
            SELECT dl.rgt INTO lft FROM dataset_level dl WHERE dataset_level_id = datasetLevelId;
        ELSE

            INSERT IGNORE INTO dataset_level (level_name, level_title, parent_id, dataset_id, lft, rgt)
                VALUES (localChildren, levelTitle, parentId, datasetId, lft, rgt);
            SET datasetLevelId = LAST_INSERT_ID();

            SET hashText = CONCAT(hash, localChildren);
            INSERT INTO ffi_level_hashes (dataset_level_id, hash) VALUES (datasetLevelId, MD5(hashText));

            IF depth < maxDepth THEN
                CALL ffi_insert_dataset_level_children(depth, maxDepth, localChildren,
                    datasetLevelId, datasetId, lft, hashText);
            END IF;

            /* Don't rewrite the SET clause because you'll likely end up with dirty data trust me */
            UPDATE dataset_level dl
            SET dl.rgt = (SELECT MAX(dl2.rgt) + 1 FROM (SELECT * FROM dataset_level) dl2
                WHERE dl2.parent_id = parentId)
            WHERE dl.dataset_level_id = parentId;

            SET length = 0;
        END IF;
    END WHILE;

    CALL debug_log("ffi_populate_database","Finished ffi_insert_dataset_level_children");
END $$

/*
 -----------------------------------------------------------------------------
  Build a select query that given a parent dataset level, finds all the distinct
  children of that parent.  Concatenate these children in a comma delimited list
  and assign them to the local variable @children.  This is a necessary evil because
  of how prepared statements function.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_build_get_children_query` $$
CREATE FUNCTION `ffi_build_get_children_query` (columnName TEXT, levelName TEXT, parentId INT, columnIndex INT)
    RETURNS TEXT
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE childQuery TEXT;
    DECLARE parentColumn TEXT;
    DECLARE parentTitle TEXT;
    DECLARE parentLevelName TEXT;
    DECLARE whereQuery TEXT;

    SELECT level_column INTO parentColumn FROM ffi_level_mappings WHERE ordering = (columnIndex - 1);
    SET whereQuery = CONCAT(parentColumn, " = '", levelName, "' ");
    SET columnIndex = columnIndex - 1;

    SELECT parent_id INTO parentId FROM dataset_level WHERE dataset_level_id = parentId;
    WHILE columnIndex > 1 DO
        SELECT level_column INTO parentColumn FROM ffi_level_mappings WHERE ordering = (columnIndex - 1);
        SELECT level_name, parent_id INTO parentLevelName, parentId FROM dataset_level
            WHERE dataset_level_id = parentId;

        SET whereQuery = CONCAT(whereQuery, " AND ", parentColumn, " = '", parentLevelName, "' ");
        SET columnIndex = columnIndex - 1;
    END WHILE;
    SET childQuery = CONCAT("SELECT GROUP_CONCAT(DISTINCT ", columnName, " SEPARATOR 'XXX') INTO @children FROM ffi_distinct_level_pairs WHERE ", whereQuery);
    RETURN childQuery;
END $$

DROP FUNCTION IF EXISTS `ffi_build_distinct_level_query` $$
CREATE FUNCTION `ffi_build_distinct_level_query` (columnName TEXT)
    RETURNS TEXT
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE distinctQuery TEXT;
    SET distinctQuery = CONCAT("SELECT GROUP_CONCAT(DISTINCT ", columnName, " SEPARATOR 'XXX') INTO @distinctLevels FROM ffi_distinct_level_pairs");
    RETURN distinctQuery;
END $$

/*
 -----------------------------------------------------------------------------
  Build a create query for the ffi_distinct_level_pairs table.  The column names
  correspond to the level_% column names in the ffi_import_file_data table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_build_dataset_level_create_table` $$
CREATE PROCEDURE `ffi_build_dataset_level_create_table` (levelColumnNames TEXT)
    SQL SECURITY INVOKER
BEGIN

    DECLARE datasetLevelColumnCount INT DEFAULT 0;
    DECLARE columnIndex INT DEFAULT 0;
    DECLARE createQuery TEXT DEFAULT "CREATE TABLE ffi_distinct_level_pairs ( ";
    DECLARE columnName TEXT DEFAULT "";

    SELECT count(*) INTO datasetLevelColumnCount FROM ffi_level_mappings;

    WHILE columnIndex < datasetLevelColumnCount DO
        SELECT level_column INTO columnName FROM ffi_level_mappings
            WHERE ordering = columnIndex + 1;

        IF columnIndex = 0 THEN
            SET createQuery = CONCAT(createQuery, columnName, " VARCHAR(100)");
        ELSE
            SET createQuery = CONCAT(createQuery, ", ", columnName, " VARCHAR(100)");
        END IF;
        SET columnIndex = columnIndex + 1;
    END WHILE;

    CALL exec(CONCAT(createQuery, ", hash CHAR(32), dataset_level_id INT, guid_hierarchy TEXT",
        ") ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin"));
END $$

/*
 -----------------------------------------------------------------------------
  Build an insert query to find all distinct dataset level trees.  Hashes are
  created to solve the difficulty of matching the assigned dataset_level_ids
  to these distinct pairs during recursive insert.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_build_distinct_level_pairs_query` $$
CREATE FUNCTION `ffi_build_distinct_level_pairs_query` (levelColumnNames TEXT)
    RETURNS TEXT
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN

    DECLARE insertQuery TEXT DEFAULT "INSERT INTO ffi_distinct_level_pairs SELECT DISTINCT ";
    DECLARE selectColumns TEXT;
    DECLARE hashFunction TEXT DEFAULT ", MD5(CONCAT(";

    SELECT GROUP_CONCAT(concat("REPLACE(", level_column, ", \"'\", '')") ORDER BY ordering) INTO selectColumns
    FROM ffi_level_mappings;

    SET hashFunction = CONCAT(hashFunction, selectColumns, ")) AS hash, ");

    SET insertQuery = CONCAT(insertQuery, selectColumns, hashFunction,
        " NULL as dataset_level_id, NULL as guid_hierarchy FROM ffi_import_file_data");

    RETURN insertQuery;
END $$

/*
 -----------------------------------------------------------------------------
 Inserts distinct dataset_level_id, problem_name pairs into the problem table.
 Update ffi_import_file_data rows with their corresponding problem_id.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_problems` $$
CREATE PROCEDURE `ffi_create_problems` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_problems");

    CREATE INDEX prob_name_ds_level_id ON ffi_import_file_data (problem_name(5), dataset_level_id);

    INSERT INTO problem (problem_name, dataset_level_id)
        SELECT DISTINCT problem_name, dataset_level_id
        FROM ffi_import_file_data;

    UPDATE ffi_import_file_data dd
        JOIN problem pr USING (problem_name, dataset_level_id)
        SET dd.problem_id = pr.problem_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_problems");
END $$

/*
 -----------------------------------------------------------------------------
  Create the subgoals and update rows in the ffi_import_file_data table with their
  corresponding subgoal_ids.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_subgoals` $$
CREATE PROCEDURE `ffi_create_subgoals` (hasStepNameHeading BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_subgoals");

    IF(hasStepNameHeading) THEN
        CREATE INDEX step_name_prob_id ON ffi_import_file_data (step_name(5), problem_id);

        INSERT INTO subgoal (subgoal_name, problem_id, guid)
        SELECT DISTINCT ifd.step_name AS subgoal_name, ifd.problem_id,
            MD5(CONCAT(IFNULL(tutor_other,"null"), guid_hierarchy,ifd.problem_name,
            IFNULL(problem_description,"null"), IFNULL(tutor_flag,"null"),
            ifd.step_name)) AS guid
        FROM ffi_import_file_data ifd
        JOIN problem p USING (problem_id)
        JOIN ffi_distinct_level_pairs dlp ON ifd.dataset_level_id = dlp.dataset_level_id
        WHERE ifd.step_name != '';

        UPDATE ffi_import_file_data dd JOIN subgoal sg ON sg.subgoal_name = dd.step_name
            AND sg.problem_id = dd.problem_id
        SET dd.subgoal_id = sg.subgoal_id;
    END IF;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_subgoals");
END $$

/*
 -----------------------------------------------------------------------------
 Create the skill models before inserting the skills.  Relies on the ffi_heading_column_map
 table, reading the model name from the column_value field.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_skill_models` $$
CREATE PROCEDURE `ffi_create_skill_models` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log("ffi_populate_database", "Starting ffi_create_skill_models");

    INSERT INTO skill_model (skill_model_name, global_flag, dataset_id, allow_lfa_flag, status,
        lfa_status, source, mapping_type, creation_time, modified_time)
    SELECT DISTINCT column_value, TRUE, datasetId, TRUE, "not ready", "queued", "imported",
        "correct-transaction-to-kc", NOW(), NOW()
    FROM ffi_heading_column_map
    WHERE column_name LIKE 'kc%';

    CALL debug_log("ffi_populate_database", "Finished ffi_create_skill_models");
END $$

/*
 -----------------------------------------------------------------------------
 Creates a skill helper table ffi_skill_categories to handily fetch the model
 name and determine those skills with and without categories.  Eventually
 calls ffi_build_skill_query to insert the skills.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_skills` $$
CREATE PROCEDURE `ffi_create_skills` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE skillsWithCats TEXT;
    DECLARE skillsWithoutCats TEXT;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_skills");

    DROP TABLE IF EXISTS ffi_skill_categories;
    CREATE TABLE ffi_skill_categories (skill_column VARCHAR(64),
        skill_category_column VARCHAR(64),
        skill_model_name VARCHAR(50),
        skill_model_id BIGINT
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
    SELECT skill_column, skill_category_column, skills.skill_model_name, skills.skill_model_id
    FROM (SELECT column_name AS skill_column, column_value AS skill_model_name, skm.skill_model_id, sequence
          FROM ffi_heading_column_map
          JOIN skill_model skm ON column_value = skm.skill_model_name
          WHERE column_name NOT LIKE 'kc\_category%' AND column_name LIKE 'kc\_%') skills
    LEFT JOIN (SELECT column_name AS skill_category_column , column_value AS skill_model_name, sequence
               FROM ffi_heading_column_map
               WHERE column_name LIKE 'kc\_category%') categories USING (sequence);

    SELECT IFNULL(GROUP_CONCAT(skill_column),'') INTO skillsWithCats
    FROM ffi_skill_categories
    WHERE skill_category_column IS NOT NULL;

    SELECT IFNULL(GROUP_CONCAT(skill_column),'') INTO skillsWithoutCats
    FROM ffi_skill_categories
    WHERE skill_category_column IS NULL;

    CALL ffi_insert_skills(skillsWithCats, skillsWithoutCats, datasetId);

    CALL debug_log("ffi_populate_database", "Finished ffi_create_skills");
END $$

/*
 -----------------------------------------------------------------------------
 Builds and executes the skill insert query.  Takes group_concated KC(skill model name)
 columns, those with KC_CATEGORY() and those without, as arguments.  Separate
 inserts are generated for each skill column for speed reasons.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_insert_skills` $$
CREATE PROCEDURE `ffi_insert_skills` (skillCat TEXT, skillNoCat TEXT, datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE insertSkillQuery TEXT DEFAULT "INSERT IGNORE INTO ffi_skill (skill_model_id, skill_name, category) ";
    DECLARE skillColumn TEXT;
    DECLARE categoryColumn TEXT;
    DECLARE length, delimPos INT;
    DECLARE skillModelId BIGINT;

    DROP TABLE IF EXISTS ffi_skill;
    CREATE TABLE ffi_skill (
            skill_model_id BIGINT,
            skill_name TEXT,
            category VARCHAR(50)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    SET length = LENGTH(skillCat);
    WHILE length > 0 DO
        SET delimPos = LOCATE(",", skillCat);

        IF delimPos != 0 THEN
            SET skillColumn = TRIM(SUBSTRING(skillCat, 1, delimPos-1));
            SET skillCat = TRIM(SUBSTRING(skillCat FROM delimPos+1));
            SET length = LENGTH(skillCat);

            SELECT skill_category_column INTO categoryColumn FROM ffi_skill_categories
                WHERE skill_column = skillColumn;

            SELECT skm.skill_model_id INTO skillModelId
            FROM ffi_skill_categories skcat
            JOIN skill_model skm ON skcat.skill_model_name = skm.skill_model_name
            WHERE skm.dataset_id = datasetId AND skcat.skill_column = skillColumn;

            CALL exec(CONCAT(insertSkillQuery,"SELECT DISTINCT ", skillModelId,
                " AS skill_model_id, ", skillColumn ," AS skill_name, IF(", categoryColumn,
                " = '',NULL,", categoryColumn,") AS category FROM ffi_import_file_data WHERE ",
                skillColumn, " != '' "));
        ELSE
            SELECT skill_category_column INTO categoryColumn FROM ffi_skill_categories
                WHERE skill_column = skillCat;

            SELECT skm.skill_model_id INTO skillModelId
            FROM ffi_skill_categories skcat
            JOIN skill_model skm ON skcat.skill_model_name = skm.skill_model_name
            WHERE skm.dataset_id = datasetId AND skcat.skill_column = skillCat;

            CALL exec(CONCAT(insertSkillQuery,"SELECT DISTINCT ", skillModelId,
                " AS skill_model_id, ", skillCat ," AS skill_name, IF(",categoryColumn,"
                = '',NULL,", categoryColumn, ") AS category FROM ffi_import_file_data WHERE ",
                skillCat, " != '' "));
            SET length = 0;
        END IF;
    END WHILE;

    SET length = LENGTH(skillNoCat);
    WHILE length > 0 DO
        SET delimPos = LOCATE(",", skillNoCat);

        IF delimPos != 0 THEN
            SET skillColumn = TRIM(SUBSTRING(skillNoCat, 1, delimPos-1));
            SET skillNoCat = TRIM(SUBSTRING(skillNoCat FROM delimPos+1));
            SET length = LENGTH(skillNoCat);

            SELECT skm.skill_model_id INTO skillModelId
            FROM ffi_skill_categories skcat
            JOIN skill_model skm ON skcat.skill_model_name = skm.skill_model_name
            WHERE skm.dataset_id = datasetId AND skcat.skill_column = skillColumn;

            CALL exec(CONCAT(insertSkillQuery,"SELECT DISTINCT ", skillModelId,
                " AS skill_model_id, ", skillColumn ," AS skill_name, NULL AS category ",
                "FROM ffi_import_file_data WHERE ", skillColumn, " != '' "));
        ELSE
            SELECT skm.skill_model_id INTO skillModelId
            FROM ffi_skill_categories skcat
            JOIN skill_model skm ON skcat.skill_model_name = skm.skill_model_name
            WHERE skm.dataset_id = datasetId AND skcat.skill_column = skillNoCat;

            CALL exec(CONCAT(insertSkillQuery,"SELECT DISTINCT ", skillModelId,
                " AS skill_model_id, ", skillNoCat ," AS skill_name, NULL AS category ",
                "FROM ffi_import_file_data WHERE ", skillNoCat, " != '' "));

            SET length = 0;
        END IF;
    END WHILE;

    INSERT INTO skill (skill_model_id, skill_name, category)
        SELECT DISTINCT skill_model_id, skill_name, category
        FROM ffi_skill;

END $$

/*
 -----------------------------------------------------------------------------
 Create and fill in all the subject data including the session table, which
 may contain class and school references.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_subject_data` $$
CREATE PROCEDURE `ffi_create_subject_data` (datasetId INT, hasClass BOOLEAN, hasSchool BOOLEAN,
                                            anonFlag BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    /* If source database isn't recreated old indexes may still exist, causing a dup key error */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '42000' SET @meaningless = 1;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_subject_data");

    IF anonFlag = TRUE THEN
        INSERT IGNORE INTO student (actual_user_id)
            SELECT DISTINCT REPLACE(anon_student_id, "'", '') AS actual_user_id
            FROM ffi_import_file_data;
        UPDATE ffi_import_file_data dd
            JOIN student stu ON REPLACE(dd.anon_student_id, "'", '') = stu.actual_user_id
            SET dd.student_id = stu.student_id;
    ELSE
        INSERT IGNORE INTO student (actual_user_id, anon_user_id)
            SELECT DISTINCT CONCAT("ds_student_", REPLACE(anon_student_id, "'", '')) AS actual_user_id,
                    REPLACE(anon_student_id, "'", '') AS anon_user_id
            FROM ffi_import_file_data;
        UPDATE ffi_import_file_data dd
            JOIN student stu ON REPLACE(dd.anon_student_id, "'", '') = stu.anon_user_id
            SET dd.student_id = stu.student_id;
    END IF;

    IF hasSchool = TRUE THEN
        IF hasClass = TRUE THEN
        /* School and Class */

            CREATE INDEX class_school ON ffi_import_file_data (class(5), school(5));

            DROP TABLE IF EXISTS ffi_distinct_school_class;
            CREATE TABLE ffi_distinct_school_class (row BIGINT AUTO_INCREMENT PRIMARY KEY,
                class_name VARCHAR(75),
                school_name VARCHAR(100),
                school_id INT,
                class_id BIGINT,
                UNIQUE (class_name, school_name),
                INDEX (school_name(5))
            ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
            IGNORE SELECT class AS class_name, school AS school_name,
                NULL as school_id
            FROM ffi_import_file_data
            WHERE class IS NOT NULL AND school IS NOT NULL;

            INSERT INTO school (school_name)
            SELECT DISTINCT school_name
            FROM ffi_distinct_school_class
            WHERE school_name != '';

            UPDATE ffi_distinct_school_class dsc JOIN school sch USING (school_name)
            SET dsc.school_id = sch.school_id;

            INSERT INTO class (class_name, school_id, src_class_id)
            SELECT class_name, school_id, row
            FROM ffi_distinct_school_class
            WHERE class_name != '';

            UPDATE ffi_distinct_school_class dsc JOIN class cl ON dsc.row = cl.src_class_id
            SET dsc.class_id = cl.class_id;

            INSERT IGNORE INTO class_dataset_map (class_id, dataset_id)
            SELECT class_id, datasetId
            FROM class;

            CREATE INDEX anon_user_id ON student (anon_user_id(10));

            INSERT IGNORE INTO roster (class_id, student_id)
                SELECT DISTINCT class_id, stu.student_id
                FROM ffi_import_file_data subdat
                JOIN ffi_distinct_school_class dsc ON dsc.class_name = subdat.class
                        AND dsc.school_name = subdat.school
                JOIN student stu ON stu.student_id = subdat.student_id
                WHERE subdat.class != '' AND subdat.class IS NOT NULL;

            CALL ffi_create_sessions_school_and_class(datasetId);
        ELSE
        /* School Only */
            CREATE INDEX school ON ffi_import_file_data (school(5));

            INSERT INTO school (school_name)
            SELECT DISTINCT school
            FROM ffi_import_file_data
            WHERE school != '';

            CREATE INDEX anon_user_id ON student (anon_user_id(10));

            CALL ffi_create_sessions_school_only(datasetId);
        END IF;
    ELSE
        IF hasClass = TRUE THEN
        /* Class Only */
            CREATE INDEX class ON ffi_import_file_data (class(5));

            INSERT INTO class (class_name)
            SELECT DISTINCT class
            FROM ffi_import_file_data
            WHERE class != '';

            INSERT IGNORE INTO class_dataset_map
            SELECT class_id, datasetId
            FROM class;

            CREATE INDEX anon_user_id ON student (anon_user_id(10));
            CREATE INDEX class ON class (class_name);

            INSERT IGNORE INTO roster (class_id, student_id)
                SELECT DISTINCT cl.class_id, stu.student_id
                FROM ffi_import_file_data subdat
                JOIN class cl ON cl.class_name = subdat.class
                JOIN student stu ON stu.student_id = subdat.student_id
                WHERE subdat.class != '' AND subdat.class IS NOT NULL;

            CALL ffi_create_sessions_class_only(datasetId);
        ELSE
        /* No School or Class */

            CREATE INDEX anon_user_id ON student (anon_user_id(10));

            CALL ffi_create_sessions(datasetId);
        END IF;
    END IF;

    UPDATE ffi_import_file_data dd
        JOIN student stu ON dd.student_id = stu.student_id
        JOIN session ss ON stu.student_id = ss.student_id AND dd.session_id = ss.session_tag
        SET dd.actual_session_id = ss.session_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_subject_data");
END $$


/*
 -----------------------------------------------------------------------------
  Create sessions with only referenced schools.  Populates the session_student_map
  as well.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_sessions_school_only` $$
CREATE PROCEDURE         `ffi_create_sessions_school_only` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log("ffi_populate_database", "Starting ffi_create_sessions_school_only");

    CREATE INDEX session_time ON ffi_import_file_data (session_id, tx_time_datetime);

    INSERT INTO session (session_tag, start_time, completion_code, dataset_id, school_id, student_id)
        SELECT session_id, MIN(tx_time_datetime), "UNKNOWN", datasetId, school_id, stu.student_id
        FROM ffi_import_file_data sd
        JOIN student stu ON stu.student_id = sd.student_id
        LEFT JOIN school sch ON sd.school = sch.school_name
        GROUP BY session_id, student_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_sessions_school_only");
END $$

/*
 -----------------------------------------------------------------------------
  Create sessions with only assigned classes not schools.  Populate the session_student_map
  as well.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_sessions_class_only` $$
CREATE PROCEDURE `ffi_create_sessions_class_only` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_sessions_class_only");

    CREATE INDEX session_time ON ffi_import_file_data (session_id, tx_time_datetime);

    INSERT INTO session (session_tag, start_time, completion_code, dataset_id, class_id, student_id)
    SELECT session_id, MIN(tx_time_datetime), "UNKNOWN", datasetId, class_id, stu.student_id
    FROM ffi_import_file_data sd
    JOIN student stu ON stu.student_id = sd.student_id
    LEFT JOIN class cl ON cl.class_name = sd.class
    GROUP BY session_id, student_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_sessions_class_only");
END $$

/*
 -----------------------------------------------------------------------------
  Creates sessions that have school and class values as well as the session_student_map.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_sessions_school_and_class` $$
CREATE PROCEDURE `ffi_create_sessions_school_and_class` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_sessions_school_and_class");

    CREATE INDEX session_time ON ffi_import_file_data (session_id, tx_time_datetime);

    INSERT INTO session (session_tag, start_time, completion_code, dataset_id, class_id, school_id, student_id)
    SELECT session_id, MIN(tx_time_datetime), "UNKNOWN", datasetId, class_id, school_id, stu.student_id
    FROM ffi_import_file_data sd
    JOIN student stu ON stu.student_id = sd.student_id
    LEFT JOIN (SELECT class_id, class_name, IFNULL(school_name, '') AS school_name
               FROM class
               LEFT JOIN school USING (school_id)) cl ON cl.class_name = sd.class AND cl.school_name = sd.school
    LEFT JOIN school sch ON sch.school_name = sd.school
    GROUP BY session_id, student_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_sessions_school_and_class");
END $$

/*
 -----------------------------------------------------------------------------
 Creates sessions without class or school references as well as the session_student_map.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_sessions` $$
CREATE PROCEDURE `ffi_create_sessions` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_sessions");

    CREATE INDEX session_time ON ffi_import_file_data (session_id, tx_time_datetime);

    INSERT INTO session (session_tag, start_time, completion_code, dataset_id, student_id)
    SELECT session_id, MIN(tx_time_datetime), "UNKNOWN", datasetId, stu.student_id
    FROM ffi_import_file_data sd
    /* [2012/14/18 - ysahn] added join for new column student_id. Also stduent_id column in insert */
    JOIN student stu ON stu.student_id = sd.student_id
    GROUP BY session_id, student_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_sessions");
END $$


/*
 -----------------------------------------------------------------------------
  Create subgoal_attempt and attempt_sai rows.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_subgoal_attempts` $$
CREATE PROCEDURE `ffi_create_subgoal_attempts` (hasStepNameHeading BOOLEAN,
    hasOutcomeHeading BOOLEAN, hasFeedbackHeading BOOLEAN, hasFeedbackClassHeading BOOLEAN,
    selectionColumns TEXT, actionColumns TEXT, inputColumns TEXT)
    SQL SECURITY INVOKER
BEGIN

    /* If source database isn't recreated old indexes may still exist, causing a dup key error */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '42000' SET @meaningless = 1;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_subgoal_attempts");

    CALL debug_log("ffi_populate_database", "Starting ffi_distinct_subgoal_attempts");
    DROP TABLE IF EXISTS ffi_distinct_subgoal_attempts;

    CALL ffi_build_subgoal_attempt_query(hasStepNameHeading, hasOutcomeHeading,
        hasFeedbackHeading, hasFeedbackClassHeading, selectionColumns, actionColumns, inputColumns);

    CALL ffi_create_subgoal_attempt_hashes(hasStepNameHeading, hasOutcomeHeading, selectionColumns,
        actionColumns,inputColumns);

    CREATE INDEX sg_hash ON ffi_distinct_subgoal_attempts (sg_hash(5));

    IF hasOutcomeHeading THEN
        /* Parse the outcome field to the correct enumeration value before insert */
        INSERT INTO subgoal_attempt (subgoal_id, correct_flag, src_subgoal_att_id)
        SELECT subgoal_id, correct_flag, row_num
        FROM ffi_distinct_subgoal_attempts;
    ELSE
        INSERT INTO subgoal_attempt (correct_flag, src_subgoal_att_id)
        SELECT 'unknown', row_num
        FROM ffi_distinct_subgoal_attempts;
    END IF;

    CREATE INDEX distinct_sg_row ON subgoal_attempt (src_subgoal_att_id);

    CALL debug_log("ffi_populate_database", "Starting ffi_build_attempt_query selection");
    CALL exec(ffi_build_attempt_query(selectionColumns, "selection"));
    CALL debug_log("ffi_populate_database", "Starting ffi_build_attempt_query action");
    CALL exec(ffi_build_attempt_query(actionColumns, "action"));
    CALL debug_log("ffi_populate_database", "Starting ffi_build_attempt_query input");
    CALL exec(ffi_build_attempt_query(inputColumns, "input"));

    IF hasFeedbackHeading THEN
        CALL ffi_create_feedback(hasFeedbackClassHeading);
    END IF;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_subgoal_attempts");
END $$

/*
 -----------------------------------------------------------------------------
  Build the create query for the ffi_distinct_subgoal_attempts table.  Table
  includes SAI columns as well as feedback if it exists.  Uses the row_num
  primary key to identify the subgoal_attempt_ids after they have been inserted
  to by writing the value to the src_subgoal_att_id column (designed for the db_merge).
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_build_subgoal_attempt_query` $$
CREATE PROCEDURE `ffi_build_subgoal_attempt_query`  (hasStepNameHeading BOOLEAN,
    hasOutcomeHeading BOOLEAN, hasFeedbackHeading BOOLEAN, hasFeedbackClassHeading BOOLEAN,
    selectionColumns TEXT, actionColumns TEXT, inputColumns TEXT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE createSubgoalAttemptTable TEXT DEFAULT "CREATE TABLE ffi_distinct_subgoal_attempts (
        row_num BIGINT AUTO_INCREMENT PRIMARY KEY,
        subgoal_id BIGINT,
        correct_flag ENUM('correct', 'incorrect', 'hint', 'unknown', 'untutored') NOT NULL,
        sg_hash CHAR(32)";
    DECLARE subgoalIdHash TEXT DEFAULT "";
    DECLARE subgoalAttemptFields TEXT DEFAULT "";
    DECLARE subgoalHashFields TEXT DEFAULT "";
    DECLARE selectionDefinition TEXT DEFAULT "";
    DECLARE actionDefinition TEXT DEFAULT "";
    DECLARE inputDefinition TEXT DEFAULT "";
    DECLARE hashFields TEXT DEFAULT "";
    DECLARE saiFields TEXT;
    DECLARE updateSubgoalHash TEXT DEFAULT "";

    IF hasStepNameHeading THEN
        SET subgoalAttemptFields = "subgoal_id";
        SET subgoalHashFields = "IF(subgoal_id IS NULL,'',subgoal_id)";
    END IF;

    IF hasOutcomeHeading THEN
        SET subgoalAttemptFields =
            CONCAT(subgoalAttemptFields, IF(hasStepNameHeading, ",", ""), "correct_flag");
        SET subgoalHashFields =
            CONCAT(subgoalHashFields, IF(hasStepNameHeading, ",", ""), "correct_flag");

        DROP TABLE IF EXISTS ffi_ifd_correct_flags;
        CREATE TABLE ffi_ifd_correct_flags (
            line_num INT PRIMARY KEY,
            correct_flag ENUM('correct', 'incorrect', 'hint', 'unknown', 'untutored') NOT NULL
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT line_num, IF(LOCATE('hint', low_outcome) OR LOCATE('help', low_outcome), 'hint',
            IF(low_outcome = 'ok' OR low_outcome = 'correct', 'correct',
            IF(low_outcome = 'error' OR low_outcome = 'bug' OR low_outcome = 'incorrect', 'incorrect', 'unknown')))
            AS correct_flag
        FROM (SELECT line_num, LOWER(outcome) AS low_outcome FROM ffi_import_file_data) lower;

    END IF;

    SELECT GROUP_CONCAT(column_name SEPARATOR " VARCHAR(255),") INTO selectionDefinition
    FROM ffi_heading_column_map WHERE heading = 'Selection';
    SET selectionDefinition = CONCAT(selectionDefinition, " VARCHAR(255),");

    SELECT GROUP_CONCAT(column_name SEPARATOR " VARCHAR(255),") INTO actionDefinition
    FROM ffi_heading_column_map WHERE heading = 'Action';
    SET actionDefinition = CONCAT(actionDefinition, " VARCHAR(255),");

    SELECT GROUP_CONCAT(column_name SEPARATOR " VARCHAR(255),") INTO inputDefinition
    FROM ffi_heading_column_map WHERE heading = 'Input';
    SET inputDefinition = CONCAT(inputDefinition, " VARCHAR(255)");

    SET saiFields = CONCAT(selectionDefinition, actionDefinition, inputDefinition);

    IF saiFields IS NOT NULL THEN
       SET createSubgoalAttemptTable = CONCAT(createSubgoalAttemptTable, ",", saiFields);
    END IF;

    SET createSubgoalAttemptTable = CONCAT(createSubgoalAttemptTable,
        " ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin ",
        " SELECT DISTINCT ",
        subgoalAttemptFields);

    IF selectionColumns IS NOT NULL THEN
       SET createSubgoalAttemptTable =
           CONCAT(createSubgoalAttemptTable,
                  IF(LENGTH(subgoalAttemptFields) > 0, ",", ""),
                  selectionColumns);
    END IF;

    IF actionColumns IS NOT NULL THEN
       SET createSubgoalAttemptTable = CONCAT(createSubgoalAttemptTable, ",", actionColumns);
    END IF;

    IF inputColumns IS NOT NULL THEN
       SET createSubgoalAttemptTable = CONCAT(createSubgoalAttemptTable, ",", inputColumns);
    END IF;

    SET createSubgoalAttemptTable = CONCAT(createSubgoalAttemptTable,
                                           " FROM ffi_import_file_data ",
                                           IF(hasOutcomeHeading,
                                              " JOIN ffi_ifd_correct_flags USING (line_num)", ""));

    CALL exec(createSubgoalAttemptTable);

    SET updateSubgoalHash = CONCAT("UPDATE ffi_distinct_subgoal_attempts ",
                                   "SET sg_hash = MD5(CONCAT(",
                                   subgoalHashFields);

    IF selectionColumns IS NOT NULL THEN
       SET updateSubgoalHash = CONCAT(updateSubgoalHash,
                             IF(LENGTH(subgoalAttemptFields) > 0, ",", ""), selectionColumns);
    END IF;

    IF actionColumns IS NOT NULL THEN
       SET updateSubgoalHash = CONCAT(updateSubgoalHash, ",", actionColumns);
    END IF;

    IF inputColumns IS NOT NULL THEN
       SET updateSubgoalHash = CONCAT(updateSubgoalHash, ",", inputColumns);
    END IF;

    SET updateSubgoalHash = CONCAT(updateSubgoalHash, "))");

    CALL exec(updateSubgoalHash);

END $$

/*
 -----------------------------------------------------------------------------
  Generalized function to build the insert query for attempt_selection,
  attempt_action, and attempt_input tables.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_build_attempt_query` $$
CREATE FUNCTION `ffi_build_attempt_query` (columns TEXT, sai TEXT)
    RETURNS TEXT
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
    DECLARE attemptQuery TEXT;
    DECLARE columnName VARCHAR(64);
    DECLARE length INT DEFAULT LENGTH(columns);
    DECLARE delimPos INT;
    DECLARE firstPass BOOLEAN DEFAULT TRUE;

    SET attemptQuery = CONCAT("INSERT INTO attempt_",sai," (subgoal_attempt_id, ", sai, ") ");
    WHILE length > 0 DO
        SET delimPos = LOCATE(",", columns);
        IF delimPos != 0 THEN
            SET columnName = TRIM(SUBSTRING(columns, 1, delimPos-1));
            SET columns = TRIM(SUBSTRING(columns FROM delimPos+1));
            SET length = LENGTH(columns);

            IF !firstPass THEN
                SET attemptQuery = CONCAT(attemptQuery, " UNION ALL ");
            ELSE
                SET firstPass = FALSE;
            END IF;

            SET attemptQuery = CONCAT(attemptQuery, "SELECT subgoal_attempt_id, ", columnName,
                " AS ", sai, " FROM ffi_distinct_subgoal_attempts dsgatt JOIN subgoal_attempt sgatt
                ON sgatt.src_subgoal_att_id = dsgatt.row_num WHERE ", columnName," != ''");
        ELSE
            IF !firstPass THEN
                SET attemptQuery = CONCAT(attemptQuery, " UNION ALL ");
            END IF;

            SET length = 0;
            SET attemptQuery = CONCAT(attemptQuery, "SELECT subgoal_attempt_id, ", columns, " AS ",
            sai," FROM ffi_distinct_subgoal_attempts dsgatt JOIN subgoal_attempt sgatt
                ON sgatt.src_subgoal_att_id = dsgatt.row_num WHERE ", columns, " != ''");
        END IF;
    END WHILE;

    RETURN attemptQuery;
END $$

/*
 -----------------------------------------------------------------------------
  Create a hash value over subgoal_attempt + SAI columns to improve join with
  ffi_distinct_subgoal_attempts table.  Updates ffi_ffi_import_file_data.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_subgoal_attempt_hashes` $$
CREATE PROCEDURE `ffi_create_subgoal_attempt_hashes` (hasStepNameHeading BOOLEAN,
    hasOutcomeHeading BOOLEAN, selectionColumns TEXT, actionColumns TEXT, inputColumns TEXT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE subgoalAttHashUpdateQuery TEXT DEFAULT "UPDATE ffi_import_file_data ";
    DECLARE subgoalAttHashJoinQuery TEXT DEFAULT "JOIN ffi_ifd_correct_flags USING (line_num) ";
    DECLARE subgoalAttHashSetQuery TEXT DEFAULT "SET sg_hash = MD5(CONCAT( ";
    DECLARE hashColumns TEXT DEFAULT "";
    /* If source database isn't recreated old indexes may still exist, causing a dup key error */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '42000' SET @meaningless = 1;

    IF hasStepNameHeading THEN
        SET hashColumns = "IF(subgoal_id IS NULL,'',subgoal_id)";
    END IF;

    IF hasOutcomeHeading THEN
        SET hashColumns = CONCAT(hashColumns, IF(hasStepNameHeading, ",", ""), "correct_flag");
        SET subgoalAttHashUpdateQuery = CONCAT(subgoalAttHashUpdateQuery, subgoalAttHashJoinQuery);
    END IF;

    IF selectionColumns IS NOT NULL THEN
       SET hashColumns = CONCAT(hashColumns, IF(LENGTH(hashColumns) > 0, ",", ""),
                                selectionColumns);
    END IF;

    IF actionColumns IS NOT NULL THEN
       SET hashColumns = CONCAT(hashColumns, IF(LENGTH(hashColumns) > 0, ",", ""), actionColumns);
    END IF;

    IF inputColumns IS NOT NULL THEN
       SET hashColumns = CONCAT(hashColumns, IF(LENGTH(hashColumns) > 0, ",", ""), inputColumns);
    END IF;

    CALL exec(CONCAT(subgoalAttHashUpdateQuery, subgoalAttHashSetQuery, hashColumns, "))"));

    /* MySQL would whine that the key length is too big if we tried sg_id + outcome + SAI */
    CREATE INDEX sg_hash ON ffi_import_file_data (sg_hash(5));

END $$

/*
 -----------------------------------------------------------------------------
 Selects distinct subgoal_attempt_id, feedback_text, and classificaiton (optional)
 from the ffi_distinct_subgoal_attempts table to create feedback records.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_feedback` $$
CREATE PROCEDURE `ffi_create_feedback` (hasFeedbackClassHeading BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_feedback");

    IF hasFeedbackClassHeading THEN
        INSERT INTO feedback (feedback_text, classification, subgoal_attempt_id)
        SELECT DISTINCT feedback_text, IF(feedback_classification = '',NULL,feedback_classification),
            subgoal_attempt_id
        FROM ffi_import_file_data
        JOIN ffi_distinct_subgoal_attempts dsgatt USING (sg_hash)
        JOIN subgoal_attempt sa ON dsgatt.row_num = sa.src_subgoal_att_id
        WHERE feedback_text != '';

        /* A derived table would need to be created without indexes otherwise in the tt query */
        DROP TABLE IF EXISTS ffi_feedback_classification;
        CREATE TABLE ffi_feedback_classification (feedback_id BIGINT,
            subgoal_attempt_id BIGINT,
            feedback_text TEXT,
            classification VARCHAR(255),
            INDEX (subgoal_attempt_id, feedback_text(10), classification(10))
        ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin
        SELECT feedback_id, subgoal_attempt_id, feedback_text, IFNULL(classification,'') AS classification
        FROM feedback;

    ELSE
        INSERT INTO feedback (feedback_text, subgoal_attempt_id)
        SELECT DISTINCT feedback_text, subgoal_attempt_id
        FROM ffi_import_file_data
        JOIN ffi_distinct_subgoal_attempts dsgatt USING (sg_hash)
        JOIN subgoal_attempt sa ON dsgatt.row_num = sa.src_subgoal_att_id
        WHERE feedback_text != '';
    END IF;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_feedback");
END $$

/*
 -----------------------------------------------------------------------------
  Now that all the dependencies are taken care of insert transaction rows.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_transactions` $$
CREATE PROCEDURE `ffi_create_transactions` (datasetId INT, hasFeedbackHeading BOOLEAN,
    hasFeedbackClassHeading BOOLEAN, hasStepNameHeading BOOLEAN, hasOutcomeHeading BOOLEAN,
    hasClassHeading BOOLEAN, hasSchoolHeading BOOLEAN)
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_transactions");

    CALL exec(ffi_build_tt_select_columns(datasetId, hasFeedbackHeading, hasFeedbackClassHeading,
        hasStepNameHeading, hasOutcomeHeading, hasClassHeading, hasSchoolHeading));

    CALL debug_log("ffi_populate_database", "Finished ffi_create_transactions");
END $$

/*
 -----------------------------------------------------------------------------
  Build the tutor transaction select and join clauses for required and optional
  columns.  Ordering is very important because columns aren't specified in the
  insert section because they are dynamic.  Aliases must be used to align the
  values with their corrersponding column.
 -----------------------------------------------------------------------------
*/
DROP FUNCTION IF EXISTS `ffi_build_tt_select_columns` $$
CREATE FUNCTION `ffi_build_tt_select_columns` (datasetId INT, hasFeedbackHeading BOOLEAN,
    hasFeedbackClassHeading BOOLEAN, hasStepNameHeading BOOLEAN, hasOutcomeHeading BOOLEAN,
    hasClassHeading BOOLEAN, hasSchoolHeading BOOLEAN)
    RETURNS TEXT
    READS SQL DATA
    SQL SECURITY INVOKER
BEGIN
    DECLARE selectQuery TEXT DEFAULT " INSERT INTO tutor_transaction
        SELECT NULL AS transaction_id, NULL AS guid, sess.session_id, ifd.tx_time_datetime AS transaction_time,
        NULL as transaction_time_ms";
    DECLARE feedbackJoinQuery TEXT DEFAULT "";
    DECLARE sessionJoinQuery TEXT DEFAULT " FROM ffi_import_file_data ifd
        JOIN session sess ON sess.session_id = ifd.actual_session_id ";
    DECLARE subgoalAttemptJoinQuery TEXT DEFAULT " JOIN ffi_distinct_subgoal_attempts dsgatt USING (sg_hash)
        JOIN subgoal_attempt sa ON dsgatt.row_num = sa.src_subgoal_att_id ";

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'time_zone'
              AND table_name = 'ffi_import_file_data' AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery, ", IF(time_zone = '',NULL,time_zone) AS time_zone" );
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS time_zone");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'tutor_response_type' AND table_name = 'ffi_import_file_data'
              AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery,
            ", IF(tutor_response_type = '',NULL,tutor_response_type) AS transaction_type_tutor");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS transaction_type_tutor");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'student_response_type'
              AND table_name = 'ffi_import_file_data' AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery,
            ", IF(student_response_type = '',NULL,student_response_type) AS transaction_type_tool");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS transaction_type_tool");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'tutor_response_subtype' AND table_name = 'ffi_import_file_data'
              AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery,
            ", IF(tutor_response_subtype = '',NULL,tutor_response_subtype) AS transaction_subtype_tutor");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS transaction_subtype_tutor");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'student_response_subtype' AND table_name = 'ffi_import_file_data'
              AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery,
            ", IF(student_response_subtype = '',NULL,student_response_subtype) AS transaction_subtype_tool");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS transaction_subtype_tool");
    END IF;

    IF hasOutcomeHeading THEN
        SET selectQuery = CONCAT(selectQuery, ", IF(ifd.outcome = '',NULL,ifd.outcome) AS outcome");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS outcome");
    END IF;

    SET selectQuery = CONCAT(selectQuery, ", NULL AS attempt_at_subgoal",
                                          ", NULL AS is_last_attempt");

    SET selectQuery = CONCAT(selectQuery, ", ", datasetId, " AS dataset_id", ", problem_id");

    IF hasStepNameHeading THEN
        SET selectQuery = CONCAT(selectQuery, ", ifd.subgoal_id");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS subgoal_id");
    END IF;

    SET selectQuery = CONCAT(selectQuery, ", sa.subgoal_attempt_id");

    IF hasFeedbackHeading THEN
        SET selectQuery = CONCAT(selectQuery, ", feedback_id");
        IF hasFeedbackClassHeading THEN
            SET feedbackJoinQuery = " LEFT JOIN ffi_feedback_classification fee
                ON sa.subgoal_attempt_id = fee.subgoal_attempt_id
                AND ifd.feedback_text = fee.feedback_text
                AND ifd.feedback_classification = fee.classification";
        ELSE
            SET feedbackJoinQuery = " LEFT JOIN feedback fee ON sa.subgoal_attempt_id = fee.subgoal_attempt_id AND ifd.feedback_text = fee.feedback_text";
        END IF;
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS feedback_id");
    END IF;

    IF hasClassHeading THEN
        SET selectQuery = CONCAT(selectQuery, ", class_id");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS class_id");
    END IF;

    IF hasSchoolHeading THEN
        SET selectQuery = CONCAT(selectQuery, ", school_id");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS school_id");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'help_level'
              AND table_name = 'ffi_import_file_data' AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery, ", IF(help_level = '', NULL, help_level) AS help_level");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS help_level");
    END IF;

    IF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE column_name = 'total_num_hints' AND table_name = 'ffi_import_file_data'
              AND table_schema = DATABASE()) THEN
        SET selectQuery = CONCAT(selectQuery, ", IF(total_num_hints = '', NULL, total_num_hints) AS total_num_hints");
    ELSE
        SET selectQuery = CONCAT(selectQuery, ", NULL AS total_num_hints");
    END IF;

    SET selectQuery = CONCAT(selectQuery, ", NULL AS duration, NULL as prob_solving_sequence");
    SET selectQuery = CONCAT(selectQuery, ", line_num AS src_transaction_id ");
    SET selectQuery = CONCAT(selectQuery, ", NULL AS problem_event_id ");
    SET selectQuery = CONCAT(selectQuery,  sessionJoinQuery, subgoalAttemptJoinQuery, feedbackJoinQuery);
    RETURN selectQuery;
END $$

/*
 -----------------------------------------------------------------------------
 Creates problem events with either problem view or problem start time information.
 Use the problem start time and ignore the problem view if both are present.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_problem_events` $$
CREATE PROCEDURE         `ffi_create_problem_events` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log("ffi_populate_database", "Starting ffi_create_problem_events");

    -- take both problem Start Time and problem view
    IF (EXISTS(SELECT column_name FROM information_schema.columns
              WHERE table_name = 'ffi_import_file_data'
              AND column_name = 'problem_start_time'
              AND table_schema = DATABASE())
              AND 
              EXISTS(SELECT column_name FROM information_schema.columns
                  WHERE table_name = 'ffi_import_file_data'
                  AND column_name = 'problem_view'
                  AND table_schema = DATABASE()) )THEN

        CALL debug_log("ffi_populate_database", "ffi_create_problem_events :: PST and PV included");

        INSERT INTO problem_event (problem_id, session_id, start_time, problem_view, event_type, event_flag)
            SELECT DISTINCT problem_id, actual_session_id, problem_start_time_datetime, problem_view, 'FFI_START_PROBLEM', 0
            FROM ffi_import_file_data
            WHERE problem_start_time != '' AND problem_start_time IS NOT NULL AND problem_view != '' AND problem_view IS NOT NULL;

        -- Set the problem event FK in the data table
        UPDATE ffi_import_file_data dd
            JOIN problem_event pe
                ON (dd.actual_session_id = pe.session_id
                AND dd.problem_id = pe.problem_id
                AND dd.problem_start_time_datetime = pe.start_time
                AND dd.problem_view = pe.problem_view)
            SET dd.problem_event_id = pe.problem_event_id;
            
    
    ELSEIF EXISTS(SELECT column_name FROM information_schema.columns
              WHERE table_name = 'ffi_import_file_data'
              AND column_name = 'problem_start_time'
              AND table_schema = DATABASE()) THEN

        CALL debug_log("ffi_populate_database", "ffi_create_problem_events :: PST included");

        INSERT INTO problem_event (problem_id, session_id, start_time, event_type, event_flag)
            SELECT DISTINCT problem_id, actual_session_id, problem_start_time_datetime, 'FFI_START_PROBLEM', 0
            FROM ffi_import_file_data
            WHERE problem_start_time != '' AND problem_start_time IS NOT NULL;

        -- Set the problem event FK in the data table
        UPDATE ffi_import_file_data dd
            JOIN problem_event pe
                ON (dd.actual_session_id = pe.session_id
                AND dd.problem_id = pe.problem_id
                AND dd.problem_start_time_datetime = pe.start_time)
            SET dd.problem_event_id = pe.problem_event_id;

    -- Problem View only
    ELSEIF EXISTS(SELECT column_name FROM information_schema.columns
                  WHERE table_name = 'ffi_import_file_data'
                  AND column_name = 'problem_view'
                  AND table_schema = DATABASE()) THEN

        CALL debug_log("ffi_populate_database", "ffi_create_problem_events :: PV included");

        INSERT INTO problem_event (problem_id, session_id, start_time, problem_view,
                                   event_type, event_flag)
            SELECT DISTINCT problem_id, actual_session_id, 0, problem_view, 'FFI_START_PROBLEM', 0
            FROM ffi_import_file_data
            WHERE problem_view != '' AND problem_view IS NOT NULL;

        -- Set the problem event FK in the data table
        UPDATE ffi_import_file_data dd
            JOIN problem_event pe
                ON (dd.actual_session_id = pe.session_id
                AND dd.problem_id = pe.problem_id
                AND dd.problem_view = pe.problem_view)
            SET dd.problem_event_id = pe.problem_event_id;

        -- Update the problem start time
        UPDATE problem_event pe
            JOIN (SELECT min(tx_time_datetime) as start_time, problem_event_id
                  FROM ffi_import_file_data dd
                  GROUP BY problem_event_id) as minTime
                  ON minTime.problem_event_id = pe.problem_event_id
            SET pe.start_time = minTime.start_time;

    END IF;

    -- Set the Problem Event FK in the Tutor Transaction table
    UPDATE tutor_transaction tt
        JOIN ffi_import_file_data dd ON (tt.src_transaction_id = dd.line_num)
        SET tt.problem_event_id = dd.problem_event_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_problem_events");
END $$

/*
 -----------------------------------------------------------------------------
  Insert into the ds_condition table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_conditions` $$
CREATE PROCEDURE `ffi_create_conditions` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE conditionColumnCount INT;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_conditions");

    IF EXISTS(SELECT column_name FROM information_schema.columns
        WHERE table_name = 'ffi_import_file_data' AND column_name LIKE 'condition_name_%'
        AND table_schema = DATABASE()) THEN

            SELECT count(column_name) INTO conditionColumnCount FROM ffi_heading_column_map
                WHERE heading = 'Condition Name';

            CALL ffi_insert_conditions(conditionColumnCount, datasetId);
    END IF;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_feedback");
END $$

/*
 -----------------------------------------------------------------------------
  Insert the conditions, once for each (condition name, condition type) column
  pair.  Place the line number the condition is found on into src_condition_id
  as a way to match back to the transaction_id when it is time to make the
  transaction_condition_map.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_insert_conditions` $$
CREATE PROCEDURE `ffi_insert_conditions` (conditionColumnCount INT, datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE insertConditionHelperQuery TEXT DEFAULT "INSERT INTO ffi_condition_helper
        (line_num, condition_name, condition_type) ";
    DECLARE insertConditionQuery TEXT DEFAULT "INSERT IGNORE INTO ds_condition
        (condition_name, type, dataset_id, src_condition_id) ";
    DECLARE insertTxConditionMapQuery TEXT DEFAULT "INSERT IGNORE INTO transaction_condition_map (transaction_id, condition_id) ";
    DECLARE conditionQuery TEXT;
    DECLARE minCondOffset INT;

    DROP TABLE IF EXISTS ffi_condition_helper;
    CREATE TABLE ffi_condition_helper (row_num INT AUTO_INCREMENT PRIMARY KEY,
        line_num BIGINT,
        condition_name VARCHAR(80),
        condition_type TINYTEXT,
        INDEX (line_num),
        INDEX (condition_name)
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    WHILE conditionColumnCount > 0 DO
        IF EXISTS(SELECT column_name FROM ffi_heading_column_map
            WHERE heading = 'Condition Type' AND sequence = conditionColumnCount) THEN

            SELECT IF(MAX(row_num) IS NULL, 0, MAX(row_num)) INTO minCondOffset FROM ffi_condition_helper;

            CALL exec(CONCAT(insertConditionHelperQuery, " SELECT line_num, condition_name_",
                conditionColumnCount, ", IF(condition_type_", conditionColumnCount,
                "= '',NULL,condition_type_", conditionColumnCount, ") ",
                "FROM ffi_import_file_data ",
                "WHERE condition_name_", conditionColumnCount, " != ''"));

            CALL exec(CONCAT(insertConditionQuery, " SELECT condition_name, condition_type, ",
                datasetId, ", row_num",
                " FROM ffi_condition_helper",
                " WHERE row_num > ", minCondOffset));

            CALL exec(CONCAT(insertTxConditionMapQuery,
                "SELECT DISTINCT transaction_id, condition_id ",
                "FROM ffi_condition_helper fch ",
                "JOIN ds_condition dsc ON dsc.condition_name = fch.condition_name ",
                "JOIN tutor_transaction tt ON tt.src_transaction_id = fch.line_num "
                "WHERE dsc.dataset_id = ", datasetId, " AND tt.dataset_id = ", datasetId,
                    " AND fch.row_num > ", minCondOffset));

        ELSE
            SELECT IF(MAX(row_num) IS NULL, 0, MAX(row_num)) INTO minCondOffset FROM ffi_condition_helper;

            CALL exec(CONCAT(insertConditionHelperQuery, " SELECT line_num, condition_name_",
                conditionColumnCount, ", NULL ",
                "FROM ffi_import_file_data ",
                "WHERE condition_name_", conditionColumnCount, " != ''"));

            CALL exec(CONCAT(insertConditionQuery, " SELECT condition_name, condition_type, ",
                datasetId, ", row_num",
                " FROM ffi_condition_helper",
                " WHERE row_num > ", minCondOffset));

            CALL exec(CONCAT(insertTxConditionMapQuery,
                "SELECT DISTINCT transaction_id, condition_id ",
                "FROM ffi_condition_helper fch ",
                "JOIN ds_condition dsc ON dsc.condition_name = fch.condition_name ",
                "JOIN tutor_transaction tt ON tt.src_transaction_id = fch.line_num "
                "WHERE dsc.dataset_id = ", datasetId, " AND tt.dataset_id = ", datasetId,
                    " AND fch.row_num > ", minCondOffset));

        END IF;
        SET conditionColumnCount = conditionColumnCount - 1;
    END WHILE;
END $$

/*
 -----------------------------------------------------------------------------
  Build the create query for the ffi_subgoal_skill_map table because we'll need
  to eventually map skills to transactions in the transaction_skill_map table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_insert_subgoal_skill_map` $$
CREATE PROCEDURE `ffi_insert_subgoal_skill_map` ()
    SQL SECURITY INVOKER
BEGIN
    DECLARE ffiSubgoalSkillMapInsert TEXT DEFAULT "INSERT INTO ffi_subgoal_skill_map (line_num, skill_id, subgoal_id) ";
    DECLARE subgoalSkillMapInsert TEXT DEFAULT "INSERT IGNORE INTO subgoal_skill_map (subgoal_id, skill_id) ";
    DECLARE startOffset BIGINT;
    DECLARE skillColumns TEXT DEFAULT "";
    DECLARE skillColumn TEXT;
    DECLARE length, delimPos, skillModelId INT;
    DECLARE category TEXT;
    DECLARE hasCategory BOOLEAN;

    SELECT GROUP_CONCAT(skill_column) INTO skillColumns FROM ffi_skill_categories;
    SET length = LENGTH(skillColumns);

    DROP TABLE IF EXISTS ffi_subgoal_skill_map;
    CREATE TABLE ffi_subgoal_skill_map (row BIGINT AUTO_INCREMENT PRIMARY KEY,
        line_num INT,
        skill_id BIGINT,
        subgoal_id BIGINT
    ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin;

    WHILE length > 0 DO
        SET delimPos = LOCATE(",", skillColumns);
        IF delimPos != 0 THEN
            SET skillColumn = TRIM(SUBSTRING(skillColumns, 1, delimPos-1));
            SET skillColumns = TRIM(SUBSTRING(skillColumns FROM delimPos+1));
            SET length = LENGTH(skillColumns);

            SELECT skill_model_id, skill_category_column INTO skillModelId, category FROM ffi_skill_categories
            WHERE skill_column = skillColumn;

            SET hasCategory = IF(category IS NOT NULL,TRUE,FALSE);

            SELECT IF(MAX(row) IS NULL,0,MAX(row)) INTO startOffset FROM ffi_subgoal_skill_map;

            CALL exec(CONCAT(ffiSubgoalSkillMapInsert,
                    "SELECT line_num, sk.skill_id, subgoal_id ",
                    "FROM ffi_import_file_data ifd JOIN skill sk ON sk.skill_name = ifd.", skillColumn,
                    IF(hasCategory, CONCAT(" AND sk.category = ifd.",category),""),
                    " AND sk.skill_model_id = ", skillModelId,
                    IF(hasCategory, CONCAT(" WHERE ifd.",category," != ''"),"")));

            /* Just to be safe match those skills with a category column but a blank category */
            IF hasCategory THEN
                CALL exec(CONCAT(ffiSubgoalSkillMapInsert,
                    "SELECT line_num, sk.skill_id, subgoal_id ",
                    "FROM ffi_import_file_data ifd JOIN skill sk ON sk.skill_name = ifd.", skillColumn,
                    " AND sk.skill_model_id = ", skillModelId,
                    " WHERE ifd.",category," = ''"));
            END IF;

            CALL exec(CONCAT(subgoalSkillMapInsert,
                "SELECT DISTINCT subgoal_id, skill_id ",
                "FROM ffi_subgoal_skill_map sgskm ",
                "WHERE row > ", startOffset, " AND subgoal_id IS NOT NULL"));

        ELSE
            SELECT skill_model_id, skill_category_column INTO skillModelId, category
            FROM ffi_skill_categories
            WHERE skill_column = skillColumns;

            SET hasCategory = IF(category IS NOT NULL,TRUE,FALSE);

            SELECT IF(MAX(row) IS NULL,0,MAX(row)) INTO startOffset FROM ffi_subgoal_skill_map;

            CALL exec(CONCAT(ffiSubgoalSkillMapInsert,
                "SELECT line_num, sk.skill_id, subgoal_id ",
                "FROM ffi_import_file_data ifd JOIN skill sk ON sk.skill_name = ifd.", skillColumns,
                " AND sk.skill_model_id = ", skillModelId,
                IF(hasCategory, CONCAT(" AND sk.category = ifd.",category),""),
                IF(hasCategory, CONCAT(" WHERE ifd.",category," != ''"),"")));

            IF hasCategory THEN
                CALL exec(CONCAT(ffiSubgoalSkillMapInsert,
                    "SELECT line_num, sk.skill_id, subgoal_id ",
                    "FROM ffi_import_file_data ifd JOIN skill sk ON sk.skill_name = ifd.", skillColumns,
                    " AND sk.skill_model_id = ", skillModelId,
                    " AND ifd.",category," = ''"));
            END IF;

            CALL exec(CONCAT(subgoalSkillMapInsert,
                "SELECT DISTINCT subgoal_id, skill_id ",
                "FROM ffi_subgoal_skill_map sgskm ",
                "WHERE row > ", startOffset, " AND subgoal_id IS NOT NULL"));

            SET length = 0;
        END IF;
    END WHILE;
END $$

/*
 -----------------------------------------------------------------------------
  Insert distinct subgoal_id, skill_id pairs into the subgoal_skill_map table
  from the helper ffi_subgoal_skill_map table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_subgoal_skill_map` $$
CREATE PROCEDURE `ffi_create_subgoal_skill_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_subgoal_skill_map");

    DROP TABLE IF EXISTS ffi_subgoal_skill_map;
    CALL ffi_insert_subgoal_skill_map();

    CREATE INDEX line_num ON ffi_subgoal_skill_map (line_num);

    CALL debug_log("ffi_populate_database", "Finished ffi_create_subgoal_skill_map");

END $$



/*
 -----------------------------------------------------------------------------
  Insert distinct transaction_id, skill_id pairs by using the previously created
  ffi_subgoal_skill_map.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_tx_skill_map` $$
CREATE PROCEDURE `ffi_create_tx_skill_map` ()
    SQL SECURITY INVOKER
BEGIN
    /* If source database isn't recreated old indexes may still exist, causing a dup key error */
    DECLARE CONTINUE HANDLER FOR SQLSTATE '42000' SET @meaningless = 1;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_tx_skill_map");

    INSERT IGNORE INTO transaction_skill_map (transaction_id, skill_id)
    SELECT DISTINCT transaction_id, skill_id
    FROM ffi_subgoal_skill_map sgskm
    JOIN tutor_transaction tt ON tt.src_transaction_id = sgskm.line_num;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_tx_skill_map");
END $$

/*
 -----------------------------------------------------------------------------
  Insert distinct transaction_id, condition_id pairs by using the previously created
  ffi_condition_helper table.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_tx_condition_map` $$
CREATE PROCEDURE `ffi_create_tx_condition_map` ()
    SQL SECURITY INVOKER
BEGIN

    CALL debug_log("ffi_populate_database", "Starting ffi_create_tx_condition_map");

    /* Join on condition type too? */
    INSERT IGNORE INTO transaction_condition_map (transaction_id, condition_id)
    SELECT DISTINCT transaction_id, condition_id
    FROM ds_condition dsc
    JOIN tutor_transaction tt ON tt.src_transaction_id = dsc.src_condition_id;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_tx_condition_map");

END $$

/*
 -----------------------------------------------------------------------------
  Insert into custom_fields by calling the helper procedure
  ffi_insert_into_custom_field.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_create_custom_fields` $$
CREATE PROCEDURE `ffi_create_custom_fields` (datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE customFieldColumns TEXT;

    CALL debug_log("ffi_populate_database", "Starting ffi_create_custom_fields");

    IF EXISTS(SELECT column_name FROM information_schema.columns WHERE
        table_name = "ffi_import_file_data" AND column_name LIKE 'cf_%'
        AND table_schema = DATABASE()) THEN

        SELECT GROUP_CONCAT(column_name) INTO customFieldColumns FROM ffi_heading_column_map
            WHERE column_name LIKE 'cf_%';

        CALL ffi_insert_into_custom_field(customFieldColumns, datasetId);
    CALL ffi_insert_into_custom_field_cf_tx_level;

    END IF;

    CALL debug_log("ffi_populate_database", "Finished ffi_create_custom_fields");
END $$

/*
 -----------------------------------------------------------------------------
  Inserts custom fields temp table, performing one insert per CF column.  Utilizes the
  fact that when inserting transactions we put the line_num of ffi_import_file_data
  into the src_transaction_id column.
 -----------------------------------------------------------------------------
*/
DROP PROCEDURE IF EXISTS `ffi_insert_into_custom_field` $$
CREATE PROCEDURE `ffi_insert_into_custom_field` (customFieldColumns TEXT, datasetId INT)
    SQL SECURITY INVOKER
BEGIN
    DECLARE cfInsertQuery TEXT DEFAULT "INSERT INTO custom_field_temp (custom_field_name, custom_field_value, dataset_id, transaction_id) ";
    DECLARE ttJoinQuery TEXT DEFAULT "FROM ffi_import_file_data ifd JOIN tutor_transaction tt ON ifd.line_num = tt.src_transaction_id ";
    DECLARE cfFinalQuery TEXT;
    DECLARE length INT DEFAULT LENGTH(customFieldColumns);
    DECLARE delimPos INT;
    DECLARE cfColumnName TEXT;
    DECLARE customFieldName TEXT;

    DROP TABLE IF EXISTS custom_field_temp;
    CREATE TABLE custom_field_temp
    (
    custom_field_id  	BIGINT,
      custom_field_name 	VARCHAR(255) ,
      custom_field_value	MEDIUMTEXT ,
      dataset_id 		INT,
    transaction_id 		BIGINT
    ) ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

    WHILE length > 0 DO
        SET delimPos = LOCATE(",", customFieldColumns);
        IF delimPos != 0 THEN
            SET cfColumnName = TRIM(SUBSTRING(customFieldColumns, 1, delimPos-1));
            SET customFieldColumns = TRIM(SUBSTRING(customFieldColumns FROM delimPos+1));
            SET length = LENGTH(customFieldColumns);

            SELECT column_value INTO customFieldName FROM ffi_heading_column_map
                WHERE column_name = cfColumnName;

            SET cfFinalQuery = CONCAT(cfInsertQuery, "SELECT '", customFieldName, "', ", cfColumnName,
                ", ", datasetId, ", transaction_id ",  ttJoinQuery, " WHERE ", cfColumnName,
                " != ''");
            CALL exec(cfFinalQuery);
        ELSE
            SELECT column_value INTO customFieldName FROM ffi_heading_column_map
                WHERE column_name = customFieldColumns;

            SET cfFinalQuery = CONCAT(cfInsertQuery, "SELECT '", customFieldName, "', ", customFieldColumns,
                ", ", datasetId, ", transaction_id ",  ttJoinQuery, " WHERE ", customFieldColumns,
                " != ''");
            CALL exec(cfFinalQuery);
            SET length = 0;
        END IF;
    END WHILE;

    ALTER TABLE custom_field_temp ADD INDEX (dataset_id),
                 ADD INDEX (custom_field_name),
                ADD INDEX (custom_field_value(255));
END $$


/*
 -----------------------------------------------------------------------------
  populate custom_field and cf_tx_level/cf_tx_level_big
 -----------------------------------------------------------------------------
*/


DROP PROCEDURE IF EXISTS `ffi_insert_into_custom_field_cf_tx_level` $$
CREATE PROCEDURE `ffi_insert_into_custom_field_cf_tx_level` ()
    SQL SECURITY INVOKER
BEGIN
    CALL debug_log("ffi_populate_database", "Starting ffi_insert_into_custom_field_cf_tx_level");
    INSERT IGNORE INTO custom_field (
        custom_field_name,
        dataset_id,
        level)
    SELECT custom_field_name, dataset_id, 'transaction'
    FROM custom_field_temp;

    -- numbers
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'number',
        custom_field_value,
        true
    FROM custom_field_temp cf_temp
    JOIN custom_field cf using (custom_field_name, dataset_id)
    WHERE length(custom_field_value) <= 255
        AND custom_field_value REGEXP '^-?[0-9]+\.{0,1}[0-9]*$';

    -- big numbers (greater than 255 chars)
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        big_value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'number',
        SUBSTRING(custom_field_value, 1, 255),
        custom_field_value,
        true
    FROM custom_field_temp cf_temp
    JOIN custom_field cf using (custom_field_name, dataset_id)
    WHERE length(custom_field_value) > 255
        AND custom_field_value REGEXP '^-?[0-9]+\.{0,1}[0-9]*$';

    -- dates: "2012/02/14 12:29:24.906"
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'date',
        custom_field_value,
        true
        FROM custom_field_temp cf_temp
        JOIN custom_field cf using (custom_field_name, dataset_id)
        WHERE STR_TO_DATE (custom_field_value, '%Y/%m/%d %H:%i:%s.%f') IS NOT NULL
            AND custom_field_value NOT REGEXP '^-?[0-9]+\.{0,1}[0-9]*$';

    -- dates: "2012-02-14 12:29:24.906"
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'date',
        custom_field_value,
        true
        FROM custom_field_temp cf_temp
        JOIN custom_field cf using (custom_field_name, dataset_id)
        WHERE STR_TO_DATE (custom_field_value, '%Y-%m-%d %H:%i:%s.%f') IS NOT NULL
            AND custom_field_value NOT REGEXP '^-?[0-9]+\.{0,1}[0-9]*$';

    -- dates: "2012-02-14 Tue"
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'date',
        custom_field_value,
        true
        FROM custom_field_temp cf_temp
        JOIN custom_field cf using (custom_field_name, dataset_id)
        WHERE STR_TO_DATE (custom_field_value, '%Y-%m-%d %a') IS NOT NULL
            AND custom_field_value NOT REGEXP '^-?[0-9]+\.{0,1}[0-9]*$';

    -- strings less than or equal to 255 characters
    -- insert ignore here in case the field is also a number or date
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'string',
        custom_field_value,
        true
        FROM custom_field_temp cf_temp
        JOIN custom_field cf using (custom_field_name, dataset_id)
        WHERE LENGTH(cf_temp.custom_field_value) <= 255;

    -- strings greater than 255 characters
    -- insert ignore here in case the field is also a number or date
    INSERT IGNORE INTO cf_tx_level (
        custom_field_id,
        transaction_id,
        type,
        value,
        big_value,
        logging_flag)
    SELECT
        cf.custom_field_id,
        transaction_id,
        'string',
        SUBSTRING(custom_field_value, 1, 255),
        custom_field_value,
        true
    FROM custom_field_temp cf_temp
    JOIN custom_field cf using (custom_field_name, dataset_id)
    WHERE LENGTH(cf_temp.custom_field_value) > 255;


    DROP TABLE IF EXISTS custom_field_temp;

    CALL debug_log("ffi_populate_database", "Finished ffi_insert_into_custom_field_cf_tx_level");


END $$


DROP PROCEDURE IF EXISTS `ffi_drop_helper_tables` $$
CREATE PROCEDURE `ffi_drop_helper_tables` ()
    SQL SECURITY INVOKER
BEGIN

END $$

DROP PROCEDURE IF EXISTS `ffi_drop_indexes` $$
CREATE PROCEDURE `ffi_drop_indexes` ()
    SQL SECURITY INVOKER
BEGIN

END $$

DELIMITER ;