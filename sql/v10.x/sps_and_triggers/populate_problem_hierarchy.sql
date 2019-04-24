-- -----------------------------------------------------------------------------------------------------
--  Carnegie Mellon University, Human-Computer Interaction Institute
--  Copyright 2013
--  All Rights Reserved
--
--  $Revision: 12404 $
--  Last modified by - $Author: ctipper $
--  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
--  $KeyWordsOff: $
--
--  Populate the problem_hierarchy table with dataset levels.
--
--  HOW TO RUN:
--     CALL populate_problem_hierarchy(datasetId);
-- ------------------------------------------------------------------------------------------------------

DELIMITER $$

-- -------------------------------------------------------------------------------
--  Populate the problem_hierarchy table with dataset_levels for the given dataset.
--  @param datasetId the dataset we wish to process.
-- -------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS    `populate_problem_hierarchy` $$
CREATE PROCEDURE            `populate_problem_hierarchy`(IN datasetId long)
    SQL SECURITY INVOKER
BEGIN
    DECLARE problemHierarchyRowsDeleted LONG;
    DECLARE problemHierarchyRowsCreated LONG;
    DECLARE initialGroupConcatMaxLen INTEGER;
    SET problemHierarchyRowsDeleted = 0;
    SET problemHierarchyRowsCreated = 0;


    CALL debug_log('populate_problem_hierarchy',
    CONCAT('Deleting any existing rows in problem_hierarchy for Dataset (', datasetId, '). '));

    DELETE FROM problem_hierarchy where dataset_id = datasetId;

    /* Save the number of deleted problem_hierarchy rows. */
    SELECT ROW_COUNT() into problemHierarchyRowsDeleted;

    CALL debug_log('populate_problem_hierarchy',
        CONCAT('Starting populate_problem_hierarchy, Dataset (', datasetId ,'). '));

    -- The default length of group_concat is 1024
    -- Increase to TEXT size and restore session value after.
    SELECT @@group_concat_max_len into initialGroupConcatMaxLen;
    SET SESSION group_concat_max_len = 65535;

    INSERT INTO problem_hierarchy (hierarchy, problem_id, dataset_id)
        SELECT DISTINCT (
            select group_concat(
                DISTINCT CONCAT(
                    IF(dl2.level_title IS NOT NULL, CONCAT(dl2.level_title, ' '), ''),
                    dl2.level_name
                ) ORDER BY dl2.lft SEPARATOR ', '
            )
        FROM dataset_level dl2
        WHERE dl2.lft <= dl.lft AND dl2.rgt >= dl.rgt
            AND dl.dataset_id = dl2.dataset_id
        GROUP BY dl2.dataset_id) AS hierarchy,
            p.problem_id, dl.dataset_id
        FROM problem  p
        LEFT JOIN dataset_level dl USING (dataset_level_id)
        WHERE dl.dataset_id = datasetId ;

    /* Save the number of created problem_hierarchy rows. */
    SELECT ROW_COUNT() into problemHierarchyRowsCreated;

    CALL debug_log('populate_problem_hierarchy',
    CONCAT('Populated problem_hierarchy, Dataset (', datasetId, '). '));
    SET SESSION group_concat_max_len = initialGroupConcatMaxLen;
END $$

DELIMITER ;
