/*
 *  -------------------------------------------------------------------------------------------------
 *  Carnegie Mellon University, Human Computer Interaction Institute
 *  Copyright 2013
 *  All Rights Reserved
 *  Last modified by - $Author: ctipper $
 *  Last modified on - $Date: 2015-06-01 11:35:47 -0400 (Mon, 01 Jun 2015) $
 *  Sets the dataset_id in the import queue to null and deletes transactions for the specified dataset
 *  before deleting the dataset and updating the dataset_system_log.
 *  -------------------------------------------------------------------------------------------------
*/

DELIMITER $$

DROP PROCEDURE IF EXISTS `purge_deleted_datasets` $$
CREATE PROCEDURE `purge_deleted_datasets`(IN `datasetId` INT)
    LANGUAGE SQL
    NOT DETERMINISTIC
    CONTAINS SQL
    SQL SECURITY INVOKER
    COMMENT ''
BEGIN
    /* Variable declaration */
    DECLARE startTime DATETIME;
    DECLARE deletedTransactions LONG;
    DECLARE successFlag BOOL;
    DECLARE info VARCHAR(255);
    DECLARE datashopVersion VARCHAR(255);
    DECLARE datasetName VARCHAR(100);

    /* Initialize */
    SELECT now() into startTime;
    SET deletedTransactions = 0;
    SET successFlag = TRUE;
    SELECT version FROM datashop_version into datashopVersion;
    SELECT dataset_name FROM ds_dataset
        WHERE dataset_id = datasetId
        INTO datasetName;

    /* Because of a constraint, if there is an import queue item, then
           ensure that it is detached from the dataset. */
    UPDATE import_queue iq
        SET iq.dataset_id = NULL
        where iq.dataset_id = datasetId;

    /* Delete tutor transactions. */
    DELETE FROM tutor_transaction
        WHERE dataset_id = datasetId;

    /* Save the number of deleted transactions. */
    SELECT ROW_COUNT() into deletedTransactions;

    /* Delete the dataset item. */
    DELETE FROM ds_dataset
        WHERE dataset_id = datasetId;

    /* Info string: Removed [count] transactions. Dataset [dataset_name] ([dataset_id]). */
    SET info = CONCAT('Removed ', deletedTransactions, ' transactions. ',
                'Dataset ', datasetName, ' (', datasetId, ').');

    /* Insert an entry into dataset_system_log. */
    INSERT INTO dataset_system_log (dataset_id, value, elapsed_time, time, action,
            info, success_flag, datashop_version)
        VALUES (datasetId,
            deletedTransactions,
            TIMESTAMPDIFF(SECOND, startTime, now()),   /* elapsed time */
            now(),                          /* time */
            "purge deleted dataset",        /* action */
            info,
            successFlag,
            datashopVersion);


END $$

DELIMITER ;